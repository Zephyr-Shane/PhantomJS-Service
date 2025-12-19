package com.dzy.peqa.service;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import com.dzy.peqa.utils.ImageUtils;
import com.dzy.peqa.utils.PhantomjsUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * GenOptionImage
 *
 * @author Zephyr.Shane
 * @version 2025/07/01 09:20
 **/
@Slf4j
@Component
public class GenOptionImage {

    @Resource
    private MyAppConfig myAppConfig;

    /**
     * option生成图片
     *
     * @param echartsOption
     * @return
     */
    public String execute(String echartsOption) {
        String pngName = UUID.randomUUID() + ".png";
        String tempPngName = UUID.randomUUID() + ".png";
        String outputPath = myAppConfig.getOutputPath();
        FileUtil.writeString(echartsOption, outputPath + tempPngName, "utf-8");
        String optionName = UUID.randomUUID() + ".txt";
        String optionPath = myAppConfig.getOptionTempTxtPath();

        //4. 生成TXT文档，将echartsOption写入文件中
        FileUtil.writeString(echartsOption, optionPath + optionName, "utf-8");

        // 5. 启动phantomjs 生成图片
        PhantomjsUtil.ExecutionResult result = PhantomjsUtil.builder()
                .phantomjsPath(myAppConfig.getPhantomjsPath())
                .scriptPath(myAppConfig.getScriptPath())
                .args("-txtPath", optionPath + optionName,
                        "-picTmpPath", outputPath + tempPngName,
                        "-picPath", outputPath + pngName)
                .setTimeout(60) // 设置60秒超时
                .build()
                .execute();


        if (result.isSuccess()) {
            log.info("Phantomjs命令执行成功!");
        } else {
            log.error("执行失败，退出码: " + result.getExitCode());
            log.error("错误信息: " + result.getErrorOutput());
        }

        // 根据临时图片将图片转为 base64
        log.info("图片转换开始...");
        //转换base64
        String imageBase64;
        imageBase64 = ImageUtils.convertImageToBase64Str(outputPath + pngName);
        System.out.println(imageBase64);
        log.info("图片生成成功!");
        FileUtil.del(outputPath + tempPngName);
        FileUtil.del(outputPath + pngName);
        FileUtil.del(optionPath + optionName);
        return imageBase64;
    }

}
