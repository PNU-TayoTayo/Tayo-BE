package pnu.cse.TayoTayo.TayoBE.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.anoncreds.CredentialsSearchForProofReq;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.ledger.LedgerResults;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import pnu.cse.TayoTayo.TayoBE.exception.ApplicationException;
import pnu.cse.TayoTayo.TayoBE.exception.ErrorCode;
import pnu.cse.TayoTayo.TayoBE.util.indy.indytest;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Getter
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

    public Wallet createMemberWallet(String userEmail, String walletPassword) throws IndyException, ExecutionException, InterruptedException {

        System.out.println("\n\n=== 회원가입시 Tayo 서비스 유저의 지갑 생성 시작===");
        Wallet.createWallet(getWalletConfig(userEmail), new JSONObject().put("key",walletPassword).toString()).get();
        Wallet memberWallet = Wallet.openWallet(getWalletConfig(userEmail), new JSONObject().put("key",walletPassword).toString()).get();

        System.out.println("\n\n==="+userEmail+"의 지갑 생성 완료===");
//        memberWallet.closeWallet().get();

        return memberWallet;
    }

    public static String getWalletConfig(String userEmail){
        return new JSONObject().put("id", userEmail).put("storage_config", new JSONObject()
                .put("path", "src/main/java/pnu/cse/TayoTayo/TayoBE/wallet/member_wallet")).toString();
    }


    public Wallet openUserWallet(String userEmail, String walletPassword) throws IndyException, ExecutionException, InterruptedException {
        // 해당 유저의 지갑 오픈
        return Wallet.openWallet(getWalletConfig(userEmail), new JSONObject().put("key", walletPassword).toString()).get();
    }

    public void closeUserWallet(Wallet userWallet) throws IndyException{
        // 해당 유저의 지갑 오픈
        userWallet.closeWallet();
    }

    public String createCarDID(Wallet userWallet) throws IndyException, ExecutionException, InterruptedException {
        // 자동차에 대한 DID 생성!!

        DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(userWallet, "{}").get();

        System.out.println("생성한 자동차 DID : " + didResult.getDid());
        System.out.println("생성한 자동차 DID의 VerKey : " + didResult.getVerkey());

        return didResult.getDid();
    }

    public String getCredentialOfferFromVCService(Long userId, Wallet userWallet) throws IndyException, ExecutionException, InterruptedException {

        // TODO : RestTemplate를 빈 설정을 통한 싱글 톤으로 하자!
        //       url도 주입 방식 사용
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8081/vc_service/offer";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();

            Tayo.put(userId+"_offer",responseBody);


            return responseBody;
        } else {
            // 에러 처리 로직 작성
            System.out.println("도착 정보 x");
            throw new ApplicationException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

    }

    public String getVC(String credentialRequestJson, String credentialOffer) {

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8081/vc_service/getVC";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> request = new HashMap<>();
        request.put("credentialRequestJson", credentialRequestJson);
        request.put("credentialOffer", credentialOffer);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);


        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
            System.out.println(responseBody);

            return responseBody;
        } else {
            // 에러 처리 로직 작성
            System.out.println("도착 정보 x");
            throw new ApplicationException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

    }

    public String getProofRequest(Long memberId) throws IndyException, ExecutionException, InterruptedException {

        String nonce = Anoncreds.generateNonce().get();
        JSONArray transcriptRestrictions =new JSONArray().put(
                new JSONObject().put("cred_def_id",
                        new JSONObject(Tayo.get(memberId + "_offer")).getString("cred_def_id") ));

        // 이게 해당 유저가 작성해야하는 VP form임 !!! (즉 요청이 들어오면 이걸 생성해야함...)
        String proofRequestJson = new JSONObject()
                .put("nonce", nonce) //재생 공격을 완화하고 SSI 생태계에서 검증자와 보유자 간의 통신의 신선함과 진정성을 보장하여 추가 보안 계층을 추가하는 것
                .put("name", "Register-Car")
                .put("version", "0.1")
                .put("requested_attributes", new JSONObject()
                        .put("attr1_referent", new JSONObject().put("name", "owner_first_name"))
                        .put("attr2_referent", new JSONObject().put("name", "owner_last_name"))
                        .put("attr3_referent", new JSONObject().put("name", "car_number").put("restrictions", transcriptRestrictions))
                        .put("attr4_referent", new JSONObject().put("name", "car_model").put("restrictions", transcriptRestrictions)))
                .put("requested_predicates", new JSONObject()
                        // 주행 거리 200,000km 이하
                        .put("predicate1_referent", new JSONObject()
                                .put("name", "driving_record")
                                .put("p_type", "<=")
                                .put("p_value", 200000)
                                .put("restrictions", transcriptRestrictions))
                        // 출고 이후 15년 미만
                        .put("predicate2_referent", new JSONObject()
                                .put("name", "car_delivery_date")
                                .put("p_type", "<=")
                                .put("p_value", LocalDate.now().minusYears(15).toString())
                                .put("restrictions", transcriptRestrictions))
                        // 정기 검사 기간 6개월 이내 (검사 결과 모두 적합!)
                        .put("predicate3_referent", new JSONObject()
                                .put("name", "inspection_record")
                                .put("p_type", "<=")
                                .put("p_value", LocalDate.now().minusYears(15).toString())
                                .put("restrictions", transcriptRestrictions))
                )
                .toString();

        return proofRequestJson;
    }

    public String createVP(String proofRequestJson, Wallet memberWallet , String masterKey) throws Exception {

        // TODO : 해당 member Wallet에 자동차에 대한 VC가 여러가지가 있으면 뭘 가져오는거지?
        CredentialsSearchForProofReq search_for_job_application_proof_request = CredentialsSearchForProofReq.open(
                memberWallet, proofRequestJson, null).get();

        // TODO : VP 채우기 위해 VC 에서 뽑는 과정 ??
        JSONArray credentialsForAttribute3 = new JSONArray(search_for_job_application_proof_request.fetchNextCredentials("attr3_referent", 100).get());
        String credentialIdForAttribute3 = credentialsForAttribute3.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForAttribute4 = new JSONArray(search_for_job_application_proof_request.fetchNextCredentials("attr4_referent", 100).get());
        String credentialIdForAttribute4 = credentialsForAttribute4.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForPredicate1 = new JSONArray(search_for_job_application_proof_request.fetchNextCredentials("predicate1_referent", 100).get());
        String credentialIdForPredicate1 = credentialsForPredicate1.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForPredicate2 = new JSONArray(search_for_job_application_proof_request.fetchNextCredentials("predicate2_referent", 100).get());
        String credentialIdForPredicate2 = credentialsForPredicate2.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForPredicate3 = new JSONArray(search_for_job_application_proof_request.fetchNextCredentials("predicate3_referent", 100).get());
        String credentialIdForPredicate3 = credentialsForPredicate3.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        search_for_job_application_proof_request.close();

        String credentialsJson = new JSONObject()
                .put("self_attested_attributes", new JSONObject()
                        .put("attr1_referent", "Alice")
                        .put("attr2_referent", "Garcia"))
                // requested_attributes는 VC에서 뽑은 데이터
                .put("requested_attributes", new JSONObject()
                        .put("attr3_referent", new JSONObject()
                                .put("cred_id", credentialIdForAttribute3)
                                .put("revealed", true))
                        .put("attr4_referent", new JSONObject()
                                .put("cred_id", credentialIdForAttribute4)
                                .put("revealed", true)))
                // requested_predicates 이거는 영지식 증명들
                .put("requested_predicates", new JSONObject()
                        .put("predicate1_referent", new JSONObject()
                                .put("cred_id",credentialIdForPredicate1))
                        .put("predicate2_referent", new JSONObject()
                                .put("cred_id",credentialIdForPredicate2))
                        .put("predicate3_referent", new JSONObject()
                                .put("cred_id",credentialIdForPredicate3))
                )
                .toString();

        //VP 생성을 위한 추가 정보들 ..?
        JSONObject schemasMap = new JSONObject();
        JSONObject credDefsMap = new JSONObject();

//        populateCredentialInfo(pool, Alice.get("did").toString(), schemasMap, credDefsMap, credentialsForAttribute3);
//        populateCredentialInfo(pool, Alice.get("did").toString(), schemasMap, credDefsMap, credentialsForAttribute4);
//        populateCredentialInfo(pool, Alice.get("did").toString(), schemasMap, credDefsMap, credentialsForAttribute5);
//        populateCredentialInfo(pool, Alice.get("did").toString(), schemasMap, credDefsMap, credentialsForAttribute6);

        String schemas = schemasMap.toString();
        String credDefs = credDefsMap.toString();
        String revocState = new JSONObject().toString();

        // 최종 제출할 VP
        String proofJson = Anoncreds.proverCreateProof(
                memberWallet
                ,proofRequestJson // proofRequestJson
                ,credentialsJson // credentialsJson
                ,masterKey  // 이건 어디서 들고오지...? (VC의 Master Key를 어디다 저장해야 할까?)
                ,schemas
                ,credDefs
                ,revocState).get();

        return proofJson;


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

    public void closeAndDeleteWallet(Wallet wallet, String config, String key) throws Exception {
        if (wallet != null) {
            wallet.closeWallet().get();
            Wallet.deleteWallet(config, key).get();
        }
    }

    private String signAndSubmitRequest(Pool pool, Wallet endorserWallet, String endorserDid, String request) throws Exception {
        return submitRequest(pool, Ledger.signRequest(endorserWallet, endorserDid, request).get());
    }

    private String submitRequest(Pool pool, String req) throws Exception {
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

    public LedgerResults.ParseResponseResult getCredDef(String carDID, String cred_def_id) throws IndyException, ExecutionException, InterruptedException {

        String get_cred_def_request = Ledger.buildGetCredDefRequest(carDID, cred_def_id).get();

        String get_cred_def_response = ensurePreviousRequestApplied(pool, get_cred_def_request, response -> {
            JSONObject getSchemaResponseObject = new JSONObject(response);
            return !getSchemaResponseObject.getJSONObject("result").isNull("seqNo");
        });
        return Ledger.parseGetCredDefResponse(get_cred_def_response).get();
    }

    public String ensurePreviousRequestApplied(Pool pool, String checkerRequest, indytest.PoolResponseChecker checker)
            throws IndyException, ExecutionException, InterruptedException {

        for (int i = 0; i < 3; i++) {
            String response = Ledger.submitRequest(pool, checkerRequest).get();
            try {
                if (checker.check(response)) {
                    return response;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                System.err.println(e.toString());
                System.err.println(response);
            }
            Thread.sleep(10000);
        }

        throw new IllegalStateException();
    }

    public void populateCredentialInfo(Pool pool, String did, JSONObject schemas, JSONObject credDefs, JSONArray credentials) throws Exception {
        for (JSONObject o : array2List(credentials)) {
            JSONObject credInfo = o.getJSONObject("cred_info");
            String schemaId = credInfo.getString("schema_id");
            String credDefId = credInfo.getString("cred_def_id");

            if (schemas.isNull(schemaId)) {
                String getSchemaRequest = Ledger.buildGetSchemaRequest(did, schemaId).get();
                String getSchemaResponse = Ledger.submitRequest(pool, getSchemaRequest).get();
                LedgerResults.ParseResponseResult parseSchemaResult = Ledger.parseGetSchemaResponse(getSchemaResponse).get();
                String schemaJson = parseSchemaResult.getObjectJson();
                schemas.put(schemaId, new JSONObject(schemaJson));
            }
            if (credDefs.isNull(credDefId)) {
                String getCredDefRequest = Ledger.buildGetCredDefRequest(did, credDefId).get();
                String getCredDefResponse = Ledger.submitRequest(pool, getCredDefRequest).get();
                LedgerResults.ParseResponseResult parseCredDefResponse = Ledger.parseGetCredDefResponse(getCredDefResponse).get();

                String credDefJson = parseCredDefResponse.getObjectJson();
                credDefs.put(credDefId, new JSONObject(credDefJson));
            }
        }
    }

    public List<JSONObject> array2List(JSONArray credentials) {
        List<JSONObject> result = new ArrayList<>();
        credentials.forEach(o -> result.add((JSONObject) o));
        return result;
    }



}
