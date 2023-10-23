package pnu.cse.TayoTayo.TayoBE.service;

import com.google.gson.JsonElement;
import com.sun.xml.bind.v2.TODO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.hyperledger.indy.sdk.anoncreds.CredentialsSearchForProofReq;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.ledger.LedgerResults;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnu.cse.TayoTayo.TayoBE.connect.TayoConnect;
import pnu.cse.TayoTayo.TayoBE.dto.response.CarResponse;
import pnu.cse.TayoTayo.TayoBE.model.Car;
import org.springframework.web.multipart.MultipartFile;
import pnu.cse.TayoTayo.TayoBE.dto.request.MemberRequest;
import pnu.cse.TayoTayo.TayoBE.dto.response.CreateVCResponse;
import pnu.cse.TayoTayo.TayoBE.dto.response.MyVCResponse;
import pnu.cse.TayoTayo.TayoBE.dto.response.RegisterCarResponse;
import pnu.cse.TayoTayo.TayoBE.exception.ApplicationException;
import pnu.cse.TayoTayo.TayoBE.exception.ErrorCode;
import pnu.cse.TayoTayo.TayoBE.model.Member;
import pnu.cse.TayoTayo.TayoBE.model.entity.MemberEntity;
import pnu.cse.TayoTayo.TayoBE.repository.MemberRepository;
import pnu.cse.TayoTayo.TayoBE.util.PoolAndWalletManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


@Service
@RequiredArgsConstructor
public class CarService {

    private final MemberRepository memberRepository;

    private final PoolAndWalletManager poolAndWalletManager;

    //private final S3Uploader s3Uploader;

    @Value("${VCService.issuer.DID}")
    private String issuerDID;

    /**
     * 1. id랑 지갑 비번을 받아서 그걸로 wallet open ㅇ
     *
     * 2. 자동차에 대한 DID를 생성한다 ㅇ
     *
     * 3. VC_Service로 Offer를 얻기 위해 HTTP 요청을 보냄 ㅇ
     *
     * 4. 그걸 받으면, 유저는 Schema_id랑 cre_def_id로 레저로부터 schema, cre_def를 가져온다 ㅇ
     *
     * 5. Master Secret도 생성 한다! ㅇ
     *
     * 6. 유저가 이걸로 Credential Request를 만든다! ㅇ
     *
     * 7. 만든 Credential Request를 VC_Service에 제출해서 VC 발급 요청을 함 + (자동차 번호등도 보내야 할듯)
     */

    @Transactional
    public String createVC(Long Id , String walletPassword, String carNumber) throws IndyException, ExecutionException, InterruptedException {

        Wallet memberWallet = null;

        try {
            MemberEntity member = memberRepository.findOne(Id);

            memberWallet = poolAndWalletManager.openUserWallet(member.getEmail(), walletPassword);

            String carDID = poolAndWalletManager.createCarDID(memberWallet, member);

            String credentialOffer = poolAndWalletManager.getCredentialOfferFromVCService(member.getId(), memberWallet);

            System.out.println("\n해당 유저가 VC 생성에 필요한 Master Secret을 생성하고 본인의 지갑에 저장");

            //String MasterSecretId = Anoncreds.proverCreateMasterSecret(memberWallet, null).get();
            String MasterSecretId = member.getWalletMasterKey();

            System.out.println("\n 해당 유저는 레저로부터 Credential Definition를 가져온다");
            LedgerResults.ParseResponseResult parsedCredDefResponse =
                    poolAndWalletManager.getCredDef(carDID, new JSONObject(credentialOffer).getString("cred_def_id").toString());

            // 여기서 Credential request 매개변수로 AliceWallet, AliceDID, Credential Offer, Credential Definition, Master Secret ID
            System.out.println("\n해당 유저는 VC생성을 위해 Issuer에게 보낼 transcript credential request를 준비한다");
            AnoncredsResults.ProverCreateCredentialRequestResult credentialRequestResult =
                    Anoncreds.proverCreateCredentialReq(memberWallet, carDID, credentialOffer,
                            parsedCredDefResponse.getObjectJson(), MasterSecretId).get();


            String carVC = poolAndWalletManager.getVC(credentialRequestResult.getCredentialRequestJson(), credentialOffer, member.getName(), carNumber);

            System.out.println("\n\n\n받은 자동차에 대한 VC : " + carVC);

            // 유저가 받은 VC를 본인 지갑에 저장
            Anoncreds.proverStoreCredential(memberWallet, null, credentialRequestResult.getCredentialRequestMetadataJson()
                    , carVC, parsedCredDefResponse.getObjectJson(), null);

            return member.getName();

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            throw new ApplicationException(ErrorCode.FAIL_CREATE_VC);
        } finally {
            poolAndWalletManager.closeUserWallet(memberWallet);
        }
    }

    @Transactional
    public MyVCResponse getVC(Long Id , String walletPassword) throws IndyException, ExecutionException, InterruptedException {

        Wallet memberWallet = null;

        try {
            MemberEntity member = memberRepository.findOne(Id);

            memberWallet = poolAndWalletManager.openUserWallet(member.getEmail(), walletPassword);

            JSONObject json = new JSONObject();

            String filter = json.put("issuer_did", issuerDID).toString();

            // 발급자의 did로 뽑아냄 VC를 뽑아냄..?
            String credentials = Anoncreds.proverGetCredentials(memberWallet, filter).get();

            poolAndWalletManager.closeUserWallet(memberWallet);

            List<MyVCResponse.VerifiableCredential> vcList = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(credentials);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject vcObject = jsonArray.getJSONObject(i);

                MyVCResponse.VerifiableCredential vcData = MyVCResponse.VerifiableCredential.builder()
                        .referent(vcObject.getString("referent"))
                        .name(vcObject.getJSONObject("attrs").getString("owner_last_name") + vcObject.getJSONObject("attrs").getString("owner_first_name"))
                        .carModel(vcObject.getJSONObject("attrs").getString("car_model"))
                        .carNumber(vcObject.getJSONObject("attrs").getString("car_number"))
                        .carFuel(vcObject.getJSONObject("attrs").getString("car_fuel"))
                        .carDeliveryDate(vcObject.getJSONObject("attrs").getString("car_delivery_date"))
                        .inspectionRecord(vcObject.getJSONObject("attrs").getString("inspection_record"))
                        .drivingRecord(vcObject.getJSONObject("attrs").getString("driving_record"))
                        .build();

                vcList.add(vcData);

            }

            MyVCResponse response = new MyVCResponse(member.getName(), vcList);

            return response;
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            throw new ApplicationException(ErrorCode.FAIL_GET_VC);
        } finally {
            poolAndWalletManager.closeUserWallet(memberWallet);
        }
    }

    /**
     *
     * 사용자에게 현재 가지고 있는 VC들을 보여줌 (선택권 부여)
     *
     *  VC마다 referent를 가지고 있다!!
     *
     * 사용자가 VC 목록에서 원하는 VC를 선택하면, 해당 VC의 식별자(referent)를 애플리케이션으로 전달
     * 전달받은 VC 식별자를 사용하여 선택된 VC를 검색하고, 해당 VC를 포함한 VP를 생성
     * 생성된 VP를 검증 요청자(Verifier)에게 제출하여 자격을 증명
     *
     */

    @Transactional
    public RegisterCarResponse postCar(Long memberId , MemberRequest.registerCarRequest request , List<MultipartFile> images) throws Exception {

        Wallet memberWallet = null;

        try {

            System.out.println("자동차 등록을 위한 VP 생성");

            MemberEntity member = memberRepository.findOne(memberId);

            memberWallet = poolAndWalletManager.openUserWallet(member.getEmail(), request.getWalletPassword());

            // 이게 제출할 VP 구조 정의한 것
            String proofRequestJson = poolAndWalletManager.getProofRequest(memberId);

            Map<String, String> vp = poolAndWalletManager.createVP(proofRequestJson, memberWallet, member.getWalletMasterKey(), member.getName(), request.getReferentVC(), memberId);

            boolean res = poolAndWalletManager.verifyVP(proofRequestJson, vp);

            String carNumber;

            if (res) {
                System.out.println("VP 검증 완료 !!!!");

                System.out.println("[차량 위치] : " + request.getLocation().toString());
                System.out.println("[이용 가격] : " + request.getSharingPrice());
                System.out.println("[이용 가능 시간]");
                for (LocalDate sd : request.getDateList()) {
                    System.out.println(sd);
                }

//                List<String> urls = s3Uploader.uploadFile(images);
//                System.out.println("[차량 이미지 url]");
//                for (String url : urls) {
//                    System.out.println(url);
//                }

                // vp 데이터 뽑기
                String proofJson = vp.get("proofJson");
                JSONObject selfAttestedAttrs = new JSONObject(proofJson).getJSONObject("requested_proof").getJSONObject("self_attested_attrs");
                JSONObject revealedAttrs = new JSONObject(proofJson).getJSONObject("requested_proof").getJSONObject("revealed_attrs");

                //String userName = selfAttestedAttrs.getString("attr1_referent") + selfAttestedAttrs.getString("attr2_referent");
                carNumber = revealedAttrs.getJSONObject("attr3_referent").getString("raw");
                String carModel = revealedAttrs.getJSONObject("attr4_referent").getString("raw");
                String carFuel = revealedAttrs.getJSONObject("attr5_referent").getString("raw");
                String drivingRecord = revealedAttrs.getJSONObject("attr6_referent").getString("raw");
                String inspectionRecord = revealedAttrs.getJSONObject("attr7_referent").getString("raw");
                String carDeliveryDate = revealedAttrs.getJSONObject("attr8_referent").getString("raw");

                System.out.println("[VP에 있는 데이터]");
                System.out.println("차주 이름 : " + member.getName());
                System.out.println("차량 번호 : " + carNumber);
                System.out.println("차량 모델 : " + carModel);
                System.out.println("차량 연료 : " + carFuel);
                System.out.println("주행 거리 : " + drivingRecord);
                System.out.println("최근 검사 날짜 : " + inspectionRecord);
                System.out.println("출고 날짜 : " + carDeliveryDate);


                // 위 데이터 기반으로 자동차 등록 chainCode 실행
                TayoConnect tayoConnect = new TayoConnect(1);

                MemberRequest.registerCarRequest rq = request;
                Double carID = generateCarID(request.getReferentVC());
                System.out.println("carID: " + carID);
                Car car = new Car(carID, member.getId(), carModel, carFuel, carDeliveryDate, Integer.valueOf(drivingRecord), inspectionRecord,
                        rq.toDateStringList(), rq.getLocation().getSharingLocation(), rq.getLocation().getSharingLocationAddress(),
                        rq.getLocation().getSharingLatitude(), rq.getLocation().getSharingLongitude(), true, rq.getSharingPrice(), 0);
                tayoConnect.createCar(car);

                poolAndWalletManager.closeUserWallet(memberWallet);

                return new RegisterCarResponse(
                        member.getName(),
                        carNumber,
                        request.getLocation(),
                        request.getSharingPrice(),
                        request.getDateList());

            } else {
                System.out.println("VP 검증 실패 !!");
                poolAndWalletManager.closeUserWallet(memberWallet);
                throw new ApplicationException(ErrorCode.FAIL_VERIFY_VP);
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            throw new ApplicationException(ErrorCode.FAIL_VERIFY_VP2);
        } finally {
            poolAndWalletManager.closeUserWallet(memberWallet);
        }

    }

    @Transactional
    public CarResponse.CarDetailWithName getDetailCar(Double carId) throws Exception {
        TayoConnect tayoConnect = new TayoConnect(1);

        JsonElement cars = tayoConnect.queryByCarID(carId);
        CarResponse.CarDetail carDetail = CarResponse.CarDetail.fromJson(cars);

        MemberEntity member = memberRepository.findOne(carDetail.getOwnerID());
        String name = member.getName();

        return new CarResponse.CarDetailWithName(carDetail, name);

    }
    public double generateCarID(String referentVC) {
        byte[] memberIDBytes = referentVC.getBytes();

        try {
            // SHA-256 해시 함수를 사용하여 해시 계산
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] carIDBytes = md.digest(memberIDBytes);

            // 바이트 배열을 double로 변환
            double carID = bytesToDouble(carIDBytes);

            return carID;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    // 바이트 배열을 double로 변환하는 함수
    public static double bytesToDouble(byte[] bytes) {
        long longBits = 0;
        for (int i = 0; i < 8; i++) {
            longBits |= (long) (bytes[i] & 0xFF) << (8 * i);
        }
        return Double.longBitsToDouble(longBits);
    }

}
