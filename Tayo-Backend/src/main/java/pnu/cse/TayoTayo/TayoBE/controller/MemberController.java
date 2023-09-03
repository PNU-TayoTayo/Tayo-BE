package pnu.cse.TayoTayo.TayoBE.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hyperledger.indy.sdk.IndyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pnu.cse.TayoTayo.TayoBE.config.security.JWTProvider;
import pnu.cse.TayoTayo.TayoBE.dto.request.MemberRequest;
import pnu.cse.TayoTayo.TayoBE.dto.response.Response;
import pnu.cse.TayoTayo.TayoBE.dto.response.MemberResponse;
import pnu.cse.TayoTayo.TayoBE.model.Member;
import pnu.cse.TayoTayo.TayoBE.service.MemberService;

import java.util.concurrent.ExecutionException;

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
    public Response<MemberResponse> join(@RequestBody MemberRequest.MemberJoinRequest request) throws IndyException, ExecutionException, InterruptedException {
        // TODO : 이미 존재하는 회원일 때 그에 맞는 Exception 처리
        Member member = memberService.join(request);

        return Response.success("회원가입 성공", MemberResponse.fromMember(member));
    }

    /**
     *
     * 로그인 API
     */

    @Operation(summary = "타요타요 서비스 로그인", description = "해당 API를 사용해서 타요타요 서비스 로그인이 가능합니다")
    //@Parameter(name = "str", description = "2번 반복할 문자열")
    @PostMapping("/login")
    public ResponseEntity<Response<MemberResponse>> login(@RequestBody MemberRequest.MemberLoginRequest request){

        Member member = memberService.login(request);
        System.out.println(member.getJwt());

        return ResponseEntity.ok().header(JWTProvider.HEADER, member.getJwt())
                .body(Response.success("로그인 성공",MemberResponse.fromMember(member)));
    }

}
