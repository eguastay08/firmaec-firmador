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
import java.util.logging.Logger;

/**
 * Permite actualizar el JAR de la aplicacion.
 *
 * @author Ricardo Arguello <ricardo.arguello@soportelibre.com>
 */
public class Update {

    private static final String JAR_BASE_URL = "http://www.firmadigital.gob.ec/firmaec";
    private static final String FIRMADOR_JAR_NAME = "firmador-jar-with-dependencies.jar";
    private static final String FIRMADOR_JAR_URL = JAR_BASE_URL + "/" + FIRMADOR_JAR_NAME;
    private static final String FIRMADOR_JAR_SHA256_URL = JAR_BASE_URL + "/" + FIRMADOR_JAR_NAME + ".sha256";

    private static final String FIRMAEC_JAR_NAME = "cliente-jar-with-dependencies.jar";
    private static final String FIRMAEC_JAR_URL = JAR_BASE_URL + "/" + FIRMAEC_JAR_NAME;
    private static final String FIRMAEC_JAR_SHA256_URL = JAR_BASE_URL + "/" + FIRMAEC_JAR_NAME + ".sha256";

    private static final int BUFFER_SIZE = 8192;

    private static final Logger logger = Logger.getLogger(Update.class.getName());

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    public File actualizarFirmador() throws IllegalArgumentException {
        // Se debe descargar?
        String path = rutaJar();
        logger.info("path=" + path);

        File file = new File(path);
        logger.info("file=" + file.getAbsolutePath() + "; canWrite=" + file.canWrite() + ";file.getName()="
                + file.getName());

        if (file.canWrite()) {
            return file;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public File actualizarCliente() throws IllegalArgumentException {
        // Se debe descargar?
        String path = rutaJar();
        logger.info("path=" + path);

        File file = new File(path);
        String firmaecJar = file.getParent() + File.separator + FIRMAEC_JAR_NAME;
        File firmaec = new File(firmaecJar);
        logger.info("file=" + firmaec.getAbsolutePath() + "; canWrite=" + firmaec.canWrite() + ";file.getName()="
                + firmaec.getName());

        if (firmaec.canWrite()) {
            return firmaec;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void updateFirmador(File file) throws IOException {
        String hashBajado = new String(download(FIRMADOR_JAR_SHA256_URL));
        logger.info("hashBajado=" + hashBajado);
        String hash = hashBajado.split("\\s")[0];

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
        byte[] jar = download(FIRMADOR_JAR_URL);

        if (!verifyHash(hash, jar)) {
            logger.severe("ERROR de verificacion de hash");
            return;
        }

        logger.info("Hash comprobado OK");

        if (!file.getName().equals(FIRMADOR_JAR_NAME)) {
            logger.severe("El nombre del archivo no es " + FIRMADOR_JAR_NAME);
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

    public void updateCliente(File file) throws IOException {
        String hashBajado = new String(download(FIRMAEC_JAR_SHA256_URL));
        logger.info("hashBajado=" + hashBajado);
        String hash = hashBajado.split("\\s")[0];

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
        byte[] jar = download(FIRMAEC_JAR_URL);

        if (!verifyHash(hash, jar)) {
            logger.severe("ERROR de verificacion de hash");
            return;
        }

        logger.info("Hash comprobado OK");

        if (!file.getName().equals(FIRMAEC_JAR_NAME)) {
            logger.severe("El nombre del archivo no es " + FIRMAEC_JAR_NAME);
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
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(jar);
            byte[] digest = md.digest();
            return printHexBinary(digest).toLowerCase();
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
            return Update.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static String printHexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }
}
