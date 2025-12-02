package com.ll.auth.controller;

import com.ll.auth.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/test")
@RequiredArgsConstructor
public class TestController {

    private final RedisService redisService;

    @GetMapping
    public String test() {
        return "TEST";
    }
}