package com.bajins.demo.poi;


import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Apache POI操作Excel对象
 * HSSF：操作Excel 2007之前版本(.xls)格式,生成的EXCEL不经过压缩直接导出，默认只处理100行数据，之前数据写入临时的XML里面
 * XSSF：操作Excel 2007及之后版本(.xlsx)格式,内存占用高于HSSF，把所有的数据放在内存中处理
 * SXSSF：从POI3.8 beta3开始支持,基于XSSF,低内存占用,专门处理大数据量(建议)。只能写(导出)不能读(导入)
 *      会把数据从内存写磁盘临时文件并压缩，当写入少量数据（1000条）时，SXSSF效率比较低
 * <p>
 * 说明:
 * .xls格式的excel(最大行数65536行,最大列数256列)
 * .xlsx格式的excel(最大行数1048576行,最大列数16384列)
 * <p>
 * 参考：https://github.com/550690513/large-amount-data-export/blob/master/src/main/java/Utils/ExcelUtil.java
 */
public class ExportExcel {
    public static final String DEFAULT_DATE_PATTERN = "yyyy年MM月dd日";// 默认日期格式
    public static final int DEFAULT_COLUMN_WIDTH = 17;// 默认列宽


    /**
     * 导出Excel(.xlsx)格式
     *
     * @param titleMap  表格标题（下标0的同时是sheet名，会合并单元格）
     * @param headMap   表格列项头信息
     * @param dataArray 数据数组
     * @param os        文件输出流
     */
    public static void exportExcelSXSSF(LinkedHashMap<String, String> titleMap, LinkedHashMap<String, String> headMap
            , List<List<Object>> dataArray, OutputStream os) throws IOException {
        String datePattern = DEFAULT_DATE_PATTERN;
        int minBytes = DEFAULT_COLUMN_WIDTH;

        /**
         * 声明一个工作薄
         */
        SXSSFWorkbook workbook = new SXSSFWorkbook(1000);// 大于1000行时会把之前的行写入硬盘
        workbook.setCompressTempFiles(true);

        // 表头1样式
        CellStyle title1Style = workbook.createCellStyle();
        title1Style.setAlignment(HorizontalAlignment.CENTER);// 水平居中
        title1Style.setVerticalAlignment(VerticalAlignment.CENTER);// 垂直居中
        Font titleFont = workbook.createFont();// 字体
        titleFont.setFontHeightInPoints((short) 20);
        titleFont.setBold(true);
        title1Style.setFont(titleFont);

        // 表头2样式
        CellStyle title2Style = workbook.createCellStyle();
        title2Style.setAlignment(HorizontalAlignment.CENTER);
        title2Style.setVerticalAlignment(VerticalAlignment.CENTER);
        title2Style.setBorderTop(BorderStyle.THIN);// 上边框
        title2Style.setBorderRight(BorderStyle.THIN);// 右
        title2Style.setBorderBottom(BorderStyle.THIN);// 下
        title2Style.setBorderLeft(BorderStyle.THIN);// 左
        Font title2Font = workbook.createFont();
        title2Font.setUnderline((byte) 1);
        title2Font.setColor(IndexedColors.BLUE.index);
        title2Style.setFont(title2Font);

        // head样式
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREEN.getIndex());// 设置颜色
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);// 前景色纯色填充
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        Font headerFont = workbook.createFont();
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setBold(true);// 字体加粗
        headerStyle.setFont(headerFont);

        // 单元格样式
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        Font cellFont = workbook.createFont();
        cellFont.setBold(true);// 字体加粗
        cellStyle.setFont(cellFont);


        String title1 = titleMap.get("title1");
        String title2 = titleMap.get("title2");

        /**
         * 生成一个(带名称)表格
         */
        SXSSFSheet sheet = workbook.createSheet(title1);
        sheet.createFreezePane(0, 3, 0, 3);// (单独)冻结前三行

        /**
         * 生成head相关信息+设置每列宽度
         */
        int[] colWidthArr = new int[headMap.size()];// 列宽数组
        String[] headKeyArr = new String[headMap.size()];// headKey数组
        String[] headValArr = new String[headMap.size()];// headVal数组
        int i = 0;
        for (Map.Entry<String, String> entry : headMap.entrySet()) {
            headKeyArr[i] = entry.getKey();
            headValArr[i] = entry.getValue();

            int bytes = headKeyArr[i].getBytes().length;
            colWidthArr[i] = bytes < minBytes ? minBytes : bytes;
            sheet.setColumnWidth(i, colWidthArr[i] * 256);// 设置列宽
            i++;
        }

        // 生成title+head信息
        SXSSFRow title1Row = sheet.createRow(0);// title1行
        title1Row.createCell(0).setCellValue(title1);
        title1Row.getCell(0).setCellStyle(title1Style);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headMap.size() - 1));// 合并单元格

        SXSSFRow title2Row = sheet.createRow(1);// title2行
        title2Row.createCell(0).setCellValue(title2);

        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFHyperlink hyperLink = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.URL);
        hyperLink.setAddress(title2);
        title2Row.getCell(0).setHyperlink(hyperLink);// 添加超链接

        title2Row.getCell(0).setCellStyle(title2Style);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, headMap.size() - 1));// 合并单元格

        SXSSFRow headerRow = sheet.createRow(2);// head行
        for (i = 0; i < headValArr.length; i++) {
            headerRow.createCell(i).setCellValue(headValArr[i]);
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
        /**
         * 遍历数据集合，产生Excel行数据
         */
        for (i = 1; i < dataArray.size(); i++) {// 行数据
            List<Object> objects = dataArray.get(i);
            // 生成数据
            SXSSFRow dataRow = sheet.createRow(i);// 创建行
            for (int j = 0; j < objects.size(); j++) {// 列数据
                Object o = objects.get(j);
                SXSSFCell cell = dataRow.createCell(j);// 创建单元格
                String cellValue = "";

                if (o == null) {
                    cellValue = "";
                } else if (o instanceof Date) {
                    cellValue = new SimpleDateFormat(datePattern).format(o);
                    cell.setCellType(CellType._NONE);
                } else if (o instanceof Float || o instanceof Double) {
                    cellValue = new BigDecimal(o.toString()).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    cell.setCellType(CellType.NUMERIC);// 数字类型
                } else {
                    cellValue = o.toString();
                    if (cellValue.equals("true")) {
                        cellValue = "男";
                    } else if (cellValue.equals("false")) {
                        cellValue = "女";
                    }
                    cell.setCellType(CellType.STRING);// 字符串类型
                }
                cell.setCellValue(cellValue);
                cell.setCellStyle(cellStyle);
            }
        }
        try {
            workbook.write(os);
        } finally {
            if (os != null) {
                os.flush();// 刷新此输出流并强制将所有缓冲的输出字节写出
                os.close();// 关闭流
            }
            workbook.dispose();// 释放workbook所占用的所有windows资源
        }
    }
}
