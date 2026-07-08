package com.ivan.erp.shared.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import java.time.Duration;

@Configuration
public class WebPerformanceConfig implements WebMvcConfigurer {

    private final int staticCacheDays;

    public WebPerformanceConfig(@Value("${app.performance.static-cache-days:30}") int staticCacheDays) {
        this.staticCacheDays = staticCacheDays;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        CacheControl staticCache = CacheControl
                .maxAge(Duration.ofDays(staticCacheDays))
                .cachePublic();

        addVersionedHandler(registry.addResourceHandler("/css/**"), staticCache, "classpath:/static/css/");
        addVersionedHandler(registry.addResourceHandler("/js/**"), staticCache, "classpath:/static/js/");
        addVersionedHandler(registry.addResourceHandler("/images/**"), staticCache, "classpath:/static/images/");
        addVersionedHandler(registry.addResourceHandler("/webjars/**"), staticCache, "classpath:/META-INF/resources/webjars/");
    }

    private void addVersionedHandler(ResourceHandlerRegistration registration, CacheControl cacheControl, String location) {
        registration
                .addResourceLocations(location)
                .setCacheControl(cacheControl)
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver())
                .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"))
                .addResolver(new PathResourceResolver());
    }
}
