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

    private final S3Uploader s3Uploader;

    @Operation(summary = "VC 생성하기", description = "VC를 생성하는 API 입니다.")
    @PostMapping("/vc")
    public void createVC(Authentication authentication,@RequestBody MemberRequest.createVCRequest request) throws IndyException, ExecutionException, InterruptedException {

        carService.createVC(((CustomUserDetails) authentication.getPrincipal()).getId(),
                request.getWalletPassword(),request.getCarNumber());


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
    @PostMapping("/vp")
    public void registerCar(Authentication authentication ,
                            @RequestPart List<MultipartFile> images,
                            @RequestPart MemberRequest.registerCarRequest request) throws Exception {

        carService.postCar(((CustomUserDetails) authentication.getPrincipal()).getId(),
                request, images);


        //return Response.success("본인 정보를 성공적으로 조회하셨습니다.", MemberInfoResponse.fromMember(member));
    }

    @Operation(summary = "본인이 등록한 자동차 조회", description = "본인이 등록한 자동차 조회하는 API입니다.")
    @GetMapping("/vp")
    public void myCar(Authentication authentication){

        // TODO : 조회 ChainCode 실행

        //return Response.success("본인 정보를 성공적으로 조회하셨습니다.", MemberInfoResponse.fromMember(member));
    }

    // 테스트용
    @PostMapping("/s3Upload")
    public void s3Test(Authentication authentication ,
                       @RequestPart List<MultipartFile> images,
                       @RequestPart MemberRequest.registerCarRequest request) throws IOException {

        List<String> urls = s3Uploader.uploadFile(images);

        System.out.println(request.getWalletPassword());
        System.out.println(request.getReferentVC());
        System.out.println(request.getLocation().toString());
        System.out.println(request.getSharingPrice());
        System.out.println("<이용 가능 시간>");
        for(MemberRequest.registerCarRequest.SharingTime st :request.getTimeList()){
            System.out.println(st.getStartTime() + " ~ " +st.getEndTime());
        }
        for(String url : urls){
            System.out.println(url);
        }
    }

}
