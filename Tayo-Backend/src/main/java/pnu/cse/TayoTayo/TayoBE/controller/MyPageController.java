package pnu.cse.TayoTayo.TayoBE.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pnu.cse.TayoTayo.TayoBE.config.security.CustomUserDetails;
import pnu.cse.TayoTayo.TayoBE.dto.request.MemberRequest;
import pnu.cse.TayoTayo.TayoBE.dto.response.MemberInfoResponse;
import pnu.cse.TayoTayo.TayoBE.dto.response.MemberIntroResponse;
import pnu.cse.TayoTayo.TayoBE.dto.response.Response;
import pnu.cse.TayoTayo.TayoBE.model.Member;
import pnu.cse.TayoTayo.TayoBE.service.MyPageService;


@Tag(name = "tayo-api", description = "타요타요 API")
@RestController
@RequestMapping("/tayo/my")
@RequiredArgsConstructor
public class MyPageController {
    private final MyPageService myPageService;

    @Operation(summary = "내 정보 조회", description = "내 정보를 조회하는 API입니다.")
    @GetMapping
    public Response<MemberInfoResponse> myInfo(Authentication authentication){
        Member member = myPageService.myInfo(((CustomUserDetails) authentication.getPrincipal()).getId());

        return Response.success("본인 정보를 성공적으로 조회하셨습니다.", MemberInfoResponse.fromMember(member));
    }



    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴하는 API입니다.")
    @DeleteMapping
    public Response<Void> deleteMember(Authentication authentication, @RequestBody MemberRequest.DeleteMemberRequest request){

        // TODO : 회원 탈퇴시 지갑 삭제도 해줘야 함!
        //          강제로 지갑 파일을 삭제해주는 식으로 해야할 듯????
        myPageService.deleteMember(((CustomUserDetails) authentication.getPrincipal()).getId(),request.getCurrentPassword());

        return Response.success("회원 탈퇴에 성공하셨습니다.");
    }


    @Operation(summary = "비밀번호 수정", description = "비밀번호 수정하는 API입니다.")
    @PatchMapping("/password")
    public Response<Void> modifyPassword(Authentication authentication, @RequestBody MemberRequest.ModifyPasswordRequest request){

        myPageService.modifyPassword(((CustomUserDetails) authentication.getPrincipal()).getId(),request);

        return Response.success("비밀번호 수정에 성공하셨습니다.");
    }


    @Operation(summary = "한줄 소개 수정", description = "한줄 소개 수정하는 API입니다.")
    @PatchMapping("/introduce")
    public Response<MemberIntroResponse> modifyIntroduce(Authentication authentication, @RequestBody MemberRequest.ModifyIntroduceRequest request){

        Member member = myPageService.modifyIntroduce(((CustomUserDetails) authentication.getPrincipal()).getId(), request.getNewIntroduce());

        return Response.success("한줄 소개 수정에 성공하셨습니다.", MemberIntroResponse.fromMember(member));
    }








    // ======================================이 밑에 구현 전 =======================================
    @Operation(summary = "현재 잔액 조회", description = "현재 잔액을 조회하는 API입니다.")
    @GetMapping("/money")
    public void getMoney(Authentication authentication){


        //return Response.success("한줄 소개 수정에 성공하셨습니다.", MemberIntroResponse.fromMember(member));
    }

    @Operation(summary = "잔액 채우기", description = "잔액 채우는 API입니다.")
    @PostMapping("/deposit")
    public void depositMoney(Authentication authentication){


        //return Response.success("한줄 소개 수정에 성공하셨습니다.", MemberIntroResponse.fromMember(member));
    }

    @Operation(summary = "출금하기", description = "출금하는 API입니다.")
    @PostMapping("/withdraw")
    public void withdrawMoney(Authentication authentication){


        //return Response.success("한줄 소개 수정에 성공하셨습니다.", MemberIntroResponse.fromMember(member));
    }

    @Operation(summary = "최근 거래 내역 조회", description = "최근 거래 내역 조회하는 API입니다.")
    @GetMapping("/recent")
    public void recentTransaction(Authentication authentication){


        //return Response.success("한줄 소개 수정에 성공하셨습니다.", MemberIntroResponse.fromMember(member));
    }



}
