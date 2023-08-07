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
import pnu.cse.TayoTayo.TayoBE.model.entity.MemberEntity;
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

    static Logger log = LoggerFactory.getLogger(PoolAndWalletManager.class);
    private final Pool pool;
    private final Wallet stewardWallet;

    private final Wallet TayoWallet;

    private Map<String, Object> steward = new HashMap<>();

    @Getter
    private Map<String, Object> Tayo = new HashMap<>();


    public PoolAndWalletManager() throws Exception {
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

        try { // 존재하는 지 체크
            stWallet = Wallet.openWallet(steward.get("wallet_config").toString(), steward.get("wallet_credentials").toString()).get();
            log.info("stwardWallet 이미 존재");

            JSONArray jsonArray3 = new JSONArray(Did.getListMyDidsWithMeta(stWallet).get());
            JSONObject jsonObject3 = jsonArray3.getJSONObject(0);
            String stewardDid = jsonObject3.getString("did");
            steward.put("did",stewardDid);

        }catch(Exception e){
            Wallet.createWallet(steward.get("wallet_config").toString(), steward.get("wallet_credentials").toString()).get();
            stWallet = Wallet.openWallet(steward.get("wallet_config").toString(), steward.get("wallet_credentials").toString()).get();

            DidResults.CreateAndStoreMyDidResult stewardDid = Did.createAndStoreMyDid(stWallet, new JSONObject().put("seed", steward.get("seed")).toString()).get();
            steward.put("did",stewardDid.getDid());
            steward.put("key",stewardDid.getVerkey());
        }


        System.out.println("\n\n===Steward의 지갑 생성 완료===");
        return stWallet;
    }

    private Wallet createTayoWallet() throws Exception {

        System.out.println("\n\n===Tayo 서비스의 지갑 생성 시작===");

        Tayo.put("wallet_config",
                new JSONObject().put("id", "Tayo_wallet")
                        .put("storage_config", new JSONObject()
                                .put("path", "src/main/java/pnu/cse/TayoTayo/TayoBE/wallet")).toString());
        Tayo.put("wallet_credentials", new JSONObject().put("key", "Tayo_wallet_key").toString());

        Wallet TaWallet = null;

        try { // 만약 존재하면 open
            TaWallet = Wallet.openWallet(Tayo.get("wallet_config").toString(), Tayo.get("wallet_credentials").toString()).get();

            JSONArray jsonArray3 = new JSONArray(Did.getListMyDidsWithMeta(TaWallet).get());
            JSONObject jsonObject3 = jsonArray3.getJSONObject(0);
            String tayoDid = jsonObject3.getString("did");
            Tayo.put("did",tayoDid);

        }catch(Exception e){ // 존재안하면 생성하고 open
            Wallet.createWallet(Tayo.get("wallet_config").toString(), Tayo.get("wallet_credentials").toString()).get();
            TaWallet = Wallet.openWallet(Tayo.get("wallet_config").toString(), Tayo.get("wallet_credentials").toString()).get();

            DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(TaWallet, "{}").get();
            Tayo.put("did",didResult.getDid());
            Tayo.put("key",didResult.getVerkey());

            String nymRequest = Ledger.buildNymRequest(steward.get("did").toString(), Tayo.get("did").toString(), Tayo.get("key").toString(),
                    null, "TRUST_ANCHOR").get();
            String res = signAndSubmitRequest(pool, stewardWallet,(String)steward.get("did"), nymRequest);
            System.out.println(res);
        }

//        // 존재하면
//        if(TaWallet != null){
//            try {
//                DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(TaWallet, "{}").get();
//                Tayo.put("did",didResult.getDid());
//                Tayo.put("key",didResult.getVerkey());
//
//                String nymRequest = Ledger.buildNymRequest(steward.get("did").toString(), Tayo.get("did").toString(), Tayo.get("key").toString(),
//                        null, "TRUST_ANCHOR").get();
//                String res = signAndSubmitRequest(pool, stewardWallet,(String)steward.get("did"), nymRequest);
//                System.out.println(res);
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }

        System.out.println("\n\n===Tayo의 지갑 생성 완료===");

        return TaWallet;
    }

    public Wallet createMemberWallet(String userEmail, String walletPassword) throws IndyException, ExecutionException, InterruptedException {

        System.out.println("\n\n=== 회원가입시 Tayo 서비스 유저의 지갑 생성 시작===");
        Wallet.createWallet(getWalletConfig(userEmail), new JSONObject().put("key",walletPassword).toString()).get();
        Wallet memberWallet = Wallet.openWallet(getWalletConfig(userEmail), new JSONObject().put("key",walletPassword).toString()).get();

        System.out.println("\n\n==="+userEmail+"의 지갑 생성 완료===");

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

    public String createCarDID(Wallet userWallet, MemberEntity member) throws IndyException, ExecutionException, InterruptedException {
        // 자동차에 대한 DID 생성!!

        DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(userWallet, "{}").get();

        Tayo.put(member.getId()+"_did",didResult.getDid());

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

    public String getVC(String credentialRequestJson, String credentialOffer, String memberName, String carNumber) {

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8081/vc_service/getVC";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> request = new HashMap<>();
        request.put("credentialRequestJson", credentialRequestJson);
        request.put("credentialOffer", credentialOffer);
        request.put("memberName",memberName);
        request.put("carNumber",carNumber);


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
                        new JSONObject(Tayo.get(memberId + "_offer").toString()).getString("cred_def_id") ));

        // 이게 해당 유저가 작성해야하는 VP form임 !!! (즉 요청이 들어오면 이걸 생성해야함...)
        String proofRequestJson = new JSONObject()
                .put("nonce", nonce)
                .put("name", "Register-Car")
                .put("version", "0.1")
                .put("requested_attributes", new JSONObject()
                        .put("attr1_referent", new JSONObject().put("name", "owner_first_name"))
                        .put("attr2_referent", new JSONObject().put("name", "owner_last_name"))
                        .put("attr3_referent", new JSONObject().put("name", "car_number").put("restrictions", transcriptRestrictions))
                        .put("attr4_referent", new JSONObject().put("name", "car_model").put("restrictions", transcriptRestrictions))
                        .put("attr5_referent", new JSONObject().put("name", "car_fuel").put("restrictions", transcriptRestrictions))
                        .put("attr6_referent", new JSONObject().put("name", "driving_record").put("restrictions", transcriptRestrictions))
                        .put("attr7_referent", new JSONObject().put("name", "inspection_record").put("restrictions", transcriptRestrictions))
                        .put("attr8_referent", new JSONObject().put("name", "car_delivery_date").put("restrictions", transcriptRestrictions)))
                .put("requested_predicates", new JSONObject()
//                        .put("predicate1_referent", new JSONObject()
//                                .put("name", "driving_record")
//                                .put("p_type", ">=")
//                                .put("p_value", 200) // 주행 거리 200,000km 이하
//                                .put("restrictions", transcriptRestrictions))
////                        // 출고 이후 15년 미만
//                        .put("predicate2_referent", new JSONObject()
//                                .put("name", "car_delivery_date")
//                                .put("p_type", ">=")
//                                .put("p_value",20220101)
//                                .put("restrictions", transcriptRestrictions))
//                        // 정기 검사 기간 6개월 이내 (검사 결과 모두 적합!)
//                        .put("predicate3_referent", new JSONObject()
//                                .put("name", "inspection_record")
//                                .put("p_type", ">=")
//                                .put("p_value", 20200101)
//                                .put("restrictions", transcriptRestrictions))
                )
                .toString();

        return proofRequestJson;
    }

    public Map<String, String> createVP(String proofRequestJson, Wallet memberWallet , String masterKey, String memberName ,String referentVC, Long memberId) throws Exception {

        CredentialsSearchForProofReq proofRequest = CredentialsSearchForProofReq.open(
                memberWallet, proofRequestJson, null).get();


        // TODO : VP 채우기 위해 VC 에서 뽑는 과정 ??
        JSONArray credentialsForAttribute3 = new JSONArray(proofRequest.fetchNextCredentials("attr3_referent", 100).get());
        String credentialIdForAttribute3 = credentialsForAttribute3.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForAttribute4 = new JSONArray(proofRequest.fetchNextCredentials("attr4_referent", 100).get());
        String credentialIdForAttribute4 = credentialsForAttribute4.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForAttribute5 = new JSONArray(proofRequest.fetchNextCredentials("attr5_referent", 100).get());
        String credentialIdForAttribute5 = credentialsForAttribute5.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForAttribute6 = new JSONArray(proofRequest.fetchNextCredentials("attr6_referent", 100).get());
        String credentialIdForAttribute6 = credentialsForAttribute6.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForAttribute7 = new JSONArray(proofRequest.fetchNextCredentials("attr7_referent", 100).get());
        String credentialIdForAttribute7 = credentialsForAttribute7.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForAttribute8 = new JSONArray(proofRequest.fetchNextCredentials("attr8_referent", 100).get());
        String credentialIdForAttribute8 = credentialsForAttribute8.getJSONObject(0).getJSONObject("cred_info").getString("referent");


//        JSONArray credentialsForPredicate1 = new JSONArray(proofRequest.fetchNextCredentials("predicate1_referent", 100).get());
//        String credentialIdForPredicate1 = credentialsForPredicate1.getJSONObject(0).getJSONObject("cred_info").getString("referent");
//
//        JSONArray credentialsForPredicate2 = new JSONArray(proofRequest.fetchNextCredentials("predicate2_referent", 100).get());
//        String credentialIdForPredicate2 = credentialsForPredicate2.getJSONObject(0).getJSONObject("cred_info").getString("referent");
//
//        JSONArray credentialsForPredicate3 = new JSONArray(proofRequest.fetchNextCredentials("predicate3_referent", 100).get());
//        String credentialIdForPredicate3 = credentialsForPredicate3.getJSONObject(0).getJSONObject("cred_info").getString("referent");
//
        proofRequest.close();

        // 이게 제출할 vp
        String credentialsJson = new JSONObject()
                .put("self_attested_attributes", new JSONObject()
                        .put("attr1_referent","kim" ) // TODO : 수정
                        .put("attr2_referent", "donwoo"))
                // requested_attributes는 VC에서 뽑은 데이터
                .put("requested_attributes", new JSONObject()
                        .put("attr3_referent", new JSONObject()
                                .put("cred_id", referentVC)
                                .put("revealed", true))
                        .put("attr4_referent", new JSONObject()
                                .put("cred_id", referentVC)
                                .put("revealed", true))
                        .put("attr5_referent", new JSONObject()
                                .put("cred_id", referentVC)
                                .put("revealed", true))
                        .put("attr6_referent", new JSONObject()
                                .put("cred_id", referentVC)
                                .put("revealed", true))
                        .put("attr7_referent", new JSONObject()
                                .put("cred_id", referentVC)
                                .put("revealed", true))
                        .put("attr8_referent", new JSONObject()
                                .put("cred_id", referentVC)
                                .put("revealed", true)))
                // requested_predicates 이거는 영지식 증명들
                .put("requested_predicates", new JSONObject()
//                        .put("predicate1_referent", new JSONObject()
//                                .put("cred_id",referentVC)))
//                        .put("predicate2_referent", new JSONObject()
//                                .put("cred_id",referentVC))
//                        .put("predicate3_referent", new JSONObject()
//                                .put("cred_id",referentVC))
//                )
                ).toString();

        System.out.println("\n\ncredentialsJson : "+credentialsJson);

        // ProofRequest 생성하기 위해
        JSONObject schemasMap = new JSONObject();
        JSONObject credDefsMap = new JSONObject();
//
//        // TODO : 여기서 해당 차량의 DID를 넣으면 되려나?? VC value에 DID값을 넣을까?

        populateCredentialInfo(pool, Tayo.get(memberId+"_did").toString(), schemasMap, credDefsMap, credentialsForAttribute3);
        populateCredentialInfo(pool, Tayo.get(memberId+"_did").toString(), schemasMap, credDefsMap, credentialsForAttribute4);
        populateCredentialInfo(pool, Tayo.get(memberId+"_did").toString(), schemasMap, credDefsMap, credentialsForAttribute5);
        populateCredentialInfo(pool, Tayo.get(memberId+"_did").toString(), schemasMap, credDefsMap, credentialsForAttribute6);
        populateCredentialInfo(pool, Tayo.get(memberId+"_did").toString(), schemasMap, credDefsMap, credentialsForAttribute7);
        populateCredentialInfo(pool, Tayo.get(memberId+"_did").toString(), schemasMap, credDefsMap, credentialsForAttribute8);

        Map<String, String> temp = new HashMap<>();

        String schemas = schemasMap.toString();
        String credDefs = credDefsMap.toString();
        String revocState = new JSONObject().toString();

        System.out.println("schemas : " + schemas);
        System.out.println("credDefs : " + credDefs);

        // 최종 제출할 VP
        String proofJson = Anoncreds.proverCreateProof(
                memberWallet
                ,proofRequestJson // proofRequestJson
                ,credentialsJson // credentialsJson
                ,masterKey
                ,schemas
                ,credDefs
                ,revocState).get();

        temp.put("schemas" , schemas);
        temp.put("credDefs" , credDefs);
        temp.put("proofJson" , proofJson);


        return temp;
    }

    public boolean verifyVP(String proofRequestJson, Map<String, String> vpMap) throws IndyException, ExecutionException, InterruptedException {

        System.out.println("\n\nvp의 schemas : " + vpMap.get("schemas"));
        System.out.println("vp의 credDefs : " + vpMap.get("credDefs"));
        System.out.println("vp의 proofJson : " + vpMap.get("proofJson"));

        String vp = vpMap.get("proofJson");

        System.out.println("\n\n 타요타요 유저의 vp를 검증하는 단계!!");

        JSONObject selfAttestedAttrs = new JSONObject(vp).getJSONObject("requested_proof").getJSONObject("self_attested_attrs");
        JSONObject revealedAttrs = new JSONObject(vp).getJSONObject("requested_proof").getJSONObject("revealed_attrs");
        System.out.println("SelfAttestedAttrs: " + selfAttestedAttrs);
        System.out.println("RevealedAttrs: " + revealedAttrs);

        String revocRegDefs = new JSONObject().toString();
        String revocRegs = new JSONObject().toString();

        System.out.println("===========");
        System.out.println(vpMap.get("schemas"));
        System.out.println(vpMap.get("credDefs"));
        System.out.println("===========");

        // 검증 과정
        Boolean same = Anoncreds.verifierVerifyProof(
                proofRequestJson,
                vp,
                vpMap.get("schemas"),
                vpMap.get("credDefs"),
                revocRegDefs, revocRegs).get();

        return same;


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
