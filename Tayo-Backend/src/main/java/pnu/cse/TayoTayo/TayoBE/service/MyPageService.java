package pnu.cse.TayoTayo.TayoBE.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnu.cse.TayoTayo.TayoBE.dto.request.MemberRequest;
import pnu.cse.TayoTayo.TayoBE.exception.ApplicationException;
import pnu.cse.TayoTayo.TayoBE.exception.ErrorCode;
import pnu.cse.TayoTayo.TayoBE.model.Member;
import pnu.cse.TayoTayo.TayoBE.model.entity.MemberEntity;
import pnu.cse.TayoTayo.TayoBE.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public Member myInfo(Long Id){
        return Member.fromEntity(memberRepository.findOne(Id));
    }

    @Transactional
    public Member modifyIntroduce(Long Id, String newIntroduce){

        MemberEntity member = memberRepository.findOne(Id);
        member.setIntroduce(newIntroduce);

        return Member.fromEntity(member);

    }

    @Transactional
    public void modifyPassword(Long Id, MemberRequest.ModifyPasswordRequest request) {

        MemberEntity member = memberRepository.findOne(Id);

        System.out.println(passwordEncoder.encode(request.getCurrentPassword()));
        System.out.println(member.getPassword());

        // 패스워드 불일치
        if(!passwordEncoder.matches(request.getCurrentPassword(),member.getPassword())){
            throw new ApplicationException(ErrorCode.PASSWORD_MISMATCH);
        }

        // TODO : 패스워드 형식 (8~16자, 영문, 숫자, 기호 포함 o, 공백 포함 x )

        // 새 패스워드랑 새 패스워드 확인이 불일치 할때
        if(!request.getNewPassword().equals(request.getCheckNewPassword())){
            throw new ApplicationException(ErrorCode.NEWPASSWORD_MISMATCH);
        }

        // TODO : 현재 비밀번호랑 일치!

        // 다 통과하면 비밀번호 수정 완료!
        member.setPassword(passwordEncoder.encode(request.getNewPassword()));

    }

    @Transactional
    public void deleteMember(Long Id, String currentPassword) {
        MemberEntity member = memberRepository.findOne(Id);

        // 패스워드 불일치
        if(!passwordEncoder.matches(currentPassword,member.getPassword())){
            throw new ApplicationException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 회원 탈퇴 (Exception)
        memberRepository.delete(member);

    }


}
