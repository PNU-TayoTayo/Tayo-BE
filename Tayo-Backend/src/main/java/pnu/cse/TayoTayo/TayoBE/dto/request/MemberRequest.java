package pnu.cse.TayoTayo.TayoBE.dto.request;

import lombok.Getter;
import lombok.Setter;


public class MemberRequest {


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
