package pnu.cse.TayoTayo.TayoBE.service;


import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnu.cse.TayoTayo.TayoBE.dto.response.NotificationsResponse;
import pnu.cse.TayoTayo.TayoBE.model.entity.ChatRoomEntity;
import pnu.cse.TayoTayo.TayoBE.model.entity.MemberEntity;
import pnu.cse.TayoTayo.TayoBE.model.entity.NotificationEntity;
import pnu.cse.TayoTayo.TayoBE.model.entity.NotificationType;
import pnu.cse.TayoTayo.TayoBE.repository.ChatRoomRepository;
import pnu.cse.TayoTayo.TayoBE.repository.MemberRepository;
import pnu.cse.TayoTayo.TayoBE.repository.NotificationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final MemberRepository memberRepository;
    
    private final ChatRoomRepository chatRoomRepository;

    private final SimpMessagingTemplate simpleMessagingTemplate;


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


    // 해당 메소드는 테스트 용임
    @Transactional
    public void createNotification(Long fromUserId, Long chatRoomId , NotificationType type){

        /*
            [알림이 생성되는 상황]

            1. 임차인이 임대인한테 대여 신청을 했을 때(채팅방 생성), 임대인한테 알림이 감 (차량검색 페이지)
            2. 임대인이 대여 신청을 수락 했을 때, 임차인한테 알림이 감 (채팅방 페이지)
            3. 임대인이 대여 신청을 거절 했을 때, 임차인한테 알림이 감 (채팅방 페이지)
            4. 임차인이 결제를 하면 임대인한테 알림이 감 (채팅방 페이지)

            믿을 수 있는 데이터는 fromUserId 뿐인 toUserId랑 chatRoomId는 흠...
         */
        
        // 1. 본인, 상대방, 채팅방 객체 들고오기
        MemberEntity fromMember = memberRepository.findOne(fromUserId);
        MemberEntity toMember;
        Optional<ChatRoomEntity> chatRoom = chatRoomRepository.findById(chatRoomId);

        if(chatRoom.get().getToMember().equals(fromMember)){ // 상대방 Member 객체 가져오기
            toMember = chatRoom.get().getFromMember();
        }else{
            toMember = chatRoom.get().getToMember();
        }


        // 2. Notification Entity 생성 후 저장
        NotificationEntity n = NotificationEntity.builder()
                .fromMember(fromMember)
                .toMember(toMember)
                .notificationType(type)
                .chatRoom(chatRoom.get())
                .build();
        notificationRepository.save(n);

        // 3. toMember의 개인큐에 실시간 알림 보내기
        String destination = "queue/" + toMember.getId();
        NotificationsResponse.Notification message = NotificationsResponse.Notification.builder()
                .opponentNickName(fromMember.getNickName())
                .notificationType(type)
                .chatRoomId(chatRoom.get().getId())
                .build();

        simpleMessagingTemplate.convertAndSend("/queue/user/"+toMember.getId(),message);

        // 4. 일단 return 값은 정의 x
    }


}

        /*
         Notification의 isRead -> 언제 알림을 Read 처리 해줘야할까? ㅇ
            후보 1: 채팅방 목록 조회했을 때..?
            후보 2: 한번이라도 로그인 했을 때..?
            후보 3: 해당 채팅방에 입장했을 떄..?
         */