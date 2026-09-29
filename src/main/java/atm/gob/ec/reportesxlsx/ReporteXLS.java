/**
 *
 * @author erik.flores
 */

package atm.gob.ec.reportesxlsx;

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

import atm.gob.ec.security.AesCryptoService;
import atm.gob.ec.security.CryptoService;

/**
 * An advanced Java program that exports data from any table to Excel file.
 *
 * @author Nam Ha Minh (C) Copyright codejava.net
 */
public class ReporteXLS {

    private static final String SECRET_KEY_PROPERTY = "atm.crypto.key";

    public void export(Properties properties, String... params) throws Exception {
        // Validar parámetros mínimos obligatorios (Nombre de archivo y Nombre de hoja)
        if (params == null || params.length < 2) {
            throw new IllegalArgumentException("Se requieren al menos 2 parámetros: [0] Nombre de archivo, [1] Nombre de hoja.");
        }

        String nombreArchivo = params[0];
        String nombreHoja = params[1];

        String secret = System.getProperty(SECRET_KEY_PROPERTY);
        CryptoService crypto = new AesCryptoService(secret);      
        
        String jdbcURL = properties.getProperty("DB.MYSQLURL");
        String username = crypto.decrypt(properties.getProperty("DB.MYSQLUSER"));
        String password = crypto.decrypt(properties.getProperty("DB.MYSQLPASSWD"));
        String sqlReporte = properties.getProperty("SQL.Q1");

        // Uso de try-with-resources para garantizar el cierre seguro de conexiones y flujos
        try (
            Connection connection = DriverManager.getConnection(jdbcURL, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(sqlReporte);
            ResultSet resultSet = preparedStatement.executeQuery();
            XSSFWorkbook workbook = new XSSFWorkbook();
            FileOutputStream outputStream = new FileOutputStream(nombreArchivo)
        ) {
            XSSFSheet sheet = workbook.createSheet(nombreHoja);

            // Crear un único estilo reutilizable para las fechas (Evita saturar el límite de POI)
            CellStyle dateCellStyle = createDateCellStyle(workbook);

            writeHeaderLine(resultSet, sheet);
            writeDataLines(resultSet, workbook, sheet, dateCellStyle);

            // Escribir al archivo una sola vez
            workbook.write(outputStream);
        } // Se cierran automáticamente Connection, PreparedStatement, ResultSet, XSSFWorkbook y FileOutputStream
    }

    private void writeHeaderLine(ResultSet result, XSSFSheet sheet) throws SQLException {
        ResultSetMetaData metaData = result.getMetaData();
        int numberOfColumns = metaData.getColumnCount();

        Row headerRow = sheet.createRow(0);

        for (int i = 1; i <= numberOfColumns; i++) {
            String columnName = metaData.getColumnName(i);
            Cell headerCell = headerRow.createCell(i - 1);
            headerCell.setCellValue(columnName);
        }
    }

    private void writeDataLines(ResultSet result, XSSFWorkbook workbook, XSSFSheet sheet, CellStyle dateCellStyle)
            throws SQLException {
        ResultSetMetaData metaData = result.getMetaData();
        int numberOfColumns = metaData.getColumnCount();

        int rowCount = 1;

        while (result.next()) {
            Row row = sheet.createRow(rowCount++);

            for (int i = 1; i <= numberOfColumns; i++) {
                Object valueObject = result.getObject(i);
                Cell cell = row.createCell(i - 1);

                if (valueObject == null) {
                    cell.setCellValue(""); // Manejo seguro de nulos
                } else if (valueObject instanceof Boolean) {
                    cell.setCellValue((Boolean) valueObject);
                } else if (valueObject instanceof Double) {
                    cell.setCellValue((Double) valueObject);
                } else if (valueObject instanceof Float) {
                    cell.setCellValue((Float) valueObject);
                } else if (valueObject instanceof BigDecimal) {
                    // Convertir a double para que Excel lo reconozca como número y permita sumarizar
                    cell.setCellValue(((BigDecimal) valueObject).doubleValue());
                } else if (valueObject instanceof Long) {
                    cell.setCellValue((Long) valueObject);
                } else if (valueObject instanceof Integer) {
                    cell.setCellValue((Integer) valueObject);
                } else if (valueObject instanceof Date) {
                    cell.setCellValue((Date) valueObject);
                    cell.setCellStyle(dateCellStyle); // Se reutiliza el estilo pre-creado
                } else {
                    cell.setCellValue(valueObject.toString());
                }
            }
        }
    }

    private CellStyle createDateCellStyle(XSSFWorkbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        CreationHelper creationHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
        return cellStyle;
    }
}
