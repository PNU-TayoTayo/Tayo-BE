package pnu.cse.TayoTayo.TayoBE.model.entity;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name="member")
@NoArgsConstructor
@AllArgsConstructor
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

    @Enumerated(EnumType.STRING)
    private MemberRole role;
    private String email;
    private String password;

    private String name;
    private String phoneNumber;
    private String nickName;
    private String introduce;

    private String walletMasterKey;


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

    @Builder
    public MemberEntity(Long id, MemberRole role, String email, String password, String name, String phoneNumber, String nickName, String introduce,String walletMasterKey) {
        this.id = id;
        this.role = role;
        this.email = email;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.nickName = nickName;
        this.introduce = introduce;
        this.walletMasterKey = walletMasterKey;
    }


}
