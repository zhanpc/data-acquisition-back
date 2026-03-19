package com.maplestone.dataCollect.common.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author hmx
 * @Description: 表格工具类
 * @date 2022/4/26 13:43
 */

public class ExcelUtils {

    public static void main(String[] args) {
        // String filePath="C:\\Users\\hmx_m\\Desktop\\"+new Date().getTime()+".xls";
        // String[] headers={"编号","图片","图片","图片"};
        // String excelName="病害";
        // List<List<String>> rows=new ArrayList<>();
        // List<String> cells=new ArrayList<>();
        // cells.add("C:\\Users\\hmx_m\\Desktop\\20211026152127.jpg");
        // cells.add("C:\\Users\\hmx_m\\Desktop\\3.4dffcb5a.jpg");
        // cells.add("C:\\Users\\hmx_m\\Desktop\\20211026152127.jpg");
        // cells.add("C:\\Users\\hmx_m\\Desktop\\3.4dffcb5a.jpg");
        // rows.add(cells);
        // List<Integer> picColumns=new ArrayList<>();
        // picColumns.add(2);
        // picColumns.add(3);
        // try {
        // //创建Excel文件薄
        // HSSFWorkbook workbook=createPictureExc(headers,rows,picColumns);
        // //创建一个文件
        // File file=new File(filePath);
        //
        // file.createNewFile();
        // FileOutputStream stream= new FileOutputStream(file);
        // workbook.write(stream);
        // stream.close();
        //
        // } catch (Exception e) {
        // e.printStackTrace();
        // }

        String[] headers = { "基础信息", "基础信息", "基础信息", "工艺文件", "施焊记录", "施焊记录", "施焊记录", "超声波探伤", "超声波探伤", "超声波探伤", "X射线探伤",
                "X射线探伤", "X射线探伤" };
        List<List<String>> rows = new ArrayList<>();
        String[] headNum = { "0,0,0,2", "0,0,4,6", "0,0,7,9", "0,0,10,12" };
        downLoadToPath(rows, headers, headNum, "C:\\Users\\OUYANG\\Desktop\\test\\", "123.xls");

    }

    /** 解析excel文件流 */
    public static List<Map<String, Object>> importExcel(InputStream inputStream, String[] headers, String suffix)
            throws Exception {
        Workbook workbook = getWorkbook(inputStream, suffix);
        return parseExcel(workbook, headers);
    }

    /** 解析excel文件 */
    public static List<Map<String, Object>> importExcel(String filePath, String[] headers) throws Exception {
        Workbook workbook = getWorkbook(FileUtils.getFileStream(filePath), filePath);
        return parseExcel(workbook, headers);
    }

    public static Workbook getWorkbook(InputStream inputStream, String suffix) throws Exception {
        Workbook workbook = null;
        if (suffix.contains("xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
        } else {
            workbook = new HSSFWorkbook(inputStream);
        }
        return workbook;
    }

    /** 校验表头 */
    public static void checkHeader(Workbook workbook, String[] headers) throws Exception {
        Sheet sheet = workbook.getSheetAt(0);
        // 获取表头
        Row rowHeader = sheet.getRow(0);
        // 获取当前行最后单元格列号
        int lastCellNum = rowHeader.getLastCellNum();
        if (lastCellNum < headers.length) {
            throw new IllegalArgumentException("表头长度不对~");
        }
        for (int j = 0; j < headers.length; j++) {
            String key = headers[j];
            Object value = null;
            Cell cell = rowHeader.getCell(j);
            if (cell != null) {
                cell.setCellType(CellType.STRING);
                value = cell.getStringCellValue();
            }
            if (!key.equals(value)) {
                throw new IllegalArgumentException("表头内容不对~");
            }
        }
    }

    /** 解析excel */
    public static List<Map<String, Object>> parseExcel(Workbook workbook, String[] headers) throws Exception {
        List<Map<String, Object>> contentList = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(0);
        // 获取sheet中最后一行行号
        int lastRowNum = sheet.getLastRowNum();
        for (int i = 1; i <= lastRowNum; i++) {// 不取表头 从第二行开始取
            Row row = sheet.getRow(i);
            // 获取当前行最后单元格列号
            int lastCellNum = row.getLastCellNum();
            Map<String, Object> contentMap = new HashMap<>();
            for (int j = 0; j < lastCellNum; j++) {
                String key = headers[j];
                Object value = null;
                Cell cell = row.getCell(j);
                if (cell != null) {
                    cell.setCellType(CellType.STRING);
                    value = cell.getStringCellValue();
                }
                contentMap.put(key, value);
            }
            contentList.add(contentMap);
        }
        return contentList;
    }

    /**
     * 生成excel到指定位置
     */
    public static void downLoadToPath(List<List<String>> rows, String[] title, String[] headnum, String fileOutPath,
            String excelName) {
        List<String[]> titles = new ArrayList<>();
        titles.add(title);
        File newFile = new File(fileOutPath);
        if (!newFile.exists()) {
            newFile.mkdirs();
        }
        writerDownLoadToPath(rows, titles, headnum, fileOutPath + "/" + excelName);
    }

    private static void writerDownLoadToPath(List<List<String>> rows, List<String[]> title, String[] headnum,
            String fileOutPath) {
        HSSFWorkbook workbook = null;
        if (headnum == null) {
            workbook = createExcel(title.get(0), rows);
        } else {
            workbook = createExcel(title, headnum, rows);
        }

        // 将工作簿写入指定文件路径
        try (FileOutputStream fileOut = new FileOutputStream(fileOutPath)) {
            workbook.write(fileOut);
            System.out.println("Excel file has been generated at " + fileOutPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 导出excel
     * 
     * @param response
     * @param rows      查到的数据集合
     * @param title     Excel表头名称
     * @param excelName Excel表名
     * @return
     */
    public static void writerDownLoad(HttpServletRequest request, HttpServletResponse response, List<List<String>> rows,
            String[] title, String excelName) {
        List<String[]> titles = new ArrayList<>();
        titles.add(title);
        writerDownLoad(request, response, rows, titles, null, excelName);
    }

    public static void writerDownLoad(HttpServletRequest request, HttpServletResponse response, List<List<String>> rows,
            List<String[]> title, String[] headnum, String excelName) {
        try {
            HSSFWorkbook workbook = null;
            if (headnum == null) {
                workbook = createExcel(title.get(0), rows);
            } else {
                workbook = createExcel(title, headnum, rows);
            }
            // response为HttpServletResponse对象
            String fileName = excelName + System.currentTimeMillis() + ".xls";
            if (request.getHeader("User-Agent").toUpperCase().indexOf("MSIE") > 0) {
                fileName = URLEncoder.encode(fileName, "utf-8");
            } else {
                fileName = new String(fileName.getBytes("utf-8"), "ISO8859-1");
            }
            response.setContentType("application/octet-stream;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("导出失败~");
        }
    }

    /** 输出excel */
    public void expExcel(String filePath, String[] headers, List<List<String>> contentList) throws Exception {
        // 创建Excel文件薄
        HSSFWorkbook workbook = createExcel(headers, contentList);
        // 创建一个文件
        File file = new File(filePath);
        file.createNewFile();
        FileOutputStream stream = new FileOutputStream(file);
        workbook.write(stream);
        stream.close();
    }

    /** 创建excel */
    public static HSSFWorkbook createExcel(String[] title, List<List<String>> excelList) {
        // 创建Excel文件薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 创建工作表sheet
        HSSFSheet sheet = workbook.createSheet();
        // 创建第一行
        HSSFRow row = sheet.createRow(0);

        HSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);// 左右居中
        style.setVerticalAlignment(VerticalAlignment.CENTER);// 上下居中

        HSSFCell cell = null;
        for (int i = 0; i < title.length; i++) {
            cell = row.createCell(i);
            cell.setCellValue(title[i]);
            cell.setCellStyle(style);
        }
        // 追加数据
        for (int i = 0; i < excelList.size(); i++) {
            List<String> rows = excelList.get(i);
            row = sheet.createRow(i + 1);
            for (int j = 0; j < title.length; j++) {
                HSSFCell cell2 = row.createCell(j);
                cell2.setCellValue(rows.get(j));
            }
        }
        return workbook;
    }

    /** 创建excel 自定义表头 */
    public static HSSFWorkbook createExcel(List<String[]> header, String[] headnum, List<List<String>> excelList) {
        // 创建Excel文件薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 创建工作表sheet
        HSSFSheet sheet = workbook.createSheet();

        HSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);// 左右居中
        style.setVerticalAlignment(VerticalAlignment.CENTER);// 上下居中

        int headerLength = 0;
        HSSFRow row = null;
        HSSFCell cell = null;
        for (int k = 0; k < header.size(); k++) {
            row = sheet.createRow(k);
            String[] title = header.get(k);
            headerLength = title.length;
            for (int i = 0; i < title.length; i++) {
                cell = row.createCell(i);
                cell.setCellValue(title[i]);
                cell.setCellStyle(style);
            }
        }
        // 动态合并单元格
        for (int i = 0; i < headnum.length; i++) {
            String[] temp = headnum[i].split(",");
            Integer startrow = Integer.parseInt(temp[0]);
            Integer overrow = Integer.parseInt(temp[1]);
            Integer startcol = Integer.parseInt(temp[2]);
            Integer overcol = Integer.parseInt(temp[3]);
            sheet.addMergedRegion(new CellRangeAddress(startrow, overrow,
                    startcol, overcol));
        }

        // 追加数据
        for (int i = 0; i < excelList.size(); i++) {
            List<String> rows = excelList.get(i);
            row = sheet.createRow(header.size() + i);
            for (int j = 0; j < headerLength; j++) {
                HSSFCell cell2 = row.createCell(j);
                cell2.setCellValue(rows.get(j));
            }
        }
        return workbook;
    }

    public static void writerPicDownLoad(HttpServletRequest request, HttpServletResponse response,
            List<List<String>> rows, String[] title, List<Integer> picColumns, String excelName) {
        try {
            HSSFWorkbook workbook = createPictureExc(title, rows, picColumns);

            // response为HttpServletResponse对象
            String fileName = excelName + System.currentTimeMillis() + ".xls";
            if (request.getHeader("User-Agent").toUpperCase().indexOf("MSIE") > 0) {
                fileName = URLEncoder.encode(fileName, "utf-8");
            } else {
                fileName = new String(fileName.getBytes("utf-8"), "ISO8859-1");
            }
            response.setContentType("application/octet-stream;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("导出失败~");
        }
    }

    /** 创建excel */
    public static HSSFWorkbook createPictureExc(String[] title, List<List<String>> excelList,
            List<Integer> picColumns) {
        // 创建Excel文件薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 创建工作表sheet
        HSSFSheet sheet = workbook.createSheet();
        // 创建第一行
        HSSFRow row = sheet.createRow(0);
        // sheet.autoSizeColumn(0);
        // 给图片所在列固定宽度
        for (Integer c : picColumns) {
            sheet.setColumnWidth(c, 256 * 30);
        }

        HSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);// 左右居中
        style.setVerticalAlignment(VerticalAlignment.CENTER);// 上下居中

        HSSFCell cell = null;
        for (int i = 0; i < title.length; i++) {
            cell = row.createCell(i);
            cell.setCellValue(title[i]);
            cell.setCellStyle(style);
        }

        HSSFPatriarch drawingPatriarch = sheet.createDrawingPatriarch();

        // 追加数据
        for (int i = 0; i < excelList.size(); i++) {
            List<String> rows = excelList.get(i);
            row = sheet.createRow(i + 1);
            for (int j = 0; j < title.length; j++) {
                HSSFCell cell2 = row.createCell(j);
                String value = rows.get(j);
                boolean isPicture = false;
                for (Integer c : picColumns) {
                    if (j == c) {
                        isPicture = true;
                    }
                }
                if (isPicture && StringUtils.isNotBlank(value)) {
                    try {
                        row.setHeightInPoints(10 * 16);
                        HSSFClientAnchor clientAnchor = workbook.getCreationHelper().createClientAnchor();
                        clientAnchor.setRow1(i + 1);
                        clientAnchor.setCol1(j);

                        String fileSuffixNotDot = FileUtils.getFileSuffixNotDot(value);
                        InputStream inputStream = new FileInputStream(rows.get(j));
                        byte[] bytes = IOUtils.toByteArray(inputStream);

                        HSSFPicture picture = drawingPatriarch.createPicture(clientAnchor,
                                workbook.addPicture(bytes, getPictureType(fileSuffixNotDot)));
                        picture.resize(1, 1);
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    cell2.setCellValue(value);
                }
            }
        }
        return workbook;
    }

    /** 图片类型 */
    private static int getPictureType(String picType) {
        if (picType.contains(".")) {
            picType = picType.substring(picType.indexOf(".") - 1);
        }
        int res = HSSFWorkbook.PICTURE_TYPE_PICT;
        if (StringUtils.isNotBlank(picType)) {
            if ("png".equalsIgnoreCase(picType)) {
                res = HSSFWorkbook.PICTURE_TYPE_PNG;
            } else if ("jpg".equalsIgnoreCase(picType) || "jpeg".equalsIgnoreCase(picType)) {
                res = HSSFWorkbook.PICTURE_TYPE_JPEG;
            } else if ("dib".equalsIgnoreCase(picType)) {
                res = HSSFWorkbook.PICTURE_TYPE_DIB;
            } else if ("emf".equalsIgnoreCase(picType)) {
                res = HSSFWorkbook.PICTURE_TYPE_EMF;
            } else if ("wmf".equalsIgnoreCase(picType)) {
                res = HSSFWorkbook.PICTURE_TYPE_WMF;
            }
        }
        return res;
    }

}
