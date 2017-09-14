/* 
 * Copyright (C) 2017 FirmaEC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ec.gob.firmadigital.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 *
 * @author desarrollo
 */
public class Validacion {
    private static Locale locale = new Locale("es","EC");
    //http://www.timezoneconverter.com/cgi-bin/findzone
    private static Calendar calendario = Calendar.getInstance(TimeZone.getTimeZone("America/Guayaquil"));
    public static Date fechaString_Date(String fecha){
        DateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = (Date) formato.parse(fecha);
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(Validacion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (Exception e) {
            date = null;
        }
        return date;
    }
    public static Date String_Date(String fecha){
        if(fecha.trim().length()==10)
        {
            if(ValidarDatosFecha(fecha)){
                DateFormat formato = new SimpleDateFormat("dd-MM-yyyy",   Locale.getDefault());
                Date date = null;
                try {
                    date = (Date) formato.parse(fecha);
                } catch (ParseException ex) {
                    java.util.logging.Logger.getLogger(Validacion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (Exception e) {
                    date = null;
                }
                return date;
            }
            else
            {
                return null;
            }
        }
        else
            return null;
        
    }
    
    private static boolean ValidarDatosFecha(String fecha){
        boolean flag=true;
        
        try {
            int dia=0,mes=0,anio=0;
            StringTokenizer st=new StringTokenizer(fecha,"-");
            if(st.hasMoreTokens())
            {
                dia=Integer.parseInt(st.nextToken());
            }
            if(st.hasMoreTokens())
            {
                mes=Integer.parseInt(st.nextToken());
            }
            if(st.hasMoreTokens())
            {
                anio=Integer.parseInt(st.nextToken());
            }
            
            if(mes > 12)
            {
                return false;
            }
            else
            {
                if(dia > 31)
                {
                    return false;
                }
                else
                {
                    if((mes == 4 || mes == 6 || mes == 9 || mes == 11)&&(dia > 30))
                    {
                        return false;
                    }
                    else
                    {
                        if(mes == 2 && bisiesto(anio) && dia > 29)
                        {
                            return false;
                        }
                        else if (mes == 2 && !bisiesto(anio) && dia > 28)
                        {
                            return false;
                        }
                        else
                        {
                            flag = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return flag;
    }
    
    private static boolean bisiesto(int anio){
        if (anio % 400 == 0)
        {
            return true;
        }
        else
        {
            if(anio % 4 == 0 && anio % 100 != 0)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    } 
//    public static String fechaSistemaAAAAmmdd(){
//        Date fecha = new Date();
//        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd", locale);
//        return formato.format(fecha);
//    }
    public static String fechaSistema(){
        Date fecha = new Date();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);
        return formato.format(fecha);
    }
    
    public static String fechaSumarRestarDias(Date fecha, int numeroDias, int constante){
        Calendar c1 = Calendar.getInstance();
        c1.setTime(fecha);
        if (numeroDias == 1){
            c1.add(Calendar.DATE, numeroDias);
            c1.add(Calendar.DATE, -constante);
        } else {
            c1.add(Calendar.DATE, -numeroDias);
            c1.add(Calendar.DATE, constante);
        }
        
        DateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        return formato.format(c1.getTime());
    }
    
    public static String fechaSumarDias(Date fecha, int numeroDias){
        Calendar c1 = Calendar.getInstance();
        c1.setTime(fecha);
        c1.add(Calendar.DATE, numeroDias);
        DateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        return formato.format(c1.getTime());
    }
    
    //<editor-fold defaultstate="collapsed" desc="FECHAS">
    public static String fechaSistema(String mascara){
        Date fecha = new Date();
        SimpleDateFormat formato = new SimpleDateFormat(mascara, locale);
        return formato.format(fecha);
    }
    public static Date fecha(String fecha, String mascara){
        DateFormat formato = new SimpleDateFormat(mascara);
        Date date = new Date();
        try {
            date = (Date) formato.parse(fecha);
        } catch (Exception e) {
            date = null;
        }
        return date;
    }
    public static String fecha(Date fecha, String mascara){
        DateFormat formato = new SimpleDateFormat(mascara);
        return formato.format(fecha);
    }
    public static String fecha(String fecha, String mascara1, String mascara2){
        String fechaDevolver = "";
        java.text.SimpleDateFormat formatoFecha1 = new java.text.SimpleDateFormat(mascara1);
        java.text.SimpleDateFormat formatoFecha2 = new java.text.SimpleDateFormat(mascara2);
        try {
            formatoFecha1.parse(fecha);
            fechaDevolver = formatoFecha2.format(formatoFecha1.getCalendar().getTime());
        } catch (java.text.ParseException ex) {}
        return fechaDevolver;
    }
    public static Boolean isFechaSuperior(String fechaFormulario, String mascara){
        Boolean boleano = null;
        java.text.SimpleDateFormat formatoFecha = new java.text.SimpleDateFormat(mascara);
        try {
            if (formatoFecha.parse(fechaFormulario).getTime() > formatoFecha.parse(fechaSistema(mascara)).getTime()){
                boleano = true;
            } else {
                boleano = false;
            }
        } catch (java.text.ParseException ex) {boleano = null;}
        return boleano;
    }
    public static Boolean isFechaSuperior(String fechaFormulario, String fechaComparar, String mascara){
        Boolean boleano = null;
        java.text.SimpleDateFormat formatoFecha = new java.text.SimpleDateFormat(mascara);
        try {
            if (formatoFecha.parse(fechaFormulario).getTime() > formatoFecha.parse(fechaComparar).getTime()){
                boleano = true;
            } else {
                boleano = false;
            }
        } catch (java.text.ParseException ex) {boleano = null;}
        return boleano;
    }
    //</editor-fold>
    
    public static int getNumeroDia(String fecha, String mascara) {
        java.text.SimpleDateFormat formatoFecha1 = new java.text.SimpleDateFormat(mascara, locale);
        try {
            formatoFecha1.parse(fecha);
        } catch (java.text.ParseException ex) {
        }
        java.util.Calendar calendarioAux = java.util.Calendar.getInstance();
        calendarioAux.setTime(formatoFecha1.getCalendar().getTime());
        return calendarioAux.get(java.util.Calendar.DAY_OF_WEEK);
    }
    
    public static int getNumeroMes(String fecha) {
        java.text.SimpleDateFormat formatoFecha1 = new java.text.SimpleDateFormat("dd-MM-yyyy");
        try {
            formatoFecha1.parse(fecha);
        } catch (java.text.ParseException ex) {
        }
        java.util.Calendar calendarioAux = java.util.Calendar.getInstance();
        calendarioAux.setTime(formatoFecha1.getCalendar().getTime());
        calendarioAux.set(calendarioAux.get(java.util.Calendar.YEAR),
                calendarioAux.get(java.util.Calendar.MONTH),
                calendarioAux.getActualMinimum(java.util.Calendar.DAY_OF_MONTH));
        return calendarioAux.get(java.util.Calendar.MONTH);
    }

    public static java.util.Date getPrimerDiaDelMes(String fecha, String mascara) {
        java.text.SimpleDateFormat formatoFecha1 = new java.text.SimpleDateFormat(mascara);
        try {
            formatoFecha1.parse(fecha);
        } catch (java.text.ParseException ex) {}
        java.util.Calendar calendarioAux = java.util.Calendar.getInstance();
        calendarioAux.setTime(formatoFecha1.getCalendar().getTime());
        calendarioAux.set(calendarioAux.get(java.util.Calendar.YEAR),
                calendarioAux.get(java.util.Calendar.MONTH),
                calendarioAux.getActualMinimum(java.util.Calendar.DAY_OF_MONTH));
        return calendarioAux.getTime();
    }

    public static java.util.Date getUltimoDiaDelMes(String fecha, String mascara) {
        java.text.SimpleDateFormat formatoFecha1 = new java.text.SimpleDateFormat(mascara);
        try {
            formatoFecha1.parse(fecha);
        } catch (java.text.ParseException ex) {}

        java.util.Calendar calendarioAux = java.util.Calendar.getInstance();
        calendarioAux.setTime(formatoFecha1.getCalendar().getTime());
        calendarioAux.set(calendarioAux.get(java.util.Calendar.YEAR),
                calendarioAux.get(java.util.Calendar.MONTH),
                calendarioAux.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
        return calendarioAux.getTime();
    }

    public static String convertirDdMmAaaaTOaaaaMmDd(String fecha){
        String fechaDevolver = "";
        java.text.SimpleDateFormat formatoFecha1 = new java.text.SimpleDateFormat("dd-MM-yyyy");
        java.text.SimpleDateFormat formatoFecha2 = new java.text.SimpleDateFormat("yyyy-MM-dd");
        try {
            formatoFecha1.parse(fecha);
            fechaDevolver = formatoFecha2.format(formatoFecha1.getCalendar().getTime());
        } catch (java.text.ParseException ex) {}
        return fechaDevolver;
    }
    public static String convertirAaaaMmDdTODdMmAaaa(String fecha){
        String fechaDevolver = "";
        java.text.SimpleDateFormat formatoFecha1 = new java.text.SimpleDateFormat("yyyy-MM-dd");
        java.text.SimpleDateFormat formatoFecha2 = new java.text.SimpleDateFormat("dd-MM-yyyy");
        try {
            formatoFecha1.parse(fecha);
            fechaDevolver = formatoFecha2.format(formatoFecha1.getCalendar().getTime());
        } catch (java.text.ParseException ex) {}
        return fechaDevolver;
    }
    public static Date fechaCaducaFacturaRetencion(String fecha, String mascara){
        DateFormat formato = new SimpleDateFormat(mascara);
        Date date = new Date();
        try {
            date = (Date) formato.parse(fecha);
        } catch (Exception e) {
            date = null;
        }
        return date;
    }

    //<editor-fold defaultstate="collapsed" desc="COMPROBAR">
    /** Valida si el par�metro es una fecha con el formato "dd-mm-yyyy".
     * @return true si cumple el formato, false en caso contrario.
     */
    public static boolean leerFecha(Date mensaje) {
        //JOptionPane.showMessageDialog(null, mensaje.toString().length());
        if (mensaje == null)
            return false;
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd", locale); //dia-mes-a�o
        String fecha = formato.format(mensaje);
        if (fecha.length() != formato.toPattern().length())
            return false;
        formato.setLenient(false);//deshabilita el modo permisivo para rango de 28,29,30 y 31 dias
        try {
            formato.parse(fecha);
        }catch (ParseException pe) {return false;}
        return true;
    }
    public static boolean leerNumerosInt(String mensaje){
        boolean retorno=true;
        try{
            Integer.parseInt(mensaje);
            //retorno=true;
        }catch(NumberFormatException x){
            retorno=false;
        }return retorno;
    }
    public static boolean leerNumerosDouble(String mensaje){
        boolean retorno=true;
        try{
            Double.parseDouble(mensaje);
            //retorno=true;
        }catch(NumberFormatException x){
            retorno=false;
        }return retorno;
    }
    public static boolean leerNumerosFloat(String mensaje){
        boolean retorno=true;
        try{
            Float.parseFloat(mensaje);
            //retorno=true;
        }catch(NumberFormatException x){
            retorno=false;
        }return retorno;
    }
    public static boolean leerLetras(String mensaje){
        boolean retorno=true;
        int i=0;
        while(i<mensaje.length()){
            if((Character.isLetter(mensaje.charAt(i))||(Character.isWhitespace(mensaje.charAt(i)))))
                retorno=true;
            else{
                retorno=false;
                break;
            }
            i++;
        }return retorno;
    }
    
    public static boolean isEmail(String correo) {
        if(correo.indexOf('@')>-1)
            return true;
        else
            return false;
    }
    //</editor-fold>
    
    public static double ConvertirObjectToDouble(Object Objeto)
    {
        double numero_double = 0;
        try{
            String Str = Objeto.toString();
            numero_double = Double.valueOf(Str).doubleValue();
        }catch(Exception e){}
        return numero_double;
    }
    
    public static java.math.BigDecimal redondeoDecimalBigDecimal(java.math.BigDecimal d) {
    	java.text.DecimalFormat formato = new java.text.DecimalFormat("#.##");
    	java.text.DecimalFormatSymbols dfs = formato.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        formato.setDecimalFormatSymbols(dfs);
    	return new java.math.BigDecimal(formato.format(d));
    }
    
    public static java.math.BigDecimal redondeoDecimalBigDecimal(java.math.BigDecimal d, int precision, java.math.RoundingMode metodoRedondeo) {
        java.math.BigDecimal retorno = d;
        return retorno.setScale(precision, metodoRedondeo);
    }
    public static java.math.BigDecimal redondeoDecimalBigDecimal(java.math.BigDecimal d, String formato) {
    	java.text.DecimalFormat format = new java.text.DecimalFormat(formato);
    	java.text.DecimalFormatSymbols dfs = format.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        format.setDecimalFormatSymbols(dfs);
    	return new java.math.BigDecimal(format.format(d));
    }

    public static Double redondeoDeDoubles(Double d){
        java.text.DecimalFormat formato = new java.text.DecimalFormat("#.##");
        java.text.DecimalFormatSymbols dfs = formato.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        formato.setDecimalFormatSymbols(dfs);
        return Double.valueOf(formato.format(d));
    }
    
    public static java.util.List<String> separar(String listaAux, String separador){
        java.util.List<String> lista = new java.util.ArrayList();
        int indice = 0;
        int token = new java.util.StringTokenizer(listaAux, separador).countTokens(); 
        for(int i=0; i<token; i++){
            if(i==0)
                lista.add(listaAux.substring(indice, listaAux.indexOf(separador, indice+1)).trim());
            else
                if(i==token-1)
                    lista.add(listaAux.substring(indice+1, listaAux.length()).trim());
                else
                    lista.add(listaAux.substring(indice+1, listaAux.indexOf(separador, indice+1)).trim());
            indice = listaAux.indexOf(separador, indice+1);
        }
        return lista;
    }

    public static java.util.List<String> separarComprobante(String comprobante){
//        String comprobante = "2011-05 | C-ANT | 0000001";
        java.util.List<String> comprobantes = new java.util.ArrayList();
        String perCodigo = "";
        String tipCodigo = "";
        String conNumero = "";
        int indice = 0;

        perCodigo = comprobante.substring(0, comprobante.indexOf("|"));
        indice = comprobante.indexOf("|");

        tipCodigo = comprobante.substring(indice + 1, comprobante.indexOf("|", indice + 1));
        indice = comprobante.indexOf("|", indice + 1);

        conNumero = comprobante.substring(indice + 1);
        
//        //*System.out.println(perCodigo.trim());
//        //*System.out.println(tipCodigo.trim());
//        //*System.out.println(conNumero.trim());
        
        comprobantes.add(perCodigo.trim());
        comprobantes.add(tipCodigo.trim());
        comprobantes.add(conNumero.trim());
        
        return comprobantes;
    }

    public static String formatoCapitalizado(String sentence){
//        sentence = "whY dId tHe Duck Eat sTuFF 111!@#$%^&*()";
        StringBuilder bob = new StringBuilder();
        if(sentence != null){
            sentence = sentence.trim();
            if(!sentence.isEmpty()){
                for (String string : sentence.split(" ")) {
                    bob.append(string.substring(0, 1).toUpperCase());
                    bob.append(string.substring(1).toLowerCase());
                    bob.append(" ");
                }
            }
        }
        return bob.toString().trim();
    }
    
    public static String eliminaCaracteres(String s_cadena, String s_caracteres)
    {
    String nueva_cadena = "";
    Character caracter = null;
    boolean valido = true;
  
    /* Va recorriendo la cadena s_cadena y copia a la cadena que va a regresar,
        sólo los caracteres que no estén en la cadena s_caracteres */
    for (int i=0; i<s_cadena.length(); i++)
        {
        valido = true;
        for (int j=0; j<s_caracteres.length(); j++)
            {
                caracter = s_caracteres.charAt(j);
    
                if (s_cadena.charAt(i) == caracter)
                {
                    valido = false;
                    break;
                }
            }
        if (valido)
            nueva_cadena += s_cadena.charAt(i);
        }

    return nueva_cadena;
    }
    
    public static void main(String args[]){
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fechaSistema = sdf.format(fecha(fechaSistema(), "yyyy-MM-dd HH:mm:ss"));
        System.out.println(fechaSistema);
        
        String fecha = "2015-11-26 10:00:00";
        
        System.out.println(fecha(fechaSistema, "yyyy-MM-dd HH:mm:ss"));
        System.out.println(fecha(fechaSistema, "yyyy-MM-dd HH:mm:ss").getTime());
        
        System.out.println(fecha(fecha, "yyyy-MM-dd HH:mm:ss"));
        System.out.println(fecha(fecha, "yyyy-MM-dd HH:mm:ss").getTime());
        
        System.out.println(fecha(fecha, "yyyy-MM-dd HH:mm:ss").getTime() > fecha(fechaSistema, "yyyy-MM-dd HH:mm:ss").getTime());
//        
//        System.out.println(fecha(a, "yyyy-MM-dd").getTime() > fecha("2015-11-25", "yyyy-MM-dd").getTime());
//        System.out.println(fecha(fechaSistema(), "yyyy-MM-dd").getTime() > fecha("2015-11-25", "yyyy-MM-dd").getTime());
//        System.out.println(fecha("25-11-2015", "yyyy-MM-dd").getTime() > fecha("26-11-2015", "yyyy-MM-dd").getTime());
//        System.out.println(fecha("2015-11-25", "yyyy-MM-dd").getTime() > fecha("2015-11-26", "yyyy-MM-dd").getTime());

//        System.out.println(""+redondeoDecimalBigDecimal(new BigDecimal("5555.123456789"), 6, RoundingMode.CEILING));
//        System.out.println(""+redondeoDecimalBigDecimal(new BigDecimal("5555.123455789"), 6, RoundingMode.DOWN));
//        System.out.println(""+redondeoDecimalBigDecimal(new BigDecimal("5555.12"), 6, RoundingMode.FLOOR));
//        System.out.println(""+redondeoDecimalBigDecimal(new BigDecimal("5555.123456789"), 6, RoundingMode.HALF_DOWN));
        //System.out.println(""+redondeoDecimalBigDecimal(BigDecimal.ONE, precision, RoundingMode.FLOOR));
//        String a = "<impuesto><codigo>1</codigo><codigoRetencion>341</codigoRetencion><codigoRetencion>355</codigoRetencion><baseImponible>100.00</baseImponible><porcentajeRetener>2.00</porcentajeRetener><valorRetenido>2.00</valorRetenido><codDocSustento>01</codDocSustento><numDocSustento>001002000000070</numDocSustento><fechaEmisionDocSustento>19/01/2015</fechaEmisionDocSustento></impuesto>";
//        java.util.List<String> b = separar(a,"<");
//        for (String c : b){
//            if(c.startsWith("codigo>"))
//                System.out.println(c.substring(c.lastIndexOf("codigo>")+7));
//            if(c.startsWith("codigoRetencion>"))
//                System.out.println(c.substring(c.lastIndexOf("codigoRetencion>")+16));
//        }
    }
   
}