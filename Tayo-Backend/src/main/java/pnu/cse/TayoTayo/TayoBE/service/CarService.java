package pnu.cse.TayoTayo.TayoBE.service;

import lombok.RequiredArgsConstructor;
import org.hyperledger.indy.sdk.IndyException;
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
     * TODO : 오늘은 여기 까지 하고 테스트도 해보기!!!!
     *
     * 4. 그걸 받으면, 유저는 Schema_id랑 cre_def_id로 레저로부터 schema, cre_def를 가져온다
     *
     * 5. Master Secret도 생성 한다!
     *
     * 6. 유저가 이걸로 Credential Request를 만든다!
     *
     * 7. 만든 Credential Request를 VC_Service에 제출해서 VC 발급 요청을 함 + (자동차 번호등도 보내야 할듯)
     */

    @Transactional
    public void createVC(Long Id , String walletPassword) throws IndyException, ExecutionException, InterruptedException {
        MemberEntity member = memberRepository.findOne(Id);

        poolAndWalletManager.getCredentialOfferFromVCService(member.getEmail(),walletPassword);

    }







    }
