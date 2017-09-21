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
import ec.gob.firmadigital.exceptions.EntidadCertificadoraNoValidaException;
import ec.gob.firmadigital.exceptions.HoraServidorException;
import ec.gob.firmadigital.firmador.DatosUsuario;
import ec.gob.firmadigital.firmador.Certificado;
import ec.gob.firmadigital.utils.CertificadoEcUtils;
import ec.gob.firmadigital.utils.FirmadorFileUtils;
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
import io.rubrica.sign.xades.XAdESSigner;
import java.util.Properties;
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
        
        Properties params = new Properties();
        Validador validador = new Validador();
        params.setProperty("signingTime", validador.getFechaHoraServidor());

        for (Alias alias : signingAliases) {

            PrivateKey pk = (PrivateKey) keyStore.getKey(alias.getAlias(), clave);
            Certificate[] chain = keyStore.getCertificateChain(alias.getAlias());
            // (byte[] data, String algorithm, PrivateKey key, Certificate[] certChain, Properties xParams)
            Signer docSigner = documentSigner(documento);
            
            signedDoc = docSigner.sign(docByteArry, SignConstants.SIGN_ALGORITHM_SHA1WITHRSA, pk, chain, params);  //(documento, pk, chain, null);

        }
        return signedDoc;
    }

    public List<Certificado> verificar(File documento) throws IOException, KeyStoreException, OcspValidationException, SignatureException, InvalidFormatException, RubricaException, ConexionInvalidaOCSPException, HoraServidorException, CertificadoInvalidoException, EntidadCertificadoraNoValidaException {

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
//IMPLEMENTAR P7M
            case "p7m":
                return new PDFSigner();
//IMPLEMENTAR P7M
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

    private List<Certificado> firmasToCertificados(List<SignInfo> firmas) throws RubricaException, ConexionInvalidaOCSPException, HoraServidorException, KeyStoreException, IOException, CertificadoInvalidoException, EntidadCertificadoraNoValidaException {
        List<Certificado> certs = new ArrayList<Certificado>();
        System.out.println("firmas a certificados");

        for (SignInfo temp : firmas) {
            temp.getCerts();
            DatosUsuario datosUsuario = CertificadoEcUtils.getDatosUsuarios(temp.getCerts()[0]);
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
    private Boolean esRevocado(X509Certificate cert) throws RubricaException, HoraServidorException, IOException, CertificadoInvalidoException, EntidadCertificadoraNoValidaException {
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
