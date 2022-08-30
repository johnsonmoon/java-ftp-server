package com.github.johnsonmoon.java.ftp.server.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;

/**
 * Create by johnsonmoon at 2018/11/28 15:55.
 */
@Api("TEXT-trans-controller")
@RestController
@RequestMapping("/text")
public class TextTransformController {
    private static Logger logger = LoggerFactory.getLogger(TextTransformController.class);

    @Value("${ftp.basic.dir.control}")
    private Boolean ftpBasicDirControl;
    @Value("${ftp.basic.dir.path}")
    private String ftpBasicDirPath;

    private String ftpBasicDirParse(String dir) {
        if (Boolean.TRUE.equals(ftpBasicDirControl)) {
            if (dir.startsWith(ftpBasicDirPath)) {
                return dir;
            } else {
                return ftpBasicDirPath + File.separator + dir;
            }
        } else {
            return dir;
        }
    }

    @ApiOperation("Get file content text.")
    @GetMapping("/show")
    public String listFiles(@RequestParam("filePath") String filePath) {
        filePath = ftpBasicDirParse(filePath);
        File file = new File(filePath);
        if (!file.exists()) {
            return "[" + filePath + "] not exists.";
        }
        if (!file.isFile()) {
            return "[" + filePath + "] is not a file.";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Show</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<pre>");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\r\n");
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
        stringBuilder.append("</pre>\n" +
                "</body>\n" +
                "</html>");
        return stringBuilder.toString();
    }
}
