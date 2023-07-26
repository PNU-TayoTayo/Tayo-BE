package pnu.cse.TayoTayo.TayoBE.service;

import com.sun.xml.bind.v2.TODO;
import lombok.RequiredArgsConstructor;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.hyperledger.indy.sdk.anoncreds.CredentialsSearchForProofReq;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.ledger.LedgerResults;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnu.cse.TayoTayo.TayoBE.dto.request.MemberRequest;
import pnu.cse.TayoTayo.TayoBE.model.Member;
import pnu.cse.TayoTayo.TayoBE.model.entity.MemberEntity;
import pnu.cse.TayoTayo.TayoBE.repository.MemberRepository;
import pnu.cse.TayoTayo.TayoBE.util.PoolAndWalletManager;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ExecutionException;


@Service
@RequiredArgsConstructor
public class CarService {

    private final MemberRepository memberRepository;

    private final PoolAndWalletManager poolAndWalletManager;



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
    public void createVC(Long Id , String walletPassword, String carNumber) throws IndyException, ExecutionException, InterruptedException {

        MemberEntity member = memberRepository.findOne(Id);

        Wallet memberWallet = poolAndWalletManager.openUserWallet(member.getEmail(), walletPassword);

        String carDID = poolAndWalletManager.createCarDID(memberWallet,member);

        String credentialOffer = poolAndWalletManager.getCredentialOfferFromVCService(member.getId(),memberWallet);

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


        String carVC = poolAndWalletManager.getVC(credentialRequestResult.getCredentialRequestJson(),credentialOffer,member.getName(),carNumber);

        System.out.println("\n\n\n받은 자동차에 대한 VC : " + carVC);

        // 유저가 받은 VC를 본인 지갑에 저장
        Anoncreds.proverStoreCredential(memberWallet, null, credentialRequestResult.getCredentialRequestMetadataJson()
                , carVC, parsedCredDefResponse.getObjectJson(), null);

        poolAndWalletManager.closeUserWallet(memberWallet);

        // TODO : VC 생성시...

    }

    @Transactional
    public void getVC(Long Id , String walletPassword) throws IndyException, ExecutionException, InterruptedException {

        MemberEntity member = memberRepository.findOne(Id);

        Wallet memberWallet = poolAndWalletManager.openUserWallet(member.getEmail(), walletPassword);

        JSONObject json = new JSONObject();

        String filter = json.put("issuer_did", "SwCFy44Qd6FKYPD2ABn7Jb").toString();

        // 발급자의 did로 뽑아냄 VC를 뽑아냄..?
        String credentials = Anoncreds.proverGetCredentials(memberWallet, filter).get();

        // TODO : 유저에게 어떤식으로 제공할 건가.... 이걸 토대로 요청 누르면 등록되는 방식이기 때문에 중요!!
        System.out.println("\n\ndid : "+Did.getListMyDidsWithMeta(memberWallet).get());
        System.out.println(credentials);

        poolAndWalletManager.closeUserWallet(memberWallet);

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
    public void postCar(Long memberId , String walletPassword , String referentVC) throws Exception {

        System.out.println("자동차 등록을 위한 VP 생성");

        MemberEntity member = memberRepository.findOne(memberId);

        Wallet memberWallet = poolAndWalletManager.openUserWallet(member.getEmail(), walletPassword);

        // 이게 제출할 VP 구조 정의한 것
        String proofRequestJson = poolAndWalletManager.getProofRequest(memberId);

        Map<String, String> vp = poolAndWalletManager.createVP(proofRequestJson, memberWallet, member.getWalletMasterKey(), member.getName(), referentVC, memberId);

        boolean res = poolAndWalletManager.verifyVP(proofRequestJson, vp);

        // 여기서 request
        if(res){ // 일치시
            // TODO : 받은 데이터들로 자동차 등록 chainCode 실행
            //      흠... VP 검증과 자동차 등록을 분리할까..
            System.out.println("일치!!!");

        }else{
            // TODO : 검증안되면 Exception 던지기
            System.out.println("불일치 !!");

        }

        poolAndWalletManager.closeUserWallet(memberWallet);

    }






}
