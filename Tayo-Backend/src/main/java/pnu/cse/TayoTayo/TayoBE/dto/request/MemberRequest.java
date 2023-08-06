package pnu.cse.TayoTayo.TayoBE.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public class MemberRequest {

    @Getter
    @Setter
    public static class s3TestRequest{
        private List<MultipartFile> content;
    }


    @Getter
    @Setter
    public static class registerCarRequest{
        private String walletPassword;
        private String referentVC;
        // TODO : 요금, 위치, 이미지 등록 등등 추가 정보
    }

    @Getter
    @Setter
    public static class getMyVCRequest{
        private String walletPassword;
    }

    @Getter
    @Setter
    public static class createVCRequest{
        private String walletPassword;
        private String carNumber;
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
        private String walletPassword;
    }

}
