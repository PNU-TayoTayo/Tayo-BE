package pnu.cse.TayoTayo.TayoBE.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;


public class MemberRequest {

    @Getter
    @Setter
    public static class sendTestChatMessage{
        private Long chatRoomId;
        private String content;
    }

    @Getter
    @Setter
    public static class createTestChatRoomRequest{
        private Long toMemberId;
        private Long carId;
    }

    @Getter
    @Setter
    public static class registerCarRequest{
        private String walletPassword; // 본인 지갑 비밀번호
        private String referentVC; // 등록할 자동차에 대한 VC

        private SharingLocation location; // 장소 관련 데이터 DTO
        private int sharingPrice; // 공유가격
        private List<SharingTime> timeList; // 공유가능한 날짜/시간 목록 DTO

        @Getter @Setter
        public static class SharingLocation{
            private String sharingLocation; // 공유 장소명
            private String sharingLocationAddress; // 공유 장소 도로명 주소
            private String sharingLatitude; // 공유 장소 위도
            private String sharingLongitude; // 공유 장소 경도

            @Override
            public String toString() {
                return "SharingLocation{" +
                        "sharingLocation='" + sharingLocation + '\'' +
                        ", sharingLocationAddress='" + sharingLocationAddress + '\'' +
                        ", sharingLatitude='" + sharingLatitude + '\'' +
                        ", sharingLongitude='" + sharingLongitude + '\'' +
                        '}';
            }
        }

        @Getter @Setter
        public static class SharingTime{ // 시간 단위..? 날짜단위 ?
            // 예시 : YYYY-MM-DD
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            private LocalDateTime startTime;
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            private LocalDateTime endTime;
        }

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
