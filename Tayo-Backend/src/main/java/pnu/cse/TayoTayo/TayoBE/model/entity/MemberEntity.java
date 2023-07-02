package pnu.cse.TayoTayo.TayoBE.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name="member")
public class MemberEntity {
    /*
        아이디 Id

        이메일 email
        비밀번호 password

        이름 Name
        연락처 phoneNumber

        닉네임 nickName
        한줄 소개 introduce

        평점, 공유횟수(거래 체결 횟수)
        한줄 소개
        지갑 Wallet?

     */

    @Id
    @GeneratedValue
    private Long id;

    private String email;
    private String password;

    private String name;
    private String phoneNumber;

    private String nickName;
    private String introduce;


    private Timestamp registeredAt;
    private Timestamp updatedAt;

    @PrePersist
    void registeredAt(){
        this.registeredAt = Timestamp.from(Instant.now());
    }

    @PreUpdate
    void updatedAt(){
        this.updatedAt = Timestamp.from(Instant.now());
    }


    public static MemberEntity of(String email, String password, String name, String phoneNumber,
                            String nickName, String introduce){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setEmail(email);
        memberEntity.setPassword(password);
        memberEntity.setName(name);
        memberEntity.setPhoneNumber(phoneNumber);
        memberEntity.setNickName(nickName);
        memberEntity.setIntroduce(introduce);

        return memberEntity;
    }

}
