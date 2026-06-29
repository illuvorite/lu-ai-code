package com.lu.luaicode.core.saver;

import cn.hutool.core.util.StrUtil;
import com.lu.luaicode.ai.model.HtmlCodeResult;
import com.lu.luaicode.exception.BusinessException;
import com.lu.luaicode.exception.ResultCode;
import com.lu.luaicode.model.enums.CodeGenTypeEnum;

/**
 * HTML代码文件保存器
 *
 * @author yupi
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        // 保存 HTML 文件
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
    }

    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        // HTML 代码不能为空
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "HTML代码内容不能为空");
        }
    }
}
