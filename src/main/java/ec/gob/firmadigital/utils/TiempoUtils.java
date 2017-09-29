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

import ec.gob.firmadigital.exceptions.HoraServidorException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.logging.Logger;

/**
 *
 * @author FirmaEC
 */
public class TiempoUtils {
    
    private static final Logger logger = Logger.getLogger(TiempoUtils.class.getName());
    private static String FECHA_HORA_URL="https://api.firmadigital.gob.ec/api/fecha-hora";
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    
    public static Date getFechaHora() throws HoraServidorException {
        String fechaHora;

        try {
            fechaHora = getFechaHoraServidor();
        } catch (IOException e) {
            logger.severe("No se puede obtener la fecha del servidor: "
                    + e.getMessage());
            //return new Date();
            throw new HoraServidorException("Error al obtener fecha y hora del servidor");            
        }

        try {
            TemporalAccessor accessor = DATE_TIME_FORMATTER.parse(fechaHora);
            return Date.from(Instant.from(accessor));
        } catch (DateTimeParseException e) {
            //logger.severe("La fecha indicada ('" + fechaHora + "') no sigue el patron ISO-8601: " + e);
            return new Date();
        }
    }

    public static String getFechaHoraServidor() throws IOException {
        URL obj = new URL(FECHA_HORA_URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        int responseCode = con.getResponseCode();
        logger.fine("GET Response Code: " + responseCode);
        System.out.println("GET Response Code: " + responseCode);

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
