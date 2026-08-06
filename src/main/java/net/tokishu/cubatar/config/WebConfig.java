package net.tokishu.cubatar.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Открытый CORS на чтение, как у публичных аватар-сервисов (Crafatar и
 * т.п.) - чтобы скины и рендеры можно было использовать с чужих сайтов в
 * canvas/WebGL (без заголовка cross-origin картинка "заражает" canvas и
 * WebGL отказывается брать её текстурой).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET");
    }
}
