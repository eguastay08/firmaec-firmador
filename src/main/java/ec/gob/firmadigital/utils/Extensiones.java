/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ec.gob.firmadigital.utils;

import java.io.File;

/**
 *
 * @author jack
 */
public class Extensiones {
    public final static String ods = "ods";
    public final static String odt = "odt";
    public final static String odp = "odp";
    public final static String pdf = "pdf";
    public final static String xml = "xml";
    public final static String docx = "docx";
    public final static String xlsx = "xlsx";
    public final static String pptx = "pptx";

    /*
     * Get the extension of a file.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

}
