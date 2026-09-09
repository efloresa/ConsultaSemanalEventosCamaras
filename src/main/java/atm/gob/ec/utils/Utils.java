/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atm.gob.ec.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

/**
 *
 * @author erik.flores
 */

public class Utils {
    private static URL urlArc = null;
    private static String userDir = "";
    private static FileInputStream file;
    
    public Utils(){
        
    }
//    
//    public static String getDirectorioSistema(){
//        String strRes="";
//        //userDir=System.getProperty("user.dir");
//        userDir=new File("").getAbsolutePath();
//
//        userDir=userDir.replaceAll("\\\\", "/");
//
//        strRes=userDir;
//
//        if (strRes.contains("file:"))
//            strRes=strRes.substring(5);
//
//        return strRes;
//    }

    
    public static String getDirectorioSistema(){
        String strRes="";
        userDir=System.getProperty("user.dir");

        userDir=userDir.replaceAll("\\\\", "/");

        strRes=userDir;

        if (strRes.contains("file:"))
            strRes=strRes.substring(5);

        return strRes;
    }
    
    public static Properties getProperties(){
        Properties properties = new Properties();
        try {
            file = new FileInputStream(getDirectorioSistema() + "/resources/resourcesATM.properties");
            properties.load(file);
            file.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }        
        return properties;
    }
    
    public static LoggerContext configureLogging() {
        LoggerContext context;        
        try {
            
            context = (LoggerContext) LogManager.getContext(false);
            
            // Find test logging configuration 
            //URL url =  this.getClass().getClassLoader().getResource("log4j2.xml");
            //URL url =  this.getClass().getClassLoader().getResource(dirLog4j2);
            URL url = new File(getDirectorioSistema() + "/resources/log4j2.xml").toURL();
            System.out.println("URL to log4j2: " + url.getFile());
            if (url != null) {
              context.setConfigLocation(url.toURI());
            } else 
                throw new RuntimeException("Logging configuration not found!");
            
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to load logging configuration.", e);
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL Malformed.", e);
        }
        return context;
    }    

    public static LoggerContext configureLogging(String dirLog4j2) {
        LoggerContext context;        
        try {
            
            context = (LoggerContext) LogManager.getContext(false);
            
            URL url = new File(dirLog4j2).toURL();
            System.out.println("URL to log4j2: " + url.getFile());
            if (url != null) {
              context.setConfigLocation(url.toURI());
            } else 
                throw new RuntimeException("Logging configuration not found!");
            
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to load logging configuration.", e);
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL Malformed.", e);
        }
        return context;
    }    
        
    public String getDirectorioSistema2(){
        String strRes="";
        userDir=System.getProperty("user.dir");
        
        try{
            
            userDir=userDir.replaceAll("\\\\", "/");
            //userDir=userDir.substring(userDir.lastIndexOf("/"));
            
            urlArc=this.getClass().getResource("/ec/gob/atm/utils/Utils.class");
            //urlArc=this.getClass().getResource("/ec/gob/atm/utils/");
            
            if (urlArc!=null){
                //Utilizar "decode" porque los espacios en blanco que puede incluir la ruta del archivo son
                //reemplazados con "%20" por el método "getPath()" y eso trae problemas al usar "FileInputStream".
                strRes=URLDecoder.decode(urlArc.getPath(),"UTF-8");
                
                //strRes=strRes.substring(0, strRes.lastIndexOf("/VerificaEventosPlavit"));
                //strRes=strRes.substring(0, strRes.lastIndexOf(userDir));
                
                //strRes=userDir;
                if (strRes.contains("file:"))
                    strRes=strRes.substring(5);
            }
        }catch (UnsupportedEncodingException e){
            //System.out.println(e.getMessage());
            strRes=null;            
            System.out.println("strRes: " + strRes);
        }
        return strRes;
    }
    
    public String getDirectorioSistema3(){
        String strRes="";
        try{
            urlArc=this.getClass().getResource("/ec/gob/atm/utils/Utils.class");
            if (urlArc!=null){
                //Utilizar "decode" porque los espacios en blanco que puede incluir la ruta del archivo son
                //reemplazados con "%20" por el método "getPath()" y eso trae problemas al usar "FileInputStream".
                strRes=URLDecoder.decode(urlArc.getPath(),"UTF-8");
                strRes=strRes.substring(0, strRes.lastIndexOf("/VerificaEventosPlavit"));
                if (strRes.contains("file:"))
                    strRes=strRes.substring(5);
            }
        }catch (UnsupportedEncodingException e){
            strRes=null;
            System.out.println("strRes: " + strRes);
        }
        return strRes;
    }
    
}

