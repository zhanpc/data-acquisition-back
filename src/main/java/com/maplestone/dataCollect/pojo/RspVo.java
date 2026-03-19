package com.maplestone.dataCollect.pojo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.maplestone.dataCollect.common.constant.HttpConst;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.List;

/**
 * 请求返回对象
 * 
 * @author: hmx
 * @create: 2020-02-27 15:41
 */

@Data
@Builder
public class RspVo {

    // 状态码
    private Integer code;
    // 数据
    private Object data;
    // 提示信息
    private String msg;
    // 数量
    private long total;

    public static RspVo getStatusJoMsg(Boolean result, String success, String failed) {
        return RspVo.builder()
                .msg(result ? success : failed)
                .code(result ? HttpConst.SUCCESS : HttpConst.BAD_REQUEST).build();
    }

    public static <T> RspVo getSuccessResponseJoData(T data) {
        if (data instanceof IPage) {
            return getSuccessResponseJoDataAndTotal(((IPage) data).getRecords(), ((IPage) data).getTotal());
        } else if (data instanceof Page) {
            return getSuccessResponseJoDataAndTotal(((Page) data).getContent(), ((Page) data).getTotalElements());
        } else if (data instanceof List) {
            return getSuccessResponseJoDataAndTotal((data), ((List) data).size());
        }
        return RspVo.builder().data(data).code(HttpConst.SUCCESS).build();
    }

    public static <T> RspVo getSuccessResponseJoDataAndTotal(T data, long total) {
        return RspVo.builder().data(data).total(total).code(HttpConst.SUCCESS).build();
    }

    public static RspVo getSuccessResponseJo() {
        return RspVo.builder().code(HttpConst.SUCCESS).build();
    }

    public static RspVo getSuccessResponseJoMsg(String msg) {
        return RspVo.builder().msg(msg).code(HttpConst.SUCCESS).build();
    }

    public static RspVo getFailureResponseJoMsg(String msg) {
        return RspVo.builder().msg(msg).code(HttpConst.BAD_REQUEST).build();
    }

    public static RspVo getFailureResponseJoMsg(String msg, Integer status) {
        return RspVo.builder().msg(msg).code(status).build();
    }

    public static RspVo getIntervalServerResponseJo() {
        return RspVo.builder().msg(HttpConst.ERROR_500).code(HttpConst.INTERNAL_SERVER_ERROR).build();
    }

    public static RspVo getIntervalServerResponseJoMsg(String msg) {
        return RspVo.builder().msg(msg).code(HttpConst.INTERNAL_SERVER_ERROR).build();
    }

    /** header 响应文件流 */
    public static void setHeader(HttpServletRequest request, HttpServletResponse response, String fileName)
            throws IOException {
        String headerFileName = "";
        if (request.getHeader("User-Agent").toUpperCase().indexOf("MSIE") > 0) {
            headerFileName = URLEncoder.encode(fileName, "utf-8");
        } else {
            headerFileName = new String(fileName.getBytes("utf-8"), "ISO8859-1");
        }
        response.setHeader("Content-disposition", "attachment;filename=\"" + headerFileName + "\"");
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    /** file 响应文件流 */
    public static void response(HttpServletRequest request, HttpServletResponse response, File file, String fileName) {
        OutputStream os = null;
        try {
            response.reset();
            os = response.getOutputStream();
            setHeader(request, response, fileName);
            InputStream inputStream = new FileInputStream(file);
            byte[] bytes = new byte[2048];
            int length = 0;
            while ((length = inputStream.read(bytes)) > 0) {
                os.write(bytes, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** inputStream 响应文件流 */
    public static void response(HttpServletRequest request, HttpServletResponse response, InputStream inputStream,
            String fileName) {
        OutputStream os = null;
        try {
            response.reset();
            os = response.getOutputStream();
            setHeader(request, response, fileName);
            byte[] bytes = new byte[2048];
            int length = 0;
            while ((length = inputStream.read(bytes)) > 0) {
                os.write(bytes, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** filePath 响应文件流 */
    public static void response(HttpServletRequest request, HttpServletResponse response, String filePath,
            String fileName) {
        OutputStream os = null;
        try {
            response.reset();
            os = response.getOutputStream();
            setHeader(request, response, fileName);
            File file = new File(filePath);
            InputStream inputStream = new FileInputStream(file);
            byte[] bytes = new byte[2048];
            int length = 0;
            while ((length = inputStream.read(bytes)) > 0) {
                os.write(bytes, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
