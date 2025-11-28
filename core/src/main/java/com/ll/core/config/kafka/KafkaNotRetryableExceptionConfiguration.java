package com.ll.core.config.kafka;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.ll.core.model.exception.BaseException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;

@Configuration
public class KafkaNotRetryableExceptionConfiguration {
    public static final Class<? extends Exception>[] NOT_RETRYABLE_EXCEPTIONS = new Class[]{
            BaseException.class,
            ValidationException.class,
            MethodArgumentNotValidException.class,
            HandlerMethodValidationException.class,
            MissingServletRequestParameterException.class,
            MissingRequestHeaderException.class,
            NoResourceFoundException.class,
            IllegalArgumentException.class,
            JsonParseException.class,

            ConstraintViolationException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalStateException.class,
            EntityNotFoundException.class,
            JsonProcessingException.class,
            AccessDeniedException.class
    };
}
