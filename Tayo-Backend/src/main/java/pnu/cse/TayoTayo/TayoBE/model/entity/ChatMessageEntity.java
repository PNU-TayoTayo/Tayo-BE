package pnu.cse.TayoTayo.TayoBE.model.entity;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ChatRoomEntity chatRoomEntity;

    private Boolean sentByCarOwner;

    private String content; // Text 타입

    private Boolean isRead; // 읽었는지 안읽었는지

    private Timestamp createdAt; // 보낸시각

    @PrePersist
    void registeredAt(){
        this.createdAt = Timestamp.from(Instant.now());
        this.isRead = false;
    }

    @Builder
    public ChatMessageEntity(Boolean sentByCarOwner, String content) {
        //this.id = id;
        this.sentByCarOwner = sentByCarOwner;
        this.content = content;
    }
}

