package com.bajins.demo.poi;

import com.bajins.api.utils.poi.convert.ExportConvert;
import com.bajins.api.utils.poi.pojo.ExportItem;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelUtils {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Class<?> mClass = null;
    private HttpServletResponse mResponse = null;
    // 分Sheet机制：每个Sheet最多多少条数据
    private Integer mMaxSheetRecords = 10000;
    // 缓存数据格式器实例,避免多次使用反射进行实例化
    private Map<String, ExportConvert> mConvertInstanceCache = new HashMap<>();

    protected ExcelUtils() {
    }

    private ExcelUtils(Class<?> clazz) {
        this(clazz, null);
    }

    private ExcelUtils(Class<?> clazz, HttpServletResponse response) {
        this.mResponse = response;
        this.mClass = clazz;
    }

    /**
     * 用于生成本地文件
     *
     * @param clazz 实体Class对象
     * @return ExcelUtils
     */
    public static ExcelUtils builder(Class<?> clazz) {
        return new ExcelUtils(clazz);
    }

    /**
     * 用于浏览器导出
     *
     * @param clazz    实体Class对象
     * @param response 原生HttpServletResponse对象
     * @return ExcelUtils
     */
    public static ExcelUtils export(Class<?> clazz, HttpServletResponse response) {
        return new ExcelUtils(clazz, response);
    }

    /**
     * 分Sheet机制：每个Sheet最多多少条数据(默认10000)
     *
     * @param size 数据条数
     * @return this
     */
    public ExcelUtils setMaxSheetRecords(Integer size) {
        this.mMaxSheetRecords = size;
        return this;
    }

    /**
     * 导出Excel(此方式需依赖浏览器实现文件下载,故应先使用export()构造器)
     *
     * @param data      数据集合
     * @param sheetName 工作表名字
     * @return true-操作成功,false-操作失败
     */
    public boolean toExcel(List<?> data, String sheetName) {
        requiredexportParams();

        try {
            return toExcel(data, sheetName, mResponse.getOutputStream());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return false;
    }

    /**
     * 针对转换方法的默认实现(提供默认样式和文件命名规则)
     *
     * @param data      数据集合
     * @param sheetName 工作表名字
     * @param out       输出流
     * @return true-操作成功,false-操作失败
     */
    public boolean toExcel(List<?> data, String sheetName, OutputStream out) {

        return toExcel(data, sheetName, new ExportHandler() {

            @Override
            public CellStyle headCellStyle(SXSSFWorkbook wb) {
                CellStyle cellStyle = wb.createCellStyle();
                // http://poi.apache.org/apidocs/dev/org/apache/poi/ss/usermodel/CellStyle.html
                Font font = wb.createFont();
                cellStyle.setFillForegroundColor((short) 12);
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);// 填充模式
                cellStyle.setBorderTop(BorderStyle.THIN);// 上边框为细边框
                cellStyle.setBorderRight(BorderStyle.THIN);// 右边框为细边框
                cellStyle.setBorderBottom(BorderStyle.THIN);// 下边框为细边框
                cellStyle.setBorderLeft(BorderStyle.THIN);// 左边框为细边框
                cellStyle.setAlignment(HorizontalAlignment.LEFT);// 对齐
                cellStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
                cellStyle.setFillBackgroundColor(IndexedColors.AQUA.getIndex());
                font.setBold(true);// 是否为粗体
                // font.setFontHeightInPoints((short) 12);// 字体大小
                font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
                // 应用标题字体到标题样式
                cellStyle.setFont(font);
                return cellStyle;
            }

            @Override
            public String exportFileName(String sheetName) {
                return String.format("导出-%s-%s", sheetName, System.currentTimeMillis());
            }
        }, out);
    }

    public boolean toExcel(List<?> data, String sheetName, ExportHandler handler, OutputStream out) {
        requiredbuilderParams();
        if (data == null || data.isEmpty()) {
            return false;
        }
        // 导出列查询。
        ExportConfig currentExportConfig;
        ExportItem currentExportItem;
        List<ExportItem> exportItems = new ArrayList<>();
        for (Field field : mClass.getDeclaredFields()) {

            currentExportConfig = field.getAnnotation(ExportConfig.class);
            if (currentExportConfig != null) {
                currentExportItem = new ExportItem().setField(field.getName())
                        .setDisplay("field".equals(currentExportConfig.value()) ? field.getName()
                                : currentExportConfig.value())
                        .setWidth(currentExportConfig.width()).setConvert(currentExportConfig.convert())
                        .setColor(currentExportConfig.color()).setReplace(currentExportConfig.replace());
                exportItems.add(currentExportItem);
            }

        }

        // 创建新的工作薄。
        SXSSFWorkbook wb = POIUtils.newSXSSFWorkbook();

        double sheetNo = Math.ceil((double) data.size() / mMaxSheetRecords);// 取出一共有多少个sheet.

        // =====多sheet生成填充数据=====
        int index = 0;
        while (index <= (sheetNo == 0.0 ? sheetNo : sheetNo - 1)) {
            SXSSFSheet sheet = POIUtils.newSXSSFSheet(wb, sheetName + (index == 0 ? "" : "_" + index));

            // 创建表头
            SXSSFRow headerRow = POIUtils.newSXSSFRow(sheet, 0);
            for (int i = 0; i < exportItems.size(); i++) {
                SXSSFCell cell = POIUtils.newSXSSFCell(headerRow, i);
                POIUtils.setColumnWidth(sheet, i, exportItems.get(i).getWidth(), exportItems.get(i).getDisplay());
                cell.setCellValue(exportItems.get(i).getDisplay());

                CellStyle style = handler.headCellStyle(wb);
                if (style != null) {
                    cell.setCellStyle(style);
                }
            }

            SXSSFRow bodyRow;
            String cellValue;
            SXSSFCell cell;
            CellStyle style = wb.createCellStyle();
            Font font = wb.createFont();
            style.setFont(font);

            // 产生数据行
            if (!data.isEmpty()) {
                int startNo = index * mMaxSheetRecords;
                int endNo = Math.min(startNo + mMaxSheetRecords, data.size());

                int i = startNo;
                while (i < endNo) {
                    bodyRow = POIUtils.newSXSSFRow(sheet, i + 1 - startNo);
                    for (int j = 0; j < exportItems.size(); j++) {
                        // 处理单元格值
                        cellValue = exportItems.get(j).getReplace();
                        if ("".equals(cellValue)) {
                            try {
                                cellValue = BeanUtils.getProperty(data.get(i), exportItems.get(j).getField());
                            } catch (Exception e) {
                                log.error(e.getMessage());
                            }
                        }

                        // 格式化单元格值
                        if (!"".equals(exportItems.get(j).getConvert())) {
                            cellValue = convertCellValue(cellValue, exportItems.get(j).getConvert());
                        }

                        // 单元格宽度
                        POIUtils.setColumnWidth(sheet, j, exportItems.get(j).getWidth(), cellValue);

                        cell = POIUtils.newSXSSFCell(bodyRow, j);
                        // fix: 当值为“”时,当前index的cell会失效
                        cell.setCellValue("".equals(cellValue) ? null : cellValue);
                        cell.setCellStyle(style);
                    }
                    i++;
                }
            }
            index++;
        }

        try {
            // 生成Excel文件并下载.(通过response对象是否为空来判定是使用浏览器下载还是直接写入到output中)
            POIUtils.writeByLocalOrBrowser(mResponse, handler.exportFileName(sheetName), wb, out);
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }

        return true;
    }

    public boolean toCsv(List<?> data, String path) {
        try {
            requiredbuilderParams();
            if (data == null || data.isEmpty()) {
                return false;
            }

            // 导出列查询。
            ExportConfig currentExportConfig;
            ExportItem currentExportItem;
            List<ExportItem> exportItems = new ArrayList<>();
            for (Field field : mClass.getDeclaredFields()) {

                currentExportConfig = field.getAnnotation(ExportConfig.class);
                if (currentExportConfig != null) {
                    currentExportItem = new ExportItem().setField(field.getName())
                            .setDisplay("field".equals(currentExportConfig.value()) ? field.getName()
                                    : currentExportConfig.value())
                            .setConvert(currentExportConfig.convert()).setReplace(currentExportConfig.replace());
                    exportItems.add(currentExportItem);
                }

            }

            String cellValue;
            FileOutputStream out = new FileOutputStream(path);
            // 解决乱码
            out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            //初始化csvformat
            CSVFormat formator = CSVFormat.DEFAULT.withRecordSeparator("\n");//CSV文件分隔符

            //创建FileWriter对象
            FileWriter fileWriter = new FileWriter(path);

            //创建CSVPrinter对象
            CSVPrinter printer = new CSVPrinter(fileWriter, formator);

            String[] csvHeadersArr = exportItems.stream().map(ExportItem::getDisplay).toArray(String[]::new);
            // 写入列头数据
            /**
             * 最后一个参数传入new  Object[]{}抑制以下警告
             * Warning:(306, 37) java: 最后一个参数使用了不准确的变量类型的 varargs 方法的非 varargs 调用;
             * 对于 varargs 调用, 应使用 java.lang.Object
             * 对于非 varargs 调用, 应使用 java.lang.Object[], 这样也可以抑制此警告
             */
            printer.printRecord(csvHeadersArr, new Object[]{});
            for (Object aData : data) {
                List<Object> csvContent = new ArrayList<>();
                for (ExportItem exportItem : exportItems) {
                    // 处理单元格值
                    cellValue = exportItem.getReplace();
                    if (!StringUtils.isNotBlank(cellValue)) {
                        cellValue = BeanUtils.getProperty(aData, exportItem.getField());
                    }

                    // 格式化单元格值
                    if (StringUtils.isNotBlank(exportItem.getConvert())) {
                        cellValue = convertCellValue(cellValue, exportItem.getConvert());
                    }
                    csvContent.add(cellValue);
                }
                String[] csvContentArr = csvContent.toArray(new String[0]);
                /**
                 * 最后一个参数传入new  Object[]{}抑制以下警告
                 * Warning:(306, 37) java: 最后一个参数使用了不准确的变量类型的 varargs 方法的非 varargs 调用;
                 * 对于 varargs 调用, 应使用 java.lang.Object
                 * 对于非 varargs 调用, 应使用 java.lang.Object[], 这样也可以抑制此警告
                 */
                printer.printRecord(csvContentArr, new Object[]{});
            }
            printer.flush();
            printer.close();
            fileWriter.close();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    // convertCellValue: number to String
    private String convertCellValue(Object oldValue, String format) {
        try {
            String protocol = format.split(":")[0];

            // 键值对字符串解析：s:1=男,2=女
            if ("s".equalsIgnoreCase(protocol)) {

                String[] pattern = format.split(":")[1].split(",");
                for (String p : pattern) {
                    String[] cp = p.split("=");
                    if (cp[0].equals(oldValue)) {
                        return cp[1];
                    }
                }

            }
            if ("c".equalsIgnoreCase(protocol)) {

                String clazz = format.split(":")[1];
                ExportConvert export = mConvertInstanceCache.get(clazz);
                if (export == null) {
                    export = (ExportConvert) Class.forName(clazz).newInstance();
                    mConvertInstanceCache.put(clazz, export);
                }

                if (mConvertInstanceCache.size() > 10)
                    mConvertInstanceCache.clear();

                return export.handler(oldValue);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return String.valueOf(oldValue);
    }

    private void requiredbuilderParams() {
        if (mClass == null) {
            throw new IllegalArgumentException("请先使用com.bajins.wechatutil.poi.ExcelUtils.builder(Class<?>)构造器初始化参数。");
        }
    }

    private void requiredexportParams() {
        if (mClass == null || mResponse == null) {
            throw new IllegalArgumentException(
                    "请先使用com.bajins.wechatutil.poi.ExcelUtils.export(Class<?>, HttpServletResponse)构造器初始化参数。");
        }

    }


    /**
     * 读取.xlsx 内容
     *
     * @param inInputStream文件流
     * @return java.util.List<java.util.ArrayList < java.lang.String>>
     */
    public static List<ArrayList<String>> readXlsx(InputStream in) throws IOException {
        List<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
        XSSFWorkbook xwb = new XSSFWorkbook(in);
        // 创建文档
        ArrayList<String> rowList = null;
        int totoalRows = 0;// 总行数
        int totalCells = 0;// 总列数
        // 读取sheet(页)
        for (int sheetIndex = 0; sheetIndex < xwb.getNumberOfSheets(); sheetIndex++) {
            XSSFSheet xssfSheet = xwb.getSheetAt(sheetIndex);

            if (xssfSheet == null) {
                continue;
            }
            totoalRows = xssfSheet.getLastRowNum();
            // 读取row
            for (int rowIndex = 0; rowIndex <= totoalRows; rowIndex++) {
                XSSFRow xssfRow = xssfSheet.getRow(rowIndex);

                if (xssfRow == null) {
                    continue;
                }
                rowList = new ArrayList<String>();
                totalCells = xssfRow.getLastCellNum();

                // 读取列
                for (int cellIndex = 0; cellIndex < totalCells; cellIndex++) {
                    XSSFCell xssfCell = xssfRow.getCell(cellIndex);
                    if (xssfCell == null) {
                        rowList.add("");
                    } else {
                        xssfCell.setCellType(CellType.STRING);
                        rowList.add(String.valueOf(xssfCell.getStringCellValue()));
                    }
                }
                list.add(rowList);
            }
        }

        return list;
    }

    /**
     * 读取 .xls内容
     *
     * @param inInputStream文件流
     * @return java.util.List<java.util.ArrayList < java.lang.String>>
     */
    public static List<ArrayList<String>> readXls(InputStream in) throws IOException {
        List<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
        HSSFWorkbook hwb = new HSSFWorkbook(in);
        ArrayList<String> rowList = null;
        int totoalRows = 0;// 总行数
        int totalCells = 0;// 总列数
        // 读取sheet(页)
        for (int sheetIndex = 0; sheetIndex < hwb.getNumberOfSheets(); sheetIndex++) {
            HSSFSheet hssfSheet = hwb.getSheetAt(sheetIndex);

            if (hssfSheet == null) {
                continue;
            }

            totoalRows = hssfSheet.getLastRowNum();
            // 读取row
            for (int rowIndex = 0; rowIndex <= totoalRows; rowIndex++) {
                HSSFRow hssfRow = hssfSheet.getRow(rowIndex);
                if (hssfRow == null) {
                    continue;
                }
                rowList = new ArrayList<String>();
                totalCells = hssfRow.getLastCellNum();

                // 读取列
                for (int cellIndex = 0; cellIndex < totalCells; cellIndex++) {
                    HSSFCell hssfCell = hssfRow.getCell(cellIndex);
                    if (hssfCell == null) {
                        rowList.add("");
                    } else {
                        hssfCell.setCellType(CellType.STRING);
                        rowList.add(String.valueOf(hssfCell.getStringCellValue()));
                    }
                }
                list.add(rowList);
            }
        }
        return list;
    }

    /**
     * 获取文件类型
     *
     * @param path
     * @return
     */
    public static String getPostfix(String path) {
        if (StringUtils.isBlank(path) || !path.contains(".")) {
            return null;
        }
        return path.substring(path.lastIndexOf(".") + 1, path.length()).trim();
    }

    /**
     * 传入输入流和文件全名称获取Excel中的内容
     *
     * @param in       输入流
     * @param fileName 文件名称
     * @return List<ArrayList < String>>
     * @throws IOException
     * @author: https://www.bajins.com
     * @date: 2019年3月21日 下午9:16:20
     */
    public static List<ArrayList<String>> readExcelInputStream(InputStream in, String fileName) throws IOException {
        String postfix = getPostfix(fileName);
        if (StringUtils.isBlank(postfix)
                || (!StringUtils.equals("xlsx", postfix) && !StringUtils.equals("xls", postfix))) {
            throw new IllegalArgumentException("文件名称错误，文件类型找不到");
        }
        // 读取文件内容
        if (StringUtils.equals("xlsx", postfix)) {
            return readXlsx(in);
        } else if (StringUtils.equals("xls", postfix)) {
            return readXls(in);
        }
        return null;
    }
}
