package pnu.cse.TayoTayo.TayoBE.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pnu.cse.TayoTayo.TayoBE.config.security.CustomUserDetails;
import pnu.cse.TayoTayo.TayoBE.dto.response.NotificationsResponse;
import pnu.cse.TayoTayo.TayoBE.dto.response.Response;
import pnu.cse.TayoTayo.TayoBE.service.NotificationService;


@Tag(name = "tayo-api", description = "타요타요 API")
@RestController
@RequestMapping("/tayo/notification")
@RequiredArgsConstructor
public class NotificationController{

    private final NotificationService notificationService;

    @Operation(summary = "타요타요 서비스 알림 조회 API", description = "로그인 해서 접속을 하면 해당 API를 사용해서 알림 조회를 합니다.")
    @GetMapping
    public Response<NotificationsResponse> getNotifications(Authentication authentication){

        NotificationsResponse response = notificationService.getNotifications(((CustomUserDetails) authentication.getPrincipal()).getId());

        return Response.success("성공적으로 알림 조회에 성공하셨습니다", response);
    }



}
