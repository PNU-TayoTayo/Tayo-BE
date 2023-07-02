package pnu.cse.TayoTayo.TayoBE.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pnu.cse.TayoTayo.TayoBE.dto.request.MemberJoinRequest;
import pnu.cse.TayoTayo.TayoBE.dto.response.Response;
import pnu.cse.TayoTayo.TayoBE.dto.response.UserJoinResponse;
import pnu.cse.TayoTayo.TayoBE.model.entity.Member;
import pnu.cse.TayoTayo.TayoBE.service.MemberService;

@Tag(name = "tayo-api", description = "타요타요 API")
@RestController
@RequestMapping("/tayo/auth")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     회원 가입 API
     */

    @Operation(summary = "타요타요 서비스 회원가입", description = "해당 API를 사용해서 타요타요 서비스 회원가입이 가능합니다")
    //@Parameter(name = "str", description = "2번 반복할 문자열")
    @PostMapping("/join")
    public Response<UserJoinResponse> join(@RequestBody MemberJoinRequest request){

        Member member = memberService.join(request);

        return Response.success("성공적으로 회원가입 완료",UserJoinResponse.fromMember(member));

    }




}
