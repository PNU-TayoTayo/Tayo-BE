package pnu.cse.TayoTayo.TayoBE.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Tayo-000", "Internal Room server error"),

    SAME_EMAIL(HttpStatus.CONFLICT, "Tayo-001", "이미 가입한 이메일입니다."),
    EMAIL_STRUCTURE(HttpStatus.FORBIDDEN,"Tayo-002","이메일 형식으로 작성해주세요"),
    PASSWORD_TERM(HttpStatus.FORBIDDEN,"Tayo-003","ex : 패스워드에 영문, 숫자, 특수문자가 포함되어야하고 공백이 포함될 수 없습니다."),
    PASSWORD_LENGTH(HttpStatus.FORBIDDEN,"Tayo-004","ex : 패스워드의 길이가 8~20자여야 합니다."),
    PASSWORD_MISMATCH(HttpStatus.CONFLICT,"Tayo-011","현재 비밀번호가 일치하지 않습니다."),
    NEWPASSWORD_MISMATCH(HttpStatus.CONFLICT,"Tayo-012","새 비밀번호랑 새 비밀번호 확인이 일치하지 않습니다.");




    private HttpStatus status;
    private String errorCode;
    private String message;
}

