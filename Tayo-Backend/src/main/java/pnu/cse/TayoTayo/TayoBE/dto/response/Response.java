package pnu.cse.TayoTayo.TayoBE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Response<T> {

    private String result;
    private String message;
    private T data;

    public static <T> Response<T> error(String message, T data){
        return new Response<>("ERROR", message , data);
    }

    public static Response<Void> success(String message){
        return new Response<Void>("SUCCESS", message,null);
    }

    public static <T> Response<T> success(String message,T data){
        return new Response<>("SUCCESS", message ,data);
    }

}
