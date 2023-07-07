package pnu.cse.TayoTayo.TayoBE.util;

import lombok.RequiredArgsConstructor;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.HTTP;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Component
public class PoolAndWalletManager {

    /*
        TODO : 지갑 생성하고 close 상태로 둬야 할까? ㅇㅇ

     */

    static Logger log = LoggerFactory.getLogger(PoolAndWalletManager.class);
    private final Pool pool;
    private final Wallet stewardWallet;

    private final Wallet TayoWallet;

    private Map<String, Object> steward = new HashMap<>();
    private Map<String, Object> Tayo = new HashMap<>();


    public PoolAndWalletManager() throws IndyException, ExecutionException, InterruptedException {
        this.pool = createPool();
        this.stewardWallet = createStewardWallet();
        this.TayoWallet = createTayoWallet();
    }

    private Pool createPool() throws IndyException, ExecutionException, InterruptedException {
        String poolName = "TayoTayoPool";
        String poolConfig = "{\"genesis_txn\": \"src/main/java/pnu/cse/TayoTayo/TayoBE/util/indy/pool1.txn\"} ";

        Pool.setProtocolVersion(2);
        Pool pool = null;
        try {
            pool = Pool.openPoolLedger(poolName, "{}").get();
        }catch(Exception e){
            Pool.createPoolLedgerConfig(poolName, poolConfig).get();
            pool = Pool.openPoolLedger(poolName, "{}").get();
        }

        System.out.println("Pool created and opened successfully.");

        return pool;
    }

    private Wallet createStewardWallet() throws IndyException, ExecutionException, InterruptedException {

        System.out.println("\n\n===Steward의 지갑 생성 시작===");

        // steward 관련 속성
        steward.put("name", "Sovrin Steward");
        steward.put("wallet_config",
                new JSONObject().put("id", "souvrin_steward_wallet")
                        .put("storage_config", new JSONObject()
                                .put("path", "src/main/java/pnu/cse/TayoTayo/TayoBE/wallet")).toString());
        steward.put("wallet_credentials", new JSONObject().put("key", "steward_wallet_key").toString());
        steward.put("seed","000000000000000000000000Steward1");


        Wallet stWallet = null;

        try {
            stWallet = Wallet.openWallet(steward.get("wallet_config").toString(), steward.get("wallet_credentials").toString()).get();
            log.info("stwardWallet 이미 존재");
        }catch(Exception e){
            Wallet.createWallet(steward.get("wallet_config").toString(), steward.get("wallet_credentials").toString()).get();
            stWallet = Wallet.openWallet(steward.get("wallet_config").toString(), steward.get("wallet_credentials").toString()).get();
        }


        DidResults.CreateAndStoreMyDidResult stewardDid = Did.createAndStoreMyDid(stWallet, new JSONObject().put("seed", steward.get("seed")).toString()).get();
        steward.put("did",stewardDid.getDid());
        steward.put("key",stewardDid.getVerkey());

        System.out.println("\n\n===Steward의 지갑 생성 완료===");
        return stWallet;
    }

    private Wallet createTayoWallet() throws IndyException, ExecutionException, InterruptedException {

        System.out.println("\n\n===Tayo 서비스의 지갑 생성 시작===");

        Tayo.put("wallet_config",
                new JSONObject().put("id", "Tayo_wallet")
                        .put("storage_config", new JSONObject()
                                .put("path", "src/main/java/pnu/cse/TayoTayo/TayoBE/wallet")).toString());
        Tayo.put("wallet_credentials", new JSONObject().put("key", "Tayo_wallet_key").toString());

        Wallet TaWallet = null;

        try { // 만약 존재하면 open
            TaWallet = Wallet.openWallet(Tayo.get("wallet_config").toString(), Tayo.get("wallet_credentials").toString()).get();
        }catch(Exception e){ // 존재안하면 생성하고 open
            Wallet.createWallet(Tayo.get("wallet_config").toString(), Tayo.get("wallet_credentials").toString()).get();
            TaWallet = Wallet.openWallet(Tayo.get("wallet_config").toString(), Tayo.get("wallet_credentials").toString()).get();
        }

        // 존재하면
        if(TaWallet != null){
            try {
                DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(TaWallet, "{}").get();
                Tayo.put("did",didResult.getDid());
                Tayo.put("key",didResult.getVerkey());

                String nymRequest = Ledger.buildNymRequest(steward.get("did").toString(), Tayo.get("did").toString(), Tayo.get("key").toString(),
                        null, "TRUST_ANCHOR").get();
                String res = signAndSubmitRequest(pool, stewardWallet,(String)steward.get("did"), nymRequest);
                System.out.println(res);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println("\n\n===Tayo의 지갑 생성 완료===");

        return TaWallet;
    }

    public void createMemberWallet(String userEmail, String walletPassword) throws IndyException, ExecutionException, InterruptedException {

        System.out.println("\n\n=== 회원가입시 Tayo 서비스 유저의 지갑 생성 시작===");
        Wallet.createWallet(getWalletConfig(userEmail), new JSONObject().put("key",walletPassword).toString()).get();
        Wallet memberWallet = Wallet.openWallet(getWalletConfig(userEmail), new JSONObject().put("key",walletPassword).toString()).get();

        System.out.println("\n\n==="+userEmail+"의 지갑 생성 완료===");
        memberWallet.closeWallet().get();
    }

    public static String getWalletConfig(String userEmail){
        return new JSONObject().put("id", userEmail).put("storage_config", new JSONObject()
                .put("path", "src/main/java/pnu/cse/TayoTayo/TayoBE/wallet/member_wallet")).toString();
    }

    public void getCredentialOfferFromVCService(String userEmail, String walletPassword) throws IndyException, ExecutionException, InterruptedException {

        // 해당 유저의 지갑 오픈
        Wallet userWallet = Wallet.openWallet(getWalletConfig(userEmail), new JSONObject().put("key", walletPassword).toString()).get();

        // 자동차에 대한 DID 생성!!
        if(userWallet != null){
            try {
                DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(userWallet, "{}").get();

                System.out.println("생성한 자동차 DID : " + didResult.getDid());
                System.out.println("생성한 자동차 DID의 VerKey : " + didResult.getVerkey());


            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // TODO : RestTemplate를 빈 설정을 통한 싱글 톤으로 하자!
        //       url도 주입 방식 사용
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8081/vc_service/offer";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
            System.out.println(userEmail+"가 요청한 credential Offer : " + responseBody);
        } else {
            // 에러 처리 로직 작성
            System.out.println("도착 정보 x");
        }





    }




    @PostConstruct
    public void init() throws IndyException, ExecutionException, InterruptedException {

        System.out.println("\n steward의 DID : "+Did.getListMyDidsWithMeta(stewardWallet).get());
        System.out.println(Did.getListMyDidsWithMeta(TayoWallet).get());
        //TODO : 앞에서 지갑 생성하고 Schema 등록이나 Credential Definition 등록 같은건 여기서 하기



    }

    @PreDestroy
    public void cleanup() throws Exception {
        // 여기서 Pool 삭제하고 Government랑 Issuer 지갑 삭제
        //closeAndDeleteWallet(stewardWallet,steward.get("wallet_config").toString(),steward.get("wallet_credentials").toString());

//        pool.closePoolLedger().get();
//        Pool.deletePoolLedgerConfig("TayoTayoPool").get();
    }

    public static void closeAndDeleteWallet(Wallet wallet, String config, String key) throws Exception {
        if (wallet != null) {
            wallet.closeWallet().get();
            Wallet.deleteWallet(config, key).get();
        }
    }
    private static String signAndSubmitRequest(Pool pool, Wallet endorserWallet, String endorserDid, String request) throws Exception {
        return submitRequest(pool, Ledger.signRequest(endorserWallet, endorserDid, request).get());
    }

    private static String submitRequest(Pool pool, String req) throws Exception {
        String res = Ledger.submitRequest(pool, req).get();
        if ("REPLY".equals(new JSONObject(res).get("op"))) {
            log.info("SubmitRequest: " + req);
            log.info("SubmitResponse: " + res);
        } else {
            log.warn("SubmitRequest: " + req);
            log.warn("SubmitResponse: " + res);
        }
        return res;
    }

}
