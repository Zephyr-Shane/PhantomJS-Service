package com.dzy.peqa.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "myapp")
public class MyAppConfig {
    /**
     * 报告PDF存储桶名称
     */
    private String bucketNameForReport;
    /**
     * 存储PDF的路径
     */
    private String ossPdfPath;
    /**
     * 临时图片存储路径
     */
    private String outputPath;
    /**
     * 报告PDF文件存储路径
     */
    private String outputPdfPath;
    /**
     * 临时Option文档存储路径
     */
    private String optionTempTxtPath;
    /**
     * phantomjs路径
     */
    private String phantomjsPath;
    /**
     * phantomjs脚本路径
     */
    private String scriptPath;
}