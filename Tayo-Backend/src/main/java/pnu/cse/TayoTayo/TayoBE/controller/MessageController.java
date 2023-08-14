package pnu.cse.TayoTayo.TayoBE.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;
import pnu.cse.TayoTayo.TayoBE.dto.request.ChatMessage;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final SimpMessagingTemplate simpleMessagingTemplate;

    //  웹소켓 앤드포인트로 들어오는 소켓 통신의 경로 중, @MessageMapping 어노테이션으로 특정 경로로 들어오는 메세지들을 @SendTo 에 명시된 경로로 뿌려짐
    // 즉, /app/chat/message/
    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessage message) throws InterruptedException {

        //TODO : 여기서 받은 메시지를 처리 !! (DB에 저장하는 방식등)

        simpleMessagingTemplate.convertAndSendToUser(message.getReceiver(), "/queue/chat", message.getContent());
        simpleMessagingTemplate.convertAndSendToUser(message.getSender(), "/queue/chat", message.getContent());
    }
}
    /*
        간단하게 정리하면 먼저 사용자가 /ws/chat 엔드포인트에 WebSocket 연결을 하고
        MessageController의 @MessageMapping 어노테이션을 사용하여 /app/chat/message에 메시지를 보낼 때
        sendMessage 메서드가 호출됨 그러면 처리하고 SimpleBroker로 보냄
        /user/{receiverSessionId}/queue/chat 이라는 개인큐로 도착함!

        흠.. 로그인 시에 웹소켓 연결을 해둔다면..?? 로그인 API 호출 시
        알람 같은게 ?

        채팅을 sender receiver 정해져있음
        receiver를 토픽으로 해야하나? 흠...

        채팅방 말고 채팅 유저를 찾을까?? ..


     */
