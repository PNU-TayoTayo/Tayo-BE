package pnu.cse.TayoTayo.TayoBE.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnu.cse.TayoTayo.TayoBE.model.Member;
import pnu.cse.TayoTayo.TayoBE.model.entity.MemberEntity;
import pnu.cse.TayoTayo.TayoBE.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final MemberRepository memberRepository;


    @Transactional
    public Member myInfo(Long Id){

        return Member.fromEntity(memberRepository.findOne(Id));
    }





}
