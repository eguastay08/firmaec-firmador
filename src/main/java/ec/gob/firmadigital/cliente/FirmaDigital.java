/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.firmadigital.cliente;

import ec.gob.firmadigital.utils.FirmadorFileUtils;
import java.io.File;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.List;
import io.rubrica.keystore.Alias;
import io.rubrica.keystore.KeyStoreUtilities;
import io.rubrica.sign.SignConstants;
import io.rubrica.sign.Signer;
import io.rubrica.sign.odf.ODFSigner;
import io.rubrica.sign.ooxml.OOXMLSigner;
import io.rubrica.sign.pdf.PDFSigner;

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
            signedDoc = docSigner.sign(docByteArry,SignConstants.SIGN_ALGORITHM_SHA1WITHRSA , pk, chain, null);  //(documento, pk, chain, null);
            
        }
        return signedDoc;
    }
    
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
    
}
