package pnu.cse.TayoTayo.TayoBE.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;

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

    public static Member fromEntity(MemberEntity entity) {
        return new Member(
                entity.getId(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getName(),
                entity.getPhoneNumber(),
                entity.getNickName(),
                entity.getIntroduce()
        );
    }
}
