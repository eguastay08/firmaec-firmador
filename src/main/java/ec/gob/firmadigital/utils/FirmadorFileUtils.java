/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.firmadigital.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author jdc
 */
public class FirmadorFileUtils {
    public static String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static byte[] fileConvertToByteArray(File file) throws IOException {
        Path documentoPath = Paths.get(file.getAbsolutePath());
        return Files.readAllBytes(documentoPath);
    }
    
    public static void saveByteArrayToDisc(byte[] archivo,String rutaNombre) throws FileNotFoundException, IOException {
        // TODO validar si hay otro archivo de momento lo sobre escribe
        FileOutputStream fos = new FileOutputStream(rutaNombre);
        fos.write(archivo);
        fos.close();
    }
}
