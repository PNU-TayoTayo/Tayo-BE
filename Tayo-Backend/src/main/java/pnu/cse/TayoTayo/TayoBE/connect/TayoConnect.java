package pnu.cse.TayoTayo.TayoBE.connect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.hyperledger.fabric.client.*;
import org.hyperledger.fabric.client.identity.*;
import org.springframework.web.bind.annotation.RequestParam;
import pnu.cse.TayoTayo.TayoBE.dto.request.CarRequest;
import pnu.cse.TayoTayo.TayoBE.model.Car;
import pnu.cse.TayoTayo.TayoBE.model.Sharing;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class TayoConnect {

    // 실제 체인코드 메서드 명과 동일 -> 헷갈리면 couchDB 보고 맞추기
    private static String ROLE, MSP_ID, CHANNEL_NAME, CHAINCODE_NAME, PEER_ENDPOINT, OVERRIDE_AUTH;
    private static Path CRYPTO_PATH, CERT_PATH, KEY_DIR_PATH, TLS_CERT_PATH;
    private final Contract contract;
    private final String assetId = "asset" + Instant.now().toEpochMilli();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public TayoConnect(int caseNumber) throws CertificateException, IOException, InvalidKeyException {
        CaseConfig caseConfig = getCaseConfig(caseNumber);
        ROLE = caseConfig.role;
        MSP_ID = caseConfig.mspId;
        CHANNEL_NAME = caseConfig.channelName;
        CHAINCODE_NAME = caseConfig.chaincodeName;
        PEER_ENDPOINT = caseConfig.peerEndpoint;
        OVERRIDE_AUTH = String.format("peer0.%s.example.com", ROLE);
        CRYPTO_PATH = Paths.get("..", "network", "organizations", "peerOrganizations", String.format("%s.example.com", ROLE));
        CERT_PATH = CRYPTO_PATH.resolve("users/User1@" + String.format("%s.example.com", ROLE) + "/msp/signcerts/cert.pem");
        KEY_DIR_PATH = CRYPTO_PATH.resolve("users/User1@" + String.format("%s.example.com", ROLE) + "/msp/keystore");
        TLS_CERT_PATH = CRYPTO_PATH.resolve("peers/peer0." + String.format("%s.example.com", ROLE) + "/tls/ca.crt");

        System.out.println("CERT_PATH: " + CERT_PATH);
        System.out.println("KEY_DIR_PATH: " + KEY_DIR_PATH);
        System.out.println("TLS_CERT_PATH: " + TLS_CERT_PATH);

        var channel = newGrpcConnection();
        var builder = Gateway.newInstance().identity(newIdentity()).signer(newSigner()).connection(channel)
                .evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                .endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
                .submitOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                .commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));
        var gateway = builder.connect();
        var network = gateway.getNetwork(CHANNEL_NAME);
        this.contract = network.getContract(CHAINCODE_NAME);
    }

    private ManagedChannel newGrpcConnection() throws IOException, CertificateException {
        var tlsCertReader = Files.newBufferedReader(TLS_CERT_PATH);
        var tlsCert = Identities.readX509Certificate(tlsCertReader);
        return NettyChannelBuilder.forTarget(PEER_ENDPOINT)
                .sslContext(GrpcSslContexts.forClient().trustManager(tlsCert).build())
                .overrideAuthority(OVERRIDE_AUTH)
                .build();
    }

    private Identity newIdentity() throws IOException, CertificateException {
        var certReader = Files.newBufferedReader(CERT_PATH);
        var certificate = Identities.readX509Certificate(certReader);
        return new X509Identity(MSP_ID, certificate);
    }

    private Signer newSigner() throws IOException, InvalidKeyException {
        var keyReader = Files.newBufferedReader(getPrivateKeyPath());
        var privateKey = Identities.readPrivateKey(keyReader);
        return Signers.newPrivateKeySigner(privateKey);
    }

    private Path getPrivateKeyPath() throws IOException {
        try (var keyFiles = Files.list(KEY_DIR_PATH)) {
            return keyFiles.findFirst().orElseThrow();
        }
    }

    /* 차량 관련 체인코드 실행
     * 1. 차량 등록 - 테스트 완료
     * 2. Owner ID로 본인 차량 조회
     * 3. Car ID로 차량 상세 조회
     * 4. 차량 검색
     * 5. 차량 삭제
     * 6. 차량 수정
     *  */
    // 1. 차량 등록
    public void createCar(Car car) throws GatewayException, CommitException {
        String carJson = gson.toJson(car);
        byte[] carAsBytes = carJson.getBytes(StandardCharsets.UTF_8);
        contract.submitTransaction("CreateCar", carAsBytes);
    }

    // 2. Owner ID로 본인 차량 조회
    public JsonElement queryCarsByOwnerID(Long ownerID) throws GatewayException {
        var result = contract.evaluateTransaction("QueryCarByOwnerID", String.valueOf(ownerID));
        System.out.println(prettyJson(result));
        return prettyJson(result);
    }

    // 3. Car ID로 차량 상세 조회
    public JsonElement queryByCarID(Long carID) throws GatewayException {
        var result = contract.evaluateTransaction("QueryCarByCarID", String.valueOf(carID));
        System.out.println(prettyJson(result));
        return prettyJson(result);
    }

    // 4. 차량 검색
    public JsonElement getAvailableCars(Double leftLatitude, Double leftLongitude, Double rightLatitude, Double rightLongitude, String date) throws GatewayException, CommitException {
        var result = contract.evaluateTransaction("GetAvailableCars",
                leftLatitude.toString(), leftLongitude.toString(),
                rightLatitude.toString(), rightLongitude.toString(), date);
        System.out.println(prettyJson(result));
        return prettyJson(result);
    }

    // 5. 차량 삭제
    public JsonElement deleteCar(Long carID) throws EndorseException, CommitException, SubmitException, CommitStatusException {
        contract.submitTransaction("DeleteCar", String.valueOf(carID));
        return null;
    }

    // 6. 차량 수정
    // 원래 매개변수로 아래 값들만 수정 가능하도록 구현하였는데, String으로 다 넘겨주기가 번거로워서 그냥 car 객체 만들어서 넘김
    // timeList, sharingLocation, sharingLocationAddress, sharingLatitude, sharingLongitude, sharingAvailable, sharingRating
    public void updateCar(Car car) throws EndorseException, CommitException, SubmitException, CommitStatusException {
        String carJson = gson.toJson(car);
        byte[] carAsBytes = carJson.getBytes(StandardCharsets.UTF_8);
        contract.submitTransaction("UpdateCar", carAsBytes);
    }

    /* 공유 관련 체인코드 실행
     * 1. 대여 신청
     * 2. 대여 신청 정보 수정
     * 3. 대여 신청 상태 변경
     * 4. 재화 거래 과정 (확정 상태에서 결제, 3)
     *  */

    // 1. 대여 신청
    public void createSharing(Sharing sharing) throws GatewayException, CommitException {
        String sharingJson = gson.toJson(sharing);
        byte[] sharingAsBytes = sharingJson.getBytes(StandardCharsets.UTF_8);
        contract.submitTransaction("CreateSharing", sharingAsBytes);
    }

    // 2. 대여 신청 정보 수정 - 공유가격, 공유시간, 공유장소명
    public void updateSharingInfo(Long carID, Integer sharingPrice, String sharingTime, String sharingLocation) throws GatewayException, CommitException {
        contract.submitTransaction("UpdateSharingInfo", String.valueOf(carID), String.valueOf(sharingPrice), sharingTime, sharingLocation);
    }

    // 3. 대여 신청 상태 변경
    // [신청(3)/확정(2, 결제대기)/거절(2)/이용완료(구분x 아무거나)] - 숫자는 Connect caseNumber
//    * TODO 1: 확정이 되면 차량 정보도 수정해야 하는데, car 체인코드에 어떻게 접근하지?
//            *  생각 중인 방법은 TayoConnect를 두 개 만들어서 2(lender, sharing)에서 확정 후
//    *  1(lender, car)를 만들어서 해당 일자를 삭제하는 것, 어차피 공유일자로 쿼리하니까 available 필드를 바꾸진 않음
//    * TODO 2: 지금은 정보 수정이랑 상태 변경을 묶어뒀는데 분리해야 할 듯
    public void updateSharingStatus(Long carID, String sharingStatus) throws GatewayException, CommitException {
        contract.submitTransaction("UpdateSharingStatus", String.valueOf(carID), sharingStatus);
    }

    // 4. 재화 거래 과정 (3) - 확정 상태에서만 결제 가능
    public void processTransaction(Long carID, Long lenderID, Long borrowerID, Integer sharingPrice) throws GatewayException, CommitException {
        contract.submitTransaction("ProcessTransaction", String.valueOf(carID), String.valueOf(lenderID), String.valueOf(borrowerID), String.valueOf(sharingPrice));
    }

    /* 지갑 관련 체인코드 실행
     * 1. 회원가입 시 지갑 생성
     * 2. 잔액 조회
     * 3. 출금
     * 4. 입금
     * (5. 최근 거래 내역 조회)
     * */

    // 1. 회원가입 시 지갑 생성(3)
    public void createWallet(Long userID) throws GatewayException, CommitException {
        contract.submitTransaction("CreateWallet", String.valueOf(userID));
    }

    // 2. 잔액 조회
    public Integer queryWalletBalance(Long userID) throws GatewayException, CommitException {
        var resultBytes = contract.evaluateTransaction("QueryWalletBalance", String.valueOf(userID));
        // byte[]를 문자열로 변환
        String resultString = new String(resultBytes);
        // 문자열을 정수로 변환
        Integer balance = Integer.parseInt(resultString);
        System.out.println(balance);

        return balance;
    }

    // 3. 출금
    public void withdraw(Long userID, Integer amount) throws GatewayException, CommitException {
        contract.submitTransaction("Withdraw", String.valueOf(userID), String.valueOf(amount));
        System.out.println("출금 후 잔액 : " + queryWalletBalance(userID));
    }

    // 4. 입금
    public void deposit(Long userID, Integer amount) throws GatewayException, CommitException {
        contract.submitTransaction("Deposit", String.valueOf(userID), String.valueOf(amount));
        System.out.println("입금 후 잔액 : " + queryWalletBalance(userID));
    }

    private JsonElement prettyJson(final byte[] json) {
        return prettyJson(new String(json, StandardCharsets.UTF_8));
    }

    private JsonElement prettyJson(final String json) {
        return JsonParser.parseString(json);
//        return gson.toJson(parsedJson);
    }

    private CaseConfig getCaseConfig(int caseNumber) {
        switch (caseNumber) {
            // 1) lender가 car 채널에서 tayocar 체인코드 사용 -> 차량 등록 및 관리하는 경우
            case 1:
                return new CaseConfig("lender", "lenderMSP", "car", "tayocar", "localhost:7051");
            // 2) lender가 sharing 채널에서 tayosharing 체인코드 사용 -> 지갑 생성 및 ...
            case 2:
                return new CaseConfig("lender", "lenderMSP", "sharing", "tayosharing", "localhost:7051");
            // 3) borrower가 sharing 채널에서 tayosharing 체인코드 사용 -> 지갑 생성 및 차량 공유, 거래 관련
            case 3:
                return new CaseConfig("borrower", "borrowerMSP", "sharing", "tayosharing", "localhost:9051");
            default:
                throw new IllegalArgumentException("Invalid case number");
        }
    }

    private static class CaseConfig {
        private final String role, mspId, channelName, chaincodeName, peerEndpoint;

        public CaseConfig(String role, String mspId, String channelName, String chaincodeName, String peerEndpoint) {
            this.role = role;
            this.mspId = mspId;
            this.channelName = channelName;
            this.chaincodeName = chaincodeName;
            this.peerEndpoint = peerEndpoint;
        }
    }

}