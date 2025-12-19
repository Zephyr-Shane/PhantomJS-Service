package com.dzy.peqa.utils;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * PhantomJS 工具类，用于执行 PhantomJS 脚本并处理执行结果
 *
 * @author Zephyr.Shane
 * @version 2025/06/26 11:00
 **/
public class PhantomjsUtil {
    private static final Logger logger = LoggerFactory.getLogger(PhantomjsUtil.class);

    private final String phantomjsPath;
    private final String scriptPath;
    private final String[] args;
    private int timeoutSeconds = 30; // 默认超时时间30秒

    private PhantomjsUtil(String phantomjsPath, String scriptPath, String[] args) {
        this.phantomjsPath = phantomjsPath;
        this.scriptPath = scriptPath;
        this.args = args != null ? args.clone() : new String[0];
    }

    /**
     * 执行PhantomJS脚本
     * @return 执行结果
     */
    public ExecutionResult execute() {
        List<String> command = new ArrayList<>();
        command.add(phantomjsPath);
        command.add(scriptPath);
        command.addAll(Arrays.asList(args));

        logger.info("执行PhantomJS命令: {}", command);

        Process process = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            // 继承当前进程的环境变量
            processBuilder.inheritIO();
            process = processBuilder.start();

            // 启动异步线程读取输出流，防止进程阻塞
            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "STDOUT");
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "STDERR");
            outputGobbler.start();
            errorGobbler.start();

            // 等待进程执行，支持超时处理
            boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!completed) {
                // 超时则销毁进程
                process.destroyForcibly();
                logger.warn("PhantomJS执行超时，已强制终止");
                return new ExecutionResult(false, -1,
                        "执行超时，已强制终止",
                        "执行超时，已强制终止");
            }

            int exitCode = process.exitValue();
            String stdOutput = outputGobbler.getOutput();
            String errorOutput = errorGobbler.getOutput();

            logger.info("PhantomJS执行完成，退出码: {}, 成功: {}", exitCode, exitCode == 0);
            return new ExecutionResult(exitCode == 0, exitCode, stdOutput, errorOutput);
        } catch (Exception e) {
            logger.error("执行PhantomJS脚本失败", e);
            throw new RuntimeException("执行PhantomJS脚本失败: " + e.getMessage(), e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 设置执行超时时间
     * @param timeoutSeconds 超时时间(秒)
     * @return 当前实例
     */
    public PhantomjsUtil setTimeout(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    /**
     * 流处理器，异步读取进程输出流
     */
    private static class StreamGobbler extends Thread {
        private final InputStream inputStream;
        private final String streamType;
        private final StringBuilder output = new StringBuilder();

        StreamGobbler(InputStream inputStream, String streamType) {
            this.inputStream = inputStream;
            this.streamType = streamType;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    logger.debug("{}: {}", streamType, line);
                }
            } catch (IOException e) {
                logger.error("读取{}流失败", streamType, e);
            }
        }

        String getOutput() {
            return output.toString();
        }
    }

    public static PhantomjsUtilBuilder builder() {
        return new PhantomjsUtilBuilder();
    }

    public static class PhantomjsUtilBuilder {
        private String phantomjsPath;
        private String scriptPath;
        private String[] args;
        private int timeoutSeconds = 30;

        public PhantomjsUtilBuilder phantomjsPath(String phantomjsPath) {
            this.phantomjsPath = phantomjsPath;
            return this;
        }

        public PhantomjsUtilBuilder scriptPath(String scriptPath) {
            this.scriptPath = scriptPath;
            return this;
        }

        public PhantomjsUtilBuilder args(String... args) {
            this.args = args;
            return this;
        }

        /**
         * 设置执行超时时间
         * @param timeoutSeconds 超时时间(秒)
         * @return 当前构建器
         */
        public PhantomjsUtilBuilder setTimeout(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public PhantomjsUtil build() {
            // 严格验证参数
            if (phantomjsPath == null || phantomjsPath.trim().isEmpty()) {
                throw new IllegalArgumentException("phantomjsPath 不能为空");
            }
            if (scriptPath == null || scriptPath.trim().isEmpty()) {
                throw new IllegalArgumentException("scriptPath 不能为空");
            }
            // 检查PhantomJS可执行文件是否存在
            File phantomjsFile = new File(phantomjsPath);
            if (!phantomjsFile.exists() || !phantomjsFile.canExecute()) {
                throw new IllegalStateException("PhantomJS可执行文件不存在或不可执行: " + phantomjsPath);
            }
            // 检查脚本文件是否存在
            File scriptFile = new File(scriptPath);
            if (!scriptFile.exists() || !scriptFile.canRead()) {
                throw new IllegalStateException("脚本文件不存在或不可读: " + scriptPath);
            }

            PhantomjsUtil util = new PhantomjsUtil(phantomjsPath, scriptPath, args);
            util.timeoutSeconds = timeoutSeconds;
            return util;
        }
    }

    @Data
    public static class ExecutionResult {
        private final boolean success;
        private final int exitCode;
        private final String stdOutput;
        private final String errorOutput;

        /**
         * 获取第一个错误行
         */
        public String getFirstError() {
            if (errorOutput == null || errorOutput.isEmpty()) {
                return "";
            }
            String[] lines = errorOutput.split("\n");
            for (String line : lines) {
                if (line.contains("Error") || line.contains("Exception") || line.contains("err")) {
                    return line;
                }
            }
            return errorOutput;
        }
    }
}