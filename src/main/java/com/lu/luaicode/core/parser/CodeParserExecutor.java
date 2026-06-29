package com.lu.luaicode.core.parser;

import com.lu.luaicode.exception.BusinessException;
import com.lu.luaicode.exception.ResultCode;
import com.lu.luaicode.exception.ThrowUtils;
import com.lu.luaicode.model.enums.CodeGenTypeEnum;

/**
 * ClassName: CodeParserExecutor
 * Package: com.lu.luaicode.core.parser
 * Description:
 *
 * @Author Dopamine
 * @Create 2026/6/29 16:32
 * @Version 1.0
 */
public class CodeParserExecutor {

    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();

    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ResultCode.PARAM_ERROR, "不支持的代码类型");
        };
    }






}
