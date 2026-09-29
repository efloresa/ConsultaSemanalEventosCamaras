/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atm.gob.ec.utils;

/**
 *
 * @author erik.flores
 */
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Fecha {
    private static String fechaActual;
    private static String formatoFecha;
    private static Date fecha;
    private static SimpleDateFormat formato;
    private static DateFormat dfFecha;
    
    /** **/
    public Fecha() {
        fechaActual = "";
        formatoFecha = "";
    }
    
    /** Costructor de la Clase Fecha. Recibe como argumento el formato en el que el 
     *  usuario quiera retornar la fecha y/o hora del sistema, siempre que ingrese
     *  un formato valido.
     * @param formato
     **/
    public Fecha(String formato) {
        fechaActual = "";
        formatoFecha = formato;
    }
    
    public static void estableceFormato(String formato){
        formatoFecha = formato;
    }
    
    private static void estableceFechaCorta() {
        try{
            if (formatoFecha == null || formatoFecha.equals("") || formatoFecha.equals("null"))
                formatoFecha = "dd/MM/yyyy";
                
            formato = new SimpleDateFormat (formatoFecha,new Locale("es","EC"));
            formato.setLenient(false);
            fecha = new Date();
            //fecha = DateFormat.parse();
            formato.applyPattern(formatoFecha);
            fechaActual = formato.format(fecha);
        }catch(IllegalArgumentException iae){
            fechaActual = "0";
        }catch(Exception iae){
            fechaActual = "0";
        }
    }
     
    private static void estableceFechaFormato() {
        try{
            if (formatoFecha == null || formatoFecha.equals("") || formatoFecha.equals("null"))
                formatoFecha = "yyyy/MM/dd";
            
            formato = new SimpleDateFormat (formatoFecha ,new Locale("es","EC"));
            formato.setLenient(false);
            fecha = new Date();
            formato.applyPattern(formatoFecha);
            fechaActual = formato.format(fecha);
        }catch(IllegalArgumentException iae){
            fechaActual = "Error de formato. " + iae.getMessage();
        }
    }
    
    private static void estableceFechaFormato(String f) { 
                
        if (formatoFecha == null || formatoFecha.equals("") || formatoFecha.equals("null"))
            formatoFecha = "yyyy/MM/dd";

        try{ 
            if (f == null || f.equals("") || f.equals("null"))
                throw new Exception();
            
            formato = new SimpleDateFormat (formatoFecha ,new Locale("es","EC")); 
            formato.setLenient(false); 
            fecha = new Date(f); 
            formato.applyPattern(formatoFecha); 
            fechaActual = formato.format(fecha); 
        }catch(IllegalArgumentException e){ 
            fechaActual = "0"; 
            //e.printStackTrace();
        }catch(Exception e){ 
            fechaActual = "0"; 
            //e.printStackTrace();
        } 
    } 
    
    private static void estableceAnio() {
        try{
            formato = new SimpleDateFormat ("yyyy",new Locale("es","EC"));
            formato.setLenient(false);
            fecha = new Date();
            formato.applyPattern("yyyy");
            fechaActual = formato.format(fecha);
        }catch(IllegalArgumentException iae){
            fechaActual = "Error de formato. " + iae.getMessage();
        }
    }
     
    private static void estableceFechayHoraActual() {
        try{
            if (formatoFecha == null || formatoFecha.equals("") || formatoFecha.equals("null"))
                formatoFecha = "dd/MMMM/yyyy HH:mm:ss";
            
            formato = new SimpleDateFormat (formatoFecha, new Locale("es","EC"));
            formato.setLenient(false);
            fecha = new Date();
            formato.applyPattern(formatoFecha);
            fechaActual = formato.format(fecha);
        }catch(IllegalArgumentException iae){
            fechaActual = "Error de formato. " + iae.getMessage();
        }
    }
     
    private static void estableceFechaLargayHoraActual() {
        try{
            if (formatoFecha == null || formatoFecha.equals("") || formatoFecha.equals("null"))
                formatoFecha = "dd/MMMM/yyyy HH:mm:ss";

            formato = new SimpleDateFormat (formatoFecha ,new Locale("es","EC"));
            formato.setLenient(false);
            fecha = new Date();
            formato.applyPattern(formatoFecha);
            fechaActual = formato.format(fecha);
        }catch(IllegalArgumentException iae){
            fechaActual = "Error de formato. " + iae.getMessage();
        }
    }
     
    private static void estableceFecha() {
        try{
            if (formatoFecha == null || formatoFecha.equals("") || formatoFecha.equals("null"))
                formatoFecha = "dd/MMMM/yyyy";
                
            formato = new SimpleDateFormat (formatoFecha,new Locale("es","EC"));
            formato.setLenient(false);
            fecha = new Date();
            formato.applyPattern(formatoFecha);
            fechaActual = formato.format(fecha);
        }catch(IllegalArgumentException iae){
            fechaActual = "Error de formato. " + iae.getMessage();
        }catch(Exception iae){
            fechaActual = "Error. " + iae;
        }
    }
    
    /** Obtiene la fecha en el formato de fecha corta dd/MM/yyyy
     * @return  **/
    public static String obtenFechaCorta() {
        estableceFechaCorta();
        return fechaActual;
    }
     
    /** Obtiene la fecha en el formato de fecha establecido por el usuario
     * @return  **/
    public static String obtenFechaActualFormato() {
        estableceFechaFormato();
        return fechaActual;
    }
     
    /** Obtiene el año en curso
     * @return  **/
    public static String obtenAnio() {
        estableceAnio();
        return fechaActual;
    }
    
    /** Obtiene la fecha en el formato dd/MM/yyyy HH:mm:s
     * @return s**/
    public static String obtenFechaCortayHora() {
        estableceFechayHoraActual();
        return fechaActual;
    }
    
    public static String obtenFechayHora() {
        estableceFechaLargayHoraActual();
        return fechaActual;
    }
    
    public static String obtenFechaActual() {
        estableceFecha();
        return fechaActual;
    }
    
    public static Date sumarRestarDias(Date f, int dias){
        Date dtFecha;
        
        if (f == null)
            dtFecha = null;
        else{
            Calendar cal = Calendar.getInstance();
            cal.setTime(f);
            cal.add(Calendar.DAY_OF_YEAR, dias);
            dtFecha = cal.getTime();
        }
        return dtFecha;
        
    }
    
    public static String sumarRestarDias(String f, int dias){
        String strFecha;
        //strFecha = "0";
        strFecha = "";
        
        if (f == null || f.equals("") || f.equals("0") )
            strFecha = "0";
        else{
            try{ 
                dfFecha = new SimpleDateFormat(formatoFecha);
            Calendar cal = Calendar.getInstance();
            //estableceFormato("dd/MM/yyyy");
            obtenFechaActualFormato();
            // fecha.setTime(dias);
            // fecha = new Date(f); 
            
            fecha = dfFecha.parse(f);
            cal.setTime(fecha);
            cal.add(Calendar.DAY_OF_YEAR, dias);

            strFecha = formato.format(cal.getTime());
            }catch(Exception ex){
                strFecha = "0";
                //ex.printStackTrace();
            }
        }
        return strFecha;
    }
    
    public static String estableceFecha(String f){
        estableceFechaFormato(f);
        return fechaActual;
    }
    
    public static void main(String arg[]){
        Fecha.estableceFormato("yyyy-MM-dd");
        String fecha2 = Fecha.obtenFechaActualFormato();
        System.out.println("Hoy: " + fecha2);
        System.out.println("Hoy 1: " + Fecha.sumarRestarDias(fecha2, 1));
        System.out.println("Hoy -1: " + Fecha.sumarRestarDias(fecha2, -1));
        Fecha.estableceFormato("dd-MMMM-yyyy HH:mm:ss");
        
        System.out.println("Fecha cualquiera: " + Fecha.estableceFecha("03/06/2019 12:34:18"));
        
        
        
        //System.out.println("Hoy con Formato: " + Fecha.obtenFechaActualFormato());
        //DateFormat df = new DateFormat();
        
        /*
        f2 = new Date(Fecha.obtenFechaActualFormato());
        
        if ( f1.before(f2) ){
            System.out.println( "La Fecha 1 es menor ");
        }else{
            if ( f2.before(f1) ){
                System.out.println( "La Fecha 1 es Mayor ");
            }else{
                System.out.println( "Las Fechas Son iguales ");
            }
        }
        */
        
        Calendar ahoraCal = Calendar.getInstance();
        System.out.println(ahoraCal.getClass());
        //ahoraCal.set(2004,1,7);
        System.out.println(ahoraCal.getTime());
        //ahoraCal.set(2004,1,7,7,0,0);
        System.out.println(ahoraCal.getTime());
        System.out.println("ANYO: "+ahoraCal.get(Calendar.YEAR));
        System.out.println("MES: "+ahoraCal.get(Calendar.MONTH));
        System.out.println("DIA: "+ahoraCal.get(Calendar.DATE));
        System.out.println("HORA: "+ahoraCal.get(Calendar.HOUR));
        
        if (ahoraCal.get(Calendar.MONTH) == Calendar.JUNE){
            System.out.println("ES JUNIO");
        }else{
            System.out.println("NO ES JUNIO");
        }

        Calendar cumpleCal = Calendar.getInstance();
        cumpleCal.set(1980,5,23); //La hora no me interesa y recuerda que los meses van de 0 a 11
        int dia = cumpleCal.get(Calendar.DAY_OF_WEEK);
        System.out.println(dia); //Día 4 = WEDNESDAY = MIÉRCOLES
        
        Calendar hoy = Calendar.getInstance();
        hoy.add(Calendar.DATE, 3);
        hoy.add(Calendar.MONTH, 2);
        System.out.println(hoy.getTime());
        
        //Calendar hoy = Calendar.getInstance();
        hoy.add(Calendar.YEAR, -5);
        hoy.add(Calendar.DATE, -50);
        System.out.println(hoy.getTime());
        
        System.out.println("Available TimeZone ");
        String [] timeZones = TimeZone.getAvailableIDs();
        for (String tz : timeZones)
            System.out.println(tz);

        System.out.println("Available Locale " /* + Arrays.toString(Locale.getAvailableLocales())*/);
        Locale [] locales = Locale.getAvailableLocales();
        for (Locale ls: locales)
            System.out.println(ls.toString());
        
        Calendar calHoy = Calendar.getInstance(new Locale("es","EC"));
        System.out.println(calHoy.getTime());
        calHoy.set(2019,03,06,12,34,18);
        System.out.println(calHoy.getTime());
        
        calHoy.setTime(new Date("03/06/2019 12:34:18"));
        System.out.println(calHoy.getTime());
        
        System.out.println(calHoy.getDisplayName(Calendar.MONTH, Calendar.LONG, new Locale("es","EC")));

    }
    
}// end class Fecha



