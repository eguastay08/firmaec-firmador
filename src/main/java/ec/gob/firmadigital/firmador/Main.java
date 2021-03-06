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

package ec.gob.firmadigital.firmador;

import ec.gob.firmadigital.cliente.FirmaDigital;
import ec.gob.firmadigital.cliente.Validador;
import ec.gob.firmadigital.exceptions.*;
import ec.gob.firmadigital.firmador.update.Update;
import ec.gob.firmadigital.utils.CertificadoEcUtils;
import ec.gob.firmadigital.utils.FirmadorFileUtils;
import ec.gob.firmadigital.utils.TiempoUtils;
import ec.gob.firmadigital.utils.WordWrapCellRenderer;
import io.rubrica.certificate.ValidationResult;
import io.rubrica.core.RubricaException;
import io.rubrica.keystore.FileKeyStoreProvider;
import io.rubrica.keystore.KeyStoreProvider;
import io.rubrica.keystore.KeyStoreProviderFactory;
import io.rubrica.ocsp.OcspValidationException;
import io.rubrica.sign.cms.DatosUsuario;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.NoSuchFileException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author jdcalle
 * @author Asamblea Nacional
 * @version 1.2.0 2018
 * @
 */
public class Main extends JFrame {

    private File documento;
    private File llave;
    private File llaveVerificar;
    private File ultimaCarpeta;
	private String alias;
    private static final String RUTA_IMG = "images/";
    private static final String CHECK_IMG = "CheckIcon.png";
    private static final String NOTCHECK_IMG = "DeleteIcon.png";
    private static final String PROPIEDADES = "mensajes.properties";
    private static final String CONFIGURACIONES = "config.properties";
    private final List<String> extensionesPermitidas;
    private final FileNameExtensionFilter filtros = new FileNameExtensionFilter("PDF - Portable Document Format", "pdf");
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private PDDocument pdfDocument;
    private PDFRenderer pdfRenderer;

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    private Properties prop;
    private Properties lastConfig;

    private BufferedImage bim = null;
    private ImageIcon icon;
    private String dir_base;
    /**
     * Creates new form Main
     * @param args
     */
    public Main(String[] args) {
        try {
            cargarPropiedades();
            dir_base = System.getProperty("user.dir");
            lastConfig = loadLastConfig(dir_base);
            if (lastConfig.isEmpty()) {
                createLastConfig();
                logger.info("Creating configuration file in: "+dir_base);
            }

        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        extensionesPermitidas = new ArrayList<>();
        // Extensiones permitidas"pdf","p7m","docx","xlsx","pptx","odt","ods","odp"
        extensionesPermitidas.add("pdf");

        ultimaCarpeta = new File(System.getProperty("user.home"));
        //fileChooser.setCurrentDirectory(ultimaCarpeta);
        // Filtro para archivos   

        initComponents();

        tblDatosDelFirmanteFirmador.getColumnModel().getColumn(0).setCellRenderer(new WordWrapCellRenderer());
        tblDatosDelFirmanteFirmador.getColumnModel().getColumn(1).setCellRenderer(new WordWrapCellRenderer());
        tblDatosDelFirmanteFirmador.getColumnModel().getColumn(2).setCellRenderer(new WordWrapCellRenderer());
        tblDatosDelFirmanteFirmador.getColumnModel().getColumn(3).setCellRenderer(new WordWrapCellRenderer());
        tblDatosDelFirmanteFirmador.getColumnModel().getColumn(4).setCellRenderer(new WordWrapCellRenderer());

        tblDatosFirmanteVerificar.getColumnModel().getColumn(0).setCellRenderer(new WordWrapCellRenderer());
        tblDatosFirmanteVerificar.getColumnModel().getColumn(1).setCellRenderer(new WordWrapCellRenderer());
        tblDatosFirmanteVerificar.getColumnModel().getColumn(2).setCellRenderer(new WordWrapCellRenderer());
        tblDatosFirmanteVerificar.getColumnModel().getColumn(3).setCellRenderer(new WordWrapCellRenderer());
        tblDatosFirmanteVerificar.getColumnModel().getColumn(4).setCellRenderer(new WordWrapCellRenderer());
        tblDatosFirmanteVerificar.getColumnModel().getColumn(5).setCellRenderer(new WordWrapCellRenderer());
        tblDatosFirmanteVerificar.getColumnModel().getColumn(6).setCellRenderer(new WordWrapCellRenderer());
        tblDatosFirmanteVerificar.getColumnModel().getColumn(7).setCellRenderer(new WordWrapCellRenderer());

        //nemoics
        jblCertificadoEnFimador.setLabelFor(jblCertificadoEnFimador);
        btnAbrirArchivoFirmar.setMnemonic(KeyEvent.VK_E);
        btnAbrirArchivoPSKFirmar.setMnemonic(KeyEvent.VK_X);
        btnFirmar.setMnemonic(KeyEvent.VK_F);
        btnResetear.setMnemonic(KeyEvent.VK_R);

        btnExaminarVerificar.setMnemonic(KeyEvent.VK_E);
        btnResetearVerificar.setMnemonic(KeyEvent.VK_R);
        //mainPanel
        mainPanel.setMnemonicAt(0, KeyEvent.VK_1);
        mainPanel.setMnemonicAt(1, KeyEvent.VK_2);
        mainPanel.setMnemonicAt(2, KeyEvent.VK_3);
       // mainPanel.setNm
        //this.firmarVerificarDocPanel.setMnemonic(KeyEvent.VK_1);

        
    }

    private Properties loadLastConfig(String dir_base) {

        Properties propTemp = new Properties();
        logger.info("Base directory: "+ dir_base);
        InputStream input = null;

        try {
            input = new FileInputStream(dir_base + "/config.properties");
            // load a properties file
            propTemp.load(input);
        } catch (IOException ex) {
            logger.severe("Reading file error: "+ dir_base+" "+ ex.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.severe("Error on close file: "+ e.getMessage());
                }
            }
        }
        return propTemp;
    }

    private void createLastConfig() {
        Properties propTemp = new Properties();
        OutputStream output = null;
        try {
            output = new FileOutputStream(dir_base + "/config.properties");
            propTemp.store(output, "Temporal configurations");
            logger.info("Creating temporal config file in: "+ dir_base);
        } catch (IOException io) {
            logger.severe("Write to file error: +"+ io.getMessage());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void updateLastConfig(Properties prop) {
        OutputStream output = null;
        try {
            output = new FileOutputStream(dir_base + "/config.properties");
            prop.store(output, "Temporal configurations");
            logger.info("Updating temporal config file in: "+ dir_base);
        } catch (IOException io) {
            logger.severe("Write to file error: +"+ io.getMessage());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Component findPrevFocus() {
        Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        Container root = c.getFocusCycleRootAncestor();

        FocusTraversalPolicy policy = root.getFocusTraversalPolicy();
        Component prevFocus = policy.getComponentBefore(root, c);
        if (prevFocus == null) {
            prevFocus = policy.getDefaultComponent(root);
        }
        return prevFocus;
    }

    private void cargarPropiedades() throws IOException {
        prop = new Properties();
        // load a properties file
        prop.load(getClass().getClassLoader().getResourceAsStream(PROPIEDADES));

    }

    private File abrirArchivo(FileNameExtensionFilter filtro) {
        final JFileChooser fileChooser = new JFileChooser();
        File archivo;
        if (filtro != null) {
            fileChooser.setFileFilter(filtro);
        }
        fileChooser.setCurrentDirectory(ultimaCarpeta);
        fileChooser.setFileFilter(filtros);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            archivo = fileChooser.getSelectedFile();
            if (archivo.exists()) {
                System.out.println("existe");
            }

            ultimaCarpeta = fileChooser.getCurrentDirectory();
            return archivo;
        }
        return null;
    }

    private File loadLastCertFile(){
        return new File(lastConfig.getProperty("user.dir"));
    }

    private File abrirArchivo() {
        final JFileChooser fileChooser = new JFileChooser();
        File archivo;
        fileChooser.setCurrentDirectory(ultimaCarpeta);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            archivo = fileChooser.getSelectedFile();
            ultimaCarpeta = fileChooser.getCurrentDirectory();
            if(!lastConfig.containsKey("user.dir")){
                //Update base dir
                lastConfig.setProperty("user.dir", fileChooser.getSelectedFile().getAbsolutePath());
                updateLastConfig(lastConfig);

            }else {
                if(!lastConfig.getProperty("user.dir").equals(fileChooser.getSelectedFile().getAbsolutePath())) {
                    lastConfig.setProperty("user.dir", fileChooser.getSelectedFile().getAbsolutePath());
                    updateLastConfig(lastConfig);
                }
            }
            return archivo;
        }
        return null;
    }

    private void resetForm() {
        this.documento = null;
        this.tipoFirmaBtnGRP.clearSelection();
        this.btnFirmar.setEnabled(false);
        this.btnVerificar.setEnabled(false);
        this.jpfClave.setText("");
        this.jtxRutaDocumentoFirmar.setText("");
        this.jpfClave.setEnabled(false);
        this.documento = null;
        this.llave = null;
        this.jtxRutaLlaveFirmar.setEnabled(false);
        this.jtxRutaLlaveFirmar.setText(lastConfig.getProperty("user.dir"));
        this.jtxArchivoFirmado.setText("");
        
        this.btnAbrirArchivoPSKFirmar.setEnabled(false);
        resetDatosTabladeFirmante();
        resetDatosTablaCertificadoFirmador();
        this.txtOptionalText.setText("");
        this.point.setLocation(new Point());
        if(preview.getComponentCount()>0){
            preview.remove(preview.getComponent(0));
        }

        preview.repaint();
        mainPanel.repaint();
    }

    private void selFirmarConArchivo() {
        this.btnFirmar.setEnabled(true);
        this.jtxRutaLlaveFirmar.setEnabled(true);
        this.btnAbrirArchivoPSKFirmar.setEnabled(true);
        // Si es windows no hay que habilitar el campo de contrase??a

        this.jpfClave.setEnabled(true);

    }

    private void selValidarArchivo() {
        jtxRutaCertificado.setEnabled(true);
        btnAbrirCertificado.setEnabled(true);
        jpfCertClaveTXT.setEnabled(true);
        jpfCertClaveTXT.setText("");
    }

    private void selFirmarConToken() {
        this.btnFirmar.setEnabled(true);
        this.jtxRutaLlaveFirmar.setEnabled(false);
        this.jtxRutaLlaveFirmar.setText("");
        this.jpfClave.setEnabled(false);
        this.jpfClave.setText("");
        this.btnAbrirArchivoPSKFirmar.setEnabled(false);

        if (!esWindows()) {
            this.jpfClave.setEnabled(true);
        } else {
            this.jpfClave.setEnabled(false);
        }
    }

    private void selValidarToken() {
        jtxRutaCertificado.setEnabled(false);
        jtxRutaCertificado.setText("");
        btnAbrirCertificado.setEnabled(false);
        jpfCertClaveTXT.setText("");

        if (esLinux()) {
            this.jpfCertClaveTXT.setEnabled(true);
        } else {
            this.jpfCertClaveTXT.setEnabled(false);
        }
    }

    private boolean esWindows() {
        return (OS.contains("win"));
    }
    
    private boolean esLinux() {
        return (OS.contains("linux"));
    }

    /*
    Valida que esten los campos necesarios para firmar
     */
    private void validacionPreFirmar() throws DocumentoNoExistenteException, TokenNoConectadoException, DocumentoNoPermitidoException, CertificadoInvalidoException, DocumentoException {
        //Revisamos si existe el documento a firmar
        // TODO no hacer un return directamente, se podria validar todos los parametros e ir aumentando los errores
        if (documento == null) {
            throw new DocumentoNoExistenteException(MessageFormat.format(prop.getProperty("mensaje.error.documento_inexistente"), this.jtxRutaDocumentoFirmar.getText()));
        }

        if (!documento.exists()) {
            throw new DocumentoNoExistenteException(MessageFormat.format(prop.getProperty("mensaje.error.documento_inexistente"), documento.getAbsolutePath()));
        }

        if (llave == null && rbFfirmarLlave.isSelected()) {
            throw new DocumentoNoExistenteException(prop.getProperty("mensaje.error.certificado_sin_seleccionar"));
        }

        if (rbFfirmarLlave.isSelected() && !llave.exists()) {
            String path=llave.getAbsolutePath();
            if (path.length()>150)
                path="..."+path.substring(path.lastIndexOf("/"),path.length());
            throw new DocumentoNoExistenteException(MessageFormat.format(prop.getProperty("mensaje.error.certificado_inexistente"), path));
        }

        if (rbFirmarToken.isSelected() && !esWindows() && jpfClave.getPassword().length == 0) {
            throw new CertificadoInvalidoException(prop.getProperty("mensaje.error.certificado_clave_vacia"));
        }
        
        if(documento.length() == 0){
            throw new DocumentoException(prop.getProperty("mensaje.error.documento_vacio"));
        }
        
        if("p7m".equals(FirmadorFileUtils.getFileExtension(documento).toLowerCase())){
            throw new DocumentoException(prop.getProperty("mensaje.error.documento_p7m"));
        }
// Digital signature insertion point verification ===============================
         if (point.getX()==0 && point.getY()==0)
            throw new DocumentoException("Elija un lugar para insertar su firma (click sobre su documento)");

        tipoDeDocumentPermitido(documento);
    }

    // Si existe el archivo
    private boolean archivoEnRutaExiste(JTextField jtextField) {
        if (jtextField == null || jtextField.getText().isEmpty()) {
            return false;
        }
        File archivo = new File(jtextField.getText());

        return archivo.exists();
    }

    /*
    verificar documento
     */
    private void verificarDocumento() throws Exception {
        // Vemos si existe
        System.out.println("Verificando Docs");
        tipoDeDocumentPermitido(documento);

        FirmaDigital firmaDigital = new FirmaDigital();

        List<Certificado> certs = firmaDigital.verificar(documento);

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        jtxDocumentoVerificado.setText(documento.getAbsolutePath());

        DefaultTableModel tableModelCertificados = (DefaultTableModel) tblDatosFirmanteVerificar.getModel();
        tableModelCertificados.setRowCount(0);
        certs.stream().map((cert) -> {
            String[] dataCert = new String[8];
            //DatosUsuario datosUsuario = FirmaDigital.getDatosUsuarios(cert);

            dataCert[0] = cert.getDatosUsuario().getCedula();
            String apellido = cert.getDatosUsuario().getApellido();
            if (cert.getDatosUsuario().getApellido() == null) {
                apellido = "";
            }
            String nombre = cert.getDatosUsuario().getNombre();
            if (cert.getDatosUsuario().getNombre() == null) {
                nombre = "";
            }
            dataCert[1] = nombre + " " + apellido;
            dataCert[2] = cert.getDatosUsuario().getInstitucion();
            dataCert[3] = cert.getDatosUsuario().getCargo();
            dataCert[4] = format1.format(cert.getValidFrom().getTime());
            dataCert[5] = format1.format(cert.getValidTo().getTime());
            dataCert[6] = format1.format(cert.getGenerated().getTime());
            String revocadoStr = prop.getProperty("mensaje.certificado.no_revocado");
            if (cert.getRevocated() == null) {
                revocadoStr = prop.getProperty("mensaje.certificado.no_se_pudo_verificar");
            } else if (cert.getRevocated()) {
                revocadoStr = prop.getProperty("mensaje.certificado.revocado");
            }
            dataCert[7] = revocadoStr;
            return dataCert;
        }).forEachOrdered((dataCert) -> {
            tableModelCertificados.addRow(dataCert);
        });

        tblDatosFirmanteVerificar.setModel(tableModelCertificados);
        tableModelCertificados.fireTableDataChanged();
    }

    // Se podria verificar el mimetype
    // Talvez eliminar el if
    private void tipoDeDocumentPermitido(File documento) throws DocumentoNoPermitidoException {
        String extDocumento = FirmadorFileUtils.getFileExtension(documento).toLowerCase();
        if(!documento.getName().contains(".")){
            throw new DocumentoNoPermitidoException(prop.getProperty("mensaje.error.extension_vacia"));
        }
        
        if (!extensionesPermitidas.stream().anyMatch((extension) -> (extension.equals(extDocumento)))) {
            throw new DocumentoNoPermitidoException(MessageFormat.format(prop.getProperty("mensaje.error.extension_no_permitida"), extDocumento));
        }
    }

    //TODO botar exceptions en vez de return false
    private boolean firmarDocumento() throws Exception {
        //Load the same certificate if previously used
        if(llave==null && lastConfig.containsKey("user.dir")){
            llave=loadLastCertFile();
        }

        // Vemos si es un documento permitido primero
        validacionPreFirmar();

        KeyStore ks;

        if (this.rbFirmarToken.isSelected()) {
            ks = KeyStoreProviderFactory.getKeyStore(new String(jpfClave.getPassword()));
            if (ks == null) {
                //JOptionPane.showMessageDialog(frame, "No se encontro un token!");
                throw new TokenNoEncontradoException(prop.getProperty("mensaje.error.token_no_encontrado"));
            }

        } else {
            KeyStoreProvider ksp = new FileKeyStoreProvider(jtxRutaLlaveFirmar.getText());
            ks = ksp.getKeystore(jpfClave.getPassword());

        }

        X509Certificate cert = validarFirma(ks);
		if(cert!=null && this.alias!=null){
			FirmaDigital firmaDigital = new FirmaDigital();

			//byte[] docSigned = firmaDigital.firmar(ks, this.alias, documento, jpfClave.getPassword());
			String nombreDocFirmado = FirmadorFileUtils.crearNombreFirmado(documento);

			String nombre = CertificadoEcUtils.getNombreCA(cert);

			System.out.println("Nombre: " + nombre);

			DatosUsuario datosUsuario = CertificadoEcUtils.getDatosUsuarios(cert);

			agregarDatosTabladeFirmante(datosUsuario);

			agregarDatosTablaCertificadoFirmador(cert, datosUsuario);

			// Adding User Data visible into document ==============================================
            datosUsuario.setFechaFirmaArchivo(tblDatosDelFirmanteFirmador.getValueAt(0,4).toString());
            byte[] docTemp=FirmadorFileUtils.addVisibleSign(documento,datosUsuario,point, (int)spNumPages.getValue()
                    ,txtOptionalText.getText(), chkNoName.isSelected());

            //Signature size and position verification
            Rectangle position;
            if(chkNoName.isSelected())
                position=new Rectangle(point.x,point.y,point.x+100,point.y+35);
            else
                position=new Rectangle(point.x,point.y,point.x+205,point.y+50);

            byte[] docSigned = firmaDigital.firmar(ks, this.alias,docTemp ,documento, jpfClave.getPassword(),position
                    ,(int)spNumPages.getValue());

			// Revisamos si el archivo existe
			nombreDocFirmado = verificarNombre(nombreDocFirmado);

			FirmadorFileUtils.saveByteArrayToDisc(docSigned, nombreDocFirmado);
			
			jtxArchivoFirmado.setText(nombreDocFirmado);
			
			this.alias = null;

			return true;
		}else
			return false;
    }


	    
    private String verificarNombre(String nombreArchivo){
        File archivo = new File(nombreArchivo);
        String nuevoNombre = nombreArchivo;
        if (archivo.exists()) {
            String nombreCompleto = archivo.getAbsolutePath();
            String nombre = nombreCompleto.replaceFirst("[.][^.]+$", "");
            String extension = FirmadorFileUtils.getFileExtension(archivo);
            
            nuevoNombre = nombre + "_1." + extension;
            nuevoNombre = verificarNombre(nuevoNombre);
        }
   
        return nuevoNombre;
    }

    private void agregarDatosTabladeFirmante(DatosUsuario datosUsuario) {
        DefaultTableModel tableModel = (DefaultTableModel) tblDatosDelFirmanteFirmador.getModel();

        if (datosUsuario == null) {
            System.out.println("datos usuarios es nulo");
            datosUsuario = new DatosUsuario();
        }

        tableModel.setRowCount(0);

        //Actualizamos los datos del archivo
        String[] data = new String[5];
        data[0] = datosUsuario.getCedula();
        String nombres = datosUsuario.getNombre() + " " + datosUsuario.getApellido();
        data[1] = nombres;
        data[2] = datosUsuario.getInstitucion();
        data[3] = datosUsuario.getCargo();
        data[4] = LocalDateTime.now().toString();

        tableModel.addRow(data);

        tblDatosDelFirmanteFirmador.setModel(tableModel);
        tableModel.fireTableDataChanged();

    }

    private void agregarDatosTablaCertificadoFirmador(X509Certificate cert, DatosUsuario datosUsuario) {
        DefaultTableModel tableModel = (DefaultTableModel) tblDatosDelCertificadoFirmador.getModel();

        tableModel.setRowCount(0);

        //Actualizamos los datos del archivo
        String[] data = new String[5];
        data[0] = MessageFormat.format(prop.getProperty("tabla.certificado.emitido_por"), CertificadoEcUtils.getNombreCA(cert));

        tableModel.addRow(data);

        data[0] = prop.getProperty("tabla.certificado.cedula") + " " + datosUsuario.getCedula();
        tableModel.addRow(data);

        data[0] = prop.getProperty("tabla.certificado.nombres") + " " + datosUsuario.getNombre();
        tableModel.addRow(data);

        data[0] = prop.getProperty("tabla.certificado.apellidos") + " " + datosUsuario.getApellido();
        tableModel.addRow(data);

        data[0] = prop.getProperty("tabla.certificado.institucion") + " " + datosUsuario.getInstitucion();
        tableModel.addRow(data);

        data[0] = prop.getProperty("tabla.certificado.cargo") + " " + datosUsuario.getCargo();
        tableModel.addRow(data);

        data[0] = prop.getProperty("tabla.certificado.fecha_de_emision") + " " + cert.getNotBefore();
        tableModel.addRow(data);

        data[0] = prop.getProperty("tabla.certificado.fecha_de_expiracion") + " " + cert.getNotAfter();
        tableModel.addRow(data);

        tblDatosDelCertificadoFirmador.setModel(tableModel);
        tableModel.fireTableDataChanged();

    }

    private void resetDatosTabladeFirmante() {
        DefaultTableModel tableModel = (DefaultTableModel) tblDatosDelFirmanteFirmador.getModel();

        DatosUsuario datosUsuario = new DatosUsuario();

        tableModel.setRowCount(0);

        //Actualizamos los datos del archivo
        String[] data = new String[5];
        data[0] = datosUsuario.getCedula();

        String nombres = datosUsuario.getNombre();
        if (nombres == null) {
            nombres = "";
        }

        String apellidos = datosUsuario.getApellido();
        if (apellidos == null) {
            apellidos = "";
        }
        data[1] = nombres + " " + apellidos;
        data[2] = datosUsuario.getInstitucion();
        data[3] = datosUsuario.getCargo();
        data[4] = ""; //LocalDateTime.now().toString();

        tableModel.addRow(data);

        tblDatosDelFirmanteFirmador.setModel(tableModel);
        tableModel.fireTableDataChanged();
    }

    private void resetDatosTablaCertificadoFirmador() {
        DefaultTableModel tableModel = (DefaultTableModel) tblDatosDelCertificadoFirmador.getModel();

        tableModel.setRowCount(0);

        //Actualizamos los datos del archivo
        String[] data = new String[5];
        data[0] = "";
        tableModel.addRow(data);

        data[0] = "";
        tableModel.addRow(data);

        data[0] = "";
        tableModel.addRow(data);

        data[0] = "";
        tableModel.addRow(data);

        data[0] = "";
        tableModel.addRow(data);

        data[0] = "";
        tableModel.addRow(data);

        data[0] = "";
        tableModel.addRow(data);

        data[0] = "";
        tableModel.addRow(data);

        data[0] = "";
        tableModel.addRow(data);

        tblDatosDelCertificadoFirmador.setModel(tableModel);
        tableModel.fireTableDataChanged();
    }

    private X509Certificate validarFirma(KeyStore ks) throws TokenNoEncontradoException, KeyStoreException, IOException, RubricaException, HoraServidorException, CertificadoInvalidoException, CRLValidationException, OcspValidationException, EntidadCertificadoraNoValidaException, ConexionValidarCRLException {
        this.alias = null;
		System.out.println("Validar Firma");
        Validador validador = new Validador();
		this.alias = validador.seleccionarAlias(ks);
		X509Certificate cert = null;
		if (alias != null)
			cert = (X509Certificate) ks.getCertificate(this.alias);

        try {
			if(cert!=null){
				cert.checkValidity(TiempoUtils.getFechaHora());
				validador.validar(cert);
			}
			return cert;
        } catch (CertificateExpiredException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            throw new CertificadoInvalidoException(MessageFormat.format(prop.getProperty("mensaje.error.certificado_caduco"), cert.getNotAfter()));
        } catch (CertificateNotYetValidException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            throw new CertificadoInvalidoException(MessageFormat.format(prop.getProperty("mensaje.error.certificado_caduco"), cert.getNotBefore()));
        }
    }

    private String obtenerUrlCRL(List<String> urls) {
        for (String url : urls) {
            if (url.toLowerCase().contains("crl")) {
                return url;
            }
        }
        return null;
    }

    private void setearInfoValidacionCertificado(X509Certificate cert) throws CertificadoInvalidoException {
        if (cert != null) {
            String emisor = CertificadoEcUtils.getNombreCA(cert);

            DatosUsuario datosUsuario = CertificadoEcUtils.getDatosUsuarios(cert);
            
            if(datosUsuario == null && (jpfCertClaveTXT.getPassword() == null || jpfCertClaveTXT.getPassword().length == 0))
                throw new CertificadoInvalidoException(prop.getProperty("mensaje.error.extraer_datos_certificados"));
            
            if(datosUsuario == null)
                throw new CertificadoInvalidoException("No se pudo extraer los datos del certificados.");

            DefaultTableModel tableModel = (DefaultTableModel) tblDatosCertificadosValidar.getModel();

            tableModel.setRowCount(0);

            //Actualizamos los datos del archivo
            String[] data = new String[1];
            
            //Si el certificado es null

            data[0] = MessageFormat.format(prop.getProperty("tabla.certificado.emitido_por"), CertificadoEcUtils.getNombreCA(cert));;
            tableModel.addRow(data);

            data[0] = prop.getProperty("tabla.certificado.cedula") + " " + datosUsuario.getCedula();
            tableModel.addRow(data);

            data[0] = prop.getProperty("tabla.certificado.nombres") + " " + datosUsuario.getNombre();
            tableModel.addRow(data);

            data[0] = prop.getProperty("tabla.certificado.apellidos") + " " + datosUsuario.getApellido();
            tableModel.addRow(data);

            data[0] = prop.getProperty("tabla.certificado.institucion") + " " + datosUsuario.getInstitucion();
            tableModel.addRow(data);

            data[0] = prop.getProperty("tabla.certificado.cargo") + " " + datosUsuario.getCargo();
            tableModel.addRow(data);

            data[0] = prop.getProperty("tabla.certificado.fecha_de_emision") + " " + cert.getNotBefore();
            tableModel.addRow(data);

            data[0] = prop.getProperty("tabla.certificado.fecha_de_expiracion") + " " + cert.getNotAfter();
            tableModel.addRow(data);

            tblDatosCertificadosValidar.setModel(tableModel);
            tableModel.fireTableDataChanged();
        }
        //TOdo botar error si es null
    }

    private void abrirDocumento() throws IOException {
        FirmadorFileUtils.abrirDocumento(jtxArchivoFirmado.getText());
    }

    private void agregarValidezCertificado(String validez, String estado) {
        DefaultTableModel tableModel = (DefaultTableModel) tblDatosCertificadosValidar.getModel();
        //Actualizamos los datos del archivo
        String[] data = new String[1];
        data[0] = prop.getProperty("tabla.certificado.validez") + " " + validez;
        tableModel.addRow(data);

        data[0] = prop.getProperty("tabla.certificado.estado") + estado;
        tableModel.addRow(data);

        tblDatosCertificadosValidar.setModel(tableModel);
        tableModel.fireTableDataChanged();
    }

    private void resetInfoValidacionCertificado() {
        DefaultTableModel tableModel = (DefaultTableModel) tblDatosCertificadosValidar.getModel();

        tableModel.setRowCount(0);

        //Actualizamos los datos del archivo
        String[] data = new String[5];
        data[0] = "";
        tableModel.addRow(data);

        data[0] = "";
        tableModel.addRow(data);

        data[0] = "";
        tableModel.addRow(data);

        data[0] = "";
        tableModel.addRow(data);

        data[0] = "";
        tableModel.addRow(data);

        data[0] = "";
        tableModel.addRow(data);

        data[0] = "";
        tableModel.addRow(data);

        data[0] = "";
        tableModel.addRow(data);

        data[0] = "";
        tableModel.addRow(data);

        tblDatosCertificadosValidar.setModel(tableModel);
        tableModel.fireTableDataChanged();
    }

    private void resetearInfoValidacionCertificado() {
        jtxRutaCertificado.setText("");
        llaveVerificar = null;
        jpfCertClaveTXT.setText("");
        resetInfoValidacionCertificado();
        //TOdo botar error si es null
    }

    private String resultadosCRL(ValidationResult result) {
        if (result == ValidationResult.CANNOT_DOWNLOAD_CRL) {
            return prop.getProperty("mensaje.resultadoscrl.no_se_pudo_descargar");
        }
        if (result.isValid()) {
            return prop.getProperty("mensaje.resultadoscrl.valido");
        }
        return prop.getProperty("mensaje.resultadoscrl.invalido");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tipoFirmaBtnGRP = new ButtonGroup();
        jScrollPane1 = new JScrollPane();
        mainPanel = new JTabbedPane();
        firmarVerificarDocPanel = new JPanel();
        jSeparator2 = new JSeparator();
        jplFirmar = new JPanel();
        jblCertificadoEnFimador = new JLabel();
        rbFfirmarLlave = new JRadioButton();
        rbFirmarToken = new JRadioButton();
        jblDocumento = new JLabel();
        jtxRutaDocumentoFirmar = new JTextField();
        btnAbrirArchivoFirmar = new JButton();
        jblCertificadoFirmar = new JLabel();
        jtxRutaLlaveFirmar = new JTextField();
        if(lastConfig.containsKey("user.dir")){
            jtxRutaLlaveFirmar.setText(lastConfig.getProperty("user.dir"));
        }
        btnAbrirArchivoPSKFirmar = new JButton();
        jblClave = new JLabel();
        jpfClave = new JPasswordField();
        btnFirmar = new JButton();
        btnResetear = new JButton();
        jPanel2 = new JPanel();
        jLabel11 = new JLabel();
        jlbArchivoFirmador = new JLabel();
        jtxArchivoFirmado = new JTextField();
        jScrollPane6 = new JScrollPane();
        tblDatosDelFirmanteFirmador = new JTable();
        jScrollPane7 = new JScrollPane();
        tblDatosDelCertificadoFirmador = new JTable();
        verificarDocumentoPanel = new JPanel();
        jSeparator1 = new JSeparator();
        jplVerificarDocumento = new JPanel();
        jlbArchivoFirmadoVerficar = new JLabel();
        jtxArchivoFirmadoVerificar = new JTextField();
        btnExaminarVerificar = new JButton();
        btnVerificar = new JButton();
        btnResetearVerificar = new JButton();
        jPanel4 = new JPanel();
        jLabel12 = new JLabel();
        jlbArchivoVerificado = new JLabel();
        jScrollPane3 = new JScrollPane();
        tblDatosFirmanteVerificar = new JTable();
        jLabel13 = new JLabel();
        jtxDocumentoVerificado = new JTextField();
        validarCertificadoPanel = new JPanel();
        jSeparator3 = new JSeparator();
        jplValidar = new JPanel();
        jlbCertificadoValidar = new JLabel();
        rbValidarLlave = new JRadioButton();
        rbValidarToken = new JRadioButton();
        jlbCertificadoVldCert = new JLabel();
        jtxRutaCertificado = new JTextField();
        btnAbrirCertificado = new JButton();
        jlbCertificadoValidarCert = new JLabel();
        jpfCertClaveTXT = new JPasswordField();
        btnValidar = new JButton();
        btnResetValidarForm = new JButton();
        jPanel6 = new JPanel();
        jLabel6 = new JLabel();
        jScrollPane5 = new JScrollPane();
        tblDatosCertificadosValidar = new JTable();
        jmbMenuPrincipal = new JMenuBar();
        jmAyuda = new JMenu();
        jmiAcerca = new JMenuItem();
        //jmiActualizar = new JMenuItem();
        lblOptionalText = new JLabel();
        txtOptionalText = new JTextField();


        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    if (pdfDocument !=null)
                        pdfDocument.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        setTitle("Firma Electr??nica - Asamblea Nacional");

        firmarVerificarDocPanel.setName(""); // NOI18N

        jblCertificadoEnFimador.setText("Certificado en:");

        tipoFirmaBtnGRP.add(rbFfirmarLlave);
        rbFfirmarLlave.setText("Archivo");
        rbFfirmarLlave.addActionListener(this::rbFfirmarLlaveActionPerformed);

        tipoFirmaBtnGRP.add(rbFirmarToken);
        rbFirmarToken.setText("Token");
        rbFirmarToken.addActionListener(this::rbFirmarTokenActionPerformed);

        jblDocumento.setText("Documento");

        jtxRutaDocumentoFirmar.setEditable(false);
        jtxRutaDocumentoFirmar.addActionListener(this::jtxRutaDocumentoFirmarActionPerformed);

        btnAbrirArchivoFirmar.setText("Examinar");
        btnAbrirArchivoFirmar.addActionListener(this::btnAbrirArchivoFirmarActionPerformed);

        jblCertificadoFirmar.setText("Certificado");

        jtxRutaLlaveFirmar.setEditable(false);
        jtxRutaLlaveFirmar.setEnabled(false);
        jtxRutaLlaveFirmar.addActionListener(this::jtxRutaLlaveFirmarActionPerformed);

        btnAbrirArchivoPSKFirmar.setText("Examinar");
        btnAbrirArchivoPSKFirmar.setEnabled(false);
        btnAbrirArchivoPSKFirmar.addActionListener(this::btnAbrirArchivoPSKFirmarActionPerformed);

        jblClave.setText("Contrase??a");

        jpfClave.setEnabled(false);

        btnFirmar.setText("Firmar");
        btnFirmar.setEnabled(false);
        btnFirmar.addActionListener(this::btnFirmarActionPerformed);

        btnResetear.setText("Restablecer");
        btnResetear.addActionListener(this::btnResetearActionPerformed);

        //============= SET DIGITAL SIGN POSITION =====================
        JPanel pagesPanel = new  JPanel(new GridLayout(2, 1));
        TitledBorder borderpNumPages = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)
                , "P??ginas");
        borderpNumPages.setTitleJustification(TitledBorder.LEFT);
        pagesPanel.setBorder(borderpNumPages);
        spNumPagesModel = new SpinnerNumberModel(0,0,0,0);
        spNumPages = new JSpinner(spNumPagesModel);

        spNumPages.addChangeListener(e -> {
            JSpinner s=(JSpinner)e.getSource();
            icon.setImage(loadPagePreview((int)s.getValue()));
            preview.repaint();

        });

        pagesPanel.add(spNumPages);
        chkNoName = new JCheckBox("Sin Nombre");
        pagesPanel.add(chkNoName);

        lblOptionalText.setText("Texto adicional:");
        preview =new JPanel(new BorderLayout());
        preview.setBounds(0,0,595,842);

        GroupLayout jplFirmarLayout = new GroupLayout(jplFirmar);
        jplFirmar.setLayout(jplFirmarLayout);

        GroupLayout.SequentialGroup hGroup= jplFirmarLayout.createSequentialGroup();
        GroupLayout.SequentialGroup vGroup= jplFirmarLayout.createSequentialGroup();

        jplFirmarLayout.setAutoCreateContainerGaps(true);
        jplFirmarLayout.setAutoCreateGaps(true);

        Box hBox=Box.createHorizontalBox();
        hBox.add(btnFirmar);
        hBox.add(Box.createHorizontalGlue());
        hBox.add(btnResetear);

        //Reformat components in group layout
        hGroup.addGroup(true,jplFirmarLayout.createParallelGroup()
                .addComponent(jblCertificadoEnFimador)
                .addComponent(jblDocumento)
                .addComponent(jblCertificadoFirmar)
                .addComponent(jblClave)
                .addComponent(lblOptionalText)
        )
                .addGroup(jplFirmarLayout.createParallelGroup()
                        .addGroup(jplFirmarLayout.createSequentialGroup()
                                .addComponent(rbFfirmarLlave)
                                .addComponent(rbFirmarToken)
                        )
                        .addComponent(jtxRutaDocumentoFirmar,300,370,Short.MAX_VALUE)
                        .addComponent(jtxRutaLlaveFirmar,300,370,Short.MAX_VALUE)
                        .addComponent(jpfClave,300,370,Short.MAX_VALUE)
                        .addComponent(txtOptionalText,300,370,Short.MAX_VALUE)
                        .addComponent(hBox)
                )
                .addGroup(jplFirmarLayout.createParallelGroup()
                        .addComponent(btnAbrirArchivoFirmar)
                        .addComponent(btnAbrirArchivoPSKFirmar)
                        .addGroup(jplFirmarLayout.createSequentialGroup()
                                .addComponent(pagesPanel,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
                        )
                );

        jplFirmarLayout.setHorizontalGroup(hGroup);

        vGroup.addGroup(jplFirmarLayout.createParallelGroup()
                .addComponent(jblCertificadoEnFimador)
                .addGroup(jplFirmarLayout.createParallelGroup()
                        .addComponent(rbFfirmarLlave)
                        .addComponent(rbFirmarToken)
                )
        )
                .addGroup(jplFirmarLayout.createParallelGroup()
                        .addComponent(jblDocumento)
                        .addComponent(jtxRutaDocumentoFirmar)
                        .addComponent(btnAbrirArchivoFirmar)
                )
                .addGroup(jplFirmarLayout.createParallelGroup()
                        .addComponent(jblCertificadoFirmar)
                        .addComponent(jtxRutaLlaveFirmar)
                        .addComponent(btnAbrirArchivoPSKFirmar)
                )

                .addGroup(jplFirmarLayout.createParallelGroup()
                        .addGroup(jplFirmarLayout.createSequentialGroup()

                                .addGroup(jplFirmarLayout.createParallelGroup()
                                        .addComponent(jblClave)
                                        .addComponent(jpfClave)
                                )
                                // OPTIONAL =======================
                                .addGroup(jplFirmarLayout.createParallelGroup()
                                        .addComponent(lblOptionalText)
                                        .addComponent(txtOptionalText)
                                )
                                        .addComponent(hBox)
                        ).addComponent(pagesPanel)
                );


        jplFirmarLayout.setVerticalGroup(vGroup);


        jLabel11.setText("<html><b>DATOS DEL FIRMANTE</b></html>");

        jlbArchivoFirmador.setText("Archivo Firmado");

        jtxArchivoFirmado.setEditable(false);

        tblDatosDelFirmanteFirmador.setModel(new DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null}
            },
            new String [] {
                "C??dula", "Nombres", "Instituci??n", "Cargo", "Fecha"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane6.setViewportView(tblDatosDelFirmanteFirmador);

        tblDatosDelCertificadoFirmador.setModel(new DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Datos del Certificado"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane7.setViewportView(tblDatosDelCertificadoFirmador);

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel11, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jlbArchivoFirmador)
                        .addGap(24, 24, 24)
                        .addComponent(jtxArchivoFirmado))
                    .addComponent(jScrollPane6)
                    .addComponent(jScrollPane7))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jtxArchivoFirmado, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jlbArchivoFirmador))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, GroupLayout.PREFERRED_SIZE, 98, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                .addGap(14, 14, 14))
        );

        GroupLayout firmarVerificarDocPanelLayout = new GroupLayout(firmarVerificarDocPanel);
        firmarVerificarDocPanel.setLayout(firmarVerificarDocPanelLayout);
        firmarVerificarDocPanelLayout.setHorizontalGroup(
            firmarVerificarDocPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator2)
            .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jplFirmar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        firmarVerificarDocPanelLayout.setVerticalGroup(
            firmarVerificarDocPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jplFirmar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
//======================= PREVIEW PANEL ==================================
        globalSignPanel = new JPanel();
        globalSignPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc= new GridBagConstraints(0,0,1,1,1.0,1.0
                ,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0);
        globalSignPanel.add(firmarVerificarDocPanel,gbc);

        gbc= new GridBagConstraints(1,0,1,1,1.0,1.0
                ,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0);
        globalSignPanel.add(preview,gbc);

        //mainPanel.addTab("<html><b>FIRMAR DOCUMENTO </b>(<u>1</u>)</html>", firmarVerificarDocPanel);
        mainPanel.addTab("<html><b>FIRMAR DOCUMENTO </b>(<u>1</u>)</html>", globalSignPanel);

        jlbArchivoFirmadoVerficar.setText("Archivo Firmado:");

        jtxArchivoFirmadoVerificar.setEditable(false);

        btnExaminarVerificar.setText("Examinar");
        btnExaminarVerificar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                btnExaminarVerificarActionPerformed(evt);
            }
        });

        btnVerificar.setMnemonic('v');
        btnVerificar.setText("Verificar Archivo");
        btnVerificar.setEnabled(false);
        btnVerificar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                btnVerificarActionPerformed(evt);
            }
        });

        btnResetearVerificar.setText("Restablecer");
        btnResetearVerificar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                btnResetearVerificarActionPerformed(evt);
            }
        });

        GroupLayout jplVerificarDocumentoLayout = new GroupLayout(jplVerificarDocumento);
        jplVerificarDocumento.setLayout(jplVerificarDocumentoLayout);
        jplVerificarDocumentoLayout.setHorizontalGroup(
            jplVerificarDocumentoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jplVerificarDocumentoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jlbArchivoFirmadoVerficar)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jplVerificarDocumentoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jplVerificarDocumentoLayout.createSequentialGroup()
                        .addComponent(btnVerificar)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnResetearVerificar))
                    .addComponent(jtxArchivoFirmadoVerificar))
                .addGap(18, 18, 18)
                .addComponent(btnExaminarVerificar)
                .addContainerGap())
        );
        jplVerificarDocumentoLayout.setVerticalGroup(
            jplVerificarDocumentoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jplVerificarDocumentoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jplVerificarDocumentoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jtxArchivoFirmadoVerificar, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExaminarVerificar)
                    .addComponent(jlbArchivoFirmadoVerficar))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jplVerificarDocumentoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btnResetearVerificar)
                    .addComponent(btnVerificar))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel12.setText("<html><b>RESULTADOS DE LA VERIFICACI??N DEL ARCHIVO FIRMADO ELECTR??NICAMENTE</b></html>");

        jlbArchivoVerificado.setText("Archivo:");

        tblDatosFirmanteVerificar.setModel(new DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "C??dula", "Nombres", "Instituci??n", "Cargo", "V??lido Desde", "V??lido Hasta", "Fecha Firmado", "Revocado"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(tblDatosFirmanteVerificar);

        jLabel13.setText("<html><b>DATOS DE LOS FIRMANTES</b></html>");

        jtxDocumentoVerificado.setEditable(false);

        GroupLayout jPanel4Layout = new GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel12, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, GroupLayout.DEFAULT_SIZE, 932, Short.MAX_VALUE)))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jlbArchivoVerificado)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jtxDocumentoVerificado))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jlbArchivoVerificado))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jtxDocumentoVerificado, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel13, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(jScrollPane3, GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                .addContainerGap())
        );

        GroupLayout verificarDocumentoPanelLayout = new GroupLayout(verificarDocumentoPanel);
        verificarDocumentoPanel.setLayout(verificarDocumentoPanelLayout);
        verificarDocumentoPanelLayout.setHorizontalGroup(
            verificarDocumentoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1, GroupLayout.Alignment.TRAILING)
            .addGroup(verificarDocumentoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(verificarDocumentoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jplVerificarDocumento, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        verificarDocumentoPanelLayout.setVerticalGroup(
            verificarDocumentoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(verificarDocumentoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jplVerificarDocumento, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        mainPanel.addTab("<html><b>VERIFICAR DOCUMENTO </b>(<u>2</u>)</html>", verificarDocumentoPanel);

        validarCertificadoPanel.setName(""); // NOI18N

        jlbCertificadoValidar.setText("Certificados en:");

        tipoFirmaBtnGRP.add(rbValidarLlave);
        rbValidarLlave.setText("Archivo");
        rbValidarLlave.addActionListener(evt -> rbValidarLlaveActionPerformed(evt));

        tipoFirmaBtnGRP.add(rbValidarToken);
        rbValidarToken.setText("Token");
        rbValidarToken.addActionListener(evt -> rbValidarTokenActionPerformed(evt));

        jlbCertificadoVldCert.setText("Certificado");

        jtxRutaCertificado.setEditable(false);
        jtxRutaCertificado.setEnabled(false);

        btnAbrirCertificado.setMnemonic('E');
        btnAbrirCertificado.setText("Examinar");
        btnAbrirCertificado.setEnabled(false);
        btnAbrirCertificado.addActionListener(this::btnAbrirCertificadoActionPerformed);

        jlbCertificadoValidarCert.setText("Contrase??a");

        jpfCertClaveTXT.setEnabled(false);

        btnValidar.setMnemonic('v');
        btnValidar.setText("Validar");
        btnValidar.addActionListener(this::btnValidarActionPerformed);

        btnResetValidarForm.setMnemonic('r');
        btnResetValidarForm.setText("Restablecer");
        btnResetValidarForm.addActionListener(this::btnResetValidarFormActionPerformed);

        GroupLayout jplValidarLayout = new GroupLayout(jplValidar);
        jplValidar.setLayout(jplValidarLayout);
        jplValidarLayout.setHorizontalGroup(
            jplValidarLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jplValidarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jplValidarLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jlbCertificadoValidarCert)
                    .addComponent(jlbCertificadoValidar)
                    .addComponent(jlbCertificadoVldCert))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jplValidarLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jplValidarLayout.createSequentialGroup()
                        .addComponent(rbValidarLlave)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbValidarToken)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(GroupLayout.Alignment.TRAILING, jplValidarLayout.createSequentialGroup()
                        .addGroup(jplValidarLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addGroup(jplValidarLayout.createSequentialGroup()
                                .addComponent(btnValidar)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnResetValidarForm))
                            .addComponent(jtxRutaCertificado, GroupLayout.Alignment.LEADING)
                            .addComponent(jpfCertClaveTXT, GroupLayout.Alignment.LEADING))
                        .addGap(18, 18, 18)
                        .addComponent(btnAbrirCertificado)))
                .addContainerGap())
        );
        jplValidarLayout.setVerticalGroup(
            jplValidarLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jplValidarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jplValidarLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(rbValidarLlave)
                    .addComponent(rbValidarToken)
                    .addComponent(jlbCertificadoValidar))
                .addGroup(jplValidarLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jlbCertificadoVldCert)
                    .addComponent(jtxRutaCertificado, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAbrirCertificado))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jplValidarLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jplValidarLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jlbCertificadoValidarCert))
                    .addComponent(jpfCertClaveTXT, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jplValidarLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(btnValidar)
                    .addComponent(btnResetValidarForm))
                .addContainerGap())
        );

        jLabel6.setText("<html><b>RESULTADOS DE VERIFICACI??N DE CERTIFICADO ELECTR??NICO</b></html>");

        tblDatosCertificadosValidar.setModel(new DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Datos del Certificado"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane5.setViewportView(tblDatosCertificadosValidar);

        GroupLayout jPanel6Layout = new GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(jScrollPane5, GroupLayout.DEFAULT_SIZE, 920, Short.MAX_VALUE)
                    .addComponent(jLabel6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                .addContainerGap())
        );

        GroupLayout validarCertificadoPanelLayout = new GroupLayout(validarCertificadoPanel);
        validarCertificadoPanel.setLayout(validarCertificadoPanelLayout);
        validarCertificadoPanelLayout.setHorizontalGroup(
            validarCertificadoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator3)
            .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(validarCertificadoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jplValidar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        validarCertificadoPanelLayout.setVerticalGroup(
            validarCertificadoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jplValidar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        mainPanel.addTab("<html><b>VALIDAR CERTIFICADO DE FIRMA ELECTR??NICA </b>(<u>3</u>)</html>", validarCertificadoPanel);

        jScrollPane1.setViewportView(mainPanel);

        jmAyuda.setMnemonic('a');
        jmAyuda.setText("Ayuda");
        jmAyuda.setHorizontalAlignment(SwingConstants.RIGHT);
        jmAyuda.setHorizontalTextPosition(SwingConstants.RIGHT);
        jmAyuda.setInheritsPopupMenu(true);

        jmiAcerca.setMnemonic('d');
        jmiAcerca.setText("Acerca de");
        jmiAcerca.addActionListener(this::jmiAcercaActionPerformed);
        jmAyuda.add(jmiAcerca);

        jmbMenuPrincipal.add(jmAyuda);

        setJMenuBar(jmbMenuPrincipal);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 770, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 625, Short.MAX_VALUE)
        );
        setPreferredSize(new Dimension(778,681));
        setMaximumSize(new Dimension(778+500,681+252));
        pack();
    }// </editor-fold>//GEN-END:initComponents

    //================= PREVIEW PDF ======================================================
    private BufferedImage loadPagePreview(int numPage){
        BufferedImage bim = null;
        try {
            bim = pdfRenderer.renderImageWithDPI(numPage==0?numPage:numPage-1, 72, ImageType.RGB);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return bim;
    }


    private void loadFirstPreview(){
        try {
            pdfDocument= PDDocument.load(documento);
            pdfRenderer = new PDFRenderer(pdfDocument);
            bim = loadPagePreview(0);

        } catch (IOException e) {
            e.printStackTrace();
        }

        spNumPagesModel = new SpinnerNumberModel(1,1,pdfDocument.getNumberOfPages(),1);
        spNumPages.setModel(spNumPagesModel);

        assert bim != null;
        icon = new ImageIcon(bim);
        JLabel label = new JLabel(icon, JLabel.LEFT);
        label.setMinimumSize(new Dimension(0,0));
        label.setPreferredSize(new Dimension(595, 842));
        label.setMaximumSize(new Dimension(595, 842));
        label.setSize(new Dimension(595, 842));

        label.addMouseListener(new MouseAdapter() {
            Image image;
            @Override
            public void mouseEntered(MouseEvent e) {
                if(Cursor.DEFAULT_CURSOR==getCursor().getType()) {
                    Cursor cursor;
                    Toolkit toolkit = Toolkit.getDefaultToolkit();
                    if(chkNoName.isSelected())
                        image = toolkit.getImage(ClassLoader.getSystemResource("images/demoData-noName.png"));
                    else
                        image = toolkit.getImage(ClassLoader.getSystemResource("images/demoData.png"));
                    Point hotSpot = new Point(0, 0);
                    cursor = toolkit.createCustomCursor(image, hotSpot, "");
                    setCursor(cursor);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
                setCursor(cursor);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                switch (e.getButton()){
                    case MouseEvent.BUTTON1:{
                        Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
                        setCursor(cursor);
                        Integer result=JOptionPane.showConfirmDialog(preview,"??Insertar firma digital aqu???"
                                ,"Datos de firma digital",JOptionPane.OK_CANCEL_OPTION,JOptionPane.INFORMATION_MESSAGE);
                        if(JOptionPane.OK_OPTION==result) {
                            point.setLocation(e.getPoint());
                            System.out.println("Digital sign point placed: X-> " + point.getX() + ", Y->" + point.getY());
                            preview.getGraphics().drawImage(image,point.x,point.y,null);
                        }
                        break;
                    }
                }
            }
        });

        if(preview.getComponentCount()>0){
            preview.remove(preview.getComponent(0));
        }
        preview.add(label,BorderLayout.PAGE_START);
        preview.repaint();
        mainPanel.repaint();
    }

    private void btnVerificarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_btnVerificarActionPerformed
        // Si lo del documento es distinto a lo de la ruta seteamos la ruta
        // del documento a la del textfield
        if (!documento.getAbsolutePath().equals(jtxArchivoFirmadoVerificar.getText())) {
            documento = new File(jtxArchivoFirmadoVerificar.getText());
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            jplVerificarDocumento.setEnabled(false);
            verificarDocumento();
            jplVerificarDocumento.setEnabled(true);
            setCursor(Cursor.getDefaultCursor());
        } catch (NoSuchFileException ex){
            setCursor(Cursor.getDefaultCursor());
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            //System.err.println("Error no se pudo verificar ");
            JOptionPane.showMessageDialog(this, prop.getProperty("mensaje.error.archivo_no_encontrado")+": "+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            jplVerificarDocumento.setEnabled(true);
        } catch (RubricaException ex) {
            setCursor(Cursor.getDefaultCursor());
            System.err.println("Error no se pudo conectar al servicio de OSCP para verificar el certificado ");
            String msgError = ex.getMessage();
            
            if(msgError.contains("Los datos indicados no se corresponden ")){
                msgError = "El archivo puede que este corrupto o que no contenga una firma";
            }
            
            if(msgError.contains("Los datos indicados no son una firma")){
                msgError = "El archivo puede que este corrupto o que no contenga una firma";
            }            
            
            JOptionPane.showMessageDialog(this, 
                    msgError, "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            jplVerificarDocumento.setEnabled(true);
        } catch (Exception ex) {
            setCursor(Cursor.getDefaultCursor());
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            //System.err.println("Error no se pudo verificar ");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            jplVerificarDocumento.setEnabled(true);
        }
    }//GEN-LAST:event_btnVerificarActionPerformed

    private void btnExaminarVerificarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_btnExaminarVerificarActionPerformed
        //filtros
        FileNameExtensionFilter filtrosVerificar = new FileNameExtensionFilter("Documentos de Oficina", "pdf", "p7m", "docx", "xlsx", "pptx", "odt", "ods", "odp", "xml", "p7m");
        documento = abrirArchivo(filtros);
        if (documento != null) {
            jtxArchivoFirmadoVerificar.setText(documento.getAbsolutePath());
            btnVerificar.setEnabled(true);
        }
    }//GEN-LAST:event_btnExaminarVerificarActionPerformed

    private void btnResetearVerificarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_btnResetearVerificarActionPerformed
        System.out.println("resetear campos");
        jtxArchivoFirmadoVerificar.setText("");
        documento = null;

        jtxDocumentoVerificado.setText("");

        DefaultTableModel tableModelCert = (DefaultTableModel) tblDatosFirmanteVerificar.getModel();

        tableModelCert.setRowCount(0);

        //Actualizamos los datos del archivo
        String[] dataCert = new String[7];
        dataCert[0] = "";
        dataCert[1] = "";
        dataCert[2] = "";
        dataCert[3] = "";
        dataCert[4] = "";
        dataCert[5] = "";
        dataCert[6] = "";
        tableModelCert.addRow(dataCert);
        tblDatosFirmanteVerificar.setModel(tableModelCert);
        tableModelCert.fireTableDataChanged();

        btnVerificar.setEnabled(false);

    }//GEN-LAST:event_btnResetearVerificarActionPerformed

    private void btnResetearActionPerformed(ActionEvent evt) {//GEN-FIRST:event_btnResetearActionPerformed
        this.resetForm();
    }//GEN-LAST:event_btnResetearActionPerformed

    private void btnFirmarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_btnFirmarActionPerformed
        if (documento == null || !documento.getAbsolutePath().equals(jtxRutaDocumentoFirmar.getText())) {
            documento = new File(jtxRutaDocumentoFirmar.getText());
        }
        //Cambiamos el cursor a que se ponga en loading
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            jplFirmar.setEnabled(false);
            // Si retorna falso significa que se puso cancelar en el alias
            // si es verdadero se procede a firmar
            if (this.firmarDocumento()) {
                // JOptionPane.showMessageDialog(this, "Documento firmado "+ this.documentoFirmadoTXT.getText(), "Firmador", JOptionPane.INFORMATION_MESSAGE, checkIcon);
                System.out.println("Documento firmado");

                JCheckBox jcbAbrirDocumento = new JCheckBox("Abrir documento");
                String nombreArchivoFirmado = this.jtxArchivoFirmado.getText();
                int tamNombre = this.jtxArchivoFirmado.getText().length();
                if (nombreArchivoFirmado.length() > 30) {
                    nombreArchivoFirmado = this.jtxArchivoFirmado.getText().substring(0, 15) + "..." + this.jtxArchivoFirmado.getText().substring((tamNombre - 10), tamNombre);
                }
                String mensaje = prop.getProperty("mensaje.firmar.documento_firmado") + ": " + nombreArchivoFirmado;

                jcbAbrirDocumento.setMnemonic(KeyEvent.VK_D);
                Object[] params = {mensaje, jcbAbrirDocumento};

                JOptionPane.showMessageDialog(this, params, prop.getProperty("mensaje.firmar.documento_firmado"), JOptionPane.INFORMATION_MESSAGE);

                if (jcbAbrirDocumento.isSelected()) {
                    abrirDocumento();
                }
            }
            jplFirmar.setEnabled(true);
            //Borramos la ruta y la clave una vez que esta firmado
            this.jpfClave.setText("");
            //this.jtxRutaLlaveFirmar.setText("");
            setCursor(Cursor.getDefaultCursor());
        } catch (KeyStoreException e) {
            this.setCursor(Cursor.getDefaultCursor());
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
            if (e.getMessage().equals("java.io.IOException: keystore password was incorrect")) {
                JOptionPane.showMessageDialog(this, prop.getProperty("mensaje.error.clave_incorrecta"), "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, prop.getProperty("mensaje.error.certificado_formato_invalido"), "Error", JOptionPane.ERROR_MESSAGE);
            }
            //Reseteamos el campo de archivo firmado y las tablas de informacion para que no hay confusi??n
            this.jtxArchivoFirmado.setText("");
            resetDatosTabladeFirmante();
            resetDatosTablaCertificadoFirmador();
            jplValidar.setEnabled(true);
        } catch (Exception ex) {
            this.setCursor(Cursor.getDefaultCursor());
            //Reseteamos el campo de archivo firmado y las tablas de informacion para que no hay confusi??n
            this.jtxArchivoFirmado.setText("");
            resetDatosTabladeFirmante();
            resetDatosTablaCertificadoFirmador();
            String mensaje = ex.getMessage()+"";
            
            System.out.println("Exception Normal "+mensaje);


            if(mensaje.contains("org.xml.sax.SAXParseException")){
                mensaje = prop.getProperty("mensaje.error.documento_corrupto");
            }
            
            if(mensaje.contains("IllegalStateException") && mensaje.contains("Content_Types")){
                mensaje = prop.getProperty("mensaje.error.documento_corrupto");
            }
            
            if(mensaje.contains("Las firmas XAdES Enveloped solo pueden realizarse sobre datos XML")){
                mensaje = prop.getProperty("mensaje.error.documento_xml");
            }
            
            JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error no se pudo firmar ");
            jplFirmar.setEnabled(true);
        }
    }//GEN-LAST:event_btnFirmarActionPerformed

    private void btnAbrirArchivoPSKFirmarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_btnAbrirArchivoPSKFirmarActionPerformed
        llave = abrirArchivo();
        if (llave != null) {
            jtxRutaLlaveFirmar.setText(llave.getAbsolutePath());
        }
    }//GEN-LAST:event_btnAbrirArchivoPSKFirmarActionPerformed

    private void jtxRutaLlaveFirmarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jtxRutaLlaveFirmarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jtxRutaLlaveFirmarActionPerformed

    private void btnAbrirArchivoFirmarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_btnAbrirArchivoFirmarActionPerformed

        // Por defecto desbloqueamos el objecto de verificar
        documento = abrirArchivo(filtros);
        if (documento != null) {
            jtxRutaDocumentoFirmar.setText(documento.getAbsolutePath());
            // ===================== load preview =====================
            loadFirstPreview();
            //this.setExtendedState( this.getExtendedState()|JFrame.MAXIMIZED_BOTH );
            //FIT form controls and document preview
            this.setSize(new Dimension(778+500,681+252));

        }
    }//GEN-LAST:event_btnAbrirArchivoFirmarActionPerformed

    private void jtxRutaDocumentoFirmarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jtxRutaDocumentoFirmarActionPerformed

    }//GEN-LAST:event_jtxRutaDocumentoFirmarActionPerformed

    private void rbFirmarTokenActionPerformed(ActionEvent evt) {//GEN-FIRST:event_rbFirmarTokenActionPerformed
        System.out.println("Firmar con Token");
        this.selFirmarConToken();
    }//GEN-LAST:event_rbFirmarTokenActionPerformed

    private void rbFfirmarLlaveActionPerformed(ActionEvent evt) {//GEN-FIRST:event_rbFfirmarLlaveActionPerformed
        System.out.println("Firmar con llave");
        this.selFirmarConArchivo();
    }//GEN-LAST:event_rbFfirmarLlaveActionPerformed

    private void btnResetValidarFormActionPerformed(ActionEvent evt) {//GEN-FIRST:event_btnResetValidarFormActionPerformed
        resetearInfoValidacionCertificado();
    }//GEN-LAST:event_btnResetValidarFormActionPerformed

    private void btnValidarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_btnValidarActionPerformed
               
        Validador validador = new Validador();
        KeyStoreProvider ksp;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        KeyStore ks;
        try {
            // Si es linux y quiere validar token debe tener por lo menos una clave 
            if(OS.equals("linux") && this.rbValidarToken.isSelected() && jpfCertClaveTXT.getPassword().length==0 ){
                throw new CertificadoInvalidoException(prop.getProperty("mensaje.error.linux_clave"));
            } 
            
            jplValidar.setEnabled(false);
            if (this.rbValidarToken.isSelected()) {
                ks = KeyStoreProviderFactory.getKeyStore(new String(jpfCertClaveTXT.getPassword()));
                //ks = KeyStoreProviderFactory.getKeyStore(null);
                if (ks == null) {
                    throw new TokenNoEncontradoException(prop.getProperty("mensaje.error.token_no_encontrado"));
                }

            } else {
                ksp = new FileKeyStoreProvider(jtxRutaCertificado.getText());
                ks = ksp.getKeystore(jpfCertClaveTXT.getPassword());

            }
            
            X509Certificate cert = validador.getCert(ks, jpfCertClaveTXT.getPassword());
			if(cert!=null){
				String revocado;
				try {
					validador.validar(cert);
					revocado = prop.getProperty("mensaje.certificado.no_revocado");
				} catch (OcspValidationException | CRLValidationException ex) {
					revocado = "Revocado";
					JOptionPane.showMessageDialog(getParent(), prop.getProperty("mensaje.error.certificado_revocado"), "Advertencia", JOptionPane.WARNING_MESSAGE);
					Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
				}

				Date fechaHora = TiempoUtils.getFechaHora();
				System.out.println("fechaHora: " + fechaHora);
				System.out.println("Antes: " + fechaHora.before(cert.getNotBefore()) + " " + cert.getNotBefore());
				System.out.println("Despu??s: " + fechaHora.after(cert.getNotAfter()) + " " + cert.getNotAfter());

				String validez = "Certificado v??lido";
				if (fechaHora.before(cert.getNotBefore()) || fechaHora.after(cert.getNotAfter())) {
					validez = "Certificado caducado";
					JOptionPane.showMessageDialog(getParent(), prop.getProperty("mensaje.error.certificado_caducado"), "Advertencia", JOptionPane.WARNING_MESSAGE);
				}
				setearInfoValidacionCertificado(cert);

				agregarValidezCertificado(validez, revocado);
				jpfCertClaveTXT.setText("");
				jplValidar.setEnabled(true);
				setCursor(Cursor.getDefaultCursor());
			}
        } catch (KeyStoreException e) {
            setCursor(Cursor.getDefaultCursor());
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
            if (e.getMessage().equals("java.io.IOException: keystore password was incorrect")) {
                JOptionPane.showMessageDialog(this, prop.getProperty("mensaje.error.clave_incorrecta"), "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, prop.getProperty("mensaje.error.certificado_formato_invalido"), "Error", JOptionPane.ERROR_MESSAGE);
            }
            jpfCertClaveTXT.setText("");
            jplValidar.setEnabled(true);
        } catch (Exception ex) {
            setCursor(Cursor.getDefaultCursor());
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            jpfCertClaveTXT.setText("");
            jplValidar.setEnabled(true);
        }
    }//GEN-LAST:event_btnValidarActionPerformed

    private void btnAbrirCertificadoActionPerformed(ActionEvent evt) {//GEN-FIRST:event_btnAbrirCertificadoActionPerformed
        llaveVerificar = abrirArchivo();
        if (llaveVerificar != null) {
            jtxRutaCertificado.setText(llaveVerificar.getAbsolutePath());

        }
    }//GEN-LAST:event_btnAbrirCertificadoActionPerformed

    private void rbValidarTokenActionPerformed(ActionEvent evt) {//GEN-FIRST:event_rbValidarTokenActionPerformed
        selValidarToken();
    }//GEN-LAST:event_rbValidarTokenActionPerformed

    private void rbValidarLlaveActionPerformed(ActionEvent evt) {//GEN-FIRST:event_rbValidarLlaveActionPerformed
        selValidarArchivo();
    }//GEN-LAST:event_rbValidarLlaveActionPerformed

    private void jmiAcercaActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jmiAcercaActionPerformed
        JPanellAcercaDe jplAcercaDe = new JPanellAcercaDe();
        JButton btnOkAcercaDe = new JButton();
        btnOkAcercaDe.setText("Aceptar");
        btnOkAcercaDe.setMnemonic(KeyEvent.VK_A);
        
        btnOkAcercaDe.addActionListener(evt1 -> {
            Component component = (Component) evt1.getSource();
            JDialog dialog = (JDialog) SwingUtilities.getRoot(component);
            dialog.dispose();
        });
        
        
        Object[] options = {btnOkAcercaDe};
        //JOptionPane.showMessageDialog(this, params, "Acerca de FirmaEC", JOptionPane.NO_OPTION);
        //JOptionPane.showM
        btnOkAcercaDe.addActionListener(evt12 -> {
            Component component = (Component) evt12.getSource();
            JDialog dialog = (JDialog) SwingUtilities.getRoot(component);
            dialog.dispose();
        });
        
        JOptionPane.showOptionDialog(getParent(), jplAcercaDe, "Acerca de FirmaEC",
                JOptionPane.OK_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        
    }//GEN-LAST:event_jmiAcercaActionPerformed

    private static boolean isMac() {
        String osName = System.getProperty("os.name");
        return osName.toUpperCase().contains("MAC");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
        //</editor-fold>

        /* Create and display the form */
        SwingUtilities.invokeLater(() -> {
            new Main(null).setVisible(true);
        });
    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton btnAbrirArchivoFirmar;
    private JButton btnAbrirArchivoPSKFirmar;
    private JButton btnAbrirCertificado;
    private JButton btnExaminarVerificar;
    private JButton btnFirmar;
    private JButton btnResetValidarForm;
    private JButton btnResetear;
    private JButton btnResetearVerificar;
    private JButton btnValidar;
    private JButton btnVerificar;
    private JPanel firmarVerificarDocPanel;
    private JLabel jLabel11;
    private JLabel jLabel12;
    private JLabel jLabel13;
    private JLabel jLabel6;
    private JPanel jPanel2;
    private JPanel jPanel4;
    private JPanel jPanel6;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane3;
    private JScrollPane jScrollPane5;
    private JScrollPane jScrollPane6;
    private JScrollPane jScrollPane7;
    private JSeparator jSeparator1;
    private JSeparator jSeparator2;
    private JSeparator jSeparator3;
    private JLabel jblCertificadoEnFimador;
    private JLabel jblCertificadoFirmar;
    private JLabel jblClave;
    private JLabel jblDocumento;
    private JLabel jlbArchivoFirmadoVerficar;
    private JLabel jlbArchivoFirmador;
    private JLabel jlbArchivoVerificado;
    private JLabel jlbCertificadoValidar;
    private JLabel jlbCertificadoValidarCert;
    private JLabel jlbCertificadoVldCert;
    private JMenu jmAyuda;
    private JMenuBar jmbMenuPrincipal;
    private JMenuItem jmiAcerca;
    //private JMenuItem jmiActualizar;
    private JPasswordField jpfCertClaveTXT;
    private JPasswordField jpfClave;
    private JPanel jplFirmar;
    private JPanel jplValidar;
    private JPanel jplVerificarDocumento;
    private JTextField jtxArchivoFirmado;
    private JTextField jtxArchivoFirmadoVerificar;
    private JTextField jtxDocumentoVerificado;
    private JTextField jtxRutaCertificado;
    private JTextField jtxRutaDocumentoFirmar;
    private JTextField jtxRutaLlaveFirmar;
    private JTabbedPane mainPanel;
    private JRadioButton rbFfirmarLlave;
    private JRadioButton rbFirmarToken;
    private JRadioButton rbValidarLlave;
    private JRadioButton rbValidarToken;
    private JTable tblDatosCertificadosValidar;
    private JTable tblDatosDelCertificadoFirmador;
    private JTable tblDatosDelFirmanteFirmador;
    private JTable tblDatosFirmanteVerificar;
    private ButtonGroup tipoFirmaBtnGRP;
    private JPanel validarCertificadoPanel;
    private JPanel verificarDocumentoPanel;
    // End of variables declaration//GEN-END:variables
    private JPanel preview;
    private JPanel globalSignPanel;
    private final Point point = new Point();
    private JLabel lblOptionalText;
    private JTextField txtOptionalText;
    private SpinnerModel spNumPagesModel;
    private JSpinner spNumPages;
    private JCheckBox chkNoName;
}
