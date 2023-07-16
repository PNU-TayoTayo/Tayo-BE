package pnu.cse.TayoTayo.TayoBE.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;
import pnu.cse.TayoTayo.TayoBE.model.entity.MemberEntity;

import java.util.Date;

@Component
public class JWTProvider {
    public static final Long EXP = 1000L * 60 * 60 * 48; // 48시간 - 테스트 하기 편함.
    public static final String TOKEN_PREFIX = "Bearer "; // 스페이스 필요함
    public static final String HEADER = "Authorization";
    public static final String SECRET = "TayoKey";

    public static String createAccessToken(MemberEntity member) {
        // 토큰은 데이터 담을 수 있음 (민감한 정보는 넣으면 x)
        String jwt = JWT.create()
                .withSubject(member.getEmail()) // jwt의 이름 설정
                .withExpiresAt(new Date(System.currentTimeMillis() + EXP))
                .withClaim("id", member.getId())
                .withClaim("email", member.getEmail())
                .withClaim("role", member.getRole().toString())
                .sign(Algorithm.HMAC512(SECRET));
        return TOKEN_PREFIX + jwt;
    }

//    public static String createRefreshToken(MemberEntity member, String AccessToken) {
//        return JWT.create()
//                .withSubject(member.getEmail())
//                .withExpiresAt(new Date(System.currentTimeMillis()+ 60000*100)) // accessToken보다 훨씬 길게
//                .withClaim("AccessToken", AccessToken)
//                .withClaim("email", member.getEmail())
//                .sign(Algorithm.HMAC512(SECRET));
//    }

    public static DecodedJWT verify(String jwt) throws SignatureVerificationException, TokenExpiredException {
        jwt = jwt.replace(JWTProvider.TOKEN_PREFIX, "");
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(SECRET))
                .build().verify(jwt);
        return decodedJWT;
    }

}