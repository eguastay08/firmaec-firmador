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

import ec.gob.firmadigital.exceptions.DocumentoException;
import io.rubrica.sign.cms.DatosUsuario;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import rst.pdfbox.layout.elements.Document;
import rst.pdfbox.layout.text.Alignment;
import rst.pdfbox.layout.text.Position;
import rst.pdfbox.layout.text.TextFlow;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 *
 * @author jdc
 * @author Asamblea Nacional
 */
public class FirmadorFileUtils {
    
    private static final Logger logger = Logger.getLogger(FirmadorFileUtils.class.getName());

    private static final Integer MAX_TEXT_WIDTH=105;
    
    public static String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static String removeP7MExtension(File file) {
        String name = file.getName();
        int lastPeriodPos = name.lastIndexOf('.');
        System.out.println("Int: " + lastPeriodPos);
        return null;
    }
    
    public static byte[] fileConvertToByteArray(File file) throws IOException {
        Path documentoPath = Paths.get(file.getAbsolutePath());
        return Files.readAllBytes(documentoPath);
    }


    public static byte[] addVisibleSign(File pdfFile, DatosUsuario datosUsuario, Point insPoint, int numPage, String optionalText
            , Boolean noName ) throws IOException {
        ByteArrayOutputStream byteArrayResult = new ByteArrayOutputStream();
        PDDocument pdf= PDDocument.load(pdfFile);

        PDPage page = pdf.getPage(numPage==0?numPage:numPage-1);
        PDRectangle pageSize = page.getMediaBox();

        PDPageContentStream contentStream =
                new PDPageContentStream(pdf, page, PDPageContentStream.AppendMode.APPEND, false);
        if(!noName) {
            TextFlow text = new TextFlow();
            text.setMaxWidth(MAX_TEXT_WIDTH);
            text.addText(datosUsuario.getNombre() + " " + datosUsuario.getApellido(), 12, PDType1Font.HELVETICA_BOLD);
            text.drawText(contentStream, new Position(insPoint.x, pageSize.getHeight() - insPoint.y), Alignment.Left, null);
        }
        TextFlow infoMetadata=new TextFlow();
        infoMetadata.setMaxWidth(MAX_TEXT_WIDTH);
        infoMetadata.addText("Firmado digitalmente por "+datosUsuario.getNombre()+" "+datosUsuario.getApellido()
                        +" "+datosUsuario.getInstitucion()
                        +" Serial: "+datosUsuario.getSerial()
                        +" Fecha: "+datosUsuario.getFechaFirmaArchivo(), 7, PDType1Font.HELVETICA);
        if (!optionalText.isEmpty())
            infoMetadata.addText(" "+optionalText,7, PDType1Font.HELVETICA_OBLIQUE);
        infoMetadata.drawText(contentStream, new Position(insPoint.x+(noName?0:MAX_TEXT_WIDTH),pageSize.getHeight()-insPoint.y)
                , Alignment.Left,null);

        contentStream.close();
        pdf.save(byteArrayResult);
        pdf.close();
        return byteArrayResult.toByteArray();
    }

    public static void saveByteArrayToDisc(byte[] archivo,String rutaNombre) throws FileNotFoundException, IOException {
        // TODO validar si hay otro archivo de momento lo sobre escribe
        FileOutputStream fos = new FileOutputStream(rutaNombre);
        File arc = new File(rutaNombre);
        
        Long espacio = arc.getFreeSpace();
        System.out.println("bytes: " + archivo.length+" espacio " +espacio);
        
        if(espacio < archivo.length){
            throw new IOException("No se puede crear el archivo firmado.  No hay espacio suficiente en el disco");
        }
        
        fos.write(archivo);
        fos.close();
    }
    
    // TODO Crear clase para manejar esto
    public static String crearNombreFirmado(File documento){
        // buscamos el nombre para crear el signed
        // TODO validar si hay otro archivo de momento lo sobre escribe
        // USAR solo getAbsolutPath, talvez sin ruta
        String nombreCompleto = documento.getAbsolutePath();
        
        String nombre = nombreCompleto.replaceFirst("[.][^.]+$", "");

        //String extension = getFileExtension(documento);
        String extension =  FirmadorFileUtils.getFileExtension(documento);
        
        System.out.println(nombre + "-signed." + extension);
        return nombre + "-signed." + extension;
    }
    
    
    public static String crearNombreArchivoP7M(File documento) throws IOException{
        // buscamos el nombre para crear el signed
        // TODO validar si hay otro archivo de momento lo sobre escribe
        // USAR solo getAbsolutPath, talvez sin ruta
        String nombreCompleto = documento.getAbsolutePath();
        String nombreSinP7m = nombreCompleto.replaceFirst("[.][^.]+$", "");
        String extension =  getFileExtension(nombreSinP7m);
        String hora = TiempoUtils.getFechaHoraServidor();
        hora = hora.replace(":", "").replace(" ", "").replace(".", "").replace("-","");
        hora = hora.substring(0,20);
        extension = extension.length()>5?"pdf":extension;
        return nombreSinP7m + "-verified-"+hora+"." + extension;
    }
    
    public static void abrirDocumento(String documento) throws IOException{
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("win")) {
            String cmd = "rundll32 url.dll,FileProtocolHandler " + documento;
            Runtime.getRuntime().exec(cmd);
        } else {
            File doc = new File(documento);
            Desktop.getDesktop().open(doc);
        }        
    }
}
