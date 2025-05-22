package notfound.ballog.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**")
                .allowedOrigins("https://k12a404.p.ssafy.io",
                                "http://k12a404.p.ssafy.io")
                .allowedMethods("OPTIONS", "GET", "POST", "PATCH", "DELETE");
    }

}