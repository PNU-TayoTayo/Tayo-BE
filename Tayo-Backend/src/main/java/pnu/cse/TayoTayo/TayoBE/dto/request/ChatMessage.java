package pnu.cse.TayoTayo.TayoBE.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    private String sender;
    private String receiver;
    private String content;

    /*
        @채팅 DB에는 sender, receiver, content, created_at?


     */

}
