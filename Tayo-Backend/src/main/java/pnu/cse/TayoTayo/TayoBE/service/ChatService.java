package pnu.cse.TayoTayo.TayoBE.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnu.cse.TayoTayo.TayoBE.dto.response.ChatMessageResponse;
import pnu.cse.TayoTayo.TayoBE.dto.response.ChatRoomsResponse;
import pnu.cse.TayoTayo.TayoBE.dto.response.SendChatResponse;
import pnu.cse.TayoTayo.TayoBE.model.ConnectState;
import pnu.cse.TayoTayo.TayoBE.model.entity.*;
import pnu.cse.TayoTayo.TayoBE.repository.ChatRoomRepository;
import pnu.cse.TayoTayo.TayoBE.repository.ChatMessageRespository;
import pnu.cse.TayoTayo.TayoBE.repository.MemberRepository;
import pnu.cse.TayoTayo.TayoBE.repository.NotificationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final MemberRepository memberRepository;

    private final ChatMessageRespository chatMessageRespository;

    private final ChatRoomRepository chatRoomRepository;
    
    private final NotificationRepository notificationRepository;

    private final ConnectState connectState;



    // 채팅방 생성 (추후에 CarController의 requestCar에서 사용 예정)
    @Transactional
    public void createChatRoom(Long fromMemberId, Long toMemberId, Long carId){

        // 1. 임차인(fromMember)이 임대인(toMember) 객체
        MemberEntity fromMember = memberRepository.findOne(fromMemberId);
        MemberEntity toMember = memberRepository.findOne(toMemberId);

        // 2. 채팅방이 만들어짐 (채팅방 save)
        ChatRoomEntity newChatRoom = ChatRoomEntity.builder()
                .fromMember(fromMember)
                .toMember(toMember)
                .build();

        chatRoomRepository.save(newChatRoom);

        // 3. 첫 메시지 하나 생성하고 addChatMessage로 참조
        ChatMessageEntity newChatMessage = ChatMessageEntity.builder()
                .sentByCarOwner(false)
                .content(fromMember.getNickName()+" 님이 "+carId+" 차량에 (날짜 + 차량정보 : 체인코드 조회 요청해서 데이터 가져와야할듯) 대해 대여 신청을 하였습니다.")
                .build();

        newChatRoom.addChatMessage(newChatMessage);
        chatMessageRespository.save(newChatMessage);

        // 알람 생성
        NotificationEntity n = NotificationEntity.builder()
                .fromMember(fromMember)
                .toMember(toMember)
                .notificationType(NotificationType.APPLY)
                .chatRoom(newChatRoom)
                .isRead(false)
                .build();

        notificationRepository.save(n);


    }



    // MessageController의 sendMessage에 DB에 저장하는 곳에 구현 예정
    @Transactional
    public SendChatResponse sendChatMessage(Long senderId, Long chatRoomId, String content,boolean isRead){

        // 1. ChatRoomEntity을 불러온다 TODO : 없으면 exception 처리
        Optional<ChatRoomEntity> chatRoom = chatRoomRepository.findById(chatRoomId);

        // 2. 메시지 보낸 애가 차주인지 확인해야 함
        //       senderId가 해당 ChatRoomEntity에서 차주인지 확인 -> sentByCarOwner
        boolean isCarOwner;
        if(senderId.equals(chatRoom.get().getToMember().getId())){ // 차주이면
            isCarOwner = true;
        }else{
            isCarOwner = false;
        }

        // 3. ChatMessageEntity를 생성 (sentByCarOwner, content)
        ChatMessageEntity newChatMessage = ChatMessageEntity.builder()
                .sentByCarOwner(isCarOwner)
                .content(content)
                .isRead(isRead)
                .build();
        chatRoom.get().addChatMessage(newChatMessage);

        // 4. 메시지 save
        chatMessageRespository.save(newChatMessage);

        // 5. return DTO
        return new SendChatResponse(isCarOwner,content,newChatMessage.getCreatedAt());

    }


    @Transactional
    public ChatRoomsResponse getChatRooms(Long userId){

        // 1. 해당 유저가 존재하는 지 체크
        MemberEntity member = memberRepository.findOne(userId);

        // 2. chatRoomRepository에서 하나 정의해야할듯
        List<ChatRoomEntity> chatRooms = chatRoomRepository.findByFromMemberOrToMemberOrderByCreatedAtDesc(userId);

        // 3. ChatRoomsResponseList 만들기
        List<ChatRoomsResponse.ChatRooms> cr = new ArrayList<>();

        for(ChatRoomEntity chatRoom : chatRooms){
            boolean isCarOwner;
            String opponentNickName;
            if(chatRoom.getFromMember().equals(member)){ //
                opponentNickName = chatRoom.getToMember().getNickName();
                isCarOwner = false;
            }else{
                opponentNickName = chatRoom.getFromMember().getNickName();
                isCarOwner = true;
            }

            ChatRoomsResponse.ChatRooms chatRoomsResponse = ChatRoomsResponse.ChatRooms.builder()
                    .chatRoomId(chatRoom.getId())
                    .opponentNickName(opponentNickName)
                    .lastMessage(chatRoom.getLastMessage())
                    //.lastMessage(chatMessageRespository.findMostRecentMessageByChatRoom(chatRoom).getContent())
                    .unreadMessageCount(chatMessageRespository.countUnreadMessages(chatRoom,!isCarOwner))
                    .build();
            cr.add(chatRoomsResponse);
        }

        return new ChatRoomsResponse(userId,cr);

    }

    @Transactional
    public void leaveRoom(Long userId){
        // TODO : 여기서 유저 접속상태 수정
        connectState.deleteUserRoom(userId);
    }

    @Transactional
    public ChatMessageResponse getMessages(Long userId , Long roomId){

        // 1. 해당 유저가 존재하는 지 체크 (없으면 exception)
        MemberEntity member = memberRepository.findOne(userId);

        // 2. 해당 headerId를 가진 모든 메시지 들고오기
        ChatRoomEntity chatRoom = chatRoomRepository.findById(roomId).get();
        List<ChatMessageEntity> chatMessages = chatRoom.getChatMessageEntities();

        // 3. 유저 접속상태
        connectState.addUserRoom(userId,roomId);

        // 4. 안읽은 알림있으면 읽음 처리하기
        List<NotificationEntity> no = notificationRepository.findUnreadNotificationsByToMemberAndChatRoom(member, chatRoom);
        for(NotificationEntity n : no){
            n.setIsRead(true);
        }

        // 3. 가지고 온 메시지들로 responseDTO 만들어서 Controller로 보내기 (ChatMessageResponse)

        String opponentNickName;
        boolean isCarOwner;
        List<ChatMessageResponse.ChatMessages> messages = new ArrayList<>();

        if(chatRoom.getToMember().equals(member)){ // 본인이 차주이면 (임대인)
            opponentNickName = chatRoom.getFromMember().getNickName(); // 상대방이 임차인
            isCarOwner = true;
        }else{ // 빌리는 사람이면
            opponentNickName = chatRoom.getToMember().getNickName(); // 상대방이 차주
            isCarOwner = false;
        }

        for(ChatMessageEntity message : chatMessages){
            /*
                isCarOwner True 일 때: (getMessage한 애가 차주)
                    => 빌리는 애가 보낸 메시지(sentByCarOwner가 false)중 read가 false인것들 모두 true로

                isCarOwner False 일 때: (요청한 사람이 빌리는 사람)
                    => 차주가 보낸 메시지(sentByCarOwner가 true) 중 read가 false 인 것들 모두 true로
             */

            // 상대방이 보낸 메시지 + Read가 false인 메시지
            if(isCarOwner != message.getSentByCarOwner() && !message.getIsRead()){
                message.setIsRead(true);
            }

            ChatMessageResponse.ChatMessages m = ChatMessageResponse.ChatMessages.builder()
                    .sentByCarOwner(message.getSentByCarOwner())
                    .content(message.getContent())
                    .sentAt(message.getCreatedAt())
                    .build();
            messages.add(m);
        }

        return new ChatMessageResponse(roomId,opponentNickName,isCarOwner,messages);
    }


}
