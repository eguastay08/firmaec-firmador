/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.firmadigital.cliente.pdf;

import ec.gob.firmadigital.firmador.Certificado;
import ec.gob.firmadigital.utils.FirmadorFileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import rubrica.sign.Signer;
import rubrica.sign.odf.ODFSigner;
import rubrica.sign.ooxml.OOXMLSigner;
import rubrica.sign.pdf.PDFSigner;

/**
 *
 * @author jdc
 */
public class VerificadorDigital {
    //TODO devolver una f
    public List<Certificado> verificar(File documento)  {
       List<Certificado> certificados = new ArrayList<>();
       Date fechax = new Date();
       for(int i= 0; i< 8; i++){
           Boolean valido = generarValidaciones();
           certificados.add(new Certificado("Juan X"+i,"Unidad Certificadora"+i,fechax,fechax,fechax,valido));
       }
       return certificados;
    }
    // TODO  cambiar por verifiers
    private Signer documentSigner(File documento){
        String extDocumento = FirmadorFileUtils.getFileExtension(documento);
        switch (extDocumento) {
            case "pdf":
                return new PDFSigner();
            case "docx":
            case "xlsx":
            case "pptx":
                return new OOXMLSigner();
            case "odt":
            case "ods":
            case "odp":
                return new ODFSigner();
            default:
                return null;
        }
    }
    
    // Funcion temporal para general
    private Boolean generarValidaciones(){
        return Math.random() < 0.5;
    }
}
