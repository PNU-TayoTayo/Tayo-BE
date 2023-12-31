package pnu.cse.TayoTayo.TayoBE.model.entity;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name="member")
@NoArgsConstructor
@AllArgsConstructor
public class MemberEntity {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberEntity that = (MemberEntity) o;
        return Objects.equals(id, that.id) && role == that.role && Objects.equals(email, that.email) && Objects.equals(password, that.password) && Objects.equals(name, that.name) && Objects.equals(phoneNumber, that.phoneNumber) && Objects.equals(nickName, that.nickName) && Objects.equals(introduce, that.introduce) && Objects.equals(walletMasterKey, that.walletMasterKey) && Objects.equals(registeredAt, that.registeredAt) && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, role, email, password, name, phoneNumber, nickName, introduce, walletMasterKey, registeredAt, updatedAt);
    }
}
