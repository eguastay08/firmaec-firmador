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

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import ec.gob.firmadigital.cliente.FirmaDigital;
import ec.gob.firmadigital.cliente.Validador;
import ec.gob.firmadigital.exceptions.CRLValidationException;
import ec.gob.firmadigital.exceptions.CertificadoInvalidoException;
import ec.gob.firmadigital.exceptions.ConexionValidarCRLException;
import ec.gob.firmadigital.exceptions.DocumentoNoExistenteException;
import ec.gob.firmadigital.exceptions.DocumentoNoPermitidoException;
import ec.gob.firmadigital.exceptions.EntidadCertificadoraNoValidaException;
import ec.gob.firmadigital.exceptions.HoraServidorException;
import ec.gob.firmadigital.exceptions.TokenNoConectadoException;
import ec.gob.firmadigital.exceptions.TokenNoEncontradoException;
import ec.gob.firmadigital.firmador.update.Update;
import ec.gob.firmadigital.utils.CertificadoEcUtils;
import ec.gob.firmadigital.utils.FirmadorFileUtils;
import ec.gob.firmadigital.utils.WordWrapCellRenderer;
import io.rubrica.certificate.ValidationResult;
import io.rubrica.core.RubricaException;
import io.rubrica.keystore.Alias;
import io.rubrica.keystore.FileKeyStoreProvider;
import io.rubrica.keystore.KeyStoreProvider;
import io.rubrica.keystore.KeyStoreProviderFactory;
import io.rubrica.keystore.KeyStoreUtilities;
import io.rubrica.ocsp.OcspValidationException;
import io.rubrica.sign.cms.DatosUsuario;
import java.awt.Cursor;
import java.awt.Desktop;
import java.time.LocalDateTime;
import javax.swing.JCheckBox;
import javax.swing.table.DefaultTableModel;


/**
 *
 * @author jdcalle
 */
public class Main extends javax.swing.JFrame {

    private KeyStore ks;
    private File documento;
    private File llave;
    private File llaveVerificar;
    private File ultimaCarpeta;
    private String mensajeError;
    private static final String RUTA_IMG = "images/";
    private static final String CHECK_IMG = "CheckIcon.png";
    private static final String NOTCHECK_IMG = "DeleteIcon.png";
    private final List<String> extensionesPermitidas;
    private final FileNameExtensionFilter filtros = new FileNameExtensionFilter("Documentos de Oficina", "pdf", "p7m", "docx", "xlsx", "pptx", "odt", "ods", "odp","xml");
    private static final String OS = System.getProperty("os.name").toLowerCase();
    //private final ImageIcon checkIcon = new ImageIcon(ClassLoader.getSystemResource(RUTA_IMG + CHECK_IMG));
    //private final ImageIcon notCheckIcon = new ImageIcon(ClassLoader.getSystemResource(RUTA_IMG + NOTCHECK_IMG));
    private PDDocument pdfDocument;
    private PDFRenderer pdfRenderer;
    private static final String URL_GOBIERNO_DIGITAL = "http://www.gobiernoelectronico.gob.ec";
    
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    

    /**
     * Creates new form Main
     */
    public Main() {
        
        extensionesPermitidas = new ArrayList<>();
        // Extensiones permitidas"pdf","p7m","docx","xlsx","pptx","odt","ods","odp"
        extensionesPermitidas.add("pdf");
        extensionesPermitidas.add("p7m");
        extensionesPermitidas.add("docx");
        extensionesPermitidas.add("xlsx");
        extensionesPermitidas.add("pptx");
        extensionesPermitidas.add("odt");
        extensionesPermitidas.add("ods");
        extensionesPermitidas.add("odp");
        extensionesPermitidas.add("xml");

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
        btnAbrirArchivoFirmar.setMnemonic(java.awt.event.KeyEvent.VK_E);
        btnAbrirArchivoPSKFirmar.setMnemonic(java.awt.event.KeyEvent.VK_X);
        btnFirmar.setMnemonic(java.awt.event.KeyEvent.VK_F);
        btnResetear.setMnemonic(java.awt.event.KeyEvent.VK_R);
        
        btnExaminarVerificar.setMnemonic(java.awt.event.KeyEvent.VK_E);
        btnResetearVerificar.setMnemonic(java.awt.event.KeyEvent.VK_R);
        
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

    private File abrirArchivo() {
        final JFileChooser fileChooser = new JFileChooser();
        File archivo;
        fileChooser.setCurrentDirectory(ultimaCarpeta);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            archivo = fileChooser.getSelectedFile();
            ultimaCarpeta = fileChooser.getCurrentDirectory();
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
        this.jtxRutaLlaveFirmar.setText("");
        //this.certificadosJTR.getModel().
        resetDatosTabladeFirmante();
        resetDatosTablaCertificadoFirmador();

    }

    private void selFirmarConArchivo() {
        this.btnFirmar.setEnabled(true);
        this.jtxRutaLlaveFirmar.setEnabled(true);
        this.btnAbrirArchivoPSKFirmar.setEnabled(true);
        // Si es windows no hay que habilitar el campo de contraseña
        
        this.jpfClave.setEnabled(true);
        
    }
    
    private void selValidarArchivo() {
        jtxRutaCertificado.setEnabled(true);
        btnAbrirCertificado.setEnabled(true);
        certClaveTXT.setEnabled(true);
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
        
        if (!esWindows()) {
            this.certClaveTXT.setEnabled(true);
        } else {
            this.certClaveTXT.setEnabled(false);
        }
    }

    private boolean esWindows() {
        return (OS.contains("win"));// .indexOf("win") >= 0);
    }

    /*
    Valida que esten los campos necesarios para firmar
     */
    private void validacionPreFirmar() throws DocumentoNoExistenteException, TokenNoConectadoException, DocumentoNoPermitidoException {
        //Revisamos si existe el documento a firmar
        // TODO no hacer un return directamente, se podria validar todos los parametros e ir aumentando los errores
        if (documento ==null )
            throw new DocumentoNoExistenteException("Documento "+this.jtxRutaDocumentoFirmar.getText() + " no existe");

        if(!documento.exists()) 
            throw new DocumentoNoExistenteException("Documento "+documento.getAbsolutePath() + " no existe");
        
        if(llave == null){
            throw new DocumentoNoExistenteException("No hay llave seleccionada");
        }    
        
        if (rbFfirmarLlave.isSelected() && !llave.exists()) {
            throw new DocumentoNoExistenteException("La llave "+llave.getAbsolutePath() + " no existe");
        }
        // Si firma con token debe
        if (rbFirmarToken.isSelected() && !hayToken()) {
            throw new TokenNoConectadoException("Token no está conectado");
        }
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

    //Revisa si hay token conectado al usb
    private boolean hayToken() {
        // TODO conectar a rubica para validar si hay token
        return true;
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
            if(cert.getDatosUsuario().getApellido()==null){
                apellido = "";
            }
            String nombre = cert.getDatosUsuario().getNombre();
            if(cert.getDatosUsuario().getNombre()==null){
                nombre = "";
            }
            dataCert[1] = nombre + " " +apellido;
            dataCert[2] = cert.getDatosUsuario().getInstitucion();
            dataCert[3] = cert.getDatosUsuario().getCargo();
            dataCert[4] = format1.format(cert.getValidFrom().getTime());
            dataCert[5] = format1.format(cert.getValidTo().getTime());
            dataCert[6] = format1.format(cert.getGenerated().getTime());
            String revocadoStr = "No ha sido revocado";
            if(cert.getRevocated()==null){
                revocadoStr = "No se pudo verificar el certificado por el Servicio API, OSCP o CRL";
            }else if(cert.getRevocated()){
                revocadoStr = "Revocado";
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
        String extDocumento = FirmadorFileUtils.getFileExtension(documento);
        if(!extensionesPermitidas.stream().anyMatch((extension) -> (extension.equals(extDocumento))))
            throw new DocumentoNoPermitidoException("Extensión ." + extDocumento + " no permitida");
    }

    //TODO botar exceptions en vez de return false
    private boolean firmarDocumento() throws Exception  {
        // Vemos si es un documento permitido primero
        validacionPreFirmar();

        Boolean validacion = validarFirma();

        FirmaDigital firmaDigital = new FirmaDigital();
        
        byte[] docSigned = firmaDigital.firmar(ks, documento, jpfClave.getPassword());
        String nombreDocFirmado = crearNombreFirmado(documento);

        // Obtenemos el certificado firmante para obtener los datos de usuarios
        List<Alias> aliases = KeyStoreUtilities.getSigningAliases(ks);
        Alias alias = aliases.get(0);
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias.getAlias());
        
        String nombre = CertificadoEcUtils.getNombreCA(cert);
        
        System.out.println("Nombre: " +nombre);
        
        DatosUsuario datosUsuario = CertificadoEcUtils.getDatosUsuarios(cert);        
        
        agregarDatosTabladeFirmante(datosUsuario);
        
        agregarDatosTablaCertificadoFirmador(cert, datosUsuario);
        
        //Si todo va bien creamos el arschivo
        FirmadorFileUtils.saveByteArrayToDisc(docSigned, nombreDocFirmado);
        jtxArchivoFirmado.setText(nombreDocFirmado);

        return true;
    }
    
    
    private void agregarDatosTabladeFirmante(DatosUsuario datosUsuario) {
        DefaultTableModel tableModel = (DefaultTableModel) tblDatosDelFirmanteFirmador.getModel();
        
        if(datosUsuario == null){
            System.out.println("datos usuarios es nulo");
            datosUsuario = new DatosUsuario();
        }

        tableModel.setRowCount(0);

        //Actualizamos los datos del archivo
        String[] data = new String[5];
        data[0] = datosUsuario.getCedula();
        String nombres = datosUsuario.getNombre()+ " " +datosUsuario.getApellido();
        data[1] = nombres;
        data[2] = datosUsuario.getInstitucion();
        data[3] = datosUsuario.getCargo();
        data[4] = LocalDateTime.now().toString();
        
        
        tableModel.addRow(data);
        
        tblDatosDelFirmanteFirmador.setModel(tableModel);
        tableModel.fireTableDataChanged();
        
    }
    
    private void agregarDatosTablaCertificadoFirmador(X509Certificate cert, DatosUsuario datosUsuario){
        DefaultTableModel tableModel = (DefaultTableModel) tblDatosDelCertificadoFirmador.getModel(); 

        tableModel.setRowCount(0);

        //Actualizamos los datos del archivo
        String[] data = new String[5];
        data[0] = "Certificado Emitido por: "+CertificadoEcUtils.getNombreCA(cert);
        tableModel.addRow(data);
        
        data[0] = "Cédula: "+datosUsuario.getCedula();
        tableModel.addRow(data);
        
        data[0] = "Nombres: "+datosUsuario.getNombre();
        tableModel.addRow(data);

        data[0] = "Apellidos: "+datosUsuario.getApellido();
        tableModel.addRow(data);

        data[0] = "Institución: "+datosUsuario.getInstitucion();
        tableModel.addRow(data);
        
        data[0] = "Cargo: "+datosUsuario.getCargo();
        tableModel.addRow(data);

        data[0] = "Fecha de Emisión: "+cert.getNotBefore();
        tableModel.addRow(data);
        
        data[0] = "Fecha de Expiración: "+ cert.getNotAfter();
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
        if(nombres == null){
            nombres = "";
        }
        
        String apellidos = datosUsuario.getApellido();
        if(apellidos == null){
            apellidos = "";
        }
        data[1] = nombres +" "+ apellidos;
        data[2] = datosUsuario.getInstitucion();
        data[3] = datosUsuario.getCargo();
        data[4] = ""; //LocalDateTime.now().toString();
        
        tableModel.addRow(data);
        
        tblDatosDelFirmanteFirmador.setModel(tableModel);
        tableModel.fireTableDataChanged();    
    }
    
    private void resetDatosTablaCertificadoFirmador(){
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

     private boolean validarFirma() throws TokenNoEncontradoException, KeyStoreException, IOException, RubricaException, HoraServidorException, CertificadoInvalidoException, CRLValidationException, OcspValidationException, EntidadCertificadoraNoValidaException, ConexionValidarCRLException    {
        System.out.println("Validar Firma");
        if (this.rbFirmarToken.isSelected()) {
            ks = KeyStoreProviderFactory.getKeyStore(jpfClave.getPassword().toString());
            if (ks == null) {
                //JOptionPane.showMessageDialog(frame, "No se encontro un token!");
                throw new TokenNoEncontradoException("No se encontro token!");
            }

        } else {
            KeyStoreProvider ksp = new FileKeyStoreProvider(jtxRutaLlaveFirmar.getText());
            ks = ksp.getKeystore(jpfClave.getPassword());
            
         }

         Validador validador = new Validador();
         X509Certificate cert = validador.getCert(ks,jpfClave.getPassword());
         validador.validar(cert);
         return true;
    }
    
    // TODO Crear clase para manejar esto
    private String crearNombreFirmado(File documento){
        // buscamos el nombre para crear el signed
        // TODO validar si hay otro archivo de momento lo sobre escribe
        // USAR solo getAbsolutPath, talvez sin ruta
        String nombreCompleto = documento.getAbsolutePath();
        
        String nombre = nombreCompleto.replaceFirst("[.][^.]+$", "");

        //String extension = getFileExtension(documento);
        String extension =  FirmadorFileUtils.getFileExtension(documento);
        
        System.out.println(nombre + "-signed." + extension);
        return nombre + "-signed." + extension;
    }
    
    private String obtenerUrlCRL(List<String> urls){
        for(String url : urls){
            if(url.toLowerCase().contains("crl"))
                return url;
        }
        return null;
    }
    
    private void setearInfoValidacionCertificado(X509Certificate cert){
        if(cert != null){
            String emisor = CertificadoEcUtils.getNombreCA(cert);
            
            DatosUsuario datosUsuario = CertificadoEcUtils.getDatosUsuarios(cert);

            DefaultTableModel tableModel = (DefaultTableModel) tblDatosCertificadosValidar.getModel();

            tableModel.setRowCount(0);

            //Actualizamos los datos del archivo
            String[] data = new String[1];
            data[0] = "Certificado Emitido por: " + CertificadoEcUtils.getNombreCA(cert);
            tableModel.addRow(data);

            data[0] = "Cédula: " + datosUsuario.getCedula();
            tableModel.addRow(data);

            data[0] = "Nombres: " + datosUsuario.getNombre();
            tableModel.addRow(data);

            data[0] = "Apellidos: " + datosUsuario.getApellido();
            tableModel.addRow(data);

            data[0] = "Institución: " + datosUsuario.getInstitucion();
            tableModel.addRow(data);

            data[0] = "Cargo: " + datosUsuario.getCargo();
            tableModel.addRow(data);

            data[0] = "Fecha de Emisión: " + cert.getNotBefore();
            tableModel.addRow(data);

            data[0] = "Fecha de Expiración: " + cert.getNotAfter();
            tableModel.addRow(data);

            tblDatosCertificadosValidar.setModel(tableModel);
            tableModel.fireTableDataChanged();
        }
        //TOdo botar error si es null
    }
    
    private void abrirDocumento() throws IOException{    
        if (esWindows()) {
            String cmd = "rundll32 url.dll,FileProtocolHandler " + jtxArchivoFirmado.getText();
            Runtime.getRuntime().exec(cmd);
        } else {
            File docFirmado = new File(jtxArchivoFirmado.getText());
            Desktop.getDesktop().open(docFirmado);
        }
    }
    
    private void agregarValidezCertificado(String validez){
        DefaultTableModel tableModel = (DefaultTableModel) tblDatosCertificadosValidar.getModel();
            //Actualizamos los datos del archivo
            String[] data = new String[1];
            data[0] = "Estado certificado: "+validez;
            tableModel.addRow(data);
            tblDatosCertificadosValidar.setModel(tableModel);
            tableModel.fireTableDataChanged();
    }
    
    
    
    private void resetInfoValidacionCertificado(){
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
        certClaveTXT.setText("");
        resetInfoValidacionCertificado();
        //TOdo botar error si es null
    }
    
    private String resultadosCRL(ValidationResult result) {
        if(result == result.CANNOT_DOWNLOAD_CRL)
            return "No se pudo descargar el archivo CRL\nRevisar conexión de Internet";
        if(result.isValid())
            return "Válido";        
        return "Inválido";
    }
    
    public void actualizar(){
        Object[] options = {"Si", "No"};
        int n = JOptionPane.showOptionDialog(getParent(), "Desea actualizar el cliente?", "Confirmar",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

        if (n == 0) {
            logger.info("Se solicita actualización...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                Update update = new Update();

//                getParent().getParent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                update.updateCliente();

                //getParent().getParent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                // mostrarMensaje("Actualizado con éxito, se cerrará la ventana");
                System.exit(0);
                
            } catch (IOException ex) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                ex.printStackTrace();
            }
        }
    }



    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tipoFirmaBtnGRP = new javax.swing.ButtonGroup();
        mainPanel = new javax.swing.JTabbedPane();
        firmarVerificarDocPanel = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();
        jplFirmar = new javax.swing.JPanel();
        jblCertificadoEnFimador = new javax.swing.JLabel();
        rbFfirmarLlave = new javax.swing.JRadioButton();
        rbFirmarToken = new javax.swing.JRadioButton();
        jblDocumento = new javax.swing.JLabel();
        jtxRutaDocumentoFirmar = new javax.swing.JTextField();
        btnAbrirArchivoFirmar = new javax.swing.JButton();
        jblCertificadoFirmar = new javax.swing.JLabel();
        jtxRutaLlaveFirmar = new javax.swing.JTextField();
        btnAbrirArchivoPSKFirmar = new javax.swing.JButton();
        jblClave = new javax.swing.JLabel();
        jpfClave = new javax.swing.JPasswordField();
        btnFirmar = new javax.swing.JButton();
        btnResetear = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jlbArchivoFirmador = new javax.swing.JLabel();
        jtxArchivoFirmado = new javax.swing.JTextField();
        jScrollPane6 = new javax.swing.JScrollPane();
        tblDatosDelFirmanteFirmador = new javax.swing.JTable();
        jScrollPane7 = new javax.swing.JScrollPane();
        tblDatosDelCertificadoFirmador = new javax.swing.JTable();
        verificarDocumentoPanel = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jplVerificarDocumento = new javax.swing.JPanel();
        jlbArchivoFirmadoVerficar = new javax.swing.JLabel();
        jtxArchivoFirmadoVerificar = new javax.swing.JTextField();
        btnExaminarVerificar = new javax.swing.JButton();
        btnVerificar = new javax.swing.JButton();
        btnResetearVerificar = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jlbArchivoVerificado = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblDatosFirmanteVerificar = new javax.swing.JTable();
        jLabel13 = new javax.swing.JLabel();
        jtxDocumentoVerificado = new javax.swing.JTextField();
        validarCertificadoPanel = new javax.swing.JPanel();
        jSeparator3 = new javax.swing.JSeparator();
        jplValidar = new javax.swing.JPanel();
        jlbCertificadoValidar = new javax.swing.JLabel();
        rbValidarLlave = new javax.swing.JRadioButton();
        rbValidarToken = new javax.swing.JRadioButton();
        jlbCertificadoVldCert = new javax.swing.JLabel();
        jtxRutaCertificado = new javax.swing.JTextField();
        btnAbrirCertificado = new javax.swing.JButton();
        jlbCertificadoValidarCert = new javax.swing.JLabel();
        certClaveTXT = new javax.swing.JPasswordField();
        btnValidar = new javax.swing.JButton();
        btnResetValidarForm = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblDatosCertificadosValidar = new javax.swing.JTable();
        jmbMenuPrincipal = new javax.swing.JMenuBar();
        jmAyuda = new javax.swing.JMenu();
        jmiAcerca = new javax.swing.JMenuItem();
        jmiActualizar = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("FirmaEC");

        firmarVerificarDocPanel.setName(""); // NOI18N

        jblCertificadoEnFimador.setText("Certificado en:");

        tipoFirmaBtnGRP.add(rbFfirmarLlave);
        rbFfirmarLlave.setText("Archivo");
        rbFfirmarLlave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbFfirmarLlaveActionPerformed(evt);
            }
        });

        tipoFirmaBtnGRP.add(rbFirmarToken);
        rbFirmarToken.setText("Token");
        rbFirmarToken.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbFirmarTokenActionPerformed(evt);
            }
        });

        jblDocumento.setText("Documento");

        jtxRutaDocumentoFirmar.setEditable(false);
        jtxRutaDocumentoFirmar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtxRutaDocumentoFirmarActionPerformed(evt);
            }
        });

        btnAbrirArchivoFirmar.setText("Examinar");
        btnAbrirArchivoFirmar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAbrirArchivoFirmarActionPerformed(evt);
            }
        });

        jblCertificadoFirmar.setText("Certificado");

        jtxRutaLlaveFirmar.setEditable(false);
        jtxRutaLlaveFirmar.setEnabled(false);
        jtxRutaLlaveFirmar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtxRutaLlaveFirmarActionPerformed(evt);
            }
        });

        btnAbrirArchivoPSKFirmar.setText("Examinar");
        btnAbrirArchivoPSKFirmar.setEnabled(false);
        btnAbrirArchivoPSKFirmar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAbrirArchivoPSKFirmarActionPerformed(evt);
            }
        });

        jblClave.setText("Contraseña");

        jpfClave.setEnabled(false);

        btnFirmar.setText("Firmar");
        btnFirmar.setEnabled(false);
        btnFirmar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFirmarActionPerformed(evt);
            }
        });

        btnResetear.setText("Restablecer");
        btnResetear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetearActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jplFirmarLayout = new javax.swing.GroupLayout(jplFirmar);
        jplFirmar.setLayout(jplFirmarLayout);
        jplFirmarLayout.setHorizontalGroup(
            jplFirmarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jplFirmarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jplFirmarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jblCertificadoFirmar)
                    .addComponent(jblClave)
                    .addComponent(jblCertificadoEnFimador)
                    .addComponent(jblDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(jplFirmarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jplFirmarLayout.createSequentialGroup()
                        .addComponent(btnFirmar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnResetear))
                    .addComponent(jpfClave)
                    .addComponent(jtxRutaLlaveFirmar)
                    .addComponent(jtxRutaDocumentoFirmar))
                .addGap(18, 18, 18)
                .addGroup(jplFirmarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAbrirArchivoPSKFirmar, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnAbrirArchivoFirmar, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
            .addGroup(jplFirmarLayout.createSequentialGroup()
                .addGap(148, 148, 148)
                .addComponent(rbFfirmarLlave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rbFirmarToken)
                .addGap(660, 660, 660))
        );
        jplFirmarLayout.setVerticalGroup(
            jplFirmarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jplFirmarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jplFirmarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbFfirmarLlave)
                    .addComponent(rbFirmarToken)
                    .addComponent(jblCertificadoEnFimador))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jplFirmarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jtxRutaDocumentoFirmar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAbrirArchivoFirmar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jblDocumento))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jplFirmarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jblCertificadoFirmar)
                    .addComponent(jtxRutaLlaveFirmar, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAbrirArchivoPSKFirmar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jplFirmarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jpfClave, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jblClave))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jplFirmarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnFirmar)
                    .addComponent(btnResetear))
                .addGap(8, 8, 8))
        );

        jLabel11.setText("<html><b>DATOS DEL FIRMANTE</b></html>");

        jlbArchivoFirmador.setText("Archivo Firmado");

        jtxArchivoFirmado.setEditable(false);

        tblDatosDelFirmanteFirmador.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null}
            },
            new String [] {
                "Cédula", "Nombres", "Institución", "Cargo", "Fecha"
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

        tblDatosDelCertificadoFirmador.setModel(new javax.swing.table.DefaultTableModel(
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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jlbArchivoFirmador)
                        .addGap(24, 24, 24)
                        .addComponent(jtxArchivoFirmado))
                    .addComponent(jScrollPane6)
                    .addComponent(jScrollPane7))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jtxArchivoFirmado, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jlbArchivoFirmador))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout firmarVerificarDocPanelLayout = new javax.swing.GroupLayout(firmarVerificarDocPanel);
        firmarVerificarDocPanel.setLayout(firmarVerificarDocPanelLayout);
        firmarVerificarDocPanelLayout.setHorizontalGroup(
            firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator2)
            .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jplFirmar, javax.swing.GroupLayout.PREFERRED_SIZE, 944, Short.MAX_VALUE))
                    .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        firmarVerificarDocPanelLayout.setVerticalGroup(
            firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jplFirmar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        mainPanel.addTab("<html><b>FIRMAR DOCUMENTO</b></html>", firmarVerificarDocPanel);

        jlbArchivoFirmadoVerficar.setText("Archivo Firmado:");

        jtxArchivoFirmadoVerificar.setEditable(false);

        btnExaminarVerificar.setText("Examinar");
        btnExaminarVerificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExaminarVerificarActionPerformed(evt);
            }
        });

        btnVerificar.setText("Verificar Archivo");
        btnVerificar.setEnabled(false);
        btnVerificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerificarActionPerformed(evt);
            }
        });

        btnResetearVerificar.setText("Restablecer");
        btnResetearVerificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetearVerificarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jplVerificarDocumentoLayout = new javax.swing.GroupLayout(jplVerificarDocumento);
        jplVerificarDocumento.setLayout(jplVerificarDocumentoLayout);
        jplVerificarDocumentoLayout.setHorizontalGroup(
            jplVerificarDocumentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jplVerificarDocumentoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jlbArchivoFirmadoVerficar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jplVerificarDocumentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jplVerificarDocumentoLayout.createSequentialGroup()
                        .addComponent(btnVerificar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnResetearVerificar))
                    .addComponent(jtxArchivoFirmadoVerificar))
                .addGap(18, 18, 18)
                .addComponent(btnExaminarVerificar)
                .addContainerGap())
        );
        jplVerificarDocumentoLayout.setVerticalGroup(
            jplVerificarDocumentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jplVerificarDocumentoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jplVerificarDocumentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jtxArchivoFirmadoVerificar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExaminarVerificar)
                    .addComponent(jlbArchivoFirmadoVerficar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jplVerificarDocumentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnResetearVerificar)
                    .addComponent(btnVerificar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel12.setText("<html><b>RESULTADOS DE LA VERIFICACIÓN DEL ARCHIVO FIRMADO ELECTRÓNICAMENTE</b></html>");

        jlbArchivoVerificado.setText("Archivo:");

        tblDatosFirmanteVerificar.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Cédula", "Nombres", "Institución", "Cargo", "Válido Desde", "Válido Hasta", "Fecha Firmado", "Revocado"
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

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 932, Short.MAX_VALUE)))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jlbArchivoVerificado)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jtxDocumentoVerificado))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jlbArchivoVerificado))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jtxDocumentoVerificado, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout verificarDocumentoPanelLayout = new javax.swing.GroupLayout(verificarDocumentoPanel);
        verificarDocumentoPanel.setLayout(verificarDocumentoPanelLayout);
        verificarDocumentoPanelLayout.setHorizontalGroup(
            verificarDocumentoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(verificarDocumentoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(verificarDocumentoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jplVerificarDocumento, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        verificarDocumentoPanelLayout.setVerticalGroup(
            verificarDocumentoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(verificarDocumentoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jplVerificarDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        mainPanel.addTab("<html><b>VERIFICAR DOCUMENTO</b></html>", verificarDocumentoPanel);

        validarCertificadoPanel.setName(""); // NOI18N

        jlbCertificadoValidar.setText("Certificados en:");

        tipoFirmaBtnGRP.add(rbValidarLlave);
        rbValidarLlave.setText("Archivo");
        rbValidarLlave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbValidarLlaveActionPerformed(evt);
            }
        });

        tipoFirmaBtnGRP.add(rbValidarToken);
        rbValidarToken.setText("Token");
        rbValidarToken.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbValidarTokenActionPerformed(evt);
            }
        });

        jlbCertificadoVldCert.setText("Certificado");

        jtxRutaCertificado.setEditable(false);
        jtxRutaCertificado.setEnabled(false);

        btnAbrirCertificado.setMnemonic('E');
        btnAbrirCertificado.setText("Examinar");
        btnAbrirCertificado.setEnabled(false);
        btnAbrirCertificado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAbrirCertificadoActionPerformed(evt);
            }
        });

        jlbCertificadoValidarCert.setText("Contraseña");

        certClaveTXT.setEnabled(false);

        btnValidar.setMnemonic('v');
        btnValidar.setText("Validar");
        btnValidar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnValidarActionPerformed(evt);
            }
        });

        btnResetValidarForm.setMnemonic('r');
        btnResetValidarForm.setText("Restablecer");
        btnResetValidarForm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetValidarFormActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jplValidarLayout = new javax.swing.GroupLayout(jplValidar);
        jplValidar.setLayout(jplValidarLayout);
        jplValidarLayout.setHorizontalGroup(
            jplValidarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jplValidarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jplValidarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jlbCertificadoValidarCert)
                    .addComponent(jlbCertificadoValidar)
                    .addComponent(jlbCertificadoVldCert))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jplValidarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jplValidarLayout.createSequentialGroup()
                        .addComponent(rbValidarLlave)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbValidarToken)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jplValidarLayout.createSequentialGroup()
                        .addGroup(jplValidarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jplValidarLayout.createSequentialGroup()
                                .addComponent(btnValidar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnResetValidarForm))
                            .addComponent(jtxRutaCertificado, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(certClaveTXT, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(18, 18, 18)
                        .addComponent(btnAbrirCertificado)))
                .addContainerGap())
        );
        jplValidarLayout.setVerticalGroup(
            jplValidarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jplValidarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jplValidarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbValidarLlave)
                    .addComponent(rbValidarToken)
                    .addComponent(jlbCertificadoValidar))
                .addGroup(jplValidarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jlbCertificadoVldCert)
                    .addComponent(jtxRutaCertificado, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAbrirCertificado))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jplValidarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jplValidarLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jlbCertificadoValidarCert))
                    .addComponent(certClaveTXT, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jplValidarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnValidar)
                    .addComponent(btnResetValidarForm))
                .addContainerGap())
        );

        jLabel6.setText("<html><b>RESULTADOS DE VERIFICACIÓN DE CERTIFICADO ELECTRÓNICO</b></html>");

        tblDatosCertificadosValidar.setModel(new javax.swing.table.DefaultTableModel(
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

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 920, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout validarCertificadoPanelLayout = new javax.swing.GroupLayout(validarCertificadoPanel);
        validarCertificadoPanel.setLayout(validarCertificadoPanelLayout);
        validarCertificadoPanelLayout.setHorizontalGroup(
            validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator3)
            .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jplValidar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        validarCertificadoPanelLayout.setVerticalGroup(
            validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jplValidar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        mainPanel.addTab("<html><b>VALIDAR CERTIFICADO DE FIRMA ELECTRÓNICA</b></html>", validarCertificadoPanel);

        jmAyuda.setText("Ayuda");
        jmAyuda.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jmAyuda.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jmAyuda.setInheritsPopupMenu(true);

        jmiAcerca.setText("Acerca de");
        jmiAcerca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiAcercaActionPerformed(evt);
            }
        });
        jmAyuda.add(jmiAcerca);

        jmiActualizar.setText("Actualizar");
        jmiActualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiActualizarActionPerformed(evt);
            }
        });
        jmAyuda.add(jmiActualizar);

        jmbMenuPrincipal.add(jmAyuda);

        setJMenuBar(jmbMenuPrincipal);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnVerificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerificarActionPerformed
        // Si lo del documento es distinto a lo de la ruta seteamos la ruta
        // del documento a la del textfield
        if (!documento.getAbsolutePath().equals(jtxArchivoFirmadoVerificar.getText())) 
            documento = new File(jtxArchivoFirmadoVerificar.getText());
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            jplVerificarDocumento.setEnabled(false);
            verificarDocumento();
            jplVerificarDocumento.setEnabled(true);
            setCursor(Cursor.getDefaultCursor());
        }catch(RubricaException ex){
            setCursor(Cursor.getDefaultCursor());
            System.err.println("Error no se pudo conectar al servicio de OSCP para verificar el certificado ");
            JOptionPane.showMessageDialog(this, "Error no se pudo conectar al servicio de OSCP para verificar el certificado\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            jplVerificarDocumento.setEnabled(true);
        }catch (Exception ex) {
            setCursor(Cursor.getDefaultCursor());
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            //System.err.println("Error no se pudo verificar ");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            jplVerificarDocumento.setEnabled(true);
        }
    }//GEN-LAST:event_btnVerificarActionPerformed

    private void btnExaminarVerificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExaminarVerificarActionPerformed
        //filtros
        FileNameExtensionFilter filtrosVerificar = new FileNameExtensionFilter("Documentos de Oficina", "pdf", "p7m", "docx", "xlsx", "pptx", "odt", "ods", "odp","xml","p7m");
        documento = abrirArchivo(filtros);
        if (documento != null) {
            jtxArchivoFirmadoVerificar.setText(documento.getAbsolutePath());
            btnVerificar.setEnabled(true);
        }
    }//GEN-LAST:event_btnExaminarVerificarActionPerformed

    private void btnResetearVerificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetearVerificarActionPerformed
        System.out.println("resetear campos");
        jtxArchivoFirmadoVerificar.setText("");
        documento=null;
        
        jtxDocumentoVerificado.setText("");
        //datosFirmanteVerificarTbl
        
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
        
    }//GEN-LAST:event_btnResetearVerificarActionPerformed

    private void btnResetearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetearActionPerformed
        this.resetForm();
    }//GEN-LAST:event_btnResetearActionPerformed

    private void btnFirmarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFirmarActionPerformed
        if (documento == null || !documento.getAbsolutePath().equals(jtxRutaDocumentoFirmar.getText()))
        documento = new File(jtxRutaDocumentoFirmar.getText());
        //Cambiamos el cursor a que se ponga en loading
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            jplFirmar.setEnabled(false);
            this.firmarDocumento();
            // JOptionPane.showMessageDialog(this, "Documento firmado "+ this.documentoFirmadoTXT.getText(), "Firmador", JOptionPane.INFORMATION_MESSAGE, checkIcon);
            System.out.println("Documento firmado");

            JCheckBox jcbAbrirDocumento = new JCheckBox("Abrir documento");
            String mensaje = "Documento firmado: " + this.jtxArchivoFirmado.getText();

            Object[] params = {mensaje, jcbAbrirDocumento};
            JOptionPane.showMessageDialog(this, params, "Documento Firmado", JOptionPane.INFORMATION_MESSAGE);

            if (jcbAbrirDocumento.isSelected()){
                abrirDocumento();
            }
            jplFirmar.setEnabled(true);
            
            //Borramos la ruta y la clave una vez que esta firmado
            this.jpfClave.setText("");
            this.jtxRutaLlaveFirmar.setText("");
            setCursor(Cursor.getDefaultCursor());
        }catch(KeyStoreException e){
            this.setCursor(Cursor.getDefaultCursor());
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
            JOptionPane.showMessageDialog(this, "Contraseña Incorrecta", "Error", JOptionPane.ERROR_MESSAGE);
            jplValidar.setEnabled(true);
        } catch (Exception ex) {
            this.setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error no se pudo firmar ");
            jplFirmar.setEnabled(true);
        } 
    }//GEN-LAST:event_btnFirmarActionPerformed

    private void btnAbrirArchivoPSKFirmarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAbrirArchivoPSKFirmarActionPerformed
        llave = abrirArchivo();
        if (llave != null) {
            jtxRutaLlaveFirmar.setText(llave.getAbsolutePath());
        }
    }//GEN-LAST:event_btnAbrirArchivoPSKFirmarActionPerformed

    private void jtxRutaLlaveFirmarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtxRutaLlaveFirmarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jtxRutaLlaveFirmarActionPerformed

    private void btnAbrirArchivoFirmarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAbrirArchivoFirmarActionPerformed

        // Por defecto desbloqueamos el objecto de verificar
        documento = abrirArchivo(filtros);
        if (documento != null) {
            jtxRutaDocumentoFirmar.setText(documento.getAbsolutePath());

        }
    }//GEN-LAST:event_btnAbrirArchivoFirmarActionPerformed

    private void jtxRutaDocumentoFirmarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtxRutaDocumentoFirmarActionPerformed

    }//GEN-LAST:event_jtxRutaDocumentoFirmarActionPerformed

    private void rbFirmarTokenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbFirmarTokenActionPerformed
        System.out.println("Firmar con Token");
        this.selFirmarConToken();
    }//GEN-LAST:event_rbFirmarTokenActionPerformed

    private void rbFfirmarLlaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbFfirmarLlaveActionPerformed
        System.out.println("Firmar con llave");
        this.selFirmarConArchivo();
    }//GEN-LAST:event_rbFfirmarLlaveActionPerformed

    private void btnResetValidarFormActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetValidarFormActionPerformed
        resetearInfoValidacionCertificado();
    }//GEN-LAST:event_btnResetValidarFormActionPerformed

    private void btnValidarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnValidarActionPerformed
        Validador validador = new Validador();
        KeyStoreProvider ksp;
        X509Certificate cert = null;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            jplValidar.setEnabled(false);
            if (this.rbValidarToken.isSelected()) {
                ks = KeyStoreProviderFactory.getKeyStore(jpfClave.getPassword().toString());
                if (ks == null) {
                    throw new TokenNoEncontradoException("No se encontro token!");
                }

            } else {
                ksp = new FileKeyStoreProvider(jtxRutaCertificado.getText());
                ks = ksp.getKeystore(certClaveTXT.getPassword());

            }
            cert = validador.getCert(ks,jpfClave.getPassword());
            String validez;
            try {
                validador.validar(cert);
                validez = "Válido";
            } catch (OcspValidationException | CRLValidationException ex) {
                validez = "Revocado";
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } 
            //cert = validador.validar(jpfClave.getPassword(), ks);
            setearInfoValidacionCertificado(cert);
            agregarValidezCertificado(validez);
            jplValidar.setEnabled(true);
            setCursor(Cursor.getDefaultCursor());
        }catch(KeyStoreException e){
            setCursor(Cursor.getDefaultCursor());
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
            JOptionPane.showMessageDialog(this, "Contraseña Incorrecta", "Error", JOptionPane.ERROR_MESSAGE);
            jplValidar.setEnabled(true);
        }catch (Exception ex) {
            setCursor(Cursor.getDefaultCursor());
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            jplValidar.setEnabled(true);
        } 
    }//GEN-LAST:event_btnValidarActionPerformed

    private void btnAbrirCertificadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAbrirCertificadoActionPerformed
        llaveVerificar = abrirArchivo();
        if (llaveVerificar != null) {
            jtxRutaCertificado.setText(llaveVerificar.getAbsolutePath());

        }
    }//GEN-LAST:event_btnAbrirCertificadoActionPerformed

    private void rbValidarTokenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbValidarTokenActionPerformed
        selValidarToken();
    }//GEN-LAST:event_rbValidarTokenActionPerformed

    private void rbValidarLlaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbValidarLlaveActionPerformed
        selValidarArchivo();
    }//GEN-LAST:event_rbValidarLlaveActionPerformed

    private void jmiAcercaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiAcercaActionPerformed
        JPanellAcercaDe jplAcercaDe = new JPanellAcercaDe();
        Object[] params = {jplAcercaDe};
        JOptionPane.showMessageDialog(this, params, "Acerca de FirmaEC", JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_jmiAcercaActionPerformed

    private void jmiActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiActualizarActionPerformed
        actualizar();
    }//GEN-LAST:event_jmiActualizarActionPerformed

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
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAbrirArchivoFirmar;
    private javax.swing.JButton btnAbrirArchivoPSKFirmar;
    private javax.swing.JButton btnAbrirCertificado;
    private javax.swing.JButton btnExaminarVerificar;
    private javax.swing.JButton btnFirmar;
    private javax.swing.JButton btnResetValidarForm;
    private javax.swing.JButton btnResetear;
    private javax.swing.JButton btnResetearVerificar;
    private javax.swing.JButton btnValidar;
    private javax.swing.JButton btnVerificar;
    private javax.swing.JPasswordField certClaveTXT;
    private javax.swing.JPanel firmarVerificarDocPanel;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel jblCertificadoEnFimador;
    private javax.swing.JLabel jblCertificadoFirmar;
    private javax.swing.JLabel jblClave;
    private javax.swing.JLabel jblDocumento;
    private javax.swing.JLabel jlbArchivoFirmadoVerficar;
    private javax.swing.JLabel jlbArchivoFirmador;
    private javax.swing.JLabel jlbArchivoVerificado;
    private javax.swing.JLabel jlbCertificadoValidar;
    private javax.swing.JLabel jlbCertificadoValidarCert;
    private javax.swing.JLabel jlbCertificadoVldCert;
    private javax.swing.JMenu jmAyuda;
    private javax.swing.JMenuBar jmbMenuPrincipal;
    private javax.swing.JMenuItem jmiAcerca;
    private javax.swing.JMenuItem jmiActualizar;
    private javax.swing.JPasswordField jpfClave;
    private javax.swing.JPanel jplFirmar;
    private javax.swing.JPanel jplValidar;
    private javax.swing.JPanel jplVerificarDocumento;
    private javax.swing.JTextField jtxArchivoFirmado;
    private javax.swing.JTextField jtxArchivoFirmadoVerificar;
    private javax.swing.JTextField jtxDocumentoVerificado;
    private javax.swing.JTextField jtxRutaCertificado;
    private javax.swing.JTextField jtxRutaDocumentoFirmar;
    private javax.swing.JTextField jtxRutaLlaveFirmar;
    private javax.swing.JTabbedPane mainPanel;
    private javax.swing.JRadioButton rbFfirmarLlave;
    private javax.swing.JRadioButton rbFirmarToken;
    private javax.swing.JRadioButton rbValidarLlave;
    private javax.swing.JRadioButton rbValidarToken;
    private javax.swing.JTable tblDatosCertificadosValidar;
    private javax.swing.JTable tblDatosDelCertificadoFirmador;
    private javax.swing.JTable tblDatosDelFirmanteFirmador;
    private javax.swing.JTable tblDatosFirmanteVerificar;
    private javax.swing.ButtonGroup tipoFirmaBtnGRP;
    private javax.swing.JPanel validarCertificadoPanel;
    private javax.swing.JPanel verificarDocumentoPanel;
    // End of variables declaration//GEN-END:variables
}
