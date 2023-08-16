package pnu.cse.TayoTayo.TayoBE.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pnu.cse.TayoTayo.TayoBE.config.security.CustomUserDetails;

import pnu.cse.TayoTayo.TayoBE.dto.request.ChatMessage;
import pnu.cse.TayoTayo.TayoBE.dto.request.MemberRequest;
import pnu.cse.TayoTayo.TayoBE.dto.response.ChatMessageResponse;
import pnu.cse.TayoTayo.TayoBE.dto.response.ChatRoomsResponse;
import pnu.cse.TayoTayo.TayoBE.dto.response.Response;
import pnu.cse.TayoTayo.TayoBE.dto.response.SendChatResponse;
import pnu.cse.TayoTayo.TayoBE.service.ChatService;

@Tag(name = "tayo-api", description = "타요타요 API")
@RestController
@RequestMapping("/tayo/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "채팅방 목록 조회 API", description = "해당 유저의 채팅방 목록을 조회 하는 API 입니다.")
    @GetMapping
    public Response<ChatRoomsResponse> getChatList(Authentication authentication) {

        ChatRoomsResponse response = chatService.getChatRooms(((CustomUserDetails) authentication.getPrincipal()).getId());

        return Response.success("채팅방 목록을 성공적으로 조회하였습니다.", response);
    }

    @Operation(summary = "채팅방 입장 시 과거 내용 조회 API", description = "선택한 채팅방에 대해서 과거 메시지 내역을 조회하는 API 입니다.")
    @GetMapping("/{roomId}")
    public Response<ChatMessageResponse> getMessages(Authentication authentication, @PathVariable Long roomId) {

        ChatMessageResponse response = chatService.getMessages(((CustomUserDetails) authentication.getPrincipal()).getId(), roomId);

        return Response.success("채팅방 과거 내용을 성공적으로 조회하였습니다.", response);
    }


    // 테스트 용 채팅 방 생성
    @PostMapping("/test/create")
    public void createTestChatRoom(Authentication authentication , @RequestBody MemberRequest.createTestChatRoomRequest request){

        chatService.createChatRoom(((CustomUserDetails) authentication.getPrincipal()).getId(), request.getToMemberId() , request.getCarId());
    }

//    // 테스트용 채팅 보내기(HTTP)
//    @PostMapping("/test/send")
//    public void sendTestChatMessage(Authentication authentication , @RequestBody MemberRequest.sendTestChatMessage request){
//
//        chatService.sendChatMessage(((CustomUserDetails) authentication.getPrincipal()).getId(),request.getChatRoomId(),request.getContent());
//
//    }

}
