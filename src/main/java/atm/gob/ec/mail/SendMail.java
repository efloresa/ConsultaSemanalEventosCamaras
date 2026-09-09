/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package atm.gob.ec.mail;

/**
 *
 * @author erik.flores
 */
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.activation.DataHandler; 
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendMail {
    
    public static String GS_CLAVE ="";
    public static String GS_TO ="";
    public static String GS_FROM ="";
    
    public static String send(String ps_smtp, 
                              String ps_from, 
                              String ps_to, 
                              String ps_cc, 
                              String ps_bcc, 
                              String ps_subject, 
                              String ps_texto,
                              String ps_password,
                              String ps_puerto) {
        
        if(ps_to == null) 
            return "Por favor envie la dirección de correo.";
        
        String ls_error;
        SendMail.GS_CLAVE = ps_password;
        SendMail.GS_TO = ps_to;
        ls_error = ""; 
        
        StringTokenizer lst_cadenaMail;
        lst_cadenaMail = new StringTokenizer(ps_to,";");
         
        ls_error = send4(ps_smtp,ps_from,ps_to,ps_cc,ps_bcc,ps_subject,ps_texto,ps_password,ps_puerto);
        
        return ls_error;
    }

    /*
    * CREADO POR: ERIK FLORES
    * FECHA: 2016-11-23
    * PROPOSITO: ENVIO DE EMAILS A MULTIPLES CUENTAS AL MISMO TIEMPO
    */
    public static String send3(String ps_smtp, 
                               String ps_from, 
                               String ps_to, 
                               String ps_cc, 
                               String ps_bcc, 
                               String ps_subject, 
                               String ps_texto,
                               String ps_password,
                               String ps_puerto) {

        String  ls_mailer = "Axis 4.00.00";
        String  ls_error="";
        boolean lb_debug = false;
        int     li_optind, li_paso=0;
        SendMail.GS_CLAVE = ps_password;
        SendMail.GS_FROM = ps_from;
       
        try {
            li_paso = 1;
            if (ps_smtp == null || ps_smtp.equals(""))
                return "Por favor, indique el servidor de correo.";
            
            li_paso = 2;
            if (ps_from == null || ps_from.equals(""))
                return "Por favor, indique la cuenta de correo emisor.";
            
            li_paso = 3;
            if (ps_to == null || ps_to.equals(""))
                return "Por favor, indique la cuenta de correo receptora.";
            
            li_paso = 4;
            if (ps_texto == null || ps_texto.equals(""))
                return "Por favor, el mensaje del correo.";
            
            li_paso = 5;
            if (ps_puerto == null || ps_puerto.equals(""))
                return "Por favor, indique el puerto de salida SMTP.";
            
            li_paso = 6;
            if (ps_subject == null || ps_subject.equals(""))
                return "Por favor, indique el asunto del correo electrónico.";
            
            li_paso = 7;
            Properties props = System.getProperties();
            
            li_paso = 8;
            props.put("mail.store.protocol", "SMTP");
            props.put("mail.smtp.host", ps_smtp);
            props.put("mail.smtp.port", ps_puerto); 
            
            if (ps_puerto.equals("587")){
                props.put("mail.smtp.starttls.enable", "true");
            }
            if (ps_puerto.equals("465")){
                props.put("mail.smtp.ssl.enable", "true");
            }

            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.user", ps_from);  
            props.put("mail.smtp.password", ps_password); 

            //autentificadorSMTP auth = new autentificadorSMTP();       
            //Session session = Session.getInstance(props,auth); 

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SendMail.GS_FROM, SendMail.GS_CLAVE);
                }
            }); 

            if (ps_to != null) 
                ps_to = ps_to.replace(";",",");   
            
            if (ps_cc != null) 
                ps_cc = ps_cc.replace(";",",");   
            
            if (ps_bcc != null) 
                ps_bcc = ps_bcc.replace(";",","); 

            li_paso = 10;
            // construct the message
            MimeMessage msg = new MimeMessage(session);
            msg.setSubject(ps_subject);
            msg.setText(ps_texto);
            msg.setFrom(new InternetAddress(ps_from)); 
            msg.setContent(ps_texto,"text/html;");
            if (ps_to != null) 
                msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(ps_to));
            
            if (ps_cc != null) 
                msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ps_cc, false));
            
            if (ps_bcc != null) 
                msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(ps_bcc, false));
            
            Transport.send(msg); 

        }catch (MessagingException e) {
            ls_error = e.toString()+"-(sendmail.send3: paso:"+li_paso+", ps_from:"+ps_from+")";
        }

        return ls_error;
    }       
    
    /*
    * CREADO POR: ERIK FLORES
    * FECHA: 2020-05-01
    * PROPOSITO: SE CAMBIA LA FORMA DE AUTENTICAR AL SERVIDOR DE CORREO
    */
    public static String send4(String ps_smtp, 
                               String ps_from, 
                               String ps_to, 
                               String ps_cc, 
                               String ps_bcc, 
                               String ps_subject, 
                               String ps_texto,
                               String ps_password,
                               String ps_puerto) {

        Session session;
        Transport trp;
        String  ls_mailer = "ATM 4.0.0";
        String  ls_error="";
        boolean lb_debug = false;
        int     li_optind, li_paso=0;
        GS_FROM = ps_from;
        GS_CLAVE = ps_password;

        try {
            li_paso = 1;
            if (ps_smtp == null || ps_smtp.equals(""))
                return "Por favor, indique el servidor de correo.";
            
            li_paso = 2;
            if (ps_from == null || ps_from.equals(""))
                return "Por favor, indique la cuenta de correo emisor.";
            
            li_paso = 3;
            if (ps_to == null || ps_to.equals(""))
                return "Por favor, indique la cuenta de correo receptora.";
            
            li_paso = 4;
            if (ps_texto == null || ps_texto.equals(""))
                return "Por favor, el mensaje del correo.";
            
            li_paso = 5;
            if (ps_puerto == null || ps_puerto.equals(""))
                return "Por favor, indique el puerto de salida SMTP.";
            
            li_paso = 6;
            if (ps_subject == null || ps_subject.equals(""))
                return "Por favor, indique el asunto del correo electrónico.";
                        
            li_paso = 7;
            Properties props = System.getProperties(); 
            
            li_paso = 8; 
            props.put("mail.store.protocol", "SMTP");
            props.put("mail.smtp.host", ps_smtp);
            props.put("mail.smtp.port", ps_puerto); 
            
            if (ps_puerto.equals("587"))
                props.put("mail.smtp.starttls.enable", "true");
            
            if (ps_puerto.equals("465"))
                props.put("mail.smtp.ssl.enable", "true");

            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.user", ps_from);  
            props.put("mail.smtp.password", ps_password); 
            
            if (ps_to != null) 
                ps_to = ps_to.replace(";",","); 
            
            if (ps_cc != null) 
                ps_cc = ps_cc.replace(";",","); 
            
            if (ps_bcc != null) 
                ps_bcc = ps_bcc.replace(";",","); 

            //autentificadorSMTP auth = new autentificadorSMTP(); 
            //session = Session.getInstance(props,auth); 
            
            session = Session.getDefaultInstance(props);
            
            li_paso = 10;
            // construct the message
            MimeMessage message = new MimeMessage(session);
            message.setSubject(ps_subject);
            message.setText(ps_texto);
            
            message.setContent(ps_texto,"text/html;");
            
            message.setFrom(new InternetAddress(ps_from)); 

            if (ps_to != null) 
                message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(ps_to));
            
            if (ps_cc != null) 
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ps_cc, false));
            
            if (ps_bcc != null) 
                message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(ps_bcc, false));
            
            trp = session.getTransport("smtp");
            trp.connect(ps_smtp, ps_from, ps_password);
            trp.sendMessage(message, message.getAllRecipients()); 
            trp.close();
            
        }catch (Exception e) {
            ls_error=e.toString()+"-(sendmail.send4: paso:"+li_paso+", ps_from:"+ps_from+")";
        }
        
        return ls_error;
    }  
         
    /*
    * CREADO POR: ERIK FLORES
    * FECHA: 2020-05-01
    * PROPOSITO: SE CAMBIA LA FORMA DE AUTENTICAR AL SERVIDOR DE CORREO
    */
    public static String sendWithAttachments(String ps_smtp, 
                                            String ps_from, 
                                            String ps_password,
                                            String ps_port,                                            
                                            String ps_to, 
                                            String ps_cc, 
                                            String ps_bcc, 
                                            String ps_subject, 
                                            String ps_message,
                                            String ps_attachments
                                            ) {

        Session session;
        Transport trp;
        String  ls_mailer = "ATM 4.0.0";
        String  ls_error="";
        boolean lb_debug = false;
        int     li_optind, li_paso=0;
        GS_FROM = ps_from;
        GS_CLAVE = ps_password;

        try {
            li_paso = 1;
            if (ps_smtp == null || ps_smtp.equals(""))
                return "Por favor, indique el servidor de correo.";
            
            li_paso = 2;
            if (ps_from == null || ps_from.equals(""))
                return "Por favor, indique la cuenta de correo emisor.";
            
            li_paso = 3;
            if (ps_to == null || ps_to.equals(""))
                return "Por favor, indique la cuenta de correo receptora.";
            
            li_paso = 4;
            if (ps_message == null || ps_message.equals(""))
                return "Por favor, el mensaje del correo.";
            
            li_paso = 5;
            if (ps_port == null || ps_port.equals(""))
                return "Por favor, indique el puerto de salida SMTP.";
            
            li_paso = 6;
            if (ps_subject == null || ps_subject.equals(""))
                return "Por favor, indique el asunto del correo electrónico.";
                        
            li_paso = 7;
            Properties props = System.getProperties(); 
            
            li_paso = 8; 
            props.put("mail.store.protocol", "SMTP");
            props.put("mail.smtp.host", ps_smtp);
            props.put("mail.smtp.port", ps_port); 
            
            if (ps_port.equals("587"))
                props.put("mail.smtp.starttls.enable", "true");
            
            if (ps_port.equals("465"))
                props.put("mail.smtp.ssl.enable", "true");

            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.user", ps_from);  
            props.put("mail.smtp.password", ps_password); 
            
            if (ps_to != null) 
                ps_to = ps_to.replace(";",","); 
            
            if (ps_cc != null) 
                ps_cc = ps_cc.replace(";",","); 
            
            if (ps_bcc != null) 
                ps_bcc = ps_bcc.replace(";",","); 

            //autentificadorSMTP auth = new autentificadorSMTP(); 
            //session = Session.getInstance(props,auth); 
            
            session = Session.getDefaultInstance(props);
            
            li_paso = 10;
            
            // construct the message
            MimeMessage message = new MimeMessage(session);
            //message.setText(ps_message);
            
            //Multipart mp = new MimeMultipart();
            
            BodyPart texto = new MimeBodyPart();
            texto.setContent(ps_message,"text/html;");
            
            BodyPart adjunto = new MimeBodyPart();
            
            if (ps_attachments != null){
                adjunto.setDataHandler(new DataHandler(new FileDataSource(ps_attachments)));
                adjunto.setFileName(ps_attachments);
            }
            
            MimeMultipart mime = new MimeMultipart();
            mime.addBodyPart(texto);
            mime.addBodyPart(adjunto);
            
            message.setFrom(new InternetAddress(ps_from)); 
            
            if (ps_to != null) 
                message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(ps_to));
            
            if (ps_cc != null) 
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ps_cc, false));
            
            if (ps_bcc != null) 
                message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(ps_bcc, false));
            
            message.setSubject(ps_subject);
            message.setSentDate(new Date());
            message.setContent(mime);
            
            li_paso = 11;
            
            trp = session.getTransport("smtp");
            trp.connect(ps_smtp, ps_from, ps_password);
            trp.sendMessage(message, message.getAllRecipients()); 
            trp.close();
            
        }catch (MessagingException e) {
            ls_error=e.toString()+"-(sendmail.sendWithAttachments: paso:"+li_paso+", ps_from:"+ps_from+")";
        }
        
        return ls_error;
    }  
         
     
    public static String envia_doc_adjunto(String ps_smtp, 
                                           String ps_from, 
                                           String ps_to, 
                                           String ps_cc, 
                                           String ps_bcc, 
                                           String ps_subject, 
                                           String ps_texto,
                                           String ps_password,
                                           String ps_puerto, 
                                           Blob p_archivo1,
                                           String ps_nombreArchivo1) {

        String  ls_mailer = "ATM 1.0.0";
        String  ls_error="";
        boolean lb_debug = false;
        int     li_optind, li_paso=0;
        SendMail.GS_CLAVE = ps_password;
        SendMail.GS_FROM = ps_from;
        SendMail.GS_TO = ps_to;

        try {
            li_paso = 1;
            if (ps_smtp == null || ps_smtp.equals(""))
                return "Por favor, indique el servidor de correo.";
            
            li_paso = 2;
            if (ps_from == null || ps_from.equals(""))
                return "Por favor, indique la cuenta de correo emisor.";
            
            li_paso = 3;
            if (ps_to == null || ps_to.equals(""))
                return "Por favor, indique la cuenta de correo receptora.";
            
            li_paso = 4;
            if (ps_texto == null || ps_texto.equals(""))
                return "Por favor, el mensaje del correo.";
            
            li_paso = 5;
            if (ps_puerto == null || ps_puerto.equals(""))
                return "Por favor, indique el puerto de salida SMTP.";
            
            li_paso = 6;
            if (ps_subject == null || ps_subject.equals(""))
                return "Por favor, indique el asunto del correo electrónico.";
                        
            li_paso = 7;
            if (ps_to != null) 
                ps_to = ps_to.replace(";",",");
            
            if (ps_cc != null) 
                ps_cc = ps_cc.replace(";",",");
            
            if (ps_bcc != null) 
                ps_bcc = ps_cc.replace(";",","); 

            Properties props = System.getProperties();

            props.put("mail.store.protocol", "SMTP");
            props.put("mail.smtp.host", ps_smtp);
            props.put("mail.smtp.port", ps_puerto); 
            
            if (ps_puerto.equals("587"))
                props.put("mail.smtp.starttls.enable", "true");
            
            if (ps_puerto.equals("465"))
                props.put("mail.smtp.ssl.enable", "true");
            
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.user", ps_from);  
            props.put("mail.smtp.password", ps_password); 
            
            //autentificadorSMTP auth = new autentificadorSMTP();
            
            Session mailSession = Session.getDefaultInstance(props);
            
            Message msg = new MimeMessage(mailSession);
            msg.setFrom(new InternetAddress(ps_from));
            
            InternetAddress[] address = InternetAddress.parse(ps_to);//{new InternetAddress(ps_to)};


            if(ps_bcc != null){
                InternetAddress[] myBccList = InternetAddress.parse(ps_bcc);
                msg.setRecipients(Message.RecipientType.BCC, myBccList);
            }

            if(ps_cc != null){
                InternetAddress[] myCcList = InternetAddress.parse(ps_cc);
                msg.setRecipients(Message.RecipientType.CC, myCcList);           
            } 

            msg.setRecipients(Message.RecipientType.TO, address);

            msg.setSubject(ps_subject);

            BodyPart texto = new MimeBodyPart();
            texto.setContent(ps_texto, "text/html");

            MimeMultipart multiParte = new MimeMultipart();  
            multiParte.addBodyPart(texto);

            if(ps_nombreArchivo1 != null){           
                BodyPart adjunto = new MimeBodyPart(); 
                adjunto.setDataHandler(new DataHandler(p_archivo1.getBytes(1, (int) p_archivo1.length()),"application/octet-stream")); 
                adjunto.setFileName(ps_nombreArchivo1); 
                multiParte.addBodyPart(adjunto);
            }

            msg.setContent(multiParte);

            Transport transport = mailSession.getTransport("smtp");
            transport.connect(ps_smtp, ps_from, ps_password);
            transport.sendMessage(msg, msg.getAllRecipients());
            transport.close();

        }catch (MessagingException e) {
            ls_error=e.toString()+ " - (sendmail.envia_doc_adjunto: paso:"+li_paso;
        }catch (SQLException e){
            ls_error=e.toString()+ " - (sendmail.envia_doc_adjunto: paso:"+li_paso;
        }catch (Exception e){
            ls_error=e.toString()+ " - (sendmail.envia_doc_adjunto: paso:"+li_paso;
        }

        return ls_error;
    } 
}

