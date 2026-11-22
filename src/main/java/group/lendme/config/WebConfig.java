package group.lendme.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Every @RestController in group.lendme is served under /api/v1,
 * so controllers declare only their own path, e.g. @RequestMapping("/auth").
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    public static final String API_V1 = "/api/v1";

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(API_V1,
                HandlerTypePredicate.forBasePackage("group.lendme")
                        .and(HandlerTypePredicate.forAnnotation(RestController.class)));
    }
}
