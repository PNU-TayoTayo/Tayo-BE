package pnu.cse.TayoTayo.TayoBE.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pnu.cse.TayoTayo.TayoBE.model.entity.MemberEntity;

@Getter
@AllArgsConstructor
public class Member {

    private Long id;

    private String email;
    private String password;

    private String name;
    private String phoneNumber;

    private String nickName;
    private String introduce;

    private String jwt;

    public static Member fromEntity(MemberEntity entity) {
        return new Member(
                entity.getId(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getName(),
                entity.getPhoneNumber(),
                entity.getNickName(),
                entity.getIntroduce(),null
        );
    }

    public static Member fromEntity(MemberEntity entity,String jwt) {
        return new Member(
                entity.getId(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getName(),
                entity.getPhoneNumber(),
                entity.getNickName(),
                entity.getIntroduce(),jwt
        );
    }
}
