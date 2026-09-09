
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template archivoEx, choose Tools | Templates
 * and open the template in the editor.
 */
package atm.gob.ec.consultaeventoscamaras;

/**
 * 
 * @author erik.flores
 * 
 * 
 */

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atm.gob.ec.encriptacion.Encriptador;

import atm.gob.ec.mail.SendMail;

import atm.gob.ec.reportesxlsx.ReporteXLS;

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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.Properties;

public class ConsultaEventosCamaras {
    
    LoggerContext context;
    static Logger logger, logger2; 
    String directorioSistema;
    Utils utl;
    String dirLog4j2;
    Properties propertie;
    String fechaReporte, emailBody; 
    File archivoEx = null;
    FileOutputStream outputStream = null;
        
    public ConsultaEventosCamaras() throws Exception {
        // TODO Auto-generated constructor stub
        super();
        
        /** 
         * OBTENER DIRECTORIO DEL SISTEMA
        */
        
        directorioSistema = Utils.getDirectorioSistema();

        propertie = Utils.getProperties();        
        
        /**
         * OBTENER LA RUTA DEL ARCHIVO log4j2.xml         
         */
        dirLog4j2 = directorioSistema + propertie.getProperty("LOG4J2.SUBDIRECTORY");
        System.out.println("Ruta LOG4J2: " + dirLog4j2);
        
        context = Utils.configureLogging();
        logger2 = LogManager.getLogger(ConsultaEventosCamaras.class);        
        
    }
    
    public void verificaEventos()  {
                
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result2 = null;
        
        String us = propertie.getProperty("DB.MYSQLUSER");
        String pw = propertie.getProperty("DB.MYSQLPASSWD");
        String driver = propertie.getProperty("DB.MYSQLDRIVER");
        //String url = propertie.getProperty("DB.MYSQLURL") + "://" + propertie.getProperty("DB.MYSQLSERVER") + ":" + propertie.getProperty("DB.MYSQLPORT") + "/" + propertie.getProperty("DB.MYSQLDATABASE");
        String url = propertie.getProperty("DB.MYSQLURL") ;
        
        String mensaje = propertie.getProperty("MAIL.BODY");
        String strFechaInicio = "";
        String strFechaFin = "";        
        
        logger2.info("Proceso de verificacion"); 
        
        emailBody = propertie.getProperty("MAIL.BODY") ;
        
        String strFechaEvento = "";
        String excelFilePath = "";
        
        try {
            
            connection = DriverManager.getConnection(url, us, Encriptador.decriptar(pw));
            
            String strSentencia = "select DATE((sysdate() - interval 7 day) + INTERVAL ( - WEEKDAY((sysdate() - interval 7 day))) DAY) fecha_inicio, DATE((sysdate() - interval 7 day) + INTERVAL (6 - WEEKDAY((sysdate() - interval 7 day))) DAY) fecha_fin";
            preparedStatement = connection.prepareStatement(strSentencia);
            
            result2 = preparedStatement.executeQuery();
            
            while (result2.next()) {                
                strFechaInicio = result2.getString("fecha_inicio");
                strFechaFin = result2.getString("fecha_fin");
            
                logger2.info("Periodo: " + strFechaInicio + " al " + strFechaFin);

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
                logger2.info("Archivo Excel: " + excelFilePath);

                ReporteXLS excel = new ReporteXLS();
                excel.export(propertie, excelFilePath, "messages_" + strFechaInicio +"_" + strFechaFin); 
            } 
            
        } catch (SQLException e) {
            e.printStackTrace();
            excelFilePath = "";
            emailBody = emailBody + "<br>" + e.getMessage();
            logger2.warn(e);
        } catch (Exception e) {
            e.printStackTrace();
            excelFilePath = "";
            emailBody = emailBody + "<br>" + e.getMessage();
            logger2.warn(e);
        } finally {
            try {
                if (result2 != null) result2.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException ex) {
                logger2.warn(ex);
            }
        }

        enviaNotificacion(emailBody, excelFilePath);
        //deleteExcelFile(excelFilePath);
        
        logger2.info("Fin de verificacion");
        
    }
    
    private void deleteExcelFile(String ps_nombreArchivo){
        try{
            logger2.info(ps_nombreArchivo);            
            
            Path fileToDeletePath = Paths.get(ps_nombreArchivo);
            Files.delete(fileToDeletePath);
            
        }catch (Exception ex){ 
            logger2.warn(ex);
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
            
            if (!ps_nombreArchivo.equals("")){
                email = SendMail.sendWithAttachments(
                        propertie.getProperty("MAIL.SERVER"),
                        propertie.getProperty("MAIL.FROM"),
                        Encriptador.decriptar(propertie.getProperty("MAIL.PASS")),
                        propertie.getProperty("MAIL.PORT"),
                        propertie.getProperty("MAIL.TO"),
                        propertie.getProperty("MAIL.CC"),
                        propertie.getProperty("MAIL.BCC"),
                        asunto,
                        mensaje,
                        ps_nombreArchivo);
            }else{
                email = SendMail.send4(
                        propertie.getProperty("MAIL.SERVER"),
                        propertie.getProperty("MAIL.FROM"),
                        propertie.getProperty("MAIL.TO"),
                        propertie.getProperty("MAIL.CC"),
                        propertie.getProperty("MAIL.BCC"),
                        asunto,
                        mensaje,
                        Encriptador.decriptar(propertie.getProperty("MAIL.PASS")),
                        propertie.getProperty("MAIL.PORT")
                        );
            }            
            
            logger2.info("Notificacion por email");
            
            if (!email.equals("")){
                logger2.warn(email);
                throw new Exception("Error al enviar notificacion por correo: " + email);
            }
            
        }catch(Exception e){
            logger2.warn(e);
        }finally{
            ;
        }
    }
    
    private String getFileName(String ps_baseName) {
        return ps_baseName.concat(String.format("_%s.xlsx", fechaReporte));
    }
    
    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        
        ConsultaEventosCamaras evento = new ConsultaEventosCamaras();
        
        logger2.info("Inicio"); 
        evento.verificaEventos();
        logger2.info("Fin");
        
    }

}
