package com.github.johnsonmoon.java.ftp.server.controller;

import com.github.johnsonmoon.java.ftp.server.common.ext.FtpException;
import com.github.johnsonmoon.java.ftp.server.entity.FileDefine;
import com.github.johnsonmoon.java.ftp.server.entity.FileDefineTypeEnum;
import com.github.johnsonmoon.java.ftp.server.entity.param.UploadFileParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.Collator;
import java.util.*;

/**
 * Create by johnsonmoon at 2018/11/28 15:55.
 */
@Api("FTP-controller")
@RestController
public class FtpController {
    private static Logger logger = LoggerFactory.getLogger(FtpController.class);

    private static String ROOT_DIR_FLAG = "root";
    private static String ROOT_DIR = "/";

    @Value("${ftp.basic.dir.control}")
    private Boolean ftpBasicDirControl;
    @Value("${ftp.basic.dir.path}")
    private String ftpBasicDirPath;

    @PostConstruct
    public void ftpBasicDirCheck() {
        if (Boolean.TRUE.equals(ftpBasicDirControl)) {
            ROOT_DIR = ftpBasicDirPath;
        }
    }

    private String ftpBasicDirParse(String dir) {
        String parsedDir = dir;
        if (Boolean.TRUE.equals(ftpBasicDirControl)) {
            if (dir.startsWith(ftpBasicDirPath)) {
                parsedDir = dir;
            } else if (ROOT_DIR.startsWith(dir)) {
                parsedDir = ROOT_DIR;
            } else {
                parsedDir = ftpBasicDirPath + File.separator + dir;
            }
        }
        return parsedDir.replace("//", "/");
    }

    @ApiOperation("Set ")
    @GetMapping("/dir/root")
    public String setRootDir(@RequestParam(value = "dir", required = false) String dir) {
        if (dir == null || dir.isEmpty()) {
            return ROOT_DIR;
        }
        dir = ftpBasicDirParse(dir);
        File file = new File(dir);
        if (!file.exists()) {
            throw new FtpException("Dir not exists!");
        }
        if (!file.isDirectory()) {
            throw new FtpException(String.format("%s is not a directory!", dir));
        }
        ROOT_DIR = dir;
        return "Root directory set [" + ROOT_DIR + "].";
    }

    @ApiOperation("List files of directory.")
    @GetMapping("/list")
    public List<FileDefine> listFiles(@RequestParam("dir") String dir) {
        if (dir == null || dir.isEmpty()) {
            throw new FtpException("Dir must not be null!");
        }
        dir = ftpBasicDirParse(dir);
        if (dir.equals(ROOT_DIR_FLAG)) {
            dir = ROOT_DIR;
        }
        File file = new File(dir);
        if (!file.exists()) {
            throw new FtpException("Dir not exists!");
        }
        if (!file.isDirectory()) {
            throw new FtpException(String.format("%s is not a directory!", dir));
        }
        File[] files = file.listFiles();
        List<FileDefine> fileDefines = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                FileDefine fileDefine = new FileDefine();
                fileDefine.setName(f.getName());
                fileDefine.setPath(f.getPath());
                fileDefine.setAbsolutePath(f.getAbsolutePath());
                if (f.isDirectory()) {
                    fileDefine.setType(FileDefineTypeEnum.DIRECTORY.getType());
                } else if (f.isFile()) {
                    fileDefine.setType(FileDefineTypeEnum.FILE.getType());
                } else {
                    continue;
                }
                fileDefine.setSize(f.length());
                fileDefines.add(fileDefine);
            }
        }
        return basicSort(fileDefines);
    }

    private List<FileDefine> basicSort(List<FileDefine> fileDefines) {
        fileDefines.sort(new Comparator<FileDefine>() {
            @Override
            public int compare(FileDefine o1, FileDefine o2) {
                Comparator comparator = Collator.getInstance(Locale.CHINESE);
                return ((Collator) comparator).compare(o1.getName(), o2.getName());
            }
        });
        final List<FileDefine> dirs = new LinkedList<>();
        final List<FileDefine> files = new LinkedList<>();
        final List<FileDefine> hiddenFiles = new LinkedList<>();
        for (FileDefine file : fileDefines) {
            if (file.getName().startsWith(".")) {
                hiddenFiles.add(file);
                continue;
            }
            String type = file.getType();
            if (type.equals(FileDefineTypeEnum.DIRECTORY.getType())) {
                dirs.add(file);
                continue;
            }
            if (type.equals(FileDefineTypeEnum.FILE.getType())) {
                files.add(file);
                continue;
            }
        }
        List<FileDefine> sorted = new LinkedList<>();
        sorted.addAll(hiddenFiles);
        sorted.addAll(dirs);
        sorted.addAll(files);
        return sorted;
    }

    @ApiOperation("Upload file.")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public Boolean uploadFile(UploadFileParam uploadFileParam) {
        if (uploadFileParam.getName() == null || uploadFileParam.getName().isEmpty()
                || uploadFileParam.getTargetDir() == null || uploadFileParam.getTargetDir().isEmpty()
                || uploadFileParam.getFile() == null) {
            throw new FtpException("Name or targetDir or file is null!");
        }
        String targetDir = ftpBasicDirParse(uploadFileParam.getTargetDir());
        File dir = new File(targetDir);
        if (!dir.exists()) {
            throw new FtpException("TargetDir not exists!");
        }
        if (!dir.isDirectory()) {
            throw new FtpException(String.format("%s is not a directory!", dir));
        }
        String filePathName = targetDir + File.separator + uploadFileParam.getName();
        File newFile = new File(filePathName);
        if (newFile.exists()) {
            if (!newFile.delete()) {
                throw new FtpException("Old file in target directory exist with same fileName and could not be deleted.");
            }
        }
        try {
            if (!newFile.createNewFile()) {
                throw new FtpException("Create new file failed.");
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new FtpException("Create new file failed.");
        }
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = uploadFileParam.getFile().getInputStream();
            outputStream = new FileOutputStream(newFile);
            byte[] bytes = new byte[32];
            int length;
            while ((length = inputStream.read(bytes)) > 0) {
                outputStream.write(bytes, 0, length);
            }
            outputStream.flush();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new FtpException("Upload file failed.");
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
        return true;
    }

    @ApiOperation("Download file.")
    @GetMapping("/download")
    public void downloadFile(@RequestParam("file") String file,
                             HttpServletResponse httpServletResponse) {
        if (file == null || file.isEmpty()) {
            throw new FtpException("File path name is null!");
        }
        file = ftpBasicDirParse(file);
        File fileF = new File(file);
        if (!fileF.exists()) {
            throw new FtpException("File not exists!");
        }
        FileInputStream fileInputStream = null;
        OutputStream outputStream = null;
        httpServletResponse.setHeader("Content-Type", "application/octet-stream");
        httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + fileF.getName());
        try {
            fileInputStream = new FileInputStream(fileF);
            outputStream = httpServletResponse.getOutputStream();
            byte[] bytes = new byte[32];
            int length;
            while ((length = fileInputStream.read(bytes)) > 0) {
                outputStream.write(bytes, 0, length);
            }
            outputStream.flush();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new FtpException(e.getMessage(), e);
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
    }

    @ApiOperation("Move file into target directory")
    @GetMapping("/move")
    public Boolean moveFile(@RequestParam("file") String file,
                            @RequestParam("targetDir") String targetDir) {
        if (file == null || file.isEmpty() || targetDir == null || targetDir.isEmpty()) {
            throw new FtpException("file or targetDir is null!");
        }
        file = ftpBasicDirParse(file);
        targetDir = ftpBasicDirParse(targetDir);
        File f = new File(file);
        File dir = new File(targetDir);
        if (!f.exists() || !dir.exists()) {
            throw new FtpException("file not exist or targetDir not exist.");
        }
        if (!f.isFile() || !dir.isDirectory()) {
            throw new FtpException("file is not a file or targetDir is not directory.");
        }
        File newFile = new File(dir.getPath() + File.separator + f.getName());
        if (newFile.exists()) {
            if (newFile.delete()) {
                throw new FtpException("Old file in target directory exist with same fileName and could not be deleted.");
            }
        }
        try {
            if (!newFile.createNewFile()) {
                throw new FtpException("Create new file failed.");
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new FtpException("Create new file failed.");
        }
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(f);
            outputStream = new FileOutputStream(newFile);
            byte[] bytes = new byte[32];
            int length;
            while ((length = inputStream.read(bytes)) > 0) {
                outputStream.write(bytes, 0, length);
            }
            outputStream.flush();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new FtpException("Move file failed.");
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
        return f.delete();
    }

    @ApiOperation("Rename file")
    @GetMapping("/rename")
    public Boolean renameFile(@RequestParam("file") String file,
                              @RequestParam("targetFile") String targetFile) {
        if (file == null || file.isEmpty() || targetFile == null || targetFile.isEmpty()) {
            throw new FtpException("file or targetDir is null!");
        }
        file = ftpBasicDirParse(file);
        targetFile = ftpBasicDirParse(targetFile);
        File f = new File(file);
        File targetF = new File(targetFile);
        if (!f.exists()) {
            throw new FtpException("file not exist.");
        }
        if (!f.isFile()) {
            throw new FtpException("file is not a file or targetDir is not directory.");
        }
        if (targetF.exists()) {
            throw new FtpException("targetFile already exist.");
        }
        try {
            if (!targetF.createNewFile()) {
                throw new FtpException("Create new file failed.");
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new FtpException("Create new file failed.");
        }
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(f);
            outputStream = new FileOutputStream(targetF);
            byte[] bytes = new byte[32];
            int length;
            while ((length = inputStream.read(bytes)) > 0) {
                outputStream.write(bytes, 0, length);
            }
            outputStream.flush();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new FtpException("Move file failed.");
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
        return f.delete();
    }

    @ApiOperation("Delete file.")
    @GetMapping("/delete")
    public Boolean deleteFile(@RequestParam("file") String file) {
        if (file == null || file.isEmpty()) {
            throw new FtpException("file is null!");
        }
        file = ftpBasicDirParse(file);
        File f = new File(file);
        if (!f.exists()) {
            throw new FtpException("file not exist.");
        }
        if (!f.isFile()) {
            throw new FtpException("file is not a file.");
        }
        return f.delete();
    }
}
