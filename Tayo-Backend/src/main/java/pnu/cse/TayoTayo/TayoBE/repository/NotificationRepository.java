package pnu.cse.TayoTayo.TayoBE.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pnu.cse.TayoTayo.TayoBE.model.entity.ChatMessageEntity;
import pnu.cse.TayoTayo.TayoBE.model.entity.ChatRoomEntity;
import pnu.cse.TayoTayo.TayoBE.model.entity.MemberEntity;
import pnu.cse.TayoTayo.TayoBE.model.entity.NotificationEntity;

import java.util.List;


public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    
    // TODO : 성능적으로 한번에 다 들고 오는 게 좋을 듯
    //      해당 유저의 nickname
    @Query("SELECT n FROM NotificationEntity n WHERE n.toMember = :toMember AND n.isRead = false")
    List<NotificationEntity> findUnreadNotificationsByToMember(MemberEntity toMember);

    // toMember랑 채팅방 받아서 isRead가 false인게 있으면 true로 바꿔주기
    @Query("SELECT n FROM NotificationEntity n WHERE n.toMember = :toMember AND n.chatRoom = :chatRoom AND  n.isRead = false")
    List<NotificationEntity> findUnreadNotificationsByToMemberAndChatRoom(MemberEntity toMember, ChatRoomEntity chatRoom);


}
