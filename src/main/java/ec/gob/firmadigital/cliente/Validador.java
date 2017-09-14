/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.firmadigital.cliente;

import ec.gob.firmadigital.crl.ServicioCRL;
import io.rubrica.certificate.CrlUtils;
import io.rubrica.certificate.ValidationResult;
import io.rubrica.certificate.ec.bce.BceSubTestCert;
import io.rubrica.certificate.ec.cj.ConsejoJudicaturaSubCert;
import io.rubrica.certificate.ec.securitydata.SecurityDataSubCaCert;
import io.rubrica.core.RubricaException;
import io.rubrica.keystore.KeyStoreProvider;
import io.rubrica.ocsp.OcspValidationException;
import io.rubrica.ocsp.ValidadorOCSP;
import io.rubrica.util.CertificateUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jdc
 */
public class Validador {
    private KeyStore ks;
    private static String FECHA_HORA_URL="http://localhost:8080/api/fecha-hora";
    
    public Validador(){
    }
    
    public Validador(KeyStore ks){
        this.ks = ks;
    }
    
    /**
     * Valida primero por OSCP, si falla lo hace por CRL
     * @param rutaCertificado
     * @param clave
     * @param ks
     * @return X509Certificate
     * @throws KeyStoreException
     * @throws IOException
     * @throws RubricaException 
     */
    public X509Certificate validar(char [] clave,KeyStore ks) throws KeyStoreException, IOException, RubricaException{
        try {
            return validarOCSP( clave,ks);
        } catch (IOException | OcspValidationException | RubricaException ex) {
            Logger.getLogger(Validador.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Fallo la validacion por OCSP, Ahora intentamos por CRL");
            return validarCRL(clave, ks);
        } 
    }
    
    public X509Certificate validarOCSP( char [] clave,KeyStore ks) throws KeyStoreException, IOException, OcspValidationException, RubricaException{
       
        String alias = ks.aliases().nextElement();
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
        Certificate[] cadenaCerts = ks.getCertificateChain(alias);
        System.out.println("cad " + cadenaCerts.length);
        List<X509Certificate> cadena = new ArrayList<>();
        for (int i = 0; i < cadenaCerts.length; i++) {
            cadena.add((X509Certificate) cadenaCerts[i]);
            System.out.println(FirmaDigital.getNombreCA(cadena.get(i)));
        }

        //setearInfoValidacionCertificado(cert);
        List<String> ocspUrls = CertificateUtils.getAuthorityInformationAccess(cert);
        for (String ocsp : ocspUrls) {
            System.out.println("OCSP=" + ocsp);
        }

        System.out.println("OCSPUrls " + ocspUrls.size());

        ValidadorOCSP validadorOCSP = new ValidadorOCSP();
        X509Certificate certRoot = cadena.get(1); //FirmaDigital.getRootCertificate(cert);

        validadorOCSP.validar(cert, certRoot, ocspUrls);

        return cert;

    }
    
    public X509Certificate validarCRL(char [] clave,KeyStore ks) throws KeyStoreException, IOException, RubricaException{
        System.out.println("Validar CRL");
       
        KeyStoreProvider ksp;

        System.out.println("Validar CR2");

    
        X509Certificate cert = (X509Certificate) ks.getCertificate(ks.aliases().nextElement());

        for (String url : CertificateUtils.getCrlDistributionPoints(cert)) {
            System.out.println("url=" + url);
        }

        String nombreCA = FirmaDigital.getNombreCA(cert);

        String urlCrl = this.obtenerUrlCRL(CertificateUtils.getCrlDistributionPoints(cert));
        ValidationResult result;
        String resultStr;
        switch (nombreCA) {
            case "Banco Central del Ecuador":
                //TODO quemado hasta que arreglen en el banco central
                urlCrl = ServicioCRL.BCE_CRL;
                result = CrlUtils.verifyCertificateCRLs(cert, new BceSubTestCert().getPublicKey(),
                        Arrays.asList(urlCrl));
                System.out.println("Validation result: " + result);

                resultStr = resultadosCRL(result);
                break;
            case "Consejo de la Judicatura":
                result = CrlUtils.verifyCertificateCRLs(cert, new ConsejoJudicaturaSubCert().getPublicKey(),
                        Arrays.asList(urlCrl));
                System.out.println("Validation result: " + result);
                resultStr = resultadosCRL(result);
                break;
            case "SecurityData":
                result = CrlUtils.verifyCertificateCRLs(cert, new SecurityDataSubCaCert().getPublicKey(),
                        Arrays.asList(urlCrl));
                System.out.println("Validation result: " + result);
                resultStr = resultadosCRL(result);
                break;
            default:
                resultStr = " Error entidad no reconocida";

                break;
        }

        System.out.println(CertificateUtils.getCN(cert));
        return cert;
           
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
            return "No se pudo descargar el archivo CRL\nRevisar conexión de Internet";
        if(result.isValid())
            return "Válido";        
        return "Inválido";
    }
    
    public Date getFechaHora() {
        String fechaHora;

        try {
            fechaHora = getFechaHoraServidor();
        } catch (IOException e) {
            /*logger.severe("No se puede obtener la fecha del servidor: "
                    + e.getMessage());*/
            return new Date();
        }

        try {
            TemporalAccessor accessor = DATE_TIME_FORMATTER.parse(fechaHora);
            return Date.from(Instant.from(accessor));
        } catch (DateTimeParseException e) {
            //logger.severe("La fecha indicada ('" + fechaHora + "') no sigue el patron ISO-8601: " + e);
            return new Date();
        }
    }

    private String getFechaHoraServidor() throws IOException {
        URL obj = new URL(FECHA_HORA_URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        int responseCode = con.getResponseCode();
        //logger.fine("GET Response Code: " + responseCode);
     

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream is = con.getInputStream();) {
                InputStreamReader reader = new InputStreamReader(is);
                BufferedReader in = new BufferedReader(reader);

                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                return response.toString();
            }
        } else {
            throw new RuntimeException(
            "Error al obtener fecha y hora del servidor");
         }
     }
    
    
}
