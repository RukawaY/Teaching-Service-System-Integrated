package com.Main.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.Main")
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private ResourceAppConfig resourceAppConfig;

    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("file:" + resourceAppConfig.getUploadPath() + "/")
                .setCachePeriod(3600);
    }
}
