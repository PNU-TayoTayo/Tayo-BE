package pnu.cse.TayoTayo.TayoBE.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnu.cse.TayoTayo.TayoBE.dto.request.MemberJoinRequest;
import pnu.cse.TayoTayo.TayoBE.model.entity.Member;
import pnu.cse.TayoTayo.TayoBE.model.entity.MemberEntity;
import pnu.cse.TayoTayo.TayoBE.repository.MemberRepository;
import pnu.cse.TayoTayo.TayoBE.exception.ApplicationException;
import pnu.cse.TayoTayo.TayoBE.exception.ErrorCode;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     *  회원 가입
     */
    @Transactional
    public Member join(MemberJoinRequest request){

        // 1. 검증 : 중복 이메일
        validateDuplicateMember(request.getEmail()); // 중복 회원 검증

        // 2. 검증 통과하면 DB 저장
        MemberEntity newMember = MemberEntity.of(request.getEmail(), request.getPassword(), request.getName(),
                request.getPhoneNumber(), request.getNickName(), request.getIntroduce());

        memberRepository.save(newMember);

        return Member.fromEntity(newMember);
    }

    private void validateDuplicateMember(String email) {
        List<MemberEntity> findMembers = memberRepository.findByEmail(email);
        if (!findMembers.isEmpty()){ // 중복 이메일이 존재하면
            throw new ApplicationException(ErrorCode.SAME_EMAIL);
        }
    }



}
