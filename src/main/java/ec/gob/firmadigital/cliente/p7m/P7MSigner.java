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
package ec.gob.firmadigital.cliente.p7m;

import io.rubrica.core.RubricaException;
import io.rubrica.sign.InvalidFormatException;
import io.rubrica.sign.SignInfo;
import io.rubrica.sign.Signer;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 *
 * @author FirmaEC
 * @author Asamblea Nacional
 */
public class P7MSigner implements Signer{
    
    private static final Logger logger = Logger.getLogger(P7MSigner.class.getName());

    @Override
    public byte[] sign(byte[] data, String algorithm, PrivateKey key, Certificate[] certChain, Properties extraParams, Boolean visibleInfo) throws RubricaException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SignInfo> getSigners(byte[] sign) throws InvalidFormatException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
