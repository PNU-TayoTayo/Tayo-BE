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
@Table(name="message")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChattingMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_id")
    private ChatHeaderEntity headerId;

    // true이면 fromMember가 보낸 메시지, false이면 반대
    // TODO : 해당 컬럼은 바뀔수도 있음
    //          이것보다 userId나 nickName 같은것도 필요할듯..?
    private Boolean isFromMember;

    private String content; // Text 타입

    private Boolean read; // 읽었는지 안읽었는지

    private Date createdAt; // 보낸시각

    @PrePersist
    void registeredAt(){
        this.createdAt = Timestamp.from(Instant.now());
    }

}

