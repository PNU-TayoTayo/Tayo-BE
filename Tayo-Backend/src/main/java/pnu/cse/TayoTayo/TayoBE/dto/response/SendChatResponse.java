package pnu.cse.TayoTayo.TayoBE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class SendChatResponse {

    private boolean sentByCarOwner; // isCarOwner가 true/false에 따라 오른쪽 왼쪽 바뀜
    private String content; // 내용
    private Date sentAt; // 보낸 날짜
    // TODO : 상대방 개인큐 알람할려면 필요

}
