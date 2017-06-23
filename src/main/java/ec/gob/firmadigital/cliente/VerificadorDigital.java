/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.firmadigital.cliente;

import ec.gob.firmadigital.firmador.Certificado;
import ec.gob.firmadigital.utils.FirmadorFileUtils;
import io.rubrica.certificate.ec.bce.CertificadoBancoCentralFactory;
import io.rubrica.certificate.ec.cj.CertificadoConsejoJudicaturaDataFactory;
import io.rubrica.certificate.ec.securitydata.CertificadoSecurityDataFactory;
import io.rubrica.core.Util;
import io.rubrica.ocsp.OcspValidationException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import io.rubrica.sign.Verificacion;
import io.rubrica.sign.pdf.VerificadorFirmaPdf;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;

/**
 *
 * @author jdc
 */
public class VerificadorDigital {
    //TODO devolver una f
    public List<Certificado> verificar(File documento) throws IOException, KeyStoreException, OcspValidationException, SignatureException  {
        List<Certificado> certificados = new ArrayList<>();
       /*Date fechax = new Date();
       for(int i= 0; i< 8; i++){
           Boolean valido = generarValidaciones();
           certificados.add(new Certificado("Juan X"+i,"Unidad Certificadora"+i,fechax,fechax,fechax,valido));
       }*/
       String extDocumento = FirmadorFileUtils.getFileExtension(documento);
       byte[] docByteArry = FirmadorFileUtils.fileConvertToByteArray(documento);
       
       switch (extDocumento) {
            case "pdf":
                certificados = verificarPDF(docByteArry);
                break;
            case "docx":
            case "xlsx":
            case "pptx":
                break;
            case "odt":
            case "ods":
            case "odp":
                break;
            default:
        }
       
       return certificados;
    }
   
    private List<Certificado> verificarPDF(byte[] documentoByte) throws IOException, KeyStoreException, OcspValidationException, SignatureException{
        List <Certificado> certs;
        VerificadorFirmaPdf verificador = new VerificadorFirmaPdf(documentoByte);
        Verificacion verificacion = verificador.verificar();
        certs = verificacion.getFirmas().stream().map(p-> new Certificado(
                Util.getCN(p.getCertificadoFirmante()), 
                //p.getCertificadoFirmante().getIssuerDN().getName(), 
                getNombreCA(p.getCertificadoFirmante()),
                dateToCalendar(p.getCertificadoFirmante().getNotBefore()), 
                dateToCalendar(p.getCertificadoFirmante().getNotAfter()), 
                p.getFechaFirma(), 
                //p.isOscpSignatureValid(), 
                esValido(getNombreCA(p.getCertificadoFirmante()), p.isOscpSignatureValid()),
                p.isOscpRevocationValid() )).collect(Collectors.toList());
        
        return certs;
    }
    
    private Calendar dateToCalendar(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;

    }
    
    /**
     * funcion temporal para verificar contral el banco central porque cambio el endpoint o algo!!!!
     */
    private boolean esValido(String nombreCA, boolean validez){
        if(nombreCA.equals("Banco Central del Ecuador") )
            return true;
        return validez;
    }
    
    //TODO poner los nombres como constantes
    private String getNombreCA(X509Certificate certificado){
        if(CertificadoBancoCentralFactory.esCertificadoDelBancoCentral(certificado))
            return "Banco Central del Ecuador";
        
        if(CertificadoConsejoJudicaturaDataFactory.esCertificadoDelConsejoJudicatura(certificado))
            return "Consejo de la Judicatura";
        
        if(CertificadoSecurityDataFactory.esCertificadoDeSecurityData(certificado))
            return "SecurityData";
        
        /******************************************
         * TODO comentar esto para produccion
         */
        if(CertificadoBancoCentralFactory.estTestCa(certificado))
            return "Banco Central del Ecuador Test, de prueba DEFINITIVAMENTE NO OFICIAL!!!!!!!!!!";
        /**
         * Fin de cambio para produccion
         */
        return "Entidad no reconocidad: "+certificado.getIssuerDN().getName();
    }
}
