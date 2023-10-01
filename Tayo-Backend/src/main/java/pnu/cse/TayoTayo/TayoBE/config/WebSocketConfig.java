package pnu.cse.TayoTayo.TayoBE.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// 만약 Websocket 연결에 Spring Security의 토큰 인증 과정을 넣고싶으면
// HandshakeInterceptor를 implements로 커스텀해서 따로 구현해야함!! (일단 이 부분은 생략..)

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 받을 때 관련 경로 설정 (SimpleBroker - 내장 브로커 사용)
        // "/queue", "/topic" 이 두 경로가 prefix에 붙은 경우, messageBroker가 잡아서 해당 채팅방을 구독하고 있는 클라이언트에게 메시지를 전달
        // 주로 "/queue"는 1대1, "/topic"은 1대다일 때 주로 사용
        registry.enableSimpleBroker("/queue","/topic");
        // 클라이언트가 메시지를 보낼 때 경로 맨앞에 "/app"이 붙어있으면 Broker로 보내짐.
        // 메시지 핸들러로 라우팅 되는 Prefix
        registry.setApplicationDestinationPrefixes("/app");
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        // Client에서 websocket 연결할 때, 사용할 API 경로를 설정
        registry.addEndpoint("/ws/chat")
                //.addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOriginPatterns("*");//.withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration){
        registration.interceptors(stompHandler);
    }
}
