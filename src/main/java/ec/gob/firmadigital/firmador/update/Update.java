/*
 * Firma Digital: Firmador
 * Copyright 2017 Secretaría Nacional de la Administración Pública
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

package ec.gob.firmadigital.firmador.update;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

/**
 * Permite actualizar el JAR de la aplicacion.
 *
 * @author Ricardo Arguello <ricardo.arguello@soportelibre.com>
 */
public class Update {

    private static final String JAR_BASE_URL = "https://firmadigital.ec/firmadigital.gob.ec/jar";
    private static final String JAR_NAME = "firmador-jar-with-dependencies.jar";
    private static final String JAR_URL = JAR_BASE_URL + "/" + JAR_NAME;
    private static final String JAR_SHA256_URL = JAR_BASE_URL + "/" + JAR_NAME + ".sha256";

    private static final int BUFFER_SIZE = 8192;

    private static final Logger logger = Logger.getLogger(Update.class.getName());

    public void updateCliente() throws IOException {
        String hashBajado = new String(download(JAR_SHA256_URL));
        logger.info("hashBajado=" + hashBajado);

        String hash;
        StringTokenizer st = new StringTokenizer(hashBajado);

        if (st.hasMoreTokens()) {
            hash = (st.nextToken());
        } else {
            throw new RuntimeException("Archivo SHA256 invalido!");
        }

        // Se debe descargar?
        String path = rutaJar();
        logger.info("path=" + path);

        File file = new File(path);
        logger.info("file=" + file.getAbsolutePath() + "; canWrite=" + file.canWrite() + ";file.getName()="
                + file.getName());

        byte[] actualJar = Files.readAllBytes(file.toPath());
        String actualHash = generateHash(actualJar);
        logger.info("actualHash=" + actualHash);

        if (actualHash.equals(hash)) {
            logger.info("Ya tiene el ultimo archivo!");
            return;
        } else {
            logger.info("No tiene la ultima version, descargando...");
        }

        // Descargar JAR actualizado
        byte[] jar = download(JAR_URL);

        if (!verifyHash(hash, jar)) {
            logger.severe("ERROR de verificacion de hash");
            return;
        }

        logger.info("Hash comprobado OK");

        if (!file.getName().equals(JAR_NAME)) {
            logger.severe("El nombre del archivo no es " + JAR_NAME);
            return;
        }

        if (!file.canWrite()) {
            logger.severe("No se puede actualizar el archivo");
            return;
        }

        try (FileOutputStream fileOuputStream = new FileOutputStream(file)) {
            fileOuputStream.write(jar);
            logger.info("Actualizado con exito!!!");
            return;
        }
    }

    private byte[] download(String strUrl) throws IOException {
        URL url = new URL(strUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.connect();

        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
        }

        byte[] buffer = new byte[BUFFER_SIZE];
        int count;
        long size = con.getContentLength();
        logger.info("size=" + size);

        try (InputStream in = con.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }

            return out.toByteArray();
        }
    }

    private boolean verifyHash(String hash, byte[] jar) {
        return hash.equals(generateHash(jar));
    }

    private String generateHash(byte[] jar) {
        try {
            logger.info("Hashing...");
            MessageDigest md;
            md = MessageDigest.getInstance("SHA-256");
            md.update(jar);
            byte[] digest = md.digest();
            return DatatypeConverter.printHexBinary(digest).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Obtener la ruta donde se almacena el JAR que contiene esta clase!
     *
     * @return
     */
    private String rutaJar() {
        try {
            String path = Update.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            return path;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}