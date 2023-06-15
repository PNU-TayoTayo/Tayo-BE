package pnu.cse.TayoTayo.TayoBE.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("TayoTayo project API 명세")
                .version("v0.0.1")
                .description("부산대학교 졸업과제 타요타요 팀 API 명세입니다.");

        return new OpenAPI()
                .components(new Components())
                .info(info);
    }

}