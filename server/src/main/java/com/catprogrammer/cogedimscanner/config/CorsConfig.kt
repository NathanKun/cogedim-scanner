package com.catprogrammer.cogedimscanner.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport

@Configuration
open class CorsConfig : WebMvcConfigurationSupport() {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
                .allowedHeaders("*")
                .allowedMethods("GET", "POST")
                .allowedOrigins("localhost", "cogedimscanner.catprogrammer.com")
                .allowCredentials(true)
    }
}
