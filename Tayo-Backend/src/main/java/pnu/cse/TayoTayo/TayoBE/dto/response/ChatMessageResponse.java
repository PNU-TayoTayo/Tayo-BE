package pnu.cse.TayoTayo.TayoBE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
public class ChatMessageResponse {

    private Long chatRoomId;
    private String opponentNickName; // 상대방 닉네임
    private boolean isCarOwner; // 본인이 차주인지 -> 이거에 따라 채팅 UI 달라짐

    private List<ChatMessages> chatMessages;

    @Getter @Setter
    @Builder
    public static class ChatMessages{
        private boolean sentByCarOwner; // isCarOwner가 true/false에 따라 오른쪽 왼쪽 바뀜
        private String content; // 내용
        private Date sentAt; // 보낸 날짜
    }

}
