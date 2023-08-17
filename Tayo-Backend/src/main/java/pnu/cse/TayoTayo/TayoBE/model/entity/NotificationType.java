package pnu.cse.TayoTayo.TayoBE.model.entity;

public enum NotificationType {
    // 1. 임차인이 임대인한테 대여 신청을 했을 때, 임대인한테 알림이 감
    //    => {임차인 NickName} + 거래 신청
    APPLY,
    // 2. 임대인이 대여 신청을 수락 했을 때, 임차인한테 알림이 감
    //    => {임대인 NickName} + 신청 수락
    ACCEPT,
    // 3. 임대인이 대여 신청을 거절 했을 때, 임차인한테 알림이 감
    //    => {임대인 NickName} + 신청 거절
    REJECT,
    // 4. 임차인이 결제를 하면 임대인한테 알림이 감
    //    => {임차인 NickName} + 결제
    PAY
}
