package pnu.cse.TayoTayo.TayoBE.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnu.cse.TayoTayo.TayoBE.dto.response.NotificationsResponse;
import pnu.cse.TayoTayo.TayoBE.model.entity.MemberEntity;
import pnu.cse.TayoTayo.TayoBE.model.entity.NotificationEntity;
import pnu.cse.TayoTayo.TayoBE.repository.MemberRepository;
import pnu.cse.TayoTayo.TayoBE.repository.NotificationRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final MemberRepository memberRepository;

    /**
     * 알림 DB에 저장되는 상황
     * <p>
     * 1. 임차인이 임대인한테 대여 신청을 했을 때, 임대인한테 알림이 감
     * -> {임차인 nickname} 님의 대여 신청이 왔어요!
     * <p>
     * 2. 임대인이 대여 신청을 수락 했을 때, 임차인한테 알림이 감
     * -> {임대인 nickname} 님이 대여신청을 수락했어요!
     * <p>
     * 3. 임대인이 대여 신청을 거절 했을 때, 임차인한테 알림이 감
     * -> {임대인 nickname} 님이 대여신청을 수락했어요!
     * <p>
     * 4. 채팅이나 결제시에는?? (일단 보류)
     * <p>
     * 알림 DB에 필요한 변수들
     * <p>
     * 1. 알림 PKId ㅇ
     * 2. 알림받을 UserId(receiverId) -> nickName ㅇ
     * 3. isRead -> 언제 알림을 Read 처리 해줘야할까? ㅇ
     * 후보 1: 채팅방 목록 조회했을 때..?
     * 후보 2: 한번이라도 로그인 했을 때..?
     * 후보 3: 해당 채팅방에 입장했을 떄..?
     * 4. 채팅방 Id
     * 5. notificationType(임차인의 대여 요청) ㅇ
     * 6. 날짜 (요청한 날짜 x)
     * <p>
     * 채팅방에도 해당 등록된 거래Id가 필요할려나??
     * 등록된걸 조회하게 되면 해당 등록된 차량의 Id가 있자나
     *
     * @return
     */

    @Transactional
    public NotificationsResponse getNotifications(Long userId){

        // 1. 본인 객체들고 오기
        MemberEntity member = memberRepository.findOne(userId);

        // 2. repository로 toMember가 본인이고 isRead가 false인 NotificationEntity를 다 들고온다
        List<NotificationEntity> unreadNotifications = notificationRepository.findUnreadNotificationsByToMember(member);

        // 3. response 구조 만들고 응답보내주기
        List<NotificationsResponse.Notification> notifications = new ArrayList<>();
        
        for(NotificationEntity unreadNotification: unreadNotifications) {

            NotificationsResponse.Notification n = NotificationsResponse.Notification.builder()
                    .opponentNickName(unreadNotification.getFromMember().getNickName())
                    .notificationType(unreadNotification.getNotificationType())
                    .chatRoomId(unreadNotification.getChatRoom().getId())
                    .build();
            notifications.add(n);

        }

        return new NotificationsResponse(userId, unreadNotifications.size(), notifications);

    }


}
