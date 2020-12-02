package com.github.johnsonmoon.java.ftp.server.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileReader;

/**
 * Create by johnsonmoon at 2018/11/28 15:55.
 */
@Api("TEXT-trans-controller")
@RestController
@RequestMapping("/text")
public class TextTransformController {
    private static Logger logger = LoggerFactory.getLogger(TextTransformController.class);

    @ApiOperation("Get file content text.")
    @GetMapping("/show")
    public String listFiles(@RequestParam("filePath") String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return "[" + filePath + "] not exists.";
        }
        if (!file.isFile()) {
            return "[" + filePath + "] is not a file.";
        }
        StringBuilder stringBuilder = new StringBuilder();
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            char[] buffer = new char[32];
            int length;
            while ((length = reader.read(buffer)) > 0) {
                stringBuilder.append(buffer, 0, length);
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
        return stringBuilder.toString();
    }
}
