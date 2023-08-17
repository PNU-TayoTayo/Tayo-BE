package pnu.cse.TayoTayo.TayoBE.model.entity;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 알림 받을 user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_member_id")
    private MemberEntity toMember;

    // 이걸로 content 구성
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_member_id")
    private MemberEntity fromMember;

    //private String fromMemberNickName; // 이게 편하긴 할 듯..

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    // 채팅방 이동을 위한 채팅방 id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id")
    private ChatRoomEntity chatRoom;

    // TODO : 해당 자동차의 등록한 시간은 추후에 Chaincode 구조보고 해야할듯

    private Boolean isRead; // 읽었는지 안읽었는지

    private Timestamp createdAt; // 요청 날짜


    @PrePersist
    void registeredAt(){
        this.createdAt = Timestamp.from(Instant.now());
        this.isRead = false;
    }

    @Builder
    public NotificationEntity(MemberEntity toMember, MemberEntity fromMember, NotificationType notificationType, ChatRoomEntity chatRoom) {
        // TODO : 생성하면 자동으로 해당 값들 들어가는지 한번 테스트 해보기
        this.toMember = toMember;
        this.fromMember = fromMember;
        this.notificationType = notificationType;
        this.chatRoom = chatRoom;
    }
}
