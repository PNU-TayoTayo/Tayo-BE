package pnu.cse.TayoTayo.TayoBE.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import pnu.cse.TayoTayo.TayoBE.dto.request.ChatMessage;
import pnu.cse.TayoTayo.TayoBE.dto.response.ChatMessageResponse;
import pnu.cse.TayoTayo.TayoBE.dto.response.SendChatResponse;
import pnu.cse.TayoTayo.TayoBE.service.ChatService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final SimpMessagingTemplate simpleMessagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/send/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessageResponse.ChatMessages sendMessage(@Payload ChatMessage message, @DestinationVariable Long roomId) throws InterruptedException {

        // 1. 채팅 내용 DB에 저장 (Long senderId, Long chatRoomId, String content 필요)
        SendChatResponse sendChatResponse = chatService.sendChatMessage(message.getSenderId(), roomId, message.getContent());

        // 2. 알림용 개인 큐에 전송 ! (response에서 꺼내야 함 <- 일단 보류)
        //simpleMessagingTemplate.convertAndSendToUser();

        // 3. 응답 메시지를 리턴하면 @SendTo 구독자에게 전달됨 (Boolean sentByCarOwner, String content, TimeStamp sentAt)
        ChatMessageResponse.ChatMessages response = ChatMessageResponse.ChatMessages.builder()
                .sentByCarOwner(sendChatResponse.isSentByCarOwner())
                .content(sendChatResponse.getContent())
                .sentAt(sendChatResponse.getSentAt())
                .build();

        return response;
    }

    /*
        1. 로그인 시에 먼저 /ws/chat 엔드포인트에 WebSocket 연결을 한다. (ws://localhost:8080/ws/chat)
        2. 채팅 탭을 제외한 곳에서는 본인의 개인 큐만 구독한다. (/queue/{userId})
            - 알림 용
        3. 채팅 탭에서는 자신이 속한 모든 채팅 방을 조회한 후, 모든 방을 구독해야 한다. (/topic/{roomId})
            - 실시간 채팅 용
            - 메시지 보낼 때(sender,receiver,content)는 @MessageMapping("/send/{roomId}")라
                /app/send/{roomId}로 메시지를 보낸다.


       -신청 알림 조회 API도 하나 만들어야겠네

     */
}

/*
    TODO : 문제점 unreadMessageCount 처리 관련 (고민...)
        실시간으로 받고 읽고 있는 상황이면 ..?


 */

