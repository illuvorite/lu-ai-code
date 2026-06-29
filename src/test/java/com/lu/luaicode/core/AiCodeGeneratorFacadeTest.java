package com.lu.luaicode.core;

import com.lu.luaicode.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClassName: AiCodeGeneratorFacadeTest
 * Package: com.lu.luaicode.core
 * Description:
 *
 * @Author Dopamine
 * @Create 2026/6/29 15:05
 * @Version 1.0
 */
@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;


    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("帮我生成一个登录页面，代码限制为50行", CodeGenTypeEnum.HTML,1L);
        assertNotNull(file);
    }
}