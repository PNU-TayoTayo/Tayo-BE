package pnu.cse.TayoTayo.TayoBE.controller;


import com.google.gson.JsonElement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hyperledger.indy.sdk.IndyException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import pnu.cse.TayoTayo.TayoBE.config.security.CustomUserDetails;
import pnu.cse.TayoTayo.TayoBE.connect.TayoConnect;
import pnu.cse.TayoTayo.TayoBE.dto.request.MemberRequest;
import pnu.cse.TayoTayo.TayoBE.dto.response.Response;
import pnu.cse.TayoTayo.TayoBE.model.Car;
import pnu.cse.TayoTayo.TayoBE.service.CarService;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@Tag(name = "tayo-api", description = "타요타요 API")
@RestController
@RequestMapping("/tayo/car")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;


    @Operation(summary = "VC 생성하기", description = "VC를 생성하는 API 입니다.")
    @PostMapping("/vc")
    public void createVC(Authentication authentication,@RequestBody MemberRequest.walletPasswordRequest request) throws IndyException, ExecutionException, InterruptedException {

        ((CustomUserDetails) authentication.getPrincipal()).getId();

        carService.createVC(((CustomUserDetails) authentication.getPrincipal()).getId(),
                request.getWalletPassword());


        //return Response.success("본인 정보를 성공적으로 조회하셨습니다.", MemberInfoResponse.fromMember(member));
    }


    @Operation(summary = "vc 조회하기", description = "본인이 가지고 있는 VC 조회하기 API 입니다.")
    @GetMapping("/vc")
    public void myVC(Authentication authentication, @RequestBody MemberRequest.getMyVCRequest request) throws IndyException, ExecutionException, InterruptedException {

        carService.getVC(((CustomUserDetails) authentication.getPrincipal()).getId(),
                request.getWalletPassword());

        //return Response.success("본인 정보를 성공적으로 조회하셨습니다.", MemberInfoResponse.fromMember(member));
    }

    @Operation(summary = "차 등록하기", description = "차 등록하는 API 입니다.")
    @PostMapping
    public void registerCar(Authentication authentication , @RequestBody MemberRequest.registerCarRequest request) throws Exception {

        ((CustomUserDetails) authentication.getPrincipal()).getId();

        carService.postCar(((CustomUserDetails) authentication.getPrincipal()).getId(),
                request.getWalletPassword(), request.getReferentVC());


        //return Response.success("본인 정보를 성공적으로 조회하셨습니다.", MemberInfoResponse.fromMember(member));
    }

    @Operation(summary = "본인이 등록한 자동차 조회", description = "본인이 등록한 자동차 조회하는 API입니다.")
    @GetMapping
    public void myCar(Authentication authentication){

        // TODO : 여긴 그냥 체인 코드 실행시키면 될듯..?

        //return Response.success("본인 정보를 성공적으로 조회하셨습니다.", MemberInfoResponse.fromMember(member));
    }

    @Operation(summary = "전체 자동차 조회", description = "전체 자동차를 조회하는 API입니다.")
    @GetMapping("/testquery")
    public Response<Void> testQuery() throws Exception {
        TayoConnect tayoConnect = new TayoConnect(1);
        JsonElement cars = tayoConnect.queryAllCars();

        return Response.success("차량 조회 결과" + cars.toString());
    }

    @Operation(summary = "차량 등록하기", description = "차량을 등록하는 API입니다.")
    @PostMapping("/testCreate")
    public Response<Void> testCreate() throws Exception {
        TayoConnect tayoConnect = new TayoConnect(1);
        Car car = new Car(10, 100, "Sedan", "V6", "2023-08-23", 0, "", new ArrayList<>(), "", "", 0.0, 0.0, false, 0);
        tayoConnect.createCar(car);

        return Response.success("차량 등록 결과" + car);
    }

}
