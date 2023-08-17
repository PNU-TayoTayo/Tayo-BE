package pnu.cse.TayoTayo.TayoBE.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pnu.cse.TayoTayo.TayoBE.dto.response.Response;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<?> applicationHandler(ApplicationException e){
        log.error("Error occurs : {}", e.toString());

        Map<String,Object> data = new HashMap<>();
        data.put("status", e.getErrorCode().getStatus());
        data.put("errorCode", e.getErrorCode().getErrorCode());
        data.put("timestamp", e.getTimestamp());

        return ResponseEntity.status(e.getErrorCode().getStatus())
                .body(Response.error(e.getErrorCode().getMessage(),data));
    }

//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<?> applicationHandler(RuntimeException e){
//        log.error("Error occurs {}", e.toString());
//        log.error(e.getMessage());
//        log.error(e.getLocalizedMessage());
//        log.error(String.valueOf(e.getStackTrace()));
//
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(Response.error("Internal Server Error",ErrorCode.INTERNAL_SERVER_ERROR.name()));
//
//    }


}
