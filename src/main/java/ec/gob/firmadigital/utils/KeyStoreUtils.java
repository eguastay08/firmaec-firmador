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
package ec.gob.firmadigital.utils;

import ec.gob.firmadigital.cliente.Validador;
import ec.gob.firmadigital.exceptions.HoraServidorException;
import io.rubrica.keystore.Alias;
import io.rubrica.util.CertificateUtils;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author FirmaEC
 */
public class KeyStoreUtils {

    private static final Logger logger = Logger.getLogger(KeyStoreUtils.class.getName());

    public static List<Alias> getSigningAliases(KeyStore keyStore) throws HoraServidorException {
        try {
            Enumeration<String> aliases = keyStore.aliases();
            List<Alias> aliasList = new ArrayList<>();
            
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

                /*try {
                    certificate.checkValidity(currentDate);
                } catch (CertificateExpiredException | CertificateNotYetValidException e) {
                    logger.warning("Certificado expirado: " + certificate.getIssuerX500Principal().toString());
                    continue;
                }*/

                String name = CertificateUtils.getCN(certificate);
                boolean[] keyUsage = certificate.getKeyUsage();

              
                if (keyUsage != null) {
                    // Certificado para Firma Digital
                    if (keyUsage[0]) {
                        aliasList.add(new Alias(alias, name));
                    }
                }
            }

            return aliasList;
        } catch (KeyStoreException e) {
            throw new IllegalStateException(e);
        }
    }
    
    public static List<Alias> getSigningAliases(KeyStore keyStore , Date currentDate) throws HoraServidorException {
        try {
            Enumeration<String> aliases = keyStore.aliases();
            List<Alias> aliasList = new ArrayList<>();
            
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

                try {
                    certificate.checkValidity(currentDate);
                } catch (CertificateExpiredException | CertificateNotYetValidException e) {
                    logger.warning("Certificado expirado: " + certificate.getIssuerX500Principal().toString());
                    //continue;
                }

                String name = CertificateUtils.getCN(certificate);
                boolean[] keyUsage = certificate.getKeyUsage();

              
                if (keyUsage != null) {
                    // Certificado para Firma Digital
                    if (keyUsage[0]) {
                        aliasList.add(new Alias(alias, name));
                    }
                }
            }

            return aliasList;
        } catch (KeyStoreException e) {
            throw new IllegalStateException(e);
        }
    }
}
