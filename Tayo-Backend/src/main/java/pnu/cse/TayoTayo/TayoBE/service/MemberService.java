package pnu.cse.TayoTayo.TayoBE.service;

import lombok.RequiredArgsConstructor;
import org.hyperledger.indy.sdk.IndyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnu.cse.TayoTayo.TayoBE.config.security.CustomUserDetails;
import pnu.cse.TayoTayo.TayoBE.config.security.JWTProvider;
import pnu.cse.TayoTayo.TayoBE.dto.request.MemberRequest;
import pnu.cse.TayoTayo.TayoBE.model.Member;
import pnu.cse.TayoTayo.TayoBE.model.entity.MemberEntity;
import pnu.cse.TayoTayo.TayoBE.model.entity.MemberRole;
import pnu.cse.TayoTayo.TayoBE.repository.MemberRepository;
import pnu.cse.TayoTayo.TayoBE.exception.ApplicationException;
import pnu.cse.TayoTayo.TayoBE.exception.ErrorCode;
import pnu.cse.TayoTayo.TayoBE.util.PoolAndWalletManager;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final PoolAndWalletManager poolAndWalletManager;

    /**
     *  회원 가입
     */
    @Transactional
    public Member join(MemberRequest.MemberJoinRequest request) throws IndyException, ExecutionException, InterruptedException {

        // 1. 검증 : 중복 이메일
        validateDuplicateMember(request.getEmail()); // 중복 회원 검증

        // 2. 검증 통과하면 DB 저장
        MemberEntity newMember = MemberEntity.builder()
                .role(MemberRole.USER)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .nickName(request.getNickName())
                .introduce(request.getIntroduce())
                .build();

        //  3. Indy 지갑 생성까지 !!
        try{
            // TODO : 여기서 생성되는 DID랑 verKey를 DB에 저장해야할까...?
            poolAndWalletManager.createMemberWallet(request.getEmail(), "tempWalletPassword");
            memberRepository.save(newMember);

        }catch (Exception e){
            e.printStackTrace();
        }

        return Member.fromEntity(newMember);
    }

    /**
     * 로그인
     */

    @Transactional
    public Member login(MemberRequest.MemberLoginRequest request){
        // 1. 로그인 검증..? 이걸 시큐리티가 해주는건가..?

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        CustomUserDetails myUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String jwt = JWTProvider.createAccessToken(myUserDetails.getMember());


        Member member = Member.fromEntity(myUserDetails.getMember(), jwt);

        return member;
    }



    private void validateDuplicateMember(String email) {
        List<MemberEntity> findMembers = memberRepository.findByEmail(email);
        if (!findMembers.isEmpty()){ // 중복 이메일이 존재하면
            throw new ApplicationException(ErrorCode.SAME_EMAIL);
        }
    }



}
