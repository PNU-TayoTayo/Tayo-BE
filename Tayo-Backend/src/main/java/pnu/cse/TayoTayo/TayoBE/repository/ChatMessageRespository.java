package pnu.cse.TayoTayo.TayoBE.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pnu.cse.TayoTayo.TayoBE.model.entity.ChatMessageEntity;
import pnu.cse.TayoTayo.TayoBE.model.entity.ChatRoomEntity;

public interface ChatMessageRespository extends JpaRepository<ChatMessageEntity, Long> {

    @Query("SELECT COUNT(m) FROM ChatMessageEntity m WHERE m.chatRoomEntity = :chatRoom AND m.read = false")
    int countUnreadMessages(@Param("chatRoom") ChatRoomEntity chatRoom);

    @Query("SELECT cm FROM ChatMessageEntity cm WHERE cm.chatRoomEntity = :chatRoomEntity ORDER BY cm.createdAt DESC")
    ChatMessageEntity findMostRecentMessageByChatRoom(@Param("chatRoomEntity") ChatRoomEntity chatRoomEntity);
}
