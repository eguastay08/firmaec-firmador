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
package ec.gob.firmadigital.cliente;

import ec.gob.firmadigital.exceptions.CRLValidationException;
import ec.gob.firmadigital.exceptions.CertificadoInvalidoException;
import ec.gob.firmadigital.exceptions.ConexionInvalidaOCSPException;
import ec.gob.firmadigital.exceptions.ConexionValidarCRLException;
import ec.gob.firmadigital.exceptions.DocumentoException;
import ec.gob.firmadigital.exceptions.EntidadCertificadoraNoValidaException;
import ec.gob.firmadigital.exceptions.HoraServidorException;
import ec.gob.firmadigital.firmador.Certificado;
import ec.gob.firmadigital.utils.CertificadoEcUtils;
import ec.gob.firmadigital.utils.FirmadorFileUtils;
import ec.gob.firmadigital.utils.TiempoUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.rubrica.core.RubricaException;
import io.rubrica.core.SignatureVerificationException;
import io.rubrica.keystore.Alias;
import io.rubrica.keystore.KeyStoreUtilities;
import io.rubrica.ocsp.OcspValidationException;
import io.rubrica.sign.InvalidFormatException;
import io.rubrica.sign.SignConstants;
import io.rubrica.sign.SignInfo;
import io.rubrica.sign.Signer;
import io.rubrica.sign.odf.ODFSigner;
import io.rubrica.sign.ooxml.OOXMLSigner;
import io.rubrica.sign.pdf.PDFSigner;
import io.rubrica.core.Util;
import io.rubrica.sign.cms.DatosUsuario;
import io.rubrica.sign.pdf.PdfUtil;
import io.rubrica.sign.xades.XAdESSigner;
import java.security.cert.CertificateEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jdc
 * @author Asamblea Nacional
 *
 */
public class FirmaDigital {
    //Position and page fields to visualize the signature was added
    public byte[] firmar(KeyStore keyStore, String alias, byte[] documento, File pathFile, char[] clave
            , Rectangle position, int page) throws Exception {
        System.out.println("Firmando ");
        Signer signer = this.documentSigner(pathFile);

        // Propiedades para personalizar la firma
        Properties params = new Properties();
        params.setProperty(PDFSigner.SIGNING_LOCATION, "");
        params.setProperty(PDFSigner.SIGNING_REASON, "Firmado digitalmente Asamblea Nacional & firmaEC");
        params.setProperty(PDFSigner.SIGN_TIME, TiempoUtils.getFechaHoraServidor());

        //Set page number
        params.setProperty(PDFSigner.LAST_PAGE,String.valueOf(page));
        //Set position
        params.setProperty(PdfUtil.positionOnPageLowerLeftX,String.valueOf(position.x));
        params.setProperty(PdfUtil.positionOnPageLowerLeftY,String.valueOf(position.y));
        params.setProperty(PdfUtil.positionOnPageUpperRightX,String.valueOf(position.width));
        params.setProperty(PdfUtil.positionOnPageUpperRightY,String.valueOf(position.height));

        // Buscar el PrivateKey en el KeyStore:
        PrivateKey key = (PrivateKey) keyStore.getKey(alias, clave);

        // Obtener el certificate chain:
        Certificate[] certChain = keyStore.getCertificateChain(alias);

        return signer.sign(documento, SignConstants.SIGN_ALGORITHM_SHA1WITHRSA, key, certChain, params, false);
    }

    public byte[] firmar(KeyStore keyStore, String alias, File documento, char[] clave) throws Exception {
        byte[] docByteArry = FirmadorFileUtils.fileConvertToByteArray(documento);
		Signer signer = this.documentSigner(documento);

        // Propiedades para personalizar la firma
        Properties params = new Properties();
        params.setProperty(PDFSigner.SIGNING_LOCATION, "");
        //Credits modified
        params.setProperty(PDFSigner.SIGNING_REASON, "Firmado digitalmente Asamblea Nacional & FirmaEC");
        params.setProperty(PDFSigner.SIGN_TIME, TiempoUtils.getFechaHoraServidor());

        // Posicion firma
        params.setProperty(PDFSigner.LAST_PAGE, "1");
        params.setProperty(PdfUtil.positionOnPageLowerLeftX, "10");
        params.setProperty(PdfUtil.positionOnPageLowerLeftY, "830");

        // Buscar el PrivateKey en el KeyStore:
        PrivateKey key = (PrivateKey) keyStore.getKey(alias, clave);

        // Obtener el certificate chain:
        Certificate[] certChain = keyStore.getCertificateChain(alias);

		return signer.sign(docByteArry, SignConstants.SIGN_ALGORITHM_SHA1WITHRSA, key, certChain, params,true);
    }

    public List<Certificado> verificar(File documento) throws IOException, KeyStoreException, OcspValidationException, SignatureException, InvalidFormatException, RubricaException, ConexionInvalidaOCSPException, HoraServidorException, CertificadoInvalidoException, EntidadCertificadoraNoValidaException, ConexionValidarCRLException, SignatureVerificationException, DocumentoException {

        byte[] docByteArry = FirmadorFileUtils.fileConvertToByteArray(documento);
        // para P7m, ya que p7m no tiene signer
        String extDocumento = FirmadorFileUtils.getFileExtension(documento);
        if(extDocumento.toLowerCase().equals("p7m")){
            VerificadorP7M verificador = new VerificadorP7M();
            byte[] archivoOriginal = verificador.verify(docByteArry);
            
            String nombreArchivo = FirmadorFileUtils.crearNombreArchivoP7M(documento);
            
            FirmadorFileUtils.saveByteArrayToDisc(archivoOriginal, nombreArchivo);
            //System.out.println(nombreArchivo);
            
            FirmadorFileUtils.abrirDocumento(nombreArchivo);
                
            return datosP7MACertificado(verificador.certificados, verificador.fechasFirmados);
        } else {
            Signer docSigner = documentSigner(documento);
            System.out.println("Verificar");
            List<Certificado> certificados = firmasToCertificados(docSigner.getSigners(docByteArry));
            System.out.println("paso Verificar");

            return certificados;
        }
    }
    
    private List<Certificado> datosP7MACertificado(List<X509Certificate> certificados,List<Date> fechasFirmados) throws RubricaException, HoraServidorException, IOException, CertificadoInvalidoException, EntidadCertificadoraNoValidaException, ConexionValidarCRLException {
        List<Certificado> certs = new ArrayList<>();
        /*
        if(fechasFirmados.size() != certificados.size()){
            throw error;
        }*/
        for(int i=0; i<certificados.size(); i++){
            X509Certificate temp = certificados.get(i);
            Date fechaFirmado = fechasFirmados.get(i);
            DatosUsuario datosUsuario = CertificadoEcUtils.getDatosUsuarios(temp);
            Certificado c = new Certificado(
                    Util.getCN(temp),
                    CertificadoEcUtils.getNombreCA(temp),
                    dateToCalendar(temp.getNotBefore()),
                    dateToCalendar(temp.getNotAfter()),
                    dateToCalendar(fechaFirmado),
                    esValido(temp, fechaFirmado),
                    esRevocado(temp),
                    datosUsuario);
            
            certs.add(c);
        }
        return certs;
    }

    private Signer documentSigner(File documento) {
        String extDocumento = FirmadorFileUtils.getFileExtension(documento);
        switch (extDocumento.toLowerCase()) {
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
            case "xml":
                return new XAdESSigner();
            default:
                return null;
        }
    }

    private List<Certificado> firmasToCertificados(List<SignInfo> firmas) throws RubricaException, ConexionInvalidaOCSPException, HoraServidorException, KeyStoreException, IOException, CertificadoInvalidoException, EntidadCertificadoraNoValidaException, ConexionValidarCRLException, DocumentoException {
        List<Certificado> certs = new ArrayList<>();
        System.out.println("firmas a certificados");
        
        if(firmas == null || firmas.size()==0){
            throw new DocumentoException("Documento sin firmas");
        }

        for (SignInfo temp : firmas) {
            temp.getCerts();
            DatosUsuario datosUsuario = CertificadoEcUtils.getDatosUsuarios(temp.getCerts()[0]);
            
            System.out.println(CertificadoEcUtils.getNombreCA(temp.getCerts()[0]));
            if(datosUsuario == null){
            System.out.println("datos usuarios nulos");
            }
            
            if(datosUsuario == null){
                byte [] file;
                try {
                    file = temp.getCerts()[0].getEncoded();
                    FirmadorFileUtils.saveByteArrayToDisc(file, "/tmp/certificadojudicatura.crt");
                } catch (CertificateEncodingException ex) {
                    Logger.getLogger(FirmaDigital.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
                
            
            Certificado c = new Certificado(
                    Util.getCN(temp.getCerts()[0]),
                    CertificadoEcUtils.getNombreCA(temp.getCerts()[0]),
                    dateToCalendar(temp.getCerts()[0].getNotBefore()),
                    dateToCalendar(temp.getCerts()[0].getNotAfter()),
                    dateToCalendar(temp.getSigningTime()),
                    esValido(temp.getCerts()[0], temp.getSigningTime()),
                    esRevocado(temp.getCerts()[0]),
                    datosUsuario);
            
            certs.add(c);
        }
        
        return certs;
    }

    private Calendar dateToCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    /**
     * Si el certificado ya caduco
     * @param cert
     * @param signingTime
     * @return 
     */
    private boolean esValido(X509Certificate cert, Date signingTime) {
        return !( signingTime.before(cert.getNotBefore()) || signingTime.after(cert.getNotAfter())) ;
    }

    /**
     * Verifica si el certificado ha sido revocado 
     * @param cert
     * @return
     * @throws RubricaException
     * @throws HoraServidorException
     * @throws IOException
     * @throws CertificadoInvalidoException 
     */
    private Boolean esRevocado(X509Certificate cert) throws RubricaException, HoraServidorException, IOException, CertificadoInvalidoException, EntidadCertificadoraNoValidaException, ConexionValidarCRLException {
        try {
            System.out.println("Revisamos si es valido el certificado contra el servicio del API");
            
            Validador validador = new Validador();
            validador.validar(cert);
            
            return false;
        } catch (CRLValidationException ex) {
            Logger.getLogger(FirmaDigital.class.getName()).log(Level.SEVERE, null, ex);
            return true;
        } catch (OcspValidationException ex) {
            Logger.getLogger(FirmaDigital.class.getName()).log(Level.SEVERE, null, ex);
            return true;
        }
    }
}
