package ec.gob.firmadigital.firmador;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;

import org.junit.Assert;
import org.junit.Test;

import rubrica.sign.Signer;
import rubrica.sign.pdf.PDFSigner;

public class FirmadorTest {
	private static final String CERT_PATH = "PRUEBA_FPUBLICO_RARGUELLO.p12";
	private static final String CERT_PASS = "12345678";
	private static final String CERT_ALIAS = "PRUEBA FPUBLICO MARCO RICARDO ARGUELLO JACOME's SECURITY DATA S.A. ID";
	private static final String DATA_FILE_PDF = "test3622650796956702606.pdf";
	
	@Test
	public void testPDFsignature()  {
		PrivateKeyEntry pke;
        try {
            pke = loadKeyEntry("/home/jdc/test/"+CERT_PATH, CERT_PASS, CERT_ALIAS);
            
        
		//byte[] pdf = Utils.getDataFromInputStream(ClassLoader.getSystemResourceAsStream(DATA_FILE_PDF));
            Path documentoPath = Paths.get("/home/jdc/test/" + DATA_FILE_PDF);
            // byte[] dataDocumento = Files.readAllBytes(documentoPath);
            byte[] pdf = Files.readAllBytes(documentoPath);
            File tempFile = File.createTempFile("pdfSign", "." + DATA_FILE_PDF);
            System.out.println("Temporal para comprobacion manual: " + tempFile.getAbsolutePath());

            if (pke == null)
                System.out.println("Es nulo");

            try (final FileOutputStream fos = new FileOutputStream(tempFile);) {
                Signer signer = new PDFSigner();
                byte[] result = signer.sign(pdf, null, pke.getPrivateKey(), pke.getCertificateChain(), null);
                fos.write(result);
                fos.flush();

                Assert.assertNotNull(result);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	


	private static PrivateKeyEntry loadKeyEntry(String certPath, String certPass, String certAlias) throws Exception {
		KeyStore ks = KeyStore.getInstance("PKCS12");
	
		ks.load(new FileInputStream(certPath), certPass.toCharArray());
		return (PrivateKeyEntry) ks.getEntry(certAlias, new KeyStore.PasswordProtection(certPass.toCharArray()));
	}

}
