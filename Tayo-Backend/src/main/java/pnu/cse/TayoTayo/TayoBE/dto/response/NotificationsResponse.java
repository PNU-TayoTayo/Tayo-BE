package pnu.cse.TayoTayo.TayoBE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pnu.cse.TayoTayo.TayoBE.model.entity.NotificationType;

import java.util.List;

@Getter
@AllArgsConstructor
public class NotificationsResponse {
    private Long userId;
    private int notificationCount;
    private List<Notification> notification;

    @Getter @Setter
    @Builder
    public static class Notification{
        private String opponentNickName;
        private NotificationType notificationType;
        private Long chatRoomId;
    }

}
