package com.ll.products;

import com.ll.core.config.swagger.SwaggerConfig;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
@Import({SwaggerConfig.class})
public class ProductsApplication {
    public static void main(String[] args) {
//        Dotenv dotenv = Dotenv.configure()
//                .directory("./") // .env 파일 경로 설정
//                .load();
//
//        dotenv.entries().forEach(entry ->
//                System.setProperty(entry.getKey(), entry.getValue())
//        );
        SpringApplication.run(ProductsApplication.class, args);
    }

}
