/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atm.gob.ec.reportesxlsx;

/**
 *
 * @author erik.flores
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import atm.gob.ec.encriptacion.Encriptador;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.Date;
import java.util.Properties;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * An advanced Java program that exports data from any table to Excel file.
 *
 * @author Nam Ha Minh (C) Copyright codejava.net
 */
public class ReporteXLS {

    //public void export(String strNombreArchivo, String strFecInicio, String strFecFin) {
    public void export(Properties propertie, String... params ) throws Exception {
        
        // String jdbcURL = propertie.getProperty("DB.MYSQLURL") + "://" + propertie.getProperty("DB.MYSQLSERVER") + ":" + propertie.getProperty("DB.MYSQLPORT") + "/" + propertie.getProperty("DB.MYSQLDATABASE");
        String jdbcURL = propertie.getProperty("DB.MYSQLURL");
        
        String username = propertie.getProperty("DB.MYSQLUSER");
        String password = Encriptador.decriptar(propertie.getProperty("DB.MYSQLPASSWD"));
        
        String nombreArchivo = params[0];
        
        Connection connection = DriverManager.getConnection(jdbcURL, username, password); 
            
        String sqlReporte = propertie.getProperty("SQL.Q1");
        
        XSSFWorkbook workbook = new XSSFWorkbook();

        PreparedStatement preparedStatement = connection.prepareStatement(sqlReporte);
        //Statement statement = connection.createStatement();

        XSSFSheet sheet = workbook.createSheet(params[1]);

        ResultSet result2 = preparedStatement.executeQuery();
        //ResultSet result2 = statement.executeQuery(sqlReporte);

        writeHeaderLine(result2, sheet);
        writeDataLines(result2, workbook, sheet);
        FileOutputStream outputStream = new FileOutputStream(nombreArchivo);
        workbook.write(outputStream);

        outputStream = new FileOutputStream(nombreArchivo);
        workbook.write(outputStream);

        preparedStatement.close();
        //statement.close();
        workbook.close();
        outputStream.close();
        result2.close();
        
    }

    private void writeHeaderLine(ResultSet result, XSSFSheet sheet) throws SQLException {
        // write header line containing column names
        ResultSetMetaData metaData = result.getMetaData();
        int numberOfColumns = metaData.getColumnCount();

        Row headerRow = sheet.createRow(0);

        // exclude the first column which is the ID field
        for (int i = 1; i <= numberOfColumns; i++) {
            String columnName = metaData.getColumnName(i);
            Cell headerCell = headerRow.createCell(i - 1);
            headerCell.setCellValue(columnName);
        }
    }

    private void writeDataLines(ResultSet result, XSSFWorkbook workbook, XSSFSheet sheet)
            throws SQLException {
        ResultSetMetaData metaData = result.getMetaData();
        int numberOfColumns = metaData.getColumnCount();

        int rowCount = 1;

        while (result.next()) {
            Row row = sheet.createRow(rowCount++);

            for (int i = 1; i <= numberOfColumns; i++) {
                Object valueObject = result.getObject(i);

                Cell cell = row.createCell(i - 1);

                if (valueObject instanceof Boolean) {
                    cell.setCellValue((Boolean) valueObject);
                } else if (valueObject instanceof Double) {
                    cell.setCellValue((double) valueObject);
                } else if (valueObject instanceof Float) {
                    cell.setCellValue((float) valueObject);
                } else if (valueObject instanceof BigDecimal) {
                    cell.setCellValue(valueObject.toString());
                } else if (valueObject instanceof Long) {
                    cell.setCellValue((long) valueObject);
                } else if (valueObject instanceof Integer) {
                    cell.setCellValue((int) valueObject);
                } else if (valueObject instanceof Date) {
                    cell.setCellValue((Date) valueObject);
                    formatDateCell(workbook, cell);
                } else {
                    cell.setCellValue((String) valueObject);
                }
            }
        }
    }

    private void formatDateCell(XSSFWorkbook workbook, Cell cell) {
        CellStyle cellStyle = workbook.createCellStyle();
        CreationHelper creationHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
        cell.setCellStyle(cellStyle);
    }
    
}

