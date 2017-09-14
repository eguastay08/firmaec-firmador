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
