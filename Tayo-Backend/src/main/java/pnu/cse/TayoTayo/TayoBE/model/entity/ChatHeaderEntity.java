package pnu.cse.TayoTayo.TayoBE.model.entity;


import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name="header")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatHeaderEntity { // 채팅방 느낌임

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_member_id")
    private MemberEntity fromMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id")
    private MemberEntity toMember;

    private String lastMessage;

    private Date createdAt; // 보낸시각

    @PrePersist
    void registeredAt(){
        this.createdAt = Timestamp.from(Instant.now());
    }

    // TODO : 업데이트 칼럼 필요할듯? -> 제일 최근 메시지 언제 들어왔는지..?


}
/*
    1. 채팅방 목록 조회하면 fromId랑 toId 중에 해당 userId가 있는거 다 긁어 와야함
    2. 채팅방 생성 시에는 fromId가
    3. 구독은 ChatHeaderEntity Id를 기준으로..?

    그리고
    fromMember랑 toMember를 인덱스처리해주는게 좋을듯?
 */
