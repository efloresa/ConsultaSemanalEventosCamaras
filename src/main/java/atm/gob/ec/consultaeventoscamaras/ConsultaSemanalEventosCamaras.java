/**
 *
 * @author erik.flores
 */

package atm.gob.ec.consultaeventoscamaras;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atm.gob.ec.mail.SendMail;
import atm.gob.ec.reportesxlsx.ReporteXLS;
import atm.gob.ec.security.AesCryptoService;
import atm.gob.ec.security.CryptoService;
import atm.gob.ec.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.SimpleDateFormat;

import java.util.Locale;
import java.util.Properties;

public class ConsultaSemanalEventosCamaras {
    
    LoggerContext context = Utils.configureLogging();
    static Logger logger = LogManager.getLogger(ConsultaSemanalEventosCamaras.class); 
    String directorioSistema = Utils.getDirectorioSistema();
    static final Properties propertie = Utils.getProperties();
    String dirLog4j2;
    String fechaReporte, emailBody; 
    File archivoEx = null;
    FileOutputStream outputStream = null;
    String strPattern = "[^A-Za-z0-9.+()'@:%/]";
    //static SimpleDateFormat formato = new SimpleDateFormat("dd-MMMMM-yyyy HH:mm:ss", new Locale("es", "ES"));    
    static SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd", new Locale("es", "ES"));    
    private static String secret = System.getProperty("atm.crypto.key");
            
    public ConsultaSemanalEventosCamaras() throws Exception {
        // super();      
        
    }
    
    private static Connection conectar() throws Exception {
        CryptoService crypto = new AesCryptoService(secret);      
        return DriverManager.getConnection(propertie.getProperty("DB.MYSQLURL"), crypto.decrypt(propertie.getProperty("DB.MYSQLUSER")), crypto.decrypt(propertie.getProperty("DB.MYSQLPASSWD")));
    }

    public void verificaEventos()  {
                
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result2 = null;
        
        String strFechaInicio = "";
        String strFechaFin = "";        
        
        logger.info("Proceso de verificacion"); 
        
        emailBody = propertie.getProperty("MAIL.BODY") ;
        
        String excelFilePath = "";
        
        try {
            
            connection = conectar();
            
            String strSentencia = "select DATE((sysdate() - interval 7 day) + INTERVAL ( - WEEKDAY((sysdate() - interval 7 day))) DAY) fecha_inicio, DATE((sysdate() - interval 7 day) + INTERVAL (6 - WEEKDAY((sysdate() - interval 7 day))) DAY) fecha_fin";
            preparedStatement = connection.prepareStatement(strSentencia);
            
            result2 = preparedStatement.executeQuery();
            
            while (result2.next()) {                
                strFechaInicio = result2.getString("fecha_inicio");
                strFechaFin = result2.getString("fecha_fin");
            
                logger.info("Periodo: " + strFechaInicio + " al " + strFechaFin);

                String strSentencia2 = "select (@row_number:=@row_number + 1) AS item, grupo_camara, location_id, camara_serial, direccion, lunes, martes, miercoles, jueves, viernes, sabado, domingo "
                		+ "from ( select coalesce((select UPPER(gc.descripcion) from fotoradar_atm.grupo_camaras gc join fotoradar_atm.detalle_grupo_camaras dgc on dgc.id_grupo = gc.id where dgc.location_id = df.location_id and dgc.estado = 'A' ), 'METROVIA') grupo_camara "
                		+ ", df.location_id "
                		+ ", df.serial camara_serial "
                		+ ", (select l.name from wd.locations as l where l.id = df.location_id ) direccion "
                		+ ", coalesce(lunes,0) lunes, coalesce(martes,0) martes, coalesce(miercoles,0) miercoles, coalesce(jueves,0) jueves, coalesce(viernes,0) viernes, coalesce(sabado,0) sabado, coalesce(domingo,0) domingo "
                		+ "from fotoradar_atm.datos_fotoradar df "
                		+ "left join ( select vcm.location_id "
                		+ ", sum(CASE WHEN dia_semana = 2 THEN cantidad_eventos ELSE 0 END) lunes "
                		+ ", sum(CASE WHEN dia_semana = 3 THEN cantidad_eventos ELSE 0 END) martes "
                		+ ", sum(CASE WHEN dia_semana = 4 THEN cantidad_eventos ELSE 0 END) miercoles "
                		+ ", sum(CASE WHEN dia_semana = 5 THEN cantidad_eventos ELSE 0 END) jueves "
                		+ ", sum(CASE WHEN dia_semana = 6 THEN cantidad_eventos ELSE 0 END) viernes "
                		+ ", sum(CASE WHEN dia_semana = 7 THEN cantidad_eventos ELSE 0 END) sabado "
                		+ ", sum(CASE WHEN dia_semana = 1 THEN cantidad_eventos ELSE 0 END) domingo "
                		+ "from cameras.v_cameras_messages vcm "
                		+ "where vcm.fecha between '" + strFechaInicio + "' AND '" + strFechaFin + "' "
                		+ "group by location_id ) cm on (df.location_id = cm.location_id) "
                		+ "where df.location_id is not null "
                		+ "and df.estado = 'A' "
                		+ "group by location_id "
                		+ "order by grupo_camara, location_id "
                		+ ") as y, (SELECT @row_number:=0) AS t "
                		+ "order by item "
                		+ "" ;
                        
                propertie.put("SQL.Q1", strSentencia2);
                propertie.put("MAIL.SUBJECT", propertie.getProperty("MAIL.SUBJECT") + " periodo del " + strFechaInicio + " al " + strFechaFin + "");
                emailBody = emailBody + " periodo del " + strFechaInicio + " al " + strFechaFin + "<br>" ;

                excelFilePath = "Export_cameras.messages_" + strFechaInicio +"_" + strFechaFin + ".xlsx";
                //String excelFilePath = directorioSistema + "/" + getFileName("Export_".concat("cameras.messages"));
                logger.info("Archivo Excel: " + excelFilePath);

                ReporteXLS excel = new ReporteXLS();
                excel.export(propertie, excelFilePath, "messages_" + strFechaInicio +"_" + strFechaFin); 
            } 
            
        } catch (SQLException e) {
            e.printStackTrace();
            excelFilePath = "";
            emailBody = emailBody + "<br>" + e.getMessage();
            logger.warn(e);
        } catch (Exception e) {
            e.printStackTrace();
            excelFilePath = "";
            emailBody = emailBody + "<br>" + e.getMessage();
            logger.warn(e);
        } finally {
            try {
                if (result2 != null) result2.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException ex) {
                logger.warn(ex);
            }
        }

        enviaNotificacion(emailBody, excelFilePath);
        deleteExcelFile(excelFilePath);
        
        logger.info("Fin de verificacion");
        
    }
    
    private void deleteExcelFile(String ps_nombreArchivo){
        try{
            logger.info(ps_nombreArchivo);            
            
            Path fileToDeletePath = Paths.get(ps_nombreArchivo);
            Files.delete(fileToDeletePath);
            
        }catch (Exception ex){ 
            logger.warn(ex);
        }
    }
      
    private void enviaNotificacion(String ps_param, String ps_nombreArchivo){
        String mensaje = "";
        //String fechaReporte = Fecha.obtenFechaActualFormato();
        String asunto =  propertie.getProperty("MAIL.SUBJECT");
        String email;
        try{
            if (!ps_param.equals("")) 
                mensaje = ps_param;
            else 
                throw new Exception("El cuerpo del mensaje de correo no puede ser nulo");
            
            mensaje = mensaje + "<br>"; 
            mensaje = mensaje + "<br>Este es un mensaje informativo por lo que le solicitamos no responder. "; 
            mensaje = mensaje + "<br>Atentamente, "; 
            mensaje = mensaje + "<br>";             
            
            email = SendMail.send(propertie.getProperty("MAIL.TO"), propertie.getProperty("MAIL.CC"), propertie.getProperty("MAIL.BCC"), asunto, mensaje, ps_nombreArchivo);            
            
            logger.info("Notificacion por email");
            
            if (!email.equals("")){
                logger.warn(email);
                throw new Exception("Error al enviar notificacion por correo: " + email);
            }
            
        }catch(Exception e){
            logger.warn(e);
        }finally{
            ;
        }
    }
    
    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        
        ConsultaSemanalEventosCamaras evento = new ConsultaSemanalEventosCamaras();
        
        logger.info("Inicio"); 
        evento.verificaEventos();
        logger.info("Fin");
        
    }

}
