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
import pnu.cse.TayoTayo.TayoBE.model.Car;

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
        OVERRIDE_AUTH = String.format("peer0.%s.example.com", caseConfig.role);
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

    /* 차량 관련 체인코드 실행 */
    //
    public JsonElement queryAllCars() throws GatewayException {
        var result = contract.evaluateTransaction("QueryAllCars");
        System.out.println(prettyJson(result));
        return prettyJson(result);
    }

    // 차량 등록
    public void createCar(Car car) throws GatewayException, CommitException {
        String carJson = gson.toJson(car);
        byte[] carAsBytes = carJson.getBytes(StandardCharsets.UTF_8);
        contract.submitTransaction("CreateCar", carAsBytes);
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