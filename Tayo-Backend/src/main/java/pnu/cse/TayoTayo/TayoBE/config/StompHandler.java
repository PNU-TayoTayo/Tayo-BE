package pnu.cse.TayoTayo.TayoBE.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import pnu.cse.TayoTayo.TayoBE.config.security.JWTProvider;
import pnu.cse.TayoTayo.TayoBE.model.ConnectState;


@Component
@RequiredArgsConstructor
@Log4j2
public class StompHandler implements ChannelInterceptor {

    private final JWTProvider jwtProvider;
    private static final String BEARER_PREFIX = "Bearer ";

    private final ConnectState connectState;


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        System.out.println("message:" + message);
        System.out.println("헤더 : " + message.getHeaders());

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            System.out.println("여기서 jwt 검증하기 "); // 검증하고 나온 userId를 addUserSession에 넣기
            System.out.println("접속 세션 : " + accessor.getSessionId());

            // TODO : jwt에서 userId 꺼내기!!!
            String authorizationHeader = String.valueOf(accessor.getNativeHeader("Authorization"));
            if(authorizationHeader == null || authorizationHeader.equals("null")){
                throw new MessageDeliveryException("헤더에 JWT 토큰이 안들어감");
            }

            Long id = jwtProvider.verify(authorizationHeader).getClaim("id").asLong();
            connectState.addUserSession(accessor.getSessionId(),id);

        }else if(StompCommand.SEND.equals(accessor.getCommand())){
            System.out.println("메시지 들어옴");
        }else if(StompCommand.DISCONNECT.equals(accessor.getCommand())){
            System.out.println("연결 끊김" + accessor.getSessionId());

            Long userId = connectState.getUserIdFromUserSession(accessor.getSessionId());
            if(userId > 0){
                connectState.deleteUserRoom(userId);
                connectState.deleteUserSession(accessor.getSessionId());
            }
        }

        return message;
    }
}
