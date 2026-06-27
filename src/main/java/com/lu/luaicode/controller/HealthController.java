package com.lu.luaicode.controller;

import com.lu.luaicode.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Tag(name = "健康检查", description = "用于检查服务是否正常运行")
public class HealthController {

    @GetMapping("/")
    @Operation(summary = "健康检查接口")
    public Result<String> healthCheck() {
        return Result.success();
    }
}
