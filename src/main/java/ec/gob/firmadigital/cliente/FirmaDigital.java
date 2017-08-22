/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.firmadigital.cliente;

import ec.gob.firmadigital.exceptions.EntidadCertificadoraNoValidaException;
import ec.gob.firmadigital.firmador.Certificado;
import ec.gob.firmadigital.utils.FirmadorFileUtils;
import io.rubrica.certificate.ec.bce.BceCaCert;
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
import java.util.stream.Collectors;

import io.rubrica.certificate.ec.bce.CertificadoBancoCentralFactory;
import io.rubrica.certificate.ec.cj.CertificadoConsejoJudicaturaDataFactory;
import io.rubrica.certificate.ec.cj.ConsejoJudicaturaCaCert;
import io.rubrica.certificate.ec.securitydata.CertificadoSecurityDataFactory;
import io.rubrica.certificate.ec.securitydata.SecurityDataCaCert;
import io.rubrica.core.RubricaException;
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
import io.rubrica.ocsp.ValidadorOCSP;
import io.rubrica.sign.xades.XAdESSigner;
import io.rubrica.util.CertificateUtils;
import io.rubrica.util.OcspUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jdc
 */
public class FirmaDigital {

    public byte[] firmar(KeyStore keyStore, File documento, char[] clave) throws Exception {
        System.out.println("Firmando ");
        byte[] docByteArry = FirmadorFileUtils.fileConvertToByteArray(documento);

        List<Alias> signingAliases = KeyStoreUtilities.getSigningAliases(keyStore);

        byte[] signedDoc = null;

        for (Alias alias : signingAliases) {

            PrivateKey pk = (PrivateKey) keyStore.getKey(alias.getAlias(), clave);
            Certificate[] chain = keyStore.getCertificateChain(alias.getAlias());
            // (byte[] data, String algorithm, PrivateKey key, Certificate[] certChain, Properties xParams)
            Signer docSigner = documentSigner(documento);
            signedDoc = docSigner.sign(docByteArry, SignConstants.SIGN_ALGORITHM_SHA1WITHRSA, pk, chain, null);  //(documento, pk, chain, null);

        }
        return signedDoc;
    }

    public List<Certificado> verificar(File documento) throws IOException, KeyStoreException, OcspValidationException, SignatureException, InvalidFormatException, RubricaException {

        byte[] docByteArry = FirmadorFileUtils.fileConvertToByteArray(documento);
        Signer docSigner = documentSigner(documento);
        System.out.println("Verificar");
        List<Certificado> certificados = firmasToCertificados(docSigner.getSigners(docByteArry));
        System.out.println("paso Verificar");
        
        return certificados;
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

    private List<Certificado> firmasToCertificados(List<SignInfo> firmas) throws RubricaException {
        List<Certificado> certs = new ArrayList<Certificado>();
        System.out.println("firmas a certificados");
        
        /*certs = firmas.stream().map(p -> new Certificado(
                Util.getCN(p.getCerts()[0]),
                //p.getCertificadoFirmante().getIssuerDN().getName(), 
                getNombreCA(p.getCerts()[0]),
                dateToCalendar(p.getCerts()[0].getNotBefore()),
                dateToCalendar(p.getCerts()[0].getNotAfter()),
                dateToCalendar(p.getSigningTime()),
                //p.isOscpSignatureValid(), 
                esValido(p.getCerts()[0], p.getSigningTime()),
                esRevocado(p.getCerts()[0]))).collect(Collectors.toList());*/
        
        for (SignInfo temp : firmas) {
            Certificado c = new Certificado(
                    Util.getCN(temp.getCerts()[0]),
                    getNombreCA(temp.getCerts()[0]),
                    dateToCalendar(temp.getCerts()[0].getNotBefore()),
                    dateToCalendar(temp.getCerts()[0].getNotAfter()),
                    dateToCalendar(temp.getSigningTime()),
                    esValido(temp.getCerts()[0], temp.getSigningTime()),
                    esRevocado(temp.getCerts()[0]));
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
     * funcion temporal para verificar contral el banco central porque cambio el
     * endpoint o algo!!!!
     */
    private boolean esValido(X509Certificate cert, Date signingTime) {
        return !( signingTime.before(cert.getNotBefore()) || signingTime.after(cert.getNotAfter())) ;
    }
    
      /**
     * funcion temporal para verificar contral el banco central porque cambio el
     * endpoint o algo!!!!
     */
    private Boolean esRevocado(X509Certificate cert) throws RubricaException {
        System.out.println("Revisamos si es valido el certificado contra un servicio OCSP");

        List<String> ocspUrls;
        try {
            ocspUrls = CertificateUtils.getAuthorityInformationAccess(cert);
            for (String ocsp : ocspUrls) {
                System.out.println("OCSP=" + ocsp);
            }
            X509Certificate certRoot = getRootCertificate(cert);
            ValidadorOCSP validadorOCSP = new ValidadorOCSP();
            validadorOCSP.validar(cert, certRoot, ocspUrls);
            return true;
        } catch (IOException |EntidadCertificadoraNoValidaException |OcspValidationException ex) {
            Logger.getLogger(FirmaDigital.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } 
        
        
    }
    
    public static X509Certificate getRootCertificate(X509Certificate cert) throws EntidadCertificadoraNoValidaException{
        String entidadCertStr = getNombreCA(cert);
        switch(entidadCertStr){
            case "Banco Central del Ecuador":
                return new BceCaCert();
            case "Consejo de la Judicatura":
                return new ConsejoJudicaturaCaCert();
            case "SecurityData":
                return new SecurityDataCaCert();
            default:
                throw new EntidadCertificadoraNoValidaException("Entidad Certificador no encontra");
        }
    }

    //TODO poner los nombres como constantes
    public static String getNombreCA(X509Certificate certificado) {
        if (CertificadoBancoCentralFactory.esCertificadoDelBancoCentral(certificado)) {
            return "Banco Central del Ecuador";
        }

        if (CertificadoConsejoJudicaturaDataFactory.esCertificadoDelConsejoJudicatura(certificado)) {
            return "Consejo de la Judicatura";
        }

        if (CertificadoSecurityDataFactory.esCertificadoDeSecurityData(certificado)) {
            return "SecurityData";
        }

        /**
         * ****************************************
         * TODO comentar esto para produccion
         */
        if (CertificadoBancoCentralFactory.estTestCa(certificado)) {
            return "Banco Central del Ecuador Test, de prueba DEFINITIVAMENTE NO OFICIAL!!!!!!!!!!";
        }
        /**
         * Fin de cambio para produccion
         */
        return "Entidad no reconocidad: " + certificado.getIssuerDN().getName();
    }
}
