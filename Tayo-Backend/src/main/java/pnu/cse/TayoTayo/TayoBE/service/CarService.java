package pnu.cse.TayoTayo.TayoBE.service;

import com.sun.xml.bind.v2.TODO;
import lombok.RequiredArgsConstructor;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.hyperledger.indy.sdk.ledger.LedgerResults;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnu.cse.TayoTayo.TayoBE.dto.request.MemberRequest;
import pnu.cse.TayoTayo.TayoBE.model.Member;
import pnu.cse.TayoTayo.TayoBE.model.entity.MemberEntity;
import pnu.cse.TayoTayo.TayoBE.repository.MemberRepository;
import pnu.cse.TayoTayo.TayoBE.util.PoolAndWalletManager;

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

    // 트랜잭션을 너무 오래 잡고있긴 하는듯..
    @Transactional
    public void createVC(Long Id , String walletPassword) throws IndyException, ExecutionException, InterruptedException {

        MemberEntity member = memberRepository.findOne(Id);

        Wallet memberWallet = poolAndWalletManager.openUserWallet(member.getEmail(), walletPassword);

        String carDID = poolAndWalletManager.createCarDID(memberWallet);

        String credentialOffer = poolAndWalletManager.getCredentialOfferFromVCService(memberWallet);

        System.out.println("\n해당 유저가 VC 생성에 필요한 Master Secret을 생성하고 본인의 지갑에 저장");
        // TODO : MasterSecretId도 어디 저장해야하나?
        String MasterSecretId = Anoncreds.proverCreateMasterSecret(memberWallet, null).get();

        System.out.println("\n 해당 유저는 레저로부터 Credential Definition를 가져온다");
        LedgerResults.ParseResponseResult parsedCredDefResponse =
                poolAndWalletManager.getCredDef(carDID, new JSONObject(credentialOffer).getString("cred_def_id").toString());

        // 여기서 Credential request 매개변수로 AliceWallet, AliceDID, Credential Offer, Credential Definition, Master Secret ID
        System.out.println("\n해당 유저는 VC생성을 위해 Issuer에게 보낼 transcript credential request를 준비한다");
        AnoncredsResults.ProverCreateCredentialRequestResult credentialRequestResult =
                Anoncreds.proverCreateCredentialReq(memberWallet, carDID, credentialOffer,
                        parsedCredDefResponse.getObjectJson(), MasterSecretId).get();


        String carVC = poolAndWalletManager.getVC(credentialRequestResult.getCredentialRequestJson(),credentialOffer);

        System.out.println("\n\n\n받은 자동차에 대한 VC : " + carVC);

        // 유저가 받은 VC를 본인 지갑에 저장
        Anoncreds.proverStoreCredential(memberWallet, null, credentialRequestResult.getCredentialRequestMetadataJson()
                , carVC, parsedCredDefResponse.getObjectJson(), null);


    }







    }
