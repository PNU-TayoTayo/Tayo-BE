package pnu.cse.TayoTayo.TayoBE.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ApplicationException extends RuntimeException {

    private ErrorCode errorCode;
    private String message;
    private LocalDateTime timestamp;


    public ApplicationException(ErrorCode errorCode){
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
        this.timestamp = LocalDateTime.now();
    }
}

