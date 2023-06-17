package com.example.Indysdktest.indy;

import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
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

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.hyperledger.indy.sdk.IndyConstants.ROLE_STEWARD;
import static org.hyperledger.indy.sdk.IndyConstants.ROLE_TRUSTEE;
import static org.hyperledger.indy.sdk.pool.Pool.openPoolLedger;

public class indytest {

    static Logger log = LoggerFactory.getLogger(indytest.class);

    public static void setUp() throws Exception {


        System.out.println("\n\n\nSTEP 1 - Connect to Pool");
        Pool pool = createAndOpenPoolLedger();

        System.out.println("\n\n\nSTEP 2 - Configuring steward");
        Map<String, Object> steward = new HashMap<>();
        steward.put("name", "Sovrin Steward");
        steward.put("wallet_config", new JSONObject().put("id", "souvrin_steward_wallet").toString());
        steward.put("wallet_credentials", new JSONObject().put("key", "steward_wallet_key").toString());
        steward.put("seed", "000000000000000000000000Steward1");

        Wallet.createWallet(steward.get("wallet_config").toString(), steward.get("wallet_credentials").toString()).get();
        Wallet stewardWallet = Wallet.openWallet(steward.get("wallet_config").toString(), steward.get("wallet_credentials").toString()).get();

        if(stewardWallet != null){
            try {
                // Create Trustee DID
                String trusteeSeed = new JSONObject().put("seed", "000000000000000000000000Steward1").toString();

                DidResults.CreateAndStoreMyDidResult stewardDid = Did.createAndStoreMyDid(stewardWallet, trusteeSeed).get();
                steward.put("did",stewardDid.getDid());
                steward.put("key",stewardDid.getVerkey());

                // 결과 출력
                System.out.println("steward DID: " + steward.get("did"));
                System.out.println("steward Key: " + steward.get("key"));

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }



        System.out.println("\n\n\n");
        System.out.println("STEP 3 - register DID for government");

        // Government 정보 설정
        Map<String, Object> government = new HashMap<>();
        government.put("name", "theGovernment");
        government.put("wallet_config", new JSONObject().put("id", "government_wallet").toString());
        government.put("wallet_credentials", new JSONObject().put("key", "government_wallet_key"));

        Wallet.createWallet(government.get("wallet_config").toString(), government.get("wallet_credentials").toString()).get();
        Wallet governmentWallet = Wallet.openWallet(government.get("wallet_config").toString(), government.get("wallet_credentials").toString()).get();

        if(governmentWallet != null){
            try {
                DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(governmentWallet, "{}").get();
                government.put("did",didResult.getDid());
                government.put("key",didResult.getVerkey());

                // 결과 출력
                System.out.println("government DID: " + government.get("did"));
                System.out.println("government Key: " + government.get("key"));

                // steward의 did를 사용하여 government의 did를 등록하고, 해당 did를 TRUST_ANCHOR 역할로 설정하는 작업을 수행
                String nymRequest = Ledger.buildNymRequest(steward.get("did").toString(), government.get("did").toString(), government.get("key").toString(),
                        null, "TRUST_ANCHOR").get();
                String res = signAndSubmitRequest(pool, stewardWallet, steward.get("did").toString(), nymRequest);
                System.out.println(res);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println(Did.getListMyDidsWithMeta(governmentWallet).get());


        System.out.println("\n\n\nSTEP 3 - register DID for University");

        // University 정보 설정
        Map<String, Object> university = new HashMap<>();
        university.put("name", "University");
        university.put("wallet_config", new JSONObject().put("id", "theUniversity_wallet").toString());
        university.put("wallet_credentials", new JSONObject().put("key", "theUniversity_wallet_key").toString());

        Wallet.createWallet(university.get("wallet_config").toString(), university.get("wallet_credentials").toString()).get();
        Wallet universityWallet = Wallet.openWallet(university.get("wallet_config").toString(), university.get("wallet_credentials").toString()).get();

        if(universityWallet != null){
            try {
                DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(universityWallet, "{}").get();
                university.put("did",didResult.getDid());
                university.put("key",didResult.getVerkey());

                // 결과 출력
                System.out.println("university DID: " + university.get("did"));
                System.out.println("university Key: " + university.get("key"));

                String nymRequest = Ledger.buildNymRequest(steward.get("did").toString(), university.get("did").toString(), university.get("key").toString(),
                        null, "TRUST_ANCHOR").get();
                String res = signAndSubmitRequest(pool, stewardWallet, steward.get("did").toString(), nymRequest);
                System.out.println(res);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println(Did.getListMyDidsWithMeta(universityWallet).get());


        System.out.println("\n\n\n");
        System.out.println("STEP 3 - register DID for Company");

        // Government 정보 설정
        Map<String, Object> company = new HashMap<>();
        company.put("name", "company");
        company.put("wallet_config", new JSONObject().put("id", "theCompany_wallet").toString());
        company.put("wallet_credentials", new JSONObject().put("key", "theCompany_wallet_key").toString());

        Wallet.createWallet(company.get("wallet_config").toString(), company.get("wallet_credentials").toString()).get();
        Wallet companyWallet = Wallet.openWallet(company.get("wallet_config").toString(), company.get("wallet_credentials").toString()).get();

        if(companyWallet != null){
            try {
                DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(companyWallet, "{}").get();
                company.put("did",didResult.getDid());
                company.put("key",didResult.getVerkey());

                System.out.println("company DID: " + company.get("did"));
                System.out.println("company Key: " + company.get("key"));

                String nymRequest = Ledger.buildNymRequest(steward.get("did").toString(), company.get("did").toString(), company.get("key").toString(),
                        null, "TRUST_ANCHOR").get();
                String res = signAndSubmitRequest(pool, stewardWallet, steward.get("did").toString(), nymRequest);
                System.out.println(res);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println(Did.getListMyDidsWithMeta(companyWallet).get());


        /*
           STEP 4 : 해당 코드는 정부가 credential schema(VC의 양식??)를 생성하고, 해당 schema를 ledger에 전송해서 등록!!
           스키마의 이름은 Transcript, 버전은 1.2 , 포함될 속성은 first_name, last_name 등등등... (나중에 우리가 정의해야할 부분)
         */

        System.out.println("\n\n\nSTEP 4 - Government creates credential schema");

        System.out.println("\n1. Government가 Transcript Schema를 생성");

        // 차대번호나 차량번호를 받아서
        // 차종류, 차량 번호, 출고일자, 운행 기록, 정기검사 유무, 검사이력? 정비이력?, 주행거리, 의무 보험 가입정보?? 사고정보?
        Map<String, Object> transcript = new HashMap<>();
        transcript.put("name", "TayoTayo Service Car Transcript");
        transcript.put("version", "1.2");
        transcript.put("attributes", new JSONArray(
                Arrays.asList("first_name","last_name","degree", "status", "year", "average", "ssn")).toString());

        // government의 did로 issuerCreateSchema 메소드로 스키마 생성!
        AnoncredsResults.IssuerCreateSchemaResult schemaResult = Anoncreds.issuerCreateSchema(
                government.get("did").toString(),
                transcript.get("name").toString(),
                transcript.get("version").toString(),
                transcript.get("attributes").toString()
        ).get();

        government.put("transcript_schema_id", schemaResult.getSchemaId());
        government.put("transcript_schema", schemaResult.getSchemaJson());
        String transcript_schema_id =government.get("transcript_schema_id").toString();

        System.out.println("\n2. Government가 Transcript Schema를 레저에 등록 ");
        System.out.println("정부가 정의해둔 Schema : " + government.get("transcript_schema").toString());

        //Ledger.buildSchemaRequest 함수를 사용하여 위에서 생성한 스키마를 ledger에 등록하기 위한 Schema Request 생성
        String schemaRequest = Ledger.buildSchemaRequest(government.get("did").toString(), government.get("transcript_schema").toString()).get();
        System.out.println(schemaRequest);

        // Ledger.signAndSubmitRequest 함수를 사용하여 스키마 등록요청을 서명하고 ledger에 제출
        //String res = signAndSubmitRequest(pool,governmentWallet,government.get("did").toString(),schemaRequest);
        String res = Ledger.signAndSubmitRequest(pool, governmentWallet, government.get("did").toString(),schemaRequest).get();
        System.out.println(res);


        // STEP - 5 : University will create a credential definition
        System.out.println("\n\n\nSTEP5 - University creates Transcript Credential Definition");

        System.out.println("\ntheUniversity(Issuer)가 레저에 등록되어 있는 Schema를 가지고 옴 (schemaId로) ");

        // 아마도 이게 SchemaID를 가지고 오는 사이클 인듯?
        String getSchemaRequest = Ledger.buildGetSchemaRequest(university.get("did").toString(), transcript_schema_id).get();
        String getSchemaResponse = ensurePreviousRequestApplied(pool, getSchemaRequest, response -> {
            JSONObject getSchemaResponseObject = new JSONObject(response);
            return !getSchemaResponseObject.getJSONObject("result").isNull("seqNo");
        });
        System.out.println(getSchemaResponse);
        LedgerResults.ParseResponseResult parseSchemaResult = Ledger.parseGetSchemaResponse(getSchemaResponse).get();
        university.put("transcript_schema_id", parseSchemaResult.getId());
        university.put("transcript_schema",parseSchemaResult.getObjectJson());

        System.out.println("\n이게 Schema랑 똑같나??? "+parseSchemaResult.getObjectJson());

        // Credential Definition 생성 요청
        System.out.println("\ntheUniversity가 지갑안에 Schema에 대한 Credential Definition를 만든다");
        AnoncredsResults.IssuerCreateAndStoreCredentialDefResult createCredDefResult = Anoncreds.issuerCreateAndStoreCredentialDef(
                universityWallet, university.get("did").toString(), university.get("transcript_schema").toString(), "TAG1", null, new JSONObject().put("support_revocation", false).toString()).get();
        university.put("transcript_cred_def_id",createCredDefResult.getCredDefId());
        university.put("transcript_cred_def",createCredDefResult.getCredDefJson());

        // Request만들어서 서명하고 제출해서 등록!!
        System.out.println("\ntheUniversity가 생성한 Credential Definition을 레저에 등록한다 ");
        String credDefRequest = Ledger.buildCredDefRequest(university.get("did").toString(),university.get("transcript_cred_def").toString()).get();
        Ledger.signAndSubmitRequest(pool, universityWallet, university.get("did").toString(), credDefRequest);

        System.out.println("등록된 Credential Definition ID : "+university.get("transcript_cred_def_id"));
        System.out.println("등록된 Credential Definition Json : "+university.get("transcript_cred_def"));


        // STEP6 - University Issues Transcript Credential to Alice - 엘리스에게 VC 발급
        System.out.println("\n\n\nSTEP6 - University Issues Transcript Credential to Alice");

        /*
            우선 엘리스 setup(지갑생성등) 부터 하자
            우린 setup과정이 회원가입일 것임
         */

        // Alice 정보 설정
        Map<String, Object> Alice = new HashMap<>();
        Alice.put("name", "Alice");
        Alice.put("wallet_config", new JSONObject().put("id", "alice_wallet").toString());
        Alice.put("wallet_credentials", new JSONObject().put("key", "alice_wallet_key").toString());

        Wallet.createWallet(Alice.get("wallet_config").toString(), Alice.get("wallet_credentials").toString()).get();
        Wallet AliceWallet = Wallet.openWallet(Alice.get("wallet_config").toString(), Alice.get("wallet_credentials").toString()).get();

        if(AliceWallet != null){
            try {
                DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(AliceWallet, "{}").get();
                Alice.put("did",didResult.getDid());
                Alice.put("key",didResult.getVerkey());

                System.out.println("Alice DID: " + Alice.get("did"));
                System.out.println("Alice Key: " + Alice.get("key"));


                // TODO : 근데 왜 정부의 did로 하는거지?
//                String nymRequest = Ledger.buildNymRequest(government.get("did").toString(), Alice.get("did").toString(), Alice.get("key").toString(),
//                        null, "ENDORSER").get();
//                String resd = Ledger.signAndSubmitRequest(pool, governmentWallet, government.get("did").toString(), nymRequest).get();
//                System.out.println(resd);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println(Did.getListMyDidsWithMeta(AliceWallet).get());


        System.out.println("\n Issuer가 definition에 대한 credential offer를 생성한다");
        String transcriptCredOffer = Anoncreds.issuerCreateCredentialOffer(universityWallet, university.get("transcript_cred_def_id").toString()).get();
        university.put("transcript_cred_offer",transcriptCredOffer);

        // offer와 관련된 내용은 이미 웹에서 제공하고 있을 듯..?
        System.out.println("\n theUniversity가 생성한 Credential Offer를 엘리스에게 보낸다");
        Alice.put("transcript_cred_offer",transcriptCredOffer);

        System.out.println("\n 엘리스는 받은 Credential offer로 부터 schemaID랑 credential definition ID를 획득");
        Alice.put("transcript_schema_id",new JSONObject(transcriptCredOffer).getString("schema_id"));
        Alice.put("transcript_cred_def_id",new JSONObject(transcriptCredOffer).getString("cred_def_id"));

        System.out.println("\n엘리스는 VC 생성에 필요한 Master Secret을 생성하고 엘리스 지갑에 저장한다");
        String AliceMasterSecretId = Anoncreds.proverCreateMasterSecret(AliceWallet, null).get();
        Alice.put("master_secret_id",AliceMasterSecretId);

        System.out.println("\n 엘리스는 알고 있는 credential definition ID로 레저로부터 Credential Definition를 가져온다");
        LedgerResults.ParseResponseResult parsedCredDefResponse = getCredDef(pool, Alice.get("did").toString(), Alice.get("transcript_cred_def_id").toString());
        Alice.put("theUniversity_transcript_cred_def",parsedCredDefResponse.getObjectJson());


        // 여기서 Credential request 매개변수로 AliceWallet, AliceDID, Credential Offer, Credential Definition, Master Secret ID
        System.out.println("\n엘리스는 VC생성을 위해 Issuer에게 보낼 transcript credential request를 준비한다");
        AnoncredsResults.ProverCreateCredentialRequestResult credentialRequestResult =
                Anoncreds.proverCreateCredentialReq(AliceWallet, Alice.get("did").toString(), Alice.get("transcript_cred_offer").toString(),
                        Alice.get("theUniversity_transcript_cred_def").toString(), Alice.get("master_secret_id").toString()).get();
        Alice.put("transcript_cred_request",credentialRequestResult.getCredentialRequestJson());
        Alice.put("transcript_cred_request_metadata",credentialRequestResult.getCredentialRequestMetadataJson());

        System.out.println("\n엘리스가 Issuer에게 Credential Request를 제출");
        university.put("transcript_cred_request",Alice.get("transcript_cred_request"));

        // 대학교에서 엘리스에게 credential을 발급해준다 (대학교가 알고 있는 엘리스의 개인정보들) - 즉, 이게 VC 인거지
        System.out.println("\n이제 대학교가 엘리스를 위한 Credential을 생성한다"); // 이건 아마도 Issuer의 DB를 하나 파야할듯?

        // TODO : 여기서 encoded는 무슨 값이 들어가야하지..?
        //      암호화 한 값인가..?
        JSONObject credValuesJson = new JSONObject()
                .put("first_name", new JSONObject().put("raw", "Alice").put("encoded", "1139481716457488690172217916278103335"))
                .put("last_name", new JSONObject().put("raw", "Garcia").put("encoded", "5321642780241790123587902456789123452"))
                .put("degree", new JSONObject().put("raw", "Bachelor of Science, Marketing").put("encoded", "12434523576212321"))
                .put("status", new JSONObject().put("raw", "graduated").put("encoded", "2213454313412354"))
                .put("ssn", new JSONObject().put("raw", "123-45-6789").put("encoded", "3124141231422543541"))
                .put("year", new JSONObject().put("raw", "2015").put("encoded", "2016"))
                .put("average", new JSONObject().put("raw", "5").put("encoded", "5"));

        university.put("alice_transcript_cred_values",credValuesJson);

        // 대학교가 CredentialResult를 생성한다 (IssuerWallet, cred_offer, cred_request, credValuesJson, ...)
        AnoncredsResults.IssuerCreateCredentialResult issuerCredentialResult =
                Anoncreds.issuerCreateCredential(universityWallet, university.get("transcript_cred_offer").toString(),university.get("transcript_cred_request").toString(),
                        university.get("alice_transcript_cred_values").toString(),null,0).get();

        // 이게 생성한 VC임
        university.put("transcript_cred",issuerCredentialResult.getCredentialJson());
        System.out.println("\n 생성한 VC : " + university.get("transcript_cred"));

        System.out.println("\ntheUniversity가 엘리스에게 생성한 VC를 보낸다");
        Alice.put("transcript_cred",university.get("transcript_cred"));

        // 생성한 VC를 엘리스 지갑에 저장
        System.out.println("\n엘리스가 받은 VC를 저장");
        Anoncreds.proverStoreCredential(AliceWallet, null, Alice.get("transcript_cred_request_metadata").toString()
                , Alice.get("transcript_cred").toString(), Alice.get("theUniversity_transcript_cred_def").toString(), null);


        // ==================================================================================================================

        // TODO : 여기 유저(Bob) 한명 더 만들어 보자!!!!!!!!!!!!
        //       Alice가 VP를 만드는 과정에서 Bob의 VC를 VP로 만들떄 잡아 낼 수 있는가?

        // Alice 정보 설정
        Map<String, Object> Bob = new HashMap<>();
        Bob.put("name", "Bob");
        Bob.put("wallet_config", new JSONObject().put("id", "Bob_wallet").toString());
        Bob.put("wallet_credentials", new JSONObject().put("key", "Bob_wallet_key").toString());

        Wallet.createWallet(Bob.get("wallet_config").toString(), Bob.get("wallet_credentials").toString()).get();
        Wallet BobWallet = Wallet.openWallet(Bob.get("wallet_config").toString(), Bob.get("wallet_credentials").toString()).get();

        if(BobWallet != null){
            try {
                DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(BobWallet, "{}").get();
                Bob.put("did",didResult.getDid());
                Bob.put("key",didResult.getVerkey());

                System.out.println("Bob DID: " + Bob.get("did"));
                System.out.println("Bob Key: " + Bob.get("key"));

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println("\n Issuer가 definition에 대한 credential offer를 생성한다");
        String transcriptCredOfferForBob = Anoncreds.issuerCreateCredentialOffer(universityWallet, university.get("transcript_cred_def_id").toString()).get();
        university.put("transcript_cred_offer",transcriptCredOfferForBob);

        // offer와 관련된 내용은 이미 웹에서 제공하고 있을 듯..?
        System.out.println("\n theUniversity가 생성한 Credential Offer를 밥에게 보낸다");
        Bob.put("transcript_cred_offer",transcriptCredOfferForBob);

        System.out.println("\n 밥은 받은 Credential offer로 부터 schemaID랑 credential definition ID를 획득");
        Bob.put("transcript_schema_id",new JSONObject(transcriptCredOfferForBob).getString("schema_id"));
        Bob.put("transcript_cred_def_id",new JSONObject(transcriptCredOfferForBob).getString("cred_def_id"));

        System.out.println("\n밥은 VC 생성에 필요한 Master Secret을 생성하고 밥의 지갑에 저장한다");
        String BobMasterSecretId = Anoncreds.proverCreateMasterSecret(BobWallet, null).get();
        Bob.put("master_secret_id",BobMasterSecretId);

        System.out.println("\n 밥은 알고 있는 credential definition ID로 레저로부터 Credential Definition를 가져온다");
        LedgerResults.ParseResponseResult BobparsedCredDefResponse = getCredDef(pool, Bob.get("did").toString(), Bob.get("transcript_cred_def_id").toString());
        Bob.put("theUniversity_transcript_cred_def",BobparsedCredDefResponse.getObjectJson());


        // 여기서 Credential request 매개변수로 AliceWallet, AliceDID, Credential Offer, Credential Definition, Master Secret ID
        System.out.println("\n밥은 VC생성을 위해 Issuer에게 보낼 transcript credential request를 준비한다");
        AnoncredsResults.ProverCreateCredentialRequestResult BobcredentialRequestResult =
                Anoncreds.proverCreateCredentialReq(BobWallet, Bob.get("did").toString(), Bob.get("transcript_cred_offer").toString(),
                        Bob.get("theUniversity_transcript_cred_def").toString(), Bob.get("master_secret_id").toString()).get();
        Bob.put("transcript_cred_request",BobcredentialRequestResult.getCredentialRequestJson());
        Bob.put("transcript_cred_request_metadata",BobcredentialRequestResult.getCredentialRequestMetadataJson());

        System.out.println("\n밥이 Issuer에게 Credential Request를 제출");
        university.put("Bob_transcript_cred_request",Bob.get("transcript_cred_request"));

        // 대학교에서 엘리스에게 credential을 발급해준다 (대학교가 알고 있는 엘리스의 개인정보들) - 즉, 이게 VC 인거지
        System.out.println("\n이제 대학교가 밥을 위한 Credential을 생성한다"); // 이건 아마도 Issuer의 DB를 하나 파야할듯?

        // TODO : 여기서 encoded는 무슨 값이 들어가야하지..?
        //      암호화 한 값인가..?
        JSONObject credValuesJsonForBob = new JSONObject()
                .put("first_name", new JSONObject().put("raw", "Bob").put("encoded", "7221791627810333511394817164574886901"))
                .put("last_name", new JSONObject().put("raw", "popo").put("encoded", "3587902456789123452532164278024179012"))
                .put("degree", new JSONObject().put("raw", "Computer Science").put("encoded", "12434523576212321"))
                .put("status", new JSONObject().put("raw", "now").put("encoded", "3412354221345431"))
                .put("ssn", new JSONObject().put("raw", "321-45-9876").put("encoded", "4225435413124141231"))
                .put("year", new JSONObject().put("raw", "2018").put("encoded", "2018"))
                .put("average", new JSONObject().put("raw", "6").put("encoded", "6"));

        university.put("Bob_transcript_cred_values",credValuesJsonForBob);

        // 대학교가 CredentialResult를 생성한다 (IssuerWallet, cred_offer, cred_request, credValuesJson, ...)
        AnoncredsResults.IssuerCreateCredentialResult issuerCredentialResultForBob =
                Anoncreds.issuerCreateCredential(universityWallet, university.get("transcript_cred_offer").toString(),university.get("Bob_transcript_cred_request").toString(),
                        university.get("Bob_transcript_cred_values").toString(),null,0).get();
        /*
                AnoncredsResults.IssuerCreateCredentialResult issuerCredentialResult =
                Anoncreds.issuerCreateCredential(universityWallet, university.get("transcript_cred_offer").toString(),university.get("transcript_cred_request").toString(),
                        university.get("alice_transcript_cred_values").toString(),null,0).get();

         */

        // 이게 생성한 VC임
        university.put("Bob_transcript_cred",issuerCredentialResultForBob.getCredentialJson());
        System.out.println("\n 생성한 VC : " + university.get("Bob_transcript_cred"));

        System.out.println("\ntheUniversity가 밥에게 생성한 VC를 보낸다");
        Bob.put("transcript_cred",university.get("Bob_transcript_cred"));

        // 생성한 VC를 밥의 지갑에 저장
        System.out.println("\n밥이 받은 VC를 저장");
        Anoncreds.proverStoreCredential(BobWallet, null, Bob.get("transcript_cred_request_metadata").toString()
                , Bob.get("transcript_cred").toString(), Bob.get("theUniversity_transcript_cred_def").toString(), null);

        // TODO : 밥의 Step 7!!
        // STEP7 - Bob makes verifiable presentation(VP) to Company 즉, VP(Verifiable Presentation) 생성
        // 우리는 차량 등록시에 VP를 제출하고 서비스에서 검증한 후 검증한 데이터를 아마도..? (애매하네)
        System.out.println("\n\n\nSTEP7 - Bob makes verifiable presentation(VP) to Company\n");

        // 우린 서비스에서 차량 전용 Proof Request를 만들어야 함 (이게 회사에서 필요한 양식)
        // 그러니깐 우리회사에 지원할 꺼면 first_name, last_name,degree,status,ssn,phone_number를 내라! 이거네
        System.out.println("\ntheCompany가 Holder가 제출해야할 Proof Request(VP양식)을 만든다");

        // TODO : transcriptRestrictions는 뭐지? 여기서 nonce는 왜 필요?
        String nonceForBob = Anoncreds.generateNonce().get();
        JSONArray transcriptRestrictionsForBob = new JSONArray().put(new JSONObject().put("cred_def_id", university.get("transcript_cred_def_id").toString()));

        // TODO : 이부분 완벽 이해 필요
        // 즉, VP 양식을 만든다 nonce로 항상 바뀔듯?
        String proofRequestJsonForBob = new JSONObject()
                .put("nonce", nonceForBob)
                .put("name", "Job-Application")
                .put("version", "0.1")
                .put("requested_attributes", new JSONObject()
                        .put("attr1_referent", new JSONObject().put("name", "first_name"))
                        .put("attr2_referent", new JSONObject().put("name", "last_name"))
                        .put("attr3_referent", new JSONObject().put("name", "degree").put("restrictions", transcriptRestrictionsForBob))
                        .put("attr4_referent", new JSONObject().put("name", "status").put("restrictions", transcriptRestrictionsForBob))
                        .put("attr5_referent", new JSONObject().put("name", "ssn").put("restrictions", transcriptRestrictionsForBob))
                        .put("attr6_referent", new JSONObject().put("name", "year").put("restrictions", transcriptRestrictionsForBob)))
                .put("requested_predicates", new JSONObject()
                        .put("predicate1_referent", new JSONObject()
                                .put("name", "average")
                                .put("p_type", ">=")
                                .put("p_value", 4)
                                .put("restrictions", transcriptRestrictionsForBob)))
                .toString();

        company.put("Bob_job_application_proof_request", proofRequestJsonForBob);
        System.out.println("\n즉, 이 Json 형식이 유저가 제출해야할 VP 양식임 " + proofRequestJsonForBob);

        // 회사가 위에서 만든 Proof Request (지원 양식)을 Alice에게 전해주면 VC로 채워서 제출
        // 아마 이건 nonce값이 있어서 흠...
        System.out.println("\ntheCompany가 엘리스에게 Proof Request(VP 양식)을 넘겨준다");
        Bob.put("job_application_proof_request", company.get("Bob_job_application_proof_request"));

        // TODO : 내가 이해한게 맞나?
        //      근데 저걸로 VC를 어떻게 찾지?
        // 즉, 받은 양식을 VC로 채워서 보내는게 VP
        System.out.println("\n 밥이 Proof Request 양식(VP)을 채우기위해 밥의 지갑에서 credential(VC)을 획득한다");
        CredentialsSearchForProofReq search_for_job_application_proof_request_forbob = CredentialsSearchForProofReq.open(
                BobWallet, Bob.get("job_application_proof_request").toString(), null).get();

        System.out.println("====================================================================");
        System.out.println("\n얘 정체는 ? :"+search_for_job_application_proof_request_forbob);
        System.out.println("====================================================================");


        // TODO : 즉 위에서 찾은 VC에서 필요한 데이터들을 뽑는 과정같음
        JSONArray credentialsForAttribute3ForBob = new JSONArray(search_for_job_application_proof_request_forbob.fetchNextCredentials("attr3_referent", 100).get());
        String credentialIdForAttribute3ForBob = credentialsForAttribute3ForBob.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForAttribute4ForBob = new JSONArray(search_for_job_application_proof_request_forbob.fetchNextCredentials("attr4_referent", 100).get());
        String credentialIdForAttribute4ForBob = credentialsForAttribute4ForBob.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForAttribute5ForBob = new JSONArray(search_for_job_application_proof_request_forbob.fetchNextCredentials("attr5_referent", 100).get());
        String credentialIdForAttribute5ForBob = credentialsForAttribute5ForBob.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForAttribute6ForBob = new JSONArray(search_for_job_application_proof_request_forbob.fetchNextCredentials("attr6_referent", 100).get());
        String credentialIdForAttribute6ForBob = credentialsForAttribute6ForBob.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForPredicate1ForBob = new JSONArray(search_for_job_application_proof_request_forbob.fetchNextCredentials("predicate1_referent", 100).get());
        String credentialIdForPredicate1ForBob = credentialsForPredicate1ForBob.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        search_for_job_application_proof_request_forbob.close();





        // ==================================================================================================================

        // TODO : 지갑에서 현재 가지고 있는 VC 조회하기!
        //          proverStoreCredential의 용도가 뭔지 확인하자
        //          그리고 Holder입장에서 VC를 어떻게

        // STEP7 - Alice makes verifiable presentation(VP) to Company 즉, VP(Verifiable Presentation) 생성
        // 우리는 차량 등록시에 VP를 제출하고 서비스에서 검증한 후 검증한 데이터를 아마도..? (애매하네)
        System.out.println("\n\n\nSTEP7 - Alice makes verifiable presentation(VP) to Company\n");

        // 우린 서비스에서 차량 전용 Proof Request를 만들어야 함 (이게 회사에서 필요한 양식)
        // 그러니깐 우리회사에 지원할 꺼면 first_name, last_name,degree,status,ssn,phone_number를 내라! 이거네
        System.out.println("\ntheCompany가 Holder가 제출해야할 Proof Request(VP양식)을 만든다");

        // TODO : transcriptRestrictions는 뭐지? 여기서 nonce는 왜 필요?
        String nonce = Anoncreds.generateNonce().get();
        JSONArray transcriptRestrictions = new JSONArray().put(new JSONObject().put("cred_def_id", university.get("transcript_cred_def_id").toString()));

        // TODO : 이부분 완벽 이해 필요
        // 즉, VP 양식을 만든다 nonce로 항상 바뀔듯?
        String proofRequestJson = new JSONObject()
                .put("nonce", nonce)
                .put("name", "Job-Application")
                .put("version", "0.1")
                .put("requested_attributes", new JSONObject()
                        .put("attr1_referent", new JSONObject().put("name", "first_name"))
                        .put("attr2_referent", new JSONObject().put("name", "last_name"))
                        .put("attr3_referent", new JSONObject().put("name", "degree").put("restrictions", transcriptRestrictions))
                        .put("attr4_referent", new JSONObject().put("name", "status").put("restrictions", transcriptRestrictions))
                        .put("attr5_referent", new JSONObject().put("name", "ssn").put("restrictions", transcriptRestrictions))
                        .put("attr6_referent", new JSONObject().put("name", "year").put("restrictions", transcriptRestrictions)))
                .put("requested_predicates", new JSONObject()
                        .put("predicate1_referent", new JSONObject()
                                .put("name", "average")
                                .put("p_type", ">=")
                                .put("p_value", 4)
                                .put("restrictions", transcriptRestrictions)))
                .toString();

        company.put("job_application_proof_request", proofRequestJson);
        System.out.println("\n즉, 이 Json 형식이 유저가 제출해야할 VP 양식임 " + proofRequestJson.toString());

        // 회사가 위에서 만든 Proof Request (지원 양식)을 Alice에게 전해주면 VC로 채워서 제출
        // 아마 이건 nonce값이 있어서 흠...
        System.out.println("\ntheCompany가 엘리스에게 Proof Request(VP 양식)을 넘겨준다");
        Alice.put("job_application_proof_request", company.get("job_application_proof_request"));

        // TODO : 내가 이해한게 맞나?
        //      근데 저걸로 VC를 어떻게 찾지?
        // 즉, 받은 양식을 VC로 채워서 보내는게 VP
        System.out.println("\n 엘리스가 Proof Request 양식(VP)을 채우기위해 엘리스 지갑에서 credential(VC)을 획득한다");
        CredentialsSearchForProofReq search_for_job_application_proof_request = CredentialsSearchForProofReq.open(
                AliceWallet, Alice.get("job_application_proof_request").toString(), null).get();
        
        System.out.println("====================================================================");
        System.out.println("\n얘 정체는 ? :"+search_for_job_application_proof_request);
        System.out.println("====================================================================");


        // TODO : 즉 위에서 찾은 VC에서 필요한 데이터들을 뽑는 과정같음
        JSONArray credentialsForAttribute3 = new JSONArray(search_for_job_application_proof_request.fetchNextCredentials("attr3_referent", 100).get());
        System.out.println("Alice !! :" + credentialsForAttribute3);
        String credentialIdForAttribute3 = credentialsForAttribute3.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForAttribute4 = new JSONArray(search_for_job_application_proof_request.fetchNextCredentials("attr4_referent", 100).get());
        String credentialIdForAttribute4 = credentialsForAttribute4.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForAttribute5 = new JSONArray(search_for_job_application_proof_request.fetchNextCredentials("attr5_referent", 100).get());
        String credentialIdForAttribute5 = credentialsForAttribute5.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForAttribute6 = new JSONArray(search_for_job_application_proof_request.fetchNextCredentials("attr6_referent", 100).get());
        String credentialIdForAttribute6 = credentialsForAttribute6.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForPredicate1 = new JSONArray(search_for_job_application_proof_request.fetchNextCredentials("predicate1_referent", 100).get());
        String credentialIdForPredicate1 = credentialsForPredicate1.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        search_for_job_application_proof_request.close();

        // Alice -> Create Job-Application Proof
        // TODO : 여기서 다른 유저(Bob)의 VC를 가지고 VP를 만들게 되면 애초에 에러가 남 !!
        //          즉 VP에 다른 VC값을 넣을 수 없는건가?
        System.out.println("\n 즉 엘리스가 VC에서 뽑은 데이터를 사용해서 VP를 만드는 과정");
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
                                .put("revealed", true))
                        .put("attr5_referent", new JSONObject()
                                .put("cred_id", credentialIdForAttribute5)
                                .put("revealed", true))
                        .put("attr6_referent", new JSONObject()
                                .put("cred_id", credentialIdForAttribute6)
                                .put("revealed", true)))
                // requested_predicates 이거는 영지식 증명들
                .put("requested_predicates", new JSONObject()
                        .put("predicate1_referent", new JSONObject()
                                .put("cred_id",credentialIdForPredicate1)))
                .toString();

        Alice.put("job_application_requested_creds",credentialsJson);


        // TODO : 이쪽은 진짜 모르겠네
        JSONObject schemasMap = new JSONObject();
        JSONObject credDefsMap = new JSONObject();

        populateCredentialInfo(pool, Alice.get("did").toString(), schemasMap, credDefsMap, credentialsForAttribute3);
        populateCredentialInfo(pool, Alice.get("did").toString(), schemasMap, credDefsMap, credentialsForAttribute4);
        populateCredentialInfo(pool, Alice.get("did").toString(), schemasMap, credDefsMap, credentialsForAttribute5);
        populateCredentialInfo(pool, Alice.get("did").toString(), schemasMap, credDefsMap, credentialsForAttribute6);

        String schemas = schemasMap.toString();
        String credDefs = credDefsMap.toString();
        String revocState = new JSONObject().toString();

        System.out.println("\nschemas :" +schemas);
        System.out.println("\ncredDefs :" +credDefs);

        // Alice가 최종 제출할 Proof(VP)
        // alice지갑, proof_request, requested_creds(VP) , Master Secret Id, schemas,credDefs,revocState)
        System.out.println("\n\n Verifier에게 제출할 최종 VP \n");
        String proofJson = Anoncreds.proverCreateProof(AliceWallet, Alice.get("job_application_proof_request").toString()
                ,Alice.get("job_application_requested_creds").toString()
                ,Alice.get("master_secret_id").toString()
                ,schemas, credDefs, revocState).get();

        Alice.put("job_application_proof", new JSONObject(proofJson));

        System.out.println(Alice.get("job_application_proof"));
        System.out.println("==============");


        // 이렇게 만든 VP를 Company에 제출!
        System.out.println("\n 엘리스가 theCompany에 Proof(VP)를 제출");
        company.put("job_application_proof", Alice.get("job_application_proof"));

        /*
            TODO : 테스트 : 만약 수정이 일어난다면 ??
         */

//        JSONObject data = new JSONObject(Alice.get("job_application_proof").toString());
//        JSONArray cList = data.getJSONObject("proof")
//                .getJSONObject("aggregated_proof")
//                .getJSONArray("c_list");
//        cList.getJSONArray(0).put(0, 150);

//        JSONObject data = new JSONObject(Alice.get("job_application_proof").toString());
//        data.getJSONObject("requested_proof")
//                .getJSONObject("revealed_attrs")
//                .getJSONObject("attr5_referent").put("raw", "55-5");
//        data.getJSONObject("requested_proof")
//                .getJSONObject("self_attested_attrs").put("attr1_referent", "donu");

//
//        System.out.println(data);
//
//        company.put("job_application_proof",data);
//        System.out.println(company.get("job_application_proof"));

        // 마지막 STEP 8 !!! 회사가 받은 엘리스의 VP를 검증하는 과정!!!!!!!!!
        // company.get("job_application_proof") 이게 받은거
        System.out.println("\n\n\n STEP8 - Company validates Alice's claims");

        // 이건 이미 결과를 알고있고 비교하는거고...
        JSONObject selfAttestedAttrs = new JSONObject(company.get("job_application_proof").toString()).getJSONObject("requested_proof").getJSONObject("self_attested_attrs");
        JSONObject revealedAttrs = new JSONObject(company.get("job_application_proof").toString()).getJSONObject("requested_proof").getJSONObject("revealed_attrs");
        System.out.println("SelfAttestedAttrs: " + selfAttestedAttrs);
        System.out.println("RevealedAttrs: " + revealedAttrs);

        System.out.println("Alice ==" + selfAttestedAttrs.getString("attr1_referent"));
        System.out.println("Garcia ==" + selfAttestedAttrs.getString("attr2_referent"));
        System.out.println("Bachelor of Science, Marketing==" + revealedAttrs.getJSONObject("attr3_referent").getString("raw"));
        System.out.println("graduated ==" + revealedAttrs.getJSONObject("attr4_referent").getString("raw"));
        System.out.println("123-45-6789 ==" + revealedAttrs.getJSONObject("attr5_referent").getString("raw"));
        System.out.println("2015 ==" + revealedAttrs.getJSONObject("attr6_referent").getString("raw"));

        String revocRegDefs = new JSONObject().toString();
        String revocRegs = new JSONObject().toString();

        Boolean same = Anoncreds.verifierVerifyProof(
                company.get("job_application_proof_request").toString(),
                company.get("job_application_proof").toString(), // 제출한 VP
                schemas, credDefs,
                revocRegDefs, revocRegs).get();

        System.out.println("일치 : " + same +"\n\n");

        // 마무리
        closeAndDeletePoolLedger(stewardWallet,governmentWallet,universityWallet,companyWallet,AliceWallet,
                BobWallet,steward,government,university,company,Alice,Bob,pool);

    }


    // TODO : 이미 존재하면 안만들도록 예외처리
    public static void createWallet(Map<String, Object> identity) throws IndyException {
        System.out.println("\"" + identity.get("name") + "\" -> Create 타요타요's Wallet");

        Wallet.createWallet(identity.get("wallet_config").toString(), identity.get("wallet_credentials").toString());
    }

    public static void deleteWallet(Map<String, Object> identity) throws IndyException {
        System.out.println("\"" + identity.get("name") + "\" -> Delete 타요타요's Wallet");

        Wallet.deleteWallet(identity.get("wallet_config").toString(), identity.get("wallet_credentials").toString());
    }

    public static Wallet openWallet(Map<String, Object> identity) throws IndyException, ExecutionException, InterruptedException {
        System.out.println("\"" + identity.get("name") + "\" -> Open 타요타요's Wallet");

        Wallet wallet = Wallet.openWallet(identity.get("wallet_config").toString(), identity.get("wallet_credentials").toString()).get();

        return wallet;
    }

    public static DidResults.CreateAndStoreMyDidResult createAndStoreUserDid(Wallet wallet, Map<String, Object> identity)
            throws IndyException, ExecutionException, InterruptedException {

        String DidInfo;
        if(identity.containsKey("seed")){
            DidInfo = "{\"seed\": \"" + identity.get("seed").toString() + "\"}";
        }else{
            DidInfo = "{}";
        }

        System.out.println("DidInfo : " + DidInfo);

        DidResults.CreateAndStoreMyDidResult DidResult = Did.createAndStoreMyDid(wallet, DidInfo).get();
        System.out.println("create and store my did 관련 : " + DidResult);

        return DidResult;
    }

    /*
        sendNym((Pool) steward.get("pool") , stewardWallet,(String)steward.get("did"),
                        (String)company.get("did"),(String)company.get("key"),(String) company.get("role"));
     */
    public static void sendNym(Pool poolHandle, Wallet wallet, String did, String newDid, String newKey, String role) throws Exception {
        // 새로운 Nym 등록을 위해 요청 생성

        String nymRequest = Ledger.buildNymRequest(did, newDid, newKey, null, role).get();
        System.out.println("buildNumRequest 관련 : "+nymRequest);

        // 생성된 요청에 서명하고 Indy 풀에 제출하여 ledger에 기록
        String res = Ledger.signAndSubmitRequest(poolHandle, wallet, did, nymRequest).get();
        System.out.println("signAndSubmitRequest 관련 : "+res);
    }

    public interface PoolResponseChecker {
        boolean check(String response);
    }

    public static String ensurePreviousRequestApplied(Pool pool, String checkerRequest, PoolResponseChecker checker)
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

    public static LedgerResults.ParseResponseResult getCredDef(Pool pool, String did, String cred_def_id) throws IndyException, ExecutionException, InterruptedException {
        String get_cred_def_request = Ledger.buildGetCredDefRequest(did, cred_def_id).get();

        String get_cred_def_response = ensurePreviousRequestApplied(pool, get_cred_def_request, response -> {
            JSONObject getSchemaResponseObject = new JSONObject(response);
            return !getSchemaResponseObject.getJSONObject("result").isNull("seqNo");
        });
        return Ledger.parseGetCredDefResponse(get_cred_def_response).get();
    }

    //populateCredentialInfo(pool, Alice.get("did").toString(), schemasMap, credDefsMap, credentialsForAttribute1);
    public static void populateCredentialInfo(Pool pool, String did, JSONObject schemas, JSONObject credDefs, JSONArray credentials) throws Exception {
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

    public static List<JSONObject> array2List(JSONArray credentials) {
        List<JSONObject> result = new ArrayList<>();
        credentials.forEach(o -> result.add((JSONObject) o));
        return result;
    }

    static Pool createAndOpenPoolLedger() throws Exception {

        String poolName = "pool1";
        String poolConfig = "{\"genesis_txn\": \"src/main/java/com/example/Indysdktest/indy/pool1.txn\"} ";

        Pool.setProtocolVersion(2);
        Pool.createPoolLedgerConfig(poolName,poolConfig);

        Pool pool = openPoolLedger(poolName, "{}").get();
        System.out.println("openPoolLedger 관련 : " + pool.toString());

        return pool;
    }

    public static void closeAndDeletePoolLedger(Wallet w1, Wallet w2, Wallet w3, Wallet w4, Wallet w5,Wallet w6,
                                                Map<String, Object> m1, Map<String, Object> m2, Map<String, Object> m3, Map<String, Object> m4 ,
                                                Map<String, Object> m5,Map<String, Object> m6,Pool pool) throws Exception {

        // 엘리스 지갑, 대학교 지갑, 회사 지갑, 정부 지갑, seward 지갑 삭제
        closeAndDeleteWallet(w1, m1.get("wallet_config").toString(), m1.get("wallet_credentials").toString());
        closeAndDeleteWallet(w2, m2.get("wallet_config").toString(), m2.get("wallet_credentials").toString());
        closeAndDeleteWallet(w3, m3.get("wallet_config").toString(), m3.get("wallet_credentials").toString());
        closeAndDeleteWallet(w4, m4.get("wallet_config").toString(), m4.get("wallet_credentials").toString());
        closeAndDeleteWallet(w5, m5.get("wallet_config").toString(), m5.get("wallet_credentials").toString());
        closeAndDeleteWallet(w6, m6.get("wallet_config").toString(), m6.get("wallet_credentials").toString());

        pool.closePoolLedger().get();
        Pool.deletePoolLedgerConfig("pool1").get();
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
        return res.toString();
    }


}


