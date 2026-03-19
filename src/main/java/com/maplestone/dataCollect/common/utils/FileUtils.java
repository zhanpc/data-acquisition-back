package com.maplestone.dataCollect.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author hmx
 * @Description: 文件操作工具类
 * @date 2022/4/25 9:43
 */

public class FileUtils {

    /**
     * 读取字节流
     */
    public static <T extends OutputStream> void readFile(T stream, File file, InputStream inputStream)
            throws Exception {
        InputStream in;
        if (inputStream == null) {
            in = new FileInputStream(file);
        } else {
            in = inputStream;
        }

        int bytesRead = 0;
        byte[] buffer = new byte[2048];
        while ((bytesRead = in.read(buffer)) != -1) {
            stream.write(buffer, 0, bytesRead);
        }
        stream.flush();
        in.close();
    }

    /**
     * 通过url地址读取文件流
     */
    public static InputStream getFileStream(String url) throws Exception {
        InputStream is = null;
        if (url.startsWith("http")) {
            URL httpUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) httpUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5 * 1000);
            is = connection.getInputStream();
        } else {
            is = new FileInputStream(url);
        }
        return is;
    }

    /**
     * 输出文件
     */
    public static void writeFromStream(InputStream inputStream, String realPath) throws Exception {
        FileOutputStream out = new FileOutputStream(realPath);
        byte[] buffer = new byte[1024];
        int pos;
        while ((pos = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, pos);
            out.flush();
        }
        inputStream.close();
        out.close();
    }

    /**
     * 输出文件
     */
    public static void outPutFile(MultipartFile file, String path) throws IOException {
        InputStream in = file.getInputStream();
        FileOutputStream out = new FileOutputStream(path);
        byte[] buffer = new byte[1024];
        int pos;
        while ((pos = in.read(buffer)) != -1) {
            out.write(buffer, 0, pos);
            out.flush();
        }
        in.close();
        out.close();
    }

    /**
     * 获取文件后缀（带点）
     */
    public static String getFileSuffix(String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        }
        int startIndex = str.lastIndexOf(".");
        if (startIndex == -1) {
            return "";
        }
        return str.substring(startIndex);
    }

    /**
     * 获取文件后缀（不带点）
     */
    public static String getFileSuffixNotDot(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        int startIndex = str.lastIndexOf(".");
        if (startIndex == -1) {
            return null;
        }
        return str.substring(startIndex + 1);
    }

    /**
     * 获取文件名称（不带点）
     */
    public static String getFileNameNotDot(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        int startIndex = str.lastIndexOf(".");
        if (startIndex == -1) {
            return null;
        }
        // 修正截取时少首字符
        return str.substring(0, startIndex);
        // return str.substring(1,startIndex );
    }

    /**
     * 文件路径获取文件名称
     */
    public static String getFileNameByPath(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        int startIndex = str.lastIndexOf("/");
        if (startIndex == -1) {
            startIndex = str.lastIndexOf("'\'");
            if (startIndex == -1) {
                startIndex = 0;
            }
        }
        return str.substring(startIndex + 1);
    }

    /**
     * 文件重命名
     */
    public boolean fileRename(String filePath, String fileName, String newName) {
        String newFileName = filePath + newName;
        return new File(filePath + fileName).renameTo(new File(newFileName));
    }

    public static void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * file文件转muiltipartFile
     */
    public static MultipartFile fileToMultipartFile(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        return new MockMultipartFile(file.getName(), file.getName(),
                ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);
    }

    /**
     * base64转file
     *
     * @param base64
     * @param filePath
     * @return
     */
    public static File base64ToFile(String base64, String filePath) {
        File file = new File(filePath);
        byte[] buffer;
        try {
            BASE64Decoder base64Decoder = new BASE64Decoder();
            buffer = base64Decoder.decodeBuffer(base64);
            FileOutputStream out = new FileOutputStream(filePath);
            out.write(buffer);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * File 转 Base64
     *
     * @param filePath
     * @return
     */
    public static String fileToBase64(String filePath) {
        File file = new File(filePath);
        FileInputStream inputFile;
        try {
            inputFile = new FileInputStream(file);
            byte[] buffer = new byte[inputFile.available()];
            inputFile.read(buffer);
            inputFile.close();
            BASE64Encoder base64Encoder = new BASE64Encoder();
            return base64Encoder.encode(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
