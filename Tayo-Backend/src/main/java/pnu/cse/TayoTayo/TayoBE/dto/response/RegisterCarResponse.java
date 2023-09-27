package pnu.cse.TayoTayo.TayoBE.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import pnu.cse.TayoTayo.TayoBE.dto.request.MemberRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class RegisterCarResponse {

    private String userName;
    private String carNumber;

    private MemberRequest.registerCarRequest.SharingLocation location; // 장소 관련 데이터 DTO
    private int sharingPrice; // 공유가격
    private List<LocalDate> dateList;
}

