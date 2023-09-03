package pnu.cse.TayoTayo.TayoBE.controller;


import com.google.gson.JsonElement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hyperledger.indy.sdk.IndyException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pnu.cse.TayoTayo.TayoBE.config.security.CustomUserDetails;
import pnu.cse.TayoTayo.TayoBE.connect.TayoConnect;
import pnu.cse.TayoTayo.TayoBE.dto.request.MemberRequest;
import pnu.cse.TayoTayo.TayoBE.dto.response.*;
import pnu.cse.TayoTayo.TayoBE.service.CarService;
import pnu.cse.TayoTayo.TayoBE.service.S3Uploader;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Tag(name = "tayo-api:car", description = "타요타요 자동차 관리 페이지 관련 API")
@RestController
@RequestMapping("/tayo/car")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;


    @Operation(summary = "VC 생성하기", description = "VC를 생성하는 API 입니다.")
    @PostMapping("/vc")
    public Response<CreateVCResponse> createVC(Authentication authentication,@RequestBody MemberRequest.createVCRequest request) throws IndyException, ExecutionException, InterruptedException {

        String userName = carService.createVC(((CustomUserDetails) authentication.getPrincipal()).getId(),
                request.getWalletPassword(), request.getCarNumber());

        return Response.success("VC를 성공적으로 생성하였습니다.", new CreateVCResponse(userName, request.getCarNumber()));
    }
    // 생성후에 자동으로 vc 조회하기까지 호출하는게 가능한가??

    @Operation(summary = "vc 조회하기", description = "본인이 가지고 있는 VC 조회하기 API 입니다.")
    @GetMapping("/vc")
    public Response<MyVCResponse> myVC(Authentication authentication, @RequestBody MemberRequest.getMyVCRequest request) throws IndyException, ExecutionException, InterruptedException {

        MyVCResponse response = carService.getVC(((CustomUserDetails) authentication.getPrincipal()).getId(),
                request.getWalletPassword());


        return Response.success("VC를 성공적으로 조회하였습니다.", response);
    }

    @Operation(summary = "차 등록하기", description = "차 등록하는 API 입니다.")
    @PostMapping("/vp")
    public Response<RegisterCarResponse> registerCar(Authentication authentication ,
                            @RequestPart List<MultipartFile> images,
                            @RequestPart MemberRequest.registerCarRequest request) throws Exception {

        RegisterCarResponse response = carService.postCar(((CustomUserDetails) authentication.getPrincipal()).getId(),
                request, images);

        return Response.success("자동차를 성공적으로 등록하였습니다.", response);
    }
  
  

  
  
    //===========================아래 구현 전===================================================================================

    @Operation(summary = "본인이 등록한 자동차 조회", description = "본인이 등록한 자동차 조회하는 API입니다.")
    @GetMapping("/vp")
    public void myCar(Authentication authentication){

        // TODO : 본인이 등록한 차량 조회 ChainCode 실행
//        public Response<Void> testQuery() throws Exception {
//        TayoConnect tayoConnect = new TayoConnect(1);
//        JsonElement cars = tayoConnect.queryCarsByOwnerID(TODO: memberID 넣어줄 것);
//        return Response.success("차량 조회 결과" + cars.toString());
    }

    @Operation(summary = "본인이 등록한 차량 삭제", description = "본인이 등록한 자동차 삭제하는 API입니다.")
    @DeleteMapping("/vp")
    public void deleteCar(Authentication authentication) throws CertificateException, IOException, InvalidKeyException {
        TayoConnect tayoConnect = new TayoConnect(1);
//        TODO: 차량 id는 어떻게 받는지??
//        JsonElement cars = tayoConnect.deleteCar();
//         return Response.success("해당 차량이 성공적으로 삭제되었습니다.");
    }

    @Operation(summary = "본인이 등록한 차량 수정", description = "본인이 등록한 자동차 수정(vp 자동차 데이터는 수정 불가능)하는 API입니다.")
    @PutMapping("/vp")
    public  Response<Void> updateCar(Authentication authentication) throws CertificateException, IOException, InvalidKeyException {
        TayoConnect tayoConnect = new TayoConnect(1);
//        TODO: 수정 가능한 값 - 공유가능일시, 공유장소명, 공유장소도로명주소, 공유위경도, 공유가능여부(Y/N)
//        평점도 수정 가능하긴 한데 이건 본인 차량 수정이라서 평점은 그냥 고정하는 걸로... + 그 외 값들은 기존 값들 불러와서 아래처럼 객체 생성해야 함...
//        Car newCar = new Car(10, 100, "Sedan", "V6", "2023-08-23", 0, "", new ArrayList<>(), "", "", 0.0, 0.0, false, 0);
//        JsonElement cars = tayoConnect.updateCar(newCar);
        return Response.success("해당 차량이 성공적으로 수정되었습니다.");
    }

    @Operation(summary = "위치 기반 차량 검색 조회", description = "위도와 경도를 기반으로 현재 지도에 있는 자동차를 조회하는 API입니다.")
    @GetMapping // latitude=?&longitude=? + 날짜 기반..?
    public void getCars(Authentication authentication, @RequestParam String latitude, @RequestParam String longitude){

        // TODO : 위도,경도, 날짜 기반 조회 ChainCode 실행

        //return Response.success("본인 정보를 성공적으로 조회하셨습니다.", MemberInfoResponse.fromMember(member));
    }

    @Operation(summary = "차량에 대한 상세 조회", description = "해당 차량에 대한 상세한 내용을 조회하는 API입니다.")
    @GetMapping("/detail/{carId}") // /tayo/car/detail/{carId}
    public void getDetailCar(Authentication authentication, @PathVariable Long carId){
        // TODO : 차량에 대한 상세 조회 ChainCode 실행
        //      여기서 carId가 chainCode에 등록되는 carId를 써야할 듯?

        //return Response.success("본인 정보를 성공적으로 조회하셨습니다.", MemberInfoResponse.fromMember(member));
    }

    @Operation(summary = "차량 대여 신청 하기", description = "임차인이 차량 대여 신청을 하면 채팅방이 생성되는 API입니다.")
    @PostMapping("/detail/{carId}") // /tayo/car/detail/{carId}
    public void requestCar(Authentication authentication, @PathVariable Long carId){

        // TODO : 상세조회 정보 기반으로 임차인과 임대인 사이에 채팅방 생성 + 임대인한테 알람
        /*
            필요한 거
            - 요청하는 유저의 Id : fromMemberId
            - 해당 {carId}의 주인 유저의 Id : toMemberId

            1. 임차인이 임대인한테 대여 신청을 했을 때, 임대인한테 알림이 감
                -> {임차인 nickname} 님의 대여 신청이 왔어요!
         */

        //return Response.success("본인 정보를 성공적으로 조회하셨습니다.", MemberInfoResponse.fromMember(member));
    }

    @Operation(summary = "차량 대여 승인 하기 (임대인)", description = "채팅방에서 임대인이 차량 대여 승인하는 API입니다.")
    @PostMapping("/accept")
    public void acceptCar(Authentication authentication){

        // TODO : Boolean으로 수락/거절인지 받아야 할듯? + 어떤 차량인지

        /*
            TODO : 알림보내기
               - 임대인이 대여 신청을 수락 했을 때, 임차인한테 알림이 감
                  -> {임대인 nickname} 님이 대여신청을 수락했어요!
               - 임대인이 대여 신청을 거절 했을 때, 임차인한테 알림이 감
                  -> {임대인 nickname} 님이 대여신청을 수락했어요!

            TODO : 채팅도 표시
         */


        //return Response.success("본인 정보를 성공적으로 조회하셨습니다.", MemberInfoResponse.fromMember(member));
    }

    @Operation(summary = "결제하기 (임차인)", description = "임차인이 결제를 하는 API입니다.")
    @PostMapping("/pay") // /tayo/car/pay
    public void payCar(Authentication authentication){

        // TODO : 결제 ChainCode 실행 + 결제 알림 날리기!

        //TODO : 채팅도 표시

        //return Response.success("본인 정보를 성공적으로 조회하셨습니다.", MemberInfoResponse.fromMember(member));
    }

    /**
     *  차량 예약 흐름도
     *
     *  1. 임대인이 차량 등록
     *  2. 임차인이 위치 기반 차량 조회 -> 상세 조회
     *  3. 임차인이 차량 대여 신청시, 임대인과의 채팅방이 생성되고 임대인에게 알림이 감
     *  4. 둘이서 채팅으로 이야기 주고 받다가
     *  5. 의견조율후 임대인이 승인을 하면 결제 활성화..?*
     *
     * (+ 공유현황 조회, 차량 정보 수정, 신청 알림 조회 ) <= 일단 다 보류
     */
}