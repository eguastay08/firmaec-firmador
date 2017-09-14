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
package ec.gob.firmadigital.crl;

import io.rubrica.util.HttpClient;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;

/**
 *
 * @author jdc
 */
public class ServicioCRL {
    public static final String BCE_CRL = "http://www.eci.bce.ec/CRL/eci_bce_ec_crlfilecomb.crl";
    public static final String SD_CRL = "https://direct.securitydata.net.ec/~crl/autoridad_de_certificacion_sub_security_data_entidad_de_certificacion_de_informacion_curity_data_s.a._c_ec_crlfile.crl";

    public static X509CRL downloadCrl(String url) throws Exception {
        byte[] content;

        HttpClient http = new HttpClient();
        content = http.download(url);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509CRL) cf.generateCRL(new ByteArrayInputStream(content));

    }
    
}
