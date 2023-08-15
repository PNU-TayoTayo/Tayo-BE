package pnu.cse.TayoTayo.TayoBE.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hyperledger.indy.sdk.IndyException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pnu.cse.TayoTayo.TayoBE.config.security.CustomUserDetails;
import pnu.cse.TayoTayo.TayoBE.dto.request.MemberRequest;
import pnu.cse.TayoTayo.TayoBE.dto.response.*;
import pnu.cse.TayoTayo.TayoBE.service.CarService;
import pnu.cse.TayoTayo.TayoBE.service.S3Uploader;

import java.io.IOException;
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

        //return Response.success("본인 정보를 성공적으로 조회하셨습니다.", MemberInfoResponse.fromMember(member));
    }

    @Operation(summary = "본인이 등록한 차량 삭제", description = "본인이 등록한 자동차 삭제하는 API입니다.")
    @DeleteMapping("/vp")
    public void deleteCar(Authentication authentication){

        // TODO : 등록된 차량 삭제하는 ChainCode 실행

        //return Response.success("본인 정보를 성공적으로 조회하셨습니다.", MemberInfoResponse.fromMember(member));
    }

    @Operation(summary = "본인이 등록한 차량 수정", description = "본인이 등록한 자동차 수정(vp 자동차 데이터는 수정 불가능)하는 API입니다.")
    @PutMapping("/vp")
    public void updateCar(Authentication authentication){

        // TODO : 등록된 차량 수정하는 ChainCode 실행

        //return Response.success("본인 정보를 성공적으로 조회하셨습니다.", MemberInfoResponse.fromMember(member));
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

    @Operation(summary = "차량 예약 신청 하기", description = "임차인이 차량 대여 신청을 하면 채팅방이 생성되는 API입니다.")
    @PostMapping("/detail/{carId}") // /tayo/car/detail/{carId}
    public void requestCar(Authentication authentication, @PathVariable Long carId){

        // TODO : 상세조회 정보 기반으로 임차인과 임대인 사이에 채팅방 생성 + 임대인한테 알람
        /*
            필요한 거
            - 요청하는 유저의 Id : fromMemberId
            - 해당 {carId}의 주인 유저의 Id : toMemberId

         */

        //return Response.success("본인 정보를 성공적으로 조회하셨습니다.", MemberInfoResponse.fromMember(member));
    }

    @Operation(summary = "차량 대여 승인 하기 (임대인)", description = "채팅방에서 임대인이 차량 대여 승인하는 API입니다.")
    @PostMapping("/grant")
    public void grantCar(Authentication authentication){

        // TODO : 흠... 이것도 ChainCode..??

        //return Response.success("본인 정보를 성공적으로 조회하셨습니다.", MemberInfoResponse.fromMember(member));
    }

    @Operation(summary = "결제하기 (임차인)", description = "임차인이 결제를 하는 API입니다.")
    @PostMapping("/pay") // /tayo/car/pay
    public void payCar(Authentication authentication){

        // TODO : 결제 ChainCode 실행

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
