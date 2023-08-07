package pnu.cse.TayoTayo.TayoBE.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateVCResponse {

    private String userName;
    private String carNumber;
    // TODO : 추가로 정보 더 필요하면 추가하기!
    //      userName님의 00가1234 번호차량의 VC가 생성되었습니다!! (+ 일단 이까지만)
}
