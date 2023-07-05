package pnu.cse.TayoTayo.TayoBE.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pnu.cse.TayoTayo.TayoBE.model.Member;


@Getter
@NoArgsConstructor
public class MemberIntroResponse {
    private Long id;
    private String email;
    private String introduce;


    @Builder
    public MemberIntroResponse(Long id, String email, String introduce) {
        this.id = id;
        this.email = email;
        this.introduce = introduce;
    }

    public static MemberIntroResponse fromMember(Member member){
        return MemberIntroResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .introduce(member.getIntroduce())
                .build();
    }

}