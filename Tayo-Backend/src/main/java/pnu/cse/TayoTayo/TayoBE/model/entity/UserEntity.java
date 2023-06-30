package pnu.cse.TayoTayo.TayoBE.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name="member")
//@SQLDelete(sql="UPDATE user SET deleted_at = NOW() where id=?")
//@Where(clause = "deleted_at is NULL")
public class UserEntity {
    /*
        아이디 userId
        비밀번호 password
        이메일 email
        연락처 phoneNumber
        이름 Name
        닉네임 nickName
        평점, 공유횟수(거래 체결 횟수)
        한줄 소개
        지갑 관리
        (결제 수단 관리)

     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name ="user_name")
    private String userName;

    @Column(name="password")
    private String password;

//    @Column(name = "role")
//    @Enumerated(EnumType.STRING)
//    private UserRole role = UserRole.USER;

    @Column(name= "registered_at")
    private Timestamp registeredAt;

    @Column(name= "updated_at")
    private Timestamp updatedAt;

    @Column(name= "deleted_at")
    private Timestamp deletedAt;

    @PrePersist
    void registeredAt(){
        this.registeredAt = Timestamp.from(Instant.now());
    }

    @PreUpdate
    void updatedAt(){
        this.updatedAt = Timestamp.from(Instant.now());
    }

    public static UserEntity of(String userName, String password){
        UserEntity userEntity = new UserEntity();
        userEntity.setUserName(userName);
        userEntity.setPassword(password);

        return userEntity;
    }

}
