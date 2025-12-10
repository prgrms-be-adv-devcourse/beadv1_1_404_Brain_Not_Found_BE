package com.ll.core.config.swagger;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "swagger")
@Getter
@Setter
public class SwaggerProperties {
    private List<String> servers;
}

