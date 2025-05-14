package cn.i0v0p.datasource;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DatasourceTools {
    public static Map<String, List<String>> getSchema(String url, String user, String password, String schemaName) throws SQLException {
        Map<String, List<String>> schema = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             ResultSet tables = conn.getMetaData().getTables(null, schemaName, null, new String[]{TABLE_TYPE.TABLE.name()})) {
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                List<String> columns = new ArrayList<>();
                try (ResultSet columnsRs = conn.getMetaData().getColumns(null, null, tableName, null)) {
                    while (columnsRs.next()) {
                        columns.add(columnsRs.getString("COLUMN_NAME") +
                                " (" + columnsRs.getString("TYPE_NAME") +
                                (columnsRs.getInt("COLUMN_SIZE") > 0 ? "(" + columnsRs.getInt("COLUMN_SIZE") + "))" : ")"));
                    }
                }
                schema.put(tableName, columns);
            }
        }
        return schema;
    }


    public static Map<String, Map<String, String>> compareSchemas(Map<String, List<String>> primaryDatabaseSource, Map<String, List<String>> secondaryDatabaseSource) {
        Map<String, Map<String, String>> differences = new HashMap<>();

        // 遍历主要库的表
        for (Map.Entry<String, List<String>> primaryEntry : primaryDatabaseSource.entrySet()) {
            String tableName = primaryEntry.getKey();//表名
            List<String> primaryColumns = primaryEntry.getValue();//字段集合

            // 获取次要库中对应表
            List<String> secondaryColumns = secondaryDatabaseSource.get(tableName);

            // 次要库中没有该表
            if (secondaryColumns == null) {
                differences.put(tableName, Collections.singletonMap("差异", "次要库中没有该表"));
                continue;
            }

            String completed = "";
            // 比较字段
            for (String primaryColumn : primaryColumns) {
                String[] primaryParts = primaryColumn.split("\\s+", 2);
                String primaryColumnName = primaryParts[0]; // 字段名
                String primaryColumnType = primaryParts[1]; // 字段类型（可能包含长度）

                boolean found = false;
                secondaryColumns.remove(completed);
                for (String secondaryColumn : secondaryColumns) {
                    String[] secondaryParts = secondaryColumn.split("\\s+", 2);
                    String secondaryColumnName = secondaryParts[0]; // 字段名
                    String secondaryColumnType = secondaryParts[1]; // 字段类型（可能包含长度）

                    if (primaryColumnName.equals(secondaryColumnName)) {
                        found = true;
                        if (!primaryColumnType.equals(secondaryColumnType)) {
                            // 字段类型不同，记录差异
                            differences.putIfAbsent(tableName, new HashMap<>());
                            differences.get(tableName).put(primaryColumnName, "主库类型: " + primaryColumnType + ", 次库类型: " + secondaryColumnType);
                        }
                        completed = secondaryColumn;
                        break;
                    }
                }
                // 次要库中没有该字段
                if (!found) {
                    differences.putIfAbsent(tableName, new HashMap<>());
                    differences.get(tableName).put(primaryColumnName, "次要库中没有该字段");
                }
            }
        }

        return differences;
    }


    public static void generateExcelReport(Map<String, List<String>> differences) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("表结构差异");

        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("表名");
        headerRow.createCell(1).setCellValue("差异");

        for (Map.Entry<String, List<String>> entry : differences.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue().toString());
        }

        try (FileOutputStream fileOut = new FileOutputStream("表结构差异.xlsx")) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    public static String generateExcelReport2(Map<String, Map<String, String>> differences, DatabaseConfig config) {
        // 创建一个 Excel 工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("差异报告");
        String primaryDatabaseSource = config.getPrimaryDatabaseSource();
        String secondaryDatabaseSource = config.getSecondaryDatabaseSource();
        String oracleSchemaName = config.getOracleSchemaName();
        String damengSchemaName = config.getDamengSchemaName();

        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("表名");
        headerRow.createCell(1).setCellValue("字段名");
        headerRow.createCell(2).setCellValue("差异描述");
        headerRow.createCell(3).setCellValue("主库：" + primaryDatabaseSource + ";次库：" + secondaryDatabaseSource);

        // 设置表头为浅蓝色
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 设置边框
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        for (int i = 0; i < 4; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
        //设置筛选列
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 2));

        // 创建差异描述列的浅红色样式
        CellStyle differenceStyle = workbook.createCellStyle();
        differenceStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        differenceStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 设置边框
        differenceStyle.setBorderTop(BorderStyle.THIN);
        differenceStyle.setBorderBottom(BorderStyle.THIN);
        differenceStyle.setBorderLeft(BorderStyle.THIN);
        differenceStyle.setBorderRight(BorderStyle.THIN);

        // 行索引
        int rowIndex = 1;

        // 遍历差异并填充到 Excel
        for (Map.Entry<String, Map<String, String>> entry : differences.entrySet()) {
            String tableName = entry.getKey();
            Map<String, String> columnsDifferences = entry.getValue();

            for (Map.Entry<String, String> columnEntry : columnsDifferences.entrySet()) {
                String columnName = columnEntry.getKey();
                String differenceDescription = columnEntry.getValue();

                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(tableName);
                row.createCell(1).setCellValue(columnName);
                Cell cell = row.createCell(2);
                cell.setCellValue(differenceDescription);

                // 设置边框
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setBorderTop(BorderStyle.THIN);
                cellStyle.setBorderBottom(BorderStyle.THIN);
                cellStyle.setBorderLeft(BorderStyle.THIN);
                cellStyle.setBorderRight(BorderStyle.THIN);

                row.getCell(0).setCellStyle(cellStyle);
                row.getCell(1).setCellStyle(cellStyle);
                cell.setCellStyle(differenceStyle); // 应用浅红色样式和边框
            }
        }

        // 自动调整列宽，并增加一些缓冲
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000); // 增加1000个单位的宽度作为缓冲
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        // 将工作簿写入文件
        String fileName = primaryDatabaseSource + "(" + damengSchemaName + ")与" +
                secondaryDatabaseSource + "(" + oracleSchemaName + ")差异报告-" + sdf.format(new Date()) + ".xlsx";
        try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭工作簿
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }
}
