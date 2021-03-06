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

import ec.gob.firmadigital.crl.ServicioCRL;
import ec.gob.firmadigital.exceptions.CRLValidationException;
import ec.gob.firmadigital.exceptions.CertificadoInvalidoException;
import ec.gob.firmadigital.exceptions.ConexionFirmadorApiException;
import ec.gob.firmadigital.exceptions.ConexionValidarCRLException;
import ec.gob.firmadigital.exceptions.EntidadCertificadoraNoValidaException;
import ec.gob.firmadigital.exceptions.HoraServidorException;
import ec.gob.firmadigital.utils.CertificadoEcUtils;
import ec.gob.firmadigital.utils.KeyStoreUtils;
import ec.gob.firmadigital.utils.TiempoUtils;
import io.rubrica.certificate.CrlUtils;
import io.rubrica.certificate.ValidationResult;
import io.rubrica.core.RubricaException;

import io.rubrica.keystore.Alias;
import io.rubrica.keystore.KeyStoreUtilities;
import io.rubrica.ocsp.OcspValidationException;
import io.rubrica.ocsp.ValidadorOCSP;
import io.rubrica.util.CertificateUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author jdc
 */
public class Validador {
    private KeyStore ks;
    
    private static final String CERTIFICADO_URL = "https://api.firmadigital.gob.ec/api/certificado/revocado";
    private static final Logger logger = Logger.getLogger(Validador.class.getName());
    private Boolean caducado;
    
    public Validador(){
    }
    
    public Validador(KeyStore ks){
        this.ks = ks;
    }
    
    /**
     * Valida primero por OSCP, si falla lo hace por CRL
     * @param cert
     * @return X509Certificate
     * @throws IOException
     * @throws RubricaException si hay un error de conexion con el CRL bota esto, si es por OCSP y falla la conexion intenta por CRL
     * @throws ec.gob.firmadigital.exceptions.HoraServidorException Si es que no puede obtener la hora con el servidor
     * @throws ec.gob.firmadigital.exceptions.CertificadoInvalidoException Si por medio del API nos confirma que esta revocado
     * @throws ec.gob.firmadigital.exceptions.CRLValidationException Si por CRL esta revocado
     * @throws io.rubrica.ocsp.OcspValidationException Si por OCSP nos dice que esta revocado
     * @throws ec.gob.firmadigital.exceptions.EntidadCertificadoraNoValidaException Cuando trata de validar certificados que no son del BCE, CJ o SecurityData
     * @throws ec.gob.firmadigital.exceptions.ConexionValidarCRLException si fallo API, fallo OCSP y fallo la conexion con el CRL
     */
    public X509Certificate validar(X509Certificate cert) throws  HoraServidorException, RubricaException, IOException, CertificadoInvalidoException, CRLValidationException, OcspValidationException, EntidadCertificadoraNoValidaException, ConexionValidarCRLException{
        try {
            BigInteger serial = cert.getSerialNumber();
            Boolean valido = this.validarCrlServidorAPI(serial);
            // Si no es valido botamos exception
            System.out.println(valido);
            if(valido)
                throw new CertificadoInvalidoException("El certificado no es v??lido");

        } catch (IOException | ConexionFirmadorApiException ex) {
            System.out.println("Fallo la validacion por el servicio del API, Ahora intentamos por OCSP");
            Logger.getLogger(Validador.class.getName()).log(Level.SEVERE, null, ex);
            try {
                cert = validarOCSP( cert);
            } catch ( IOException |RubricaException ex1) {
                System.out.println("Fallo la validacion por OCSP, Ahora intentamos por CRL");
                Logger.getLogger(Validador.class.getName()).log(Level.SEVERE, null, ex1);
                cert = validarCRL(cert);
                
            }
        } 

        return cert;
    }
    
    public X509Certificate validarOCSP( X509Certificate cert) throws IOException, OcspValidationException, RubricaException, EntidadCertificadoraNoValidaException{
        List<String> ocspUrls = CertificateUtils.getAuthorityInformationAccess(cert);
        for (String ocsp : ocspUrls) {
            System.out.println("OCSP=" + ocsp);
        }

        System.out.println("OCSPUrls " + ocspUrls.size());

        ValidadorOCSP validadorOCSP = new ValidadorOCSP();
        X509Certificate certRoot  =  CertificadoEcUtils.getRootCertificate(cert); //FirmaDigital.getRootCertificate(cert);

        validadorOCSP.validar(cert, certRoot, ocspUrls);

        return cert;

    }
    
    public X509Certificate validarCRL(X509Certificate cert) throws IOException, RubricaException, CRLValidationException, EntidadCertificadoraNoValidaException, ConexionValidarCRLException{
        System.out.println("Validar CRL");


        for (String url : CertificateUtils.getCrlDistributionPoints(cert)) {
            System.out.println("url=" + url);
        }

        String nombreCA = CertificadoEcUtils.getNombreCA(cert);

        String urlCrl = this.obtenerUrlCRL(CertificateUtils.getCrlDistributionPoints(cert));
        ValidationResult result = null;
        
        if(nombreCA.equals("Banco Central del Ecuador")){
            urlCrl = ServicioCRL.BCE_CRL;
        }
        /*
        if(nombreCA.equals("Consejo de la Judicatura")){
            urlCrl = ServicioCRL.
        }*/
        X509Certificate root = CertificadoEcUtils.getRootCertificate(cert);
        result = CrlUtils.verifyCertificateCRLs(cert, root.getPublicKey(),
                Arrays.asList(urlCrl));
    
    //    System.out.println("Validation result: " + result);

        if(result == result.CANNOT_DOWNLOAD_CRL){
            throw new ConexionValidarCRLException("No se puede validar contra la lista de revocaci??n:"+urlCrl);
        }
        
        // Si el certificado no es valido botamos exception
        if(!result.isValid()){
            throw new CRLValidationException("Certificado Inv??lido");
        }
        
        System.out.println(CertificateUtils.getCN(cert));
        return cert;
    }
    
    public X509Certificate getCert(KeyStore ks, char[] clave) throws KeyStoreException, CertificadoInvalidoException, HoraServidorException, RubricaException {
        String alias = seleccionarAlias(ks);
		if (alias != null){
			X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
			return cert;
		}else
			return null;
    }
    
    public boolean validarCrlServidorAPI(BigInteger serial) throws IOException, ConexionFirmadorApiException {
        URL url = new URL(CERTIFICADO_URL + "/" + serial);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        int responseCode = urlConnection.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            logger.severe(CERTIFICADO_URL + "/" + serial + ": Response Code: " + responseCode);
            //return false;
            throw new ConexionFirmadorApiException("No se pudo conectar API. "+CERTIFICADO_URL + " Response Code: " + responseCode);
        }

        try (InputStream is = urlConnection.getInputStream()) {
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader in = new BufferedReader(reader);
            return Boolean.valueOf(in.readLine());
        }
    }
    
    public void validarFecha(X509Certificate cert) throws HoraServidorException{
        Date fechaActual = TiempoUtils.getFechaHora();
        System.out.println("Fecha actual: " +fechaActual);
        //signingTime.before(cert.getNotBefore()) || signingTime.after(cert.getNotAfter()
        if(fechaActual.before(cert.getNotBefore())){
            
        }
       // if(fechaActual)
    }
    
    private String obtenerUrlCRL(List<String> urls){
        for(String url : urls){
            if(url.toLowerCase().contains("crl"))
                return url;
        }
        return null;
    }
    
    private String resultadosCRL(ValidationResult result) {
        if(result == result.CANNOT_DOWNLOAD_CRL)
            return "No se pudo descargar el archivo CRL\nRevisar conexi??n de Internet";
        if(result.isValid())
            return "V??lido";        
        return "Inv??lido";
    }

    public String seleccionarAlias(KeyStore keyStore) throws RubricaException {
        String aliasString = null;
        // Con que certificado firmar?
        List<Alias> signingAliases = KeyStoreUtilities.getSigningAliases(keyStore);

        if (signingAliases.isEmpty()) {
            throw new RubricaException("No se encontr?? un certificado para firmar");
        }

        if (signingAliases.size() == 1) {
            aliasString = signingAliases.get(0).getAlias();
        } else {
            Alias alias = (Alias) JOptionPane.showInputDialog(null, "Escoja...", "Certificado para firmar",
                    JOptionPane.QUESTION_MESSAGE, null, signingAliases.toArray(), signingAliases.get(0));
			if (alias != null)
				aliasString = alias.getAlias();
        }
        return aliasString;
    }
}