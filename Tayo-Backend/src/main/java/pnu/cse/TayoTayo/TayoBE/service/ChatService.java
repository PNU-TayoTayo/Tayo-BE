package pnu.cse.TayoTayo.TayoBE.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnu.cse.TayoTayo.TayoBE.dto.response.ChatMessageResponse;
import pnu.cse.TayoTayo.TayoBE.model.entity.ChatMessageEntity;
import pnu.cse.TayoTayo.TayoBE.model.entity.ChatRoomEntity;
import pnu.cse.TayoTayo.TayoBE.model.entity.MemberEntity;
import pnu.cse.TayoTayo.TayoBE.repository.ChatRoomRepository;
import pnu.cse.TayoTayo.TayoBE.repository.ChatMessageRespository;
import pnu.cse.TayoTayo.TayoBE.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final MemberRepository memberRepository;

    private final ChatMessageRespository chatMessageRespository;

    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public void getChatList(Long userId){

        // 1. 해당 유저가 존재하는 지 체크
        MemberEntity member = memberRepository.findOne(userId);

        // 2.


        /*
            채팅방 목록 조회 API

                => ChatRoomEntity에서 fromMember랑 toMember에 해당 유저가 존재하는
                모든 ChatRoom 가져오기 (update 날짜 추가해서 이 순서대로 가져와야 할듯?)

                response (HeaderId, lastMessage, 안읽은 메시지 수, 상대방 NickName)
                1. HeaderId
                2. 마지막 메시지 : lastMessage로 처리 (채팅 보낼 때마다 갱신해줘야 하나?)
                3. 안읽은 채팅 수 : headerId로 ChattingMessageEntity에 접근하고, isFromMember로
                                다 가져오고 read가 false인거 가져와서 갯수를 다 계산해야하나?
                                (성능은 안좋을 거 같음..)
                4. 상대방 nickName : Member nickName

                TODO : 여기서 애매한건 만약 새로 메시지가 오면 API를 다시 호출하는건가? (채팅 목록 관리 및 실시간 업데이트)
                    =>  프론트쪽에 물어봐야할듯 (마지막 메시지와 안읽은 채팅 수 채팅방 목록 조회 갱신 어떻게 ??)

         */


    }


    @Transactional
    public ChatMessageResponse getMessages(Long userId , Long roomId){ // 아마도 headerId랑 유저 id

        // 1. 해당 유저가 존재하는 지 체크 (없으면 exception)
        MemberEntity member = memberRepository.findOne(userId);

        // 2. 해당 headerId를 가진 모든 메시지 들고오기
        // TODO : 받은 chatMessageEntities 중에 상대방이 보낸 메시지 중에 read값이 false인 것들 모두 true로 변경하기 (추후에 구현)

        ChatRoomEntity chatRoom = chatRoomRepository.findById(roomId).get();
        List<ChatMessageEntity> chatMessages = chatRoom.getChatMessageEntities();

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
