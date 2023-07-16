package pnu.cse.TayoTayo.TayoBE.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pnu.cse.TayoTayo.TayoBE.model.entity.MemberEntity;
import pnu.cse.TayoTayo.TayoBE.repository.MemberRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    // 로그인 요청
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        List<MemberEntity> findMembers = memberRepository.findByEmail(email);
        //TODO : 여기서 member 잘 찾았는지 정보다 찍어보자!

        System.out.println(findMembers);

        if (findMembers.isEmpty()) {
            log.warn("로그인에 실패하였습니다.");
            return null;
        } else {
            MemberEntity member = findMembers.get(0);
            // 여기서 반환을 UserDetails로 해줘야 함
            return new CustomUserDetails(member);
        }
    }
}

/*
   private void validateDuplicateMember(String email) {
        List<MemberEntity> findMembers = memberRepository.findByEmail(email);
        if (!findMembers.isEmpty()){ // 중복 이메일이 존재하면
            throw new ApplicationException(ErrorCode.SAME_EMAIL);
        }
    }
 */