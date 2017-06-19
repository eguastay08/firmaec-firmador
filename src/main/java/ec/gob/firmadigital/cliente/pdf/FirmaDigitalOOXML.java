/*
 * Firma Digital: Cliente
 * Copyright (C) 2017 Secretaría Nacional de la Administración Pública
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

package ec.gob.firmadigital.cliente.pdf;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.List;

import rubrica.keystore.Alias;
import rubrica.keystore.KeyStoreUtilities;
import rubrica.sign.SignConstants;
import rubrica.sign.Signer;
import rubrica.sign.ooxml.OOXMLSigner;
import rubrica.sign.pdf.PDFSigner;
import rubrica.util.BouncyCastleUtils;

/**
 * Clase utilitaria para firmar digitalmente un MSOffice mediante la libreria
 * Rubrica.
 * 
 * @author Ricardo Arguello <ricardo.arguello@soportelibre.com>
 */
public class FirmaDigitalOOXML {

    static {
        BouncyCastleUtils.initializeBouncyCastle();
    }

    /**
     * Firmar un documento PDF usando un KeyStore y una clave.
     * 
     * @param keyStore
     * @param documento
     * @param clave
     * @return
     * @throws Exception
     */
    public byte[] firmar(KeyStore keyStore, byte[] documento, String clave) throws Exception {
        char[] password = (clave != null) ? clave.toCharArray() : null;
        List<Alias> signingAliases = KeyStoreUtilities.getSigningAliases(keyStore);

        byte[] signedMSOffice = null;
        
        for (Alias alias : signingAliases) {
            PrivateKey pk = (PrivateKey) keyStore.getKey(alias.getAlias(), password);
            Certificate[] chain = keyStore.getCertificateChain(alias.getAlias());
            // (byte[] data, String algorithm, PrivateKey key, Certificate[] certChain, Properties xParams)
            Signer oOXMLSigner = new OOXMLSigner();
            signedMSOffice = oOXMLSigner.sign(documento,SignConstants.SIGN_ALGORITHM_SHA1WITHRSA , pk, chain, null);  //(documento, pk, chain, null);
            
        }
        return signedMSOffice;
    }
    
    /**
     * 
     * @param keyStore
     * @param documento
     * @param clave
     * @return
     * @throws Exception
     */
    public byte[] firmar(KeyStore keyStore, byte[] documento, char[] clave) throws Exception {
        System.out.println("Firmando OOXML");
    
        List<Alias> signingAliases = KeyStoreUtilities.getSigningAliases(keyStore);

        byte[] signedMSOffice = null;
        
        for (Alias alias : signingAliases) {
 
            PrivateKey pk = (PrivateKey) keyStore.getKey(alias.getAlias(), clave);
            Certificate[] chain = keyStore.getCertificateChain(alias.getAlias());
            // (byte[] data, String algorithm, PrivateKey key, Certificate[] certChain, Properties xParams)
            Signer oOXMLSigner = new OOXMLSigner();
            signedMSOffice = oOXMLSigner.sign(documento,SignConstants.SIGN_ALGORITHM_SHA1WITHRSA , pk, chain, null);  //(documento, pk, chain, null);
            
        }
        return signedMSOffice;
    }
}
