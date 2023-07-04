package pnu.cse.TayoTayo.TayoBE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pnu.cse.TayoTayo.TayoBE.model.Member;

@Getter
@NoArgsConstructor
public class MemberInfoResponse {

    private Long id;
    private String nickName;
    private String introduce;

    private String name;
    private String email;
    private String phoneNumber;


    @Builder
    public MemberInfoResponse(Long id, String nickName, String introduce, String name, String email, String phoneNumber) {
        this.id = id;
        this.nickName = nickName;
        this.introduce = introduce;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public static MemberInfoResponse fromMember(Member member){
        return MemberInfoResponse.builder()
                .id(member.getId())
                .nickName(member.getNickName())
                .introduce(member.getIntroduce())
                .name(member.getName())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .build();
    }

}