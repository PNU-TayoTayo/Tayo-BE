package pnu.cse.TayoTayo.TayoBE.dto.request;

import lombok.Getter;
import lombok.Setter;


public class MemberRequest {


    @Getter
    @Setter
    public static class walletPasswordRequest{
        private String walletPassword;
    }

    @Getter
    @Setter
    public static class DeleteMemberRequest{
        private String currentPassword;
    }

    @Getter
    @Setter
    public static class ModifyPasswordRequest{
        private String currentPassword;
        private String newPassword;
        private String checkNewPassword;
    }

    @Getter
    @Setter
    public static class ModifyIntroduceRequest{
        private String newIntroduce;
    }

    @Getter
    @Setter
    public static class MemberLoginRequest{
        private String email;
        private String password;
    }

    @Getter
    @Setter
    public static class MemberJoinRequest{
        private String email;
        private String password;
        private String name;
        private String phoneNumber;
        private String nickName;
        private String introduce;
    }

}
