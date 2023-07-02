package pnu.cse.TayoTayo.TayoBE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pnu.cse.TayoTayo.TayoBE.model.entity.Member;

@Getter
@AllArgsConstructor
public class UserJoinResponse {

    private Long id;
    private String name;
    private String nickName;

    public static UserJoinResponse fromMember(Member member){
        return new UserJoinResponse(
                member.getId(),
                member.getName(),
                member.getNickName()
        );
    }



}
