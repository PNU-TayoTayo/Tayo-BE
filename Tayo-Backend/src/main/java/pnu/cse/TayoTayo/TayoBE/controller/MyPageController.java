package pnu.cse.TayoTayo.TayoBE.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pnu.cse.TayoTayo.TayoBE.config.security.CustomUserDetails;
import pnu.cse.TayoTayo.TayoBE.dto.response.MemberInfoResponse;
import pnu.cse.TayoTayo.TayoBE.dto.response.Response;
import pnu.cse.TayoTayo.TayoBE.model.Member;
import pnu.cse.TayoTayo.TayoBE.service.MemberService;
import pnu.cse.TayoTayo.TayoBE.service.MyPageService;


@Tag(name = "tayo-api", description = "타요타요 API")
@RestController
@RequestMapping("/tayo/my")
@RequiredArgsConstructor
public class MyPageController {

    private final MemberService memberService;

    private final MyPageService myPageService;

    @Operation(summary = "내 정보 조회", description = "내 정보를 조회하는 API입니다.")
    @GetMapping
    public Response<MemberInfoResponse> myInfo(Authentication authentication){
        Member member = myPageService.myInfo(((CustomUserDetails) authentication.getPrincipal()).getId());

        return Response.success("본인 정보를 성공적으로 가져옴(임시)", MemberInfoResponse.fromMember(member));
    }

//    @Operation(summary = "비밀번호 수정", description = "비밀번호 수정하는 API입니다.")
//    @GetMapping
//    public Response<MemberInfoResponse> myInfo(Authentication authentication){
//        Member member = myPageService.myInfo(((CustomUserDetails) authentication.getPrincipal()).getId());
//
//        return Response.success("본인 정보를 성공적으로 가져옴(임시)", MemberInfoResponse.fromMember(member));
//    }
//
//
//    @Operation(summary = "내 정보 조회", description = "내 정보를 조회하는 API입니다.")
//    @GetMapping
//    public Response<MemberInfoResponse> myInfo(Authentication authentication){
//        Member member = myPageService.myInfo(((CustomUserDetails) authentication.getPrincipal()).getId());
//
//        return Response.success("본인 정보를 성공적으로 가져옴(임시)", MemberInfoResponse.fromMember(member));
//    }







}
