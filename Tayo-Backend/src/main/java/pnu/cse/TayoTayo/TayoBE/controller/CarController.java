package pnu.cse.TayoTayo.TayoBE.controller;


import com.google.gson.JsonElement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hyperledger.indy.sdk.IndyException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pnu.cse.TayoTayo.TayoBE.config.security.CustomUserDetails;
import pnu.cse.TayoTayo.TayoBE.connect.TayoConnect;
import pnu.cse.TayoTayo.TayoBE.dto.request.MemberRequest;
import pnu.cse.TayoTayo.TayoBE.dto.response.Response;
import pnu.cse.TayoTayo.TayoBE.service.CarService;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
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
    public Response<Void> registerCar(Authentication authentication , @RequestBody MemberRequest.registerCarRequest request) throws Exception {

        ((CustomUserDetails) authentication.getPrincipal()).getId();

        carService.postCar(((CustomUserDetails) authentication.getPrincipal()).getId(),
                request.getWalletPassword(), request.getReferentVC());

        return Response.success("차량을 성공적으로 등록하셨습니다.");
    }

    @Operation(summary = "본인 차량 조회하기", description = "본인 차량 조회 API입니다.")
    @GetMapping
    public void myCar(Authentication authentication){
//        public Response<Void> testQuery() throws Exception {
//        TayoConnect tayoConnect = new TayoConnect(1);
//        JsonElement cars = tayoConnect.queryCarsByOwnerID(TODO: memberID 넣어줄 것);
//        return Response.success("차량 조회 결과" + cars.toString());
    }

    @Operation(summary = "차량 삭제하기", description = "차량 삭제 API입니다.")
    @DeleteMapping
    public Response<Void> deleteCar(Authentication authentication) throws CertificateException, IOException, InvalidKeyException {
        TayoConnect tayoConnect = new TayoConnect(1);
//        TODO: 차량 id는 어떻게 받는지??
//        JsonElement cars = tayoConnect.deleteCar();
        return Response.success("해당 차량이 성공적으로 삭제되었습니다.");
    }

    @Operation(summary = "차량 수정하기", description = "차량 수정 API입니다.")
    @PutMapping
    public Response<Void> updateCar(Authentication authentication) throws CertificateException, IOException, InvalidKeyException {
        TayoConnect tayoConnect = new TayoConnect(1);
//        TODO: 수정 가능한 값 - 공유가능일시, 공유장소명, 공유장소도로명주소, 공유위경도, 공유가능여부(Y/N)
//        평점도 수정 가능하긴 한데 이건 본인 차량 수정이라서 평점은 그냥 고정하는 걸로... + 그 외 값들은 기존 값들 불러와서 아래처럼 객체 생성해야 함...
//        Car newCar = new Car(10, 100, "Sedan", "V6", "2023-08-23", 0, "", new ArrayList<>(), "", "", 0.0, 0.0, false, 0);
//        JsonElement cars = tayoConnect.updateCar(newCar);
        return Response.success("해당 차량이 성공적으로 수정되었습니다.");
    }


//    @Operation(summary = "차량 등록하기", description = "차량을 등록하는 API입니다.")
//    @PostMapping("/testCreate")
//    public Response<Void> testCreate() throws Exception {
//        TayoConnect tayoConnect = new TayoConnect(1);
//        Car car = new Car(10, 100, "Sedan", "V6", "2023-08-23", 0, "", new ArrayList<>(), "", "", 0.0, 0.0, false, 0);
//        tayoConnect.createCar(car);
//
//        return Response.success("차량 등록 결과" + car);
//    }

}
