package pnu.cse.TayoTayo.TayoBE.controller;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.client.*;
import org.hyperledger.indy.sdk.IndyException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pnu.cse.TayoTayo.TayoBE.config.security.CustomUserDetails;
import pnu.cse.TayoTayo.TayoBE.connect.TayoConnect;
import pnu.cse.TayoTayo.TayoBE.dto.request.CarRequest;
import pnu.cse.TayoTayo.TayoBE.dto.request.MemberRequest;
import pnu.cse.TayoTayo.TayoBE.dto.response.*;
import pnu.cse.TayoTayo.TayoBE.model.Car;
import pnu.cse.TayoTayo.TayoBE.model.Sharing;
import pnu.cse.TayoTayo.TayoBE.service.CarService;
import pnu.cse.TayoTayo.TayoBE.service.ChatService;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Tag(name = "tayo-api:car", description = "타요타요 자동차 관리 페이지 관련 API")
@RestController
@RequestMapping("/tayo/car")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;
    private final ChatService chatService;


    @Operation(summary = "VC 생성하기", description = "VC를 생성하는 API 입니다.")
    @PostMapping("/vc")
    public Response<CreateVCResponse> createVC(Authentication authentication,@RequestBody MemberRequest.createVCRequest request) throws IndyException, ExecutionException, InterruptedException {

        String userName = carService.createVC(((CustomUserDetails) authentication.getPrincipal()).getId(),
                request.getWalletPassword(), request.getCarNumber());

        return Response.success("VC를 성공적으로 생성하였습니다.", new CreateVCResponse(userName, request.getCarNumber()));
    }
    // 생성후에 자동으로 vc 조회하기까지 호출하는게 가능한가??

    @Operation(summary = "vc 조회하기", description = "본인이 가지고 있는 VC 조회하기 API 입니다.")
    @PostMapping("/getvc")
    public Response<MyVCResponse> myVC(Authentication authentication, @RequestBody MemberRequest.getMyVCRequest request) throws IndyException, ExecutionException, InterruptedException {

        MyVCResponse response = carService.getVC(((CustomUserDetails) authentication.getPrincipal()).getId(),
                request.getWalletPassword());


        return Response.success("VC를 성공적으로 조회하였습니다.", response);
    }

    @Operation(summary = "차 등록하기", description = "차 등록하는 API 입니다.")
    @PostMapping("/create")
    public Response<RegisterCarResponse> registerCar(Authentication authentication ,
                                                     @RequestPart(required = false) List<MultipartFile> images,
                            @RequestPart MemberRequest.registerCarRequest request) throws Exception {

        RegisterCarResponse response = carService.postCar(((CustomUserDetails) authentication.getPrincipal()).getId(),
                request, images);

        return Response.success("자동차를 성공적으로 등록하였습니다.", response);
    }

    @Operation(summary = "본인이 등록한 자동차 조회", description = "본인이 등록한 자동차 조회하는 API입니다.")
    @GetMapping("/queryowner")
    public Response<CarResponse> myCar(Authentication authentication) throws CertificateException, IOException, InvalidKeyException, GatewayException {
        TayoConnect tayoConnect = new TayoConnect(1);
        JsonArray cars = tayoConnect.queryCarsByOwnerID(((CustomUserDetails) authentication.getPrincipal()).getId());

        List<CarResponse.CarDetail> carDetailList = new ArrayList<>();

        for (JsonElement carElement : cars) {
            CarResponse.CarDetail carDetail = CarResponse.CarDetail.fromJson(carElement);
            carDetailList.add(carDetail);
        }

        CarResponse carResponse = new CarResponse(carDetailList);

        return Response.success("본인 차량 조회 결과", carResponse);
    }

    @Operation(summary = "차량에 대한 상세 조회", description = "해당 차량에 대한 상세한 내용을 조회하는 API입니다.")
    @GetMapping("/detail/{carId}")
    public Response<CarResponse.CarDetail> getDetailCar(Authentication authentication, @PathVariable Double carId) throws CertificateException, IOException, InvalidKeyException, GatewayException {
        TayoConnect tayoConnect = new TayoConnect(1);
        JsonElement cars = tayoConnect.queryByCarID(carId);

        CarResponse.CarDetail carDetail = CarResponse.CarDetail.fromJson(cars);

        return Response.success("차량 조회 결과", carDetail);
    }

    @Operation(summary = "본인이 등록한 차량 수정", description = "본인이 등록한 자동차 수정(vp 자동차 데이터는 수정 불가능)하는 API입니다.")
    @PutMapping("/update")
    public  Response<Void> updateCar(Authentication authentication, @RequestBody CarRequest.modifyCarRequest request) throws CertificateException, IOException, InvalidKeyException, EndorseException, CommitException, SubmitException, CommitStatusException {
        TayoConnect tayoConnect = new TayoConnect(1);
        // 수정 가능한 값 - 공유가능일시, 공유장소명, 공유장소도로명주소, 공유위경도, 공유가능여부(Y/N), 공유가격
        tayoConnect.updateCar(request);
        return Response.success("해당 차량이 성공적으로 수정되었습니다.");
    }

    @Operation(summary = "차량 삭제", description = "자동차를 삭제하는 API입니다.")
    @DeleteMapping("/delete/{carId}")
    public Response<Void> deleteCar(Authentication authentication, @PathVariable Double carId) throws CertificateException, IOException, InvalidKeyException, EndorseException, CommitException, SubmitException, CommitStatusException {
        TayoConnect tayoConnect = new TayoConnect(1);
        tayoConnect.deleteCar(carId);
        return Response.success("해당 차량이 성공적으로 삭제되었습니다.");
    }

    @Operation(summary = "위치 기반 차량 검색 조회", description = "위경도와 날짜를 기반으로 현재 지도에 있는 자동차를 조회하는 API입니다.")
    @GetMapping("/search")
    public Response<CarResponse> getCars(Authentication authentication,
          @RequestParam Double leftLatitude, @RequestParam Double leftLongitude, @RequestParam Double rightLatitude, @RequestParam Double rightLongitude,
          @RequestParam String date) throws CertificateException, IOException, InvalidKeyException, CommitException, GatewayException {

        TayoConnect tayoConnect = new TayoConnect(1);
        JsonArray cars = tayoConnect.getAvailableCars(leftLatitude, leftLongitude, rightLatitude, rightLongitude, date);

        List<CarResponse.CarDetail> carDetailList = new ArrayList<>();

        for (JsonElement carElement : cars) {
            CarResponse.CarDetail carDetail = CarResponse.CarDetail.fromJson(carElement);
            carDetailList.add(carDetail);
        }

        CarResponse carResponse = new CarResponse(carDetailList);

        return Response.success("위치 기반 차량 조회 결과", carResponse);
    }

    @Operation(summary = "차량 대여 신청 하기", description = "임차인이 차량 대여 신청을 하면 채팅방이 생성되는 API입니다.")
    @PostMapping("/sharing")
    public Response<Void> requestCar(Authentication authentication, @RequestBody CarRequest.sharingRequest request) throws CertificateException, IOException, InvalidKeyException, CommitException, GatewayException {

        // TODO : 상세조회 정보 기반으로 임차인과 임대인 사이에 채팅방 생성 + 임대인한테 알람
        /*
            필요한 거
            - 요청하는 유저의 Id : fromMemberId(borrowerID)
            - 해당 {carId}의 주인 유저의 Id : toMemberId(lenderID)


            1. 임차인이 임대인한테 대여 신청을 했을 때, 임대인한테 알림이 감
                -> {임차인 nickname} 님의 대여 신청이 왔어요!
         */
        TayoConnect tayoConnect = new TayoConnect(3);
        CarRequest.sharingRequest rq = request;
        // sharingID(unique 값 - carID로 생성), carID, lenderID, borrowerID, sharingPrice, sharingTime
        // borrowerID는 로그인 중인 사용자 값 전달함!
        Sharing sharing = new Sharing(rq.generateSharingIDFromCarID(), rq.getCarID(),
                rq.getLenderID(), ((CustomUserDetails) authentication.getPrincipal()).getId(), rq.getSharingPrice(),
                rq.getSharingDate(), rq.getSharingLocation(), rq.getSharingStatus());

        tayoConnect.createSharing(sharing);

        // public void createChatRoom(Long fromMemberId, Long toMemberId, Long carId){
        chatService.createChatRoom(((CustomUserDetails) authentication.getPrincipal()).getId(),
                request.getLenderID(), (long) request.getCarID());

        return Response.success("차량 대여 신청 완료");
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