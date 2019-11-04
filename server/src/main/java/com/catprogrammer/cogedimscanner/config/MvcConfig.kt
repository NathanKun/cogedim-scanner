package com.catprogrammer.cogedimscanner.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Configuration
open class MvcConfig : WebMvcConfigurer {

    /**
     * CORS
     */
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
                .allowedHeaders("*")
                .allowedMethods("GET", "POST")
                .allowedOrigins("http://localhost:4200", "https://cogedimscanner.catprogrammer.com")
                .allowCredentials(true)
    }

    /**
     * 更改jackson默认配置
     */
    @Bean
    @Primary
    open fun objectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        // 对于空的对象转json的时候不抛出错误
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        // 禁用遇到未知属性抛出异常
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        // 序列化BigDecimal时不使用科学计数法输出
        objectMapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
        // 日期和时间格式化
        val javaTimeModule = JavaTimeModule()
        javaTimeModule.addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
        javaTimeModule.addSerializer(LocalDate::class.java, LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        javaTimeModule.addSerializer(LocalTime::class.java, LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")))
        javaTimeModule.addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
        javaTimeModule.addDeserializer(LocalDate::class.java, LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        javaTimeModule.addDeserializer(LocalTime::class.java, LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")))
        objectMapper.registerModule(javaTimeModule)
        return objectMapper
    }
}
