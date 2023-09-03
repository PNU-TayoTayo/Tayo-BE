package pnu.cse.TayoTayo.TayoBE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChatRoomsResponse {


    private Long userId; // 본인 userId
    private List<ChatRooms> chatRooms;

    @Getter @Setter
    @Builder
    public static class ChatRooms{
        private Long chatRoomId;
        private String opponentNickName;
        private String lastMessage;
        private int unreadMessageCount;
    }

}
