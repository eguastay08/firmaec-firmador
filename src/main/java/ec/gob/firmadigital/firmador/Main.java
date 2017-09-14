/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.firmadigital.firmador;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.SignatureException;
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
import javax.swing.UIManager.*;

import ec.gob.firmadigital.cliente.FirmaDigital;
import ec.gob.firmadigital.cliente.Validador;
import ec.gob.firmadigital.exceptions.ConexionInvalidaOCSPException;
import ec.gob.firmadigital.exceptions.DocumentoNoExistenteException;
import ec.gob.firmadigital.exceptions.DocumentoNoPermitido;
import ec.gob.firmadigital.exceptions.TokenNoConectadoException;
import ec.gob.firmadigital.exceptions.TokenNoEncontrado;
import ec.gob.firmadigital.utils.Fichero;
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
import io.rubrica.sign.InvalidFormatException;
import java.time.LocalDateTime;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
;

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
    private final FileNameExtensionFilter filtros = new FileNameExtensionFilter("Documentos de Oficina", "pdf", "docx", "xlsx", "pptx", "odt", "ods", "odp","xml");
    private static final String OS = System.getProperty("os.name").toLowerCase();
    //private final ImageIcon checkIcon = new ImageIcon(ClassLoader.getSystemResource(RUTA_IMG + CHECK_IMG));
    //private final ImageIcon notCheckIcon = new ImageIcon(ClassLoader.getSystemResource(RUTA_IMG + NOTCHECK_IMG));
    private PDDocument pdfDocument;
    private PDFRenderer pdfRenderer;

    /**
     * Creates new form Main
     */
    public Main() {
        /*try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                System.out.println("LF disponibles "  +info.getName());
                if ("GTK+".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("No tiene gtk+");
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }*/

        
        
        extensionesPermitidas = new ArrayList<>();
        // Extensiones permitidas"pdf","docx","xlsx","pptx","odt","ods","odp"
        extensionesPermitidas.add("pdf");
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
        
        datosDelFirmanteFirmadorTbl.getColumnModel().getColumn(0).setCellRenderer(new WordWrapCellRenderer());
        datosDelFirmanteFirmadorTbl.getColumnModel().getColumn(1).setCellRenderer(new WordWrapCellRenderer());
        datosDelFirmanteFirmadorTbl.getColumnModel().getColumn(2).setCellRenderer(new WordWrapCellRenderer());
        datosDelFirmanteFirmadorTbl.getColumnModel().getColumn(3).setCellRenderer(new WordWrapCellRenderer());
        datosDelFirmanteFirmadorTbl.getColumnModel().getColumn(4).setCellRenderer(new WordWrapCellRenderer());
        
        //nemoics
        certificadoEnFimadorLbl.setLabelFor(certificadoEnFimadorLbl);
        abrirArchivoFirmarBtn.setMnemonic(java.awt.event.KeyEvent.VK_E);
        btnAbrirArchivoPSKFirmar.setMnemonic(java.awt.event.KeyEvent.VK_X);
        btnFirmar.setMnemonic(java.awt.event.KeyEvent.VK_F);
        btnResetear.setMnemonic(java.awt.event.KeyEvent.VK_R);
        
        examinarVerificarBtn.setMnemonic(java.awt.event.KeyEvent.VK_E);
        
        
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
        this.verificarBTN.setEnabled(false);
        this.claveTXT.setText("");
        this.rutaDocumentoFirmarTxt.setText("");
        this.claveTXT.setEnabled(false);
        this.documento = null;
        this.llave = null;
        this.rutaLlaveFirmarTxt.setEnabled(false);
        this.rutaLlaveFirmarTxt.setText("");
        //this.certificadosJTR.getModel().
        resetDatosTabladeFirmante();
        resetDatosTablaCertificadoFirmador();

    }

    private void selFirmarConArchivo() {
        this.btnFirmar.setEnabled(true);
        this.rutaLlaveFirmarTxt.setEnabled(true);
        this.btnAbrirArchivoPSKFirmar.setEnabled(true);
        // Si es windows no hay que habilitar el campo de contraseña
        
        this.claveTXT.setEnabled(true);
        
    }
    
    private void selValidarArchivo() {
        rutaCertificadoTxt.setEnabled(true);
        abrirCertificadoBtn.setEnabled(true);
        certClaveTXT.setEnabled(true);
    }

    private void selFirmarConToken() {
        this.btnFirmar.setEnabled(true);
        this.rutaLlaveFirmarTxt.setEnabled(false);
        this.rutaLlaveFirmarTxt.setText("");
        this.claveTXT.setEnabled(false);
        this.claveTXT.setText("");
        this.btnAbrirArchivoPSKFirmar.setEnabled(false);
        
        if (!esWindows()) {
            this.claveTXT.setEnabled(true);
        } else {
            this.claveTXT.setEnabled(false);
        }
    }
    
    private void selValidarToken() {
        rutaCertificadoTxt.setEnabled(false);
        rutaCertificadoTxt.setText("");
        abrirCertificadoBtn.setEnabled(false);
        
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
    private void validacionPreFirmar() throws DocumentoNoExistenteException, TokenNoConectadoException, DocumentoNoPermitido {
        //Revisamos si existe el documento a firmar
        // TODO no hacer un return directamente, se podria validar todos los parametros e ir aumentando los errores
        if (documento ==null )
            throw new DocumentoNoExistenteException("Documento "+this.rutaDocumentoFirmarTxt.getText() + " no existe");

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
    private void verificarDocumento() throws DocumentoNoPermitido, IOException, KeyStoreException, OcspValidationException, SignatureException, InvalidFormatException, RubricaException, ConexionInvalidaOCSPException {
        // Vemos si existe
        System.out.println("Verificando Docs");
        /*if (documento == null || !documento.exists()) {
            return false;
        }*/
        // Vemos si es un documento permitido primero
        tipoDeDocumentPermitido(documento);
        
        FirmaDigital firmaDigital = new FirmaDigital();
        
        List<Certificado> certs = firmaDigital.verificar(documento);
        
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        
        DefaultTableModel tableModel = (DefaultTableModel) datosArchivoVerificarTbl.getModel();
        
        tableModel.setRowCount(0);
       
        //Actualizamos los datos del archivo
        String[] data = new String[1];
        data[0] = "Documento Verificado: " + documento.getAbsolutePath();
        tableModel.addRow(data);

        datosArchivoVerificarTbl.setModel(tableModel);
        tableModel.fireTableDataChanged();

        DefaultTableModel tableModelCertificados = (DefaultTableModel) datosFirmanteVerificarTbl.getModel();
        tableModelCertificados.setRowCount(0);
        for (Certificado cert: certs) {
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
            if(cert.getRevocated()){
                revocadoStr = "Revocado";
            }
            dataCert[7] = revocadoStr;
            tableModelCertificados.addRow(dataCert);
        }
        
        datosFirmanteVerificarTbl.setModel(tableModelCertificados);
        tableModelCertificados.fireTableDataChanged();

    }
    

    
    // Se podria verificar el mimetype
    // Talvez eliminar el if
    private void tipoDeDocumentPermitido(File documento) throws DocumentoNoPermitido {
        String extDocumento = FirmadorFileUtils.getFileExtension(documento);
        if(!extensionesPermitidas.stream().anyMatch((extension) -> (extension.equals(extDocumento))))
            throw new DocumentoNoPermitido("Extensión ." + extDocumento + " no permitida");
    }

    //TODO botar exceptions en vez de return false
    private boolean firmarDocumento() throws Exception  {
        // Vemos si es un documento permitido primero
        validacionPreFirmar();

        Boolean validacion = validarFirma();

        FirmaDigital firmaDigital = new FirmaDigital();
        
        byte[] docSigned = firmaDigital.firmar(ks, documento, claveTXT.getPassword());
        String nombreDocFirmado = crearNombreFirmado(documento);

        // Obtenemos el certificado firmante para obtener los datos de usuarios
        List<Alias> aliases = KeyStoreUtilities.getSigningAliases(ks);
        Alias alias = aliases.get(0);
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias.getAlias());
        
        String nombre = FirmaDigital.getNombreCA(cert);
        
        System.out.println("Nombre: " +nombre);
        
        DatosUsuario datosUsuario = FirmaDigital.getDatosUsuarios(cert);        
        
        agregarDatosTabladeFirmante(datosUsuario);
        
        agregarDatosTablaCertificadoFirmador(cert, datosUsuario);
        
        //Si todo va bien creamos el arschivo
        FirmadorFileUtils.saveByteArrayToDisc(docSigned, nombreDocFirmado);
        archivoFirmadoTxt.setText(nombreDocFirmado);

        return true;
    }
    
    
    private void agregarDatosTabladeFirmante(DatosUsuario datosUsuario) {
        DefaultTableModel tableModel = (DefaultTableModel) datosDelFirmanteFirmadorTbl.getModel();
        
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
        
        datosDelFirmanteFirmadorTbl.setModel(tableModel);
        tableModel.fireTableDataChanged();
        
    }
    
    private void agregarDatosTablaCertificadoFirmador(X509Certificate cert, DatosUsuario datosUsuario){
        DefaultTableModel tableModel = (DefaultTableModel) datosDelCertificadoFirmadorTbl.getModel(); 

        tableModel.setRowCount(0);

        //Actualizamos los datos del archivo
        String[] data = new String[5];
        data[0] = "Certificado Emitido por: "+FirmaDigital.getNombreCA(cert);
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

        
        datosDelCertificadoFirmadorTbl.setModel(tableModel);
        tableModel.fireTableDataChanged();
        
    }
   
    
    private void resetDatosTabladeFirmante() {
        DefaultTableModel tableModel = (DefaultTableModel) datosDelFirmanteFirmadorTbl.getModel();
        
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
        
        datosDelFirmanteFirmadorTbl.setModel(tableModel);
        tableModel.fireTableDataChanged();    
    }
    
    private void resetDatosTablaCertificadoFirmador(){
        DefaultTableModel tableModel = (DefaultTableModel) datosDelCertificadoFirmadorTbl.getModel(); 

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
        
        datosDelCertificadoFirmadorTbl.setModel(tableModel);
        tableModel.fireTableDataChanged();
    }

    // TODO botar esto a una clase talvez FirmaDigital y botar exceptions
     private boolean validarFirma() throws TokenNoEncontrado, KeyStoreException, IOException, RubricaException    {
        System.out.println("Validar Firma");
        if (this.rbFirmarToken.isSelected()) {
            ks = KeyStoreProviderFactory.getKeyStore(claveTXT.getPassword().toString());
            if (ks == null) {
                //JOptionPane.showMessageDialog(frame, "No se encontro un token!");
                throw new TokenNoEncontrado("No se encontro token!");
            }

        } else {
            KeyStoreProvider ksp = new FileKeyStoreProvider(rutaLlaveFirmarTxt.getText());
            ks = ksp.getKeystore(claveTXT.getPassword());
            
         }

         Validador validador = new Validador();
         validador.validar(claveTXT.getPassword(), ks);
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
            String emisor = FirmaDigital.getNombreCA(cert);
            
            DatosUsuario datosUsuario = FirmaDigital.getDatosUsuarios(cert);

            DefaultTableModel tableModel = (DefaultTableModel) datosCertificadosValidarTbl.getModel();

            tableModel.setRowCount(0);

            //Actualizamos los datos del archivo
            String[] data = new String[1];
            data[0] = "Certificado Emitido por: " + FirmaDigital.getNombreCA(cert);
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

            datosCertificadosValidarTbl.setModel(tableModel);
            tableModel.fireTableDataChanged();
        }
        //TOdo botar error si es null
    }
    
    private void agregarValidezCertificado(String validez){
        DefaultTableModel tableModel = (DefaultTableModel) datosCertificadosValidarTbl.getModel();
            //Actualizamos los datos del archivo
            String[] data = new String[1];
            data[0] = "Estado certificado: "+validez;
            tableModel.addRow(data);
            datosCertificadosValidarTbl.setModel(tableModel);
            tableModel.fireTableDataChanged();
    }
    
    
    
    private void resetInfoValidacionCertificado(){
        DefaultTableModel tableModel = (DefaultTableModel) datosCertificadosValidarTbl.getModel();

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

        datosCertificadosValidarTbl.setModel(tableModel);
        tableModel.fireTableDataChanged();
    }
    
    private void resetearInfoValidacionCertificado() {
        rutaCertificadoTxt.setText("");
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
        jPanel1 = new javax.swing.JPanel();
        certificadoEnFimadorLbl = new javax.swing.JLabel();
        rbFfirmarLlave = new javax.swing.JRadioButton();
        rbFirmarToken = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        rutaDocumentoFirmarTxt = new javax.swing.JTextField();
        abrirArchivoFirmarBtn = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        rutaLlaveFirmarTxt = new javax.swing.JTextField();
        btnAbrirArchivoPSKFirmar = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        claveTXT = new javax.swing.JPasswordField();
        btnFirmar = new javax.swing.JButton();
        btnResetear = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        archivoFirmadoTxt = new javax.swing.JTextField();
        jScrollPane6 = new javax.swing.JScrollPane();
        datosDelFirmanteFirmadorTbl = new javax.swing.JTable();
        jScrollPane7 = new javax.swing.JScrollPane();
        datosDelCertificadoFirmadorTbl = new javax.swing.JTable();
        verificarDocumentoPanel = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel3 = new javax.swing.JPanel();
        archivoFirmadoVerficarLbl = new javax.swing.JLabel();
        archivoFirmadoVerificarTxt = new javax.swing.JTextField();
        examinarVerificarBtn = new javax.swing.JButton();
        verificarBTN = new javax.swing.JButton();
        resetearBTN1 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        datosArchivoVerificarTbl = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        datosFirmanteVerificarTbl = new javax.swing.JTable();
        jLabel13 = new javax.swing.JLabel();
        validarCertificadoPanel = new javax.swing.JPanel();
        jSeparator3 = new javax.swing.JSeparator();
        jPanel5 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        validarLlaveRBTN = new javax.swing.JRadioButton();
        validarTokenRBTN = new javax.swing.JRadioButton();
        certificadoVldCertLbl = new javax.swing.JLabel();
        rutaCertificadoTxt = new javax.swing.JTextField();
        abrirCertificadoBtn = new javax.swing.JButton();
        certificadoVldCertLbl1 = new javax.swing.JLabel();
        certClaveTXT = new javax.swing.JPasswordField();
        validarBtn = new javax.swing.JButton();
        resetValidarFormBtn = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        datosCertificadosValidarTbl = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("FirmaEC");

        firmarVerificarDocPanel.setName(""); // NOI18N

        certificadoEnFimadorLbl.setText("Certificado en:");

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

        jLabel1.setText("Documento");

        rutaDocumentoFirmarTxt.setEditable(false);
        rutaDocumentoFirmarTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rutaDocumentoFirmarTxtActionPerformed(evt);
            }
        });

        abrirArchivoFirmarBtn.setText("Examinar");
        abrirArchivoFirmarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abrirArchivoFirmarBtnActionPerformed(evt);
            }
        });

        jLabel2.setText("Certificado");

        rutaLlaveFirmarTxt.setEditable(false);
        rutaLlaveFirmarTxt.setEnabled(false);
        rutaLlaveFirmarTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rutaLlaveFirmarTxtActionPerformed(evt);
            }
        });

        btnAbrirArchivoPSKFirmar.setText("Examinar");
        btnAbrirArchivoPSKFirmar.setEnabled(false);
        btnAbrirArchivoPSKFirmar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAbrirArchivoPSKFirmarActionPerformed(evt);
            }
        });

        jLabel3.setText("Contraseña");

        claveTXT.setEnabled(false);

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(certificadoEnFimadorLbl)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnFirmar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnResetear))
                    .addComponent(claveTXT)
                    .addComponent(rutaLlaveFirmarTxt)
                    .addComponent(rutaDocumentoFirmarTxt))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAbrirArchivoPSKFirmar, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(abrirArchivoFirmarBtn, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(148, 148, 148)
                .addComponent(rbFfirmarLlave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rbFirmarToken)
                .addGap(660, 660, 660))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbFfirmarLlave)
                    .addComponent(rbFirmarToken)
                    .addComponent(certificadoEnFimadorLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rutaDocumentoFirmarTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(abrirArchivoFirmarBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(rutaLlaveFirmarTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAbrirArchivoPSKFirmar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(claveTXT, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnFirmar)
                    .addComponent(btnResetear))
                .addGap(19, 19, 19))
        );

        jLabel11.setText("<html><b>DATOS DEL FIRMANTE</b></html>");

        jLabel15.setText("Archivo Firmado");

        archivoFirmadoTxt.setEditable(false);

        datosDelFirmanteFirmadorTbl.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane6.setViewportView(datosDelFirmanteFirmadorTbl);

        datosDelCertificadoFirmadorTbl.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane7.setViewportView(datosDelCertificadoFirmadorTbl);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addGap(24, 24, 24)
                        .addComponent(archivoFirmadoTxt))
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
                    .addComponent(archivoFirmadoTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout firmarVerificarDocPanelLayout = new javax.swing.GroupLayout(firmarVerificarDocPanel);
        firmarVerificarDocPanel.setLayout(firmarVerificarDocPanelLayout);
        firmarVerificarDocPanelLayout.setHorizontalGroup(
            firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 944, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(jSeparator2)
        );
        firmarVerificarDocPanelLayout.setVerticalGroup(
            firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        mainPanel.addTab("<html><b>FIRMAR ELECTRÓNICA DE DOCUMENTO</b></html>", firmarVerificarDocPanel);

        archivoFirmadoVerficarLbl.setText("Archivo Firmado:");

        archivoFirmadoVerificarTxt.setEditable(false);

        examinarVerificarBtn.setText("Examinar");
        examinarVerificarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                examinarVerificarBtnActionPerformed(evt);
            }
        });

        verificarBTN.setText("Verificar Archivo");
        verificarBTN.setEnabled(false);
        verificarBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verificarBTNActionPerformed(evt);
            }
        });

        resetearBTN1.setText("Restablecer");
        resetearBTN1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetearBTN1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(archivoFirmadoVerficarLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(verificarBTN)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(resetearBTN1))
                    .addComponent(archivoFirmadoVerificarTxt))
                .addGap(18, 18, 18)
                .addComponent(examinarVerificarBtn)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(archivoFirmadoVerificarTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(examinarVerificarBtn)
                    .addComponent(archivoFirmadoVerficarLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resetearBTN1)
                    .addComponent(verificarBTN))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel12.setText("<html><b>RESULTADOS DE LA VERIFICACIÓN DEL ARCHIVO FIRMADO ELECTRÓNICAMENTE</b></html>");

        jLabel14.setText("Datos del Archivo");

        datosArchivoVerificarTbl.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null}
            },
            new String [] {
                "Archivo"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(datosArchivoVerificarTbl);
        if (datosArchivoVerificarTbl.getColumnModel().getColumnCount() > 0) {
            datosArchivoVerificarTbl.getColumnModel().getColumn(0).setResizable(false);
        }

        datosFirmanteVerificarTbl.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Cédula", "Nombres", "Institucion ", "Cargo", "Válido Desde", "Válido Hasta", "Fecha Firmado", "Revocado"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(datosFirmanteVerificarTbl);

        jLabel13.setText("Datos De Los Firmantes");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14)
                    .addComponent(jLabel13))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.CENTER, jPanel4Layout.createSequentialGroup()
                .addGap(203, 203, 203)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(204, 204, 204))
            .addGroup(javax.swing.GroupLayout.Alignment.CENTER, jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.CENTER, jPanel4Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jScrollPane3))
                    .addGroup(javax.swing.GroupLayout.Alignment.CENTER, jPanel4Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jScrollPane4)))
                .addGap(12, 12, 12))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addGap(8, 8, 8)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
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
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        verificarDocumentoPanelLayout.setVerticalGroup(
            verificarDocumentoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(verificarDocumentoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        mainPanel.addTab("<html><b>VERIFICAR DOCUMENTO</b></html>", verificarDocumentoPanel);

        validarCertificadoPanel.setName(""); // NOI18N

        jLabel4.setText("Certificados en:");

        tipoFirmaBtnGRP.add(validarLlaveRBTN);
        validarLlaveRBTN.setText("Archivo");
        validarLlaveRBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validarLlaveRBTNActionPerformed(evt);
            }
        });

        tipoFirmaBtnGRP.add(validarTokenRBTN);
        validarTokenRBTN.setText("Firmar con Token");
        validarTokenRBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validarTokenRBTNActionPerformed(evt);
            }
        });

        certificadoVldCertLbl.setText("Certificado");

        rutaCertificadoTxt.setEditable(false);
        rutaCertificadoTxt.setEnabled(false);

        abrirCertificadoBtn.setText("Examinar");
        abrirCertificadoBtn.setEnabled(false);
        abrirCertificadoBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abrirCertificadoBtnActionPerformed(evt);
            }
        });

        certificadoVldCertLbl1.setText("Contraseña");

        certClaveTXT.setEnabled(false);

        validarBtn.setText("Validar");
        validarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validarBtnActionPerformed(evt);
            }
        });

        resetValidarFormBtn.setText("Restablecer");
        resetValidarFormBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetValidarFormBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(certificadoVldCertLbl1)
                    .addComponent(jLabel4)
                    .addComponent(certificadoVldCertLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(validarLlaveRBTN)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(validarTokenRBTN)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(validarBtn)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(resetValidarFormBtn))
                            .addComponent(rutaCertificadoTxt, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(certClaveTXT, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(18, 18, 18)
                        .addComponent(abrirCertificadoBtn)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(validarLlaveRBTN)
                    .addComponent(validarTokenRBTN)
                    .addComponent(jLabel4))
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(certificadoVldCertLbl)
                    .addComponent(rutaCertificadoTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(abrirCertificadoBtn)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(certificadoVldCertLbl1))
                    .addComponent(certClaveTXT, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(validarBtn)
                    .addComponent(resetValidarFormBtn))
                .addContainerGap())
        );

        jLabel6.setText("<html><b>RESULTADOS DE VERIFICACIÓN DE CERTIFICADO ELECTRÓNICO</b></html>");

        datosCertificadosValidarTbl.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane5.setViewportView(datosCertificadosValidarTbl);

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
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
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
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        validarCertificadoPanelLayout.setVerticalGroup(
            validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        mainPanel.addTab("<html><b>VALIDAR CERTIFICADO DE FIRMA ELECTRÓNICA</b></html>", validarCertificadoPanel);

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

    private void verificarBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verificarBTNActionPerformed
        // Si lo del documento es distinto a lo de la ruta seteamos la ruta
        // del documento a la del textfield
        if (!documento.getAbsolutePath().equals(archivoFirmadoVerificarTxt.getText())) 
            documento = new File(archivoFirmadoVerificarTxt.getText());
        
        try {
            verificarDocumento();
        }catch(RubricaException ex){
            System.err.println("Error no se pudo conectar al servicio de OSCP para verificar el certificado ");
            JOptionPane.showMessageDialog(this, "Error no se pudo conectar al servicio de OSCP para verificar el certificado\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }catch (Exception ex) {
            //TODO agregar mensaje de error
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error no se pudo verificar ");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }//GEN-LAST:event_verificarBTNActionPerformed

    private void examinarVerificarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_examinarVerificarBtnActionPerformed
       documento = abrirArchivo(filtros);
        if (documento != null) {
            archivoFirmadoVerificarTxt.setText(documento.getAbsolutePath());
            verificarBTN.setEnabled(true);
        }
    }//GEN-LAST:event_examinarVerificarBtnActionPerformed

    private void resetearBTN1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetearBTN1ActionPerformed
        System.out.println("resetear campos");
        archivoFirmadoVerificarTxt.setText("");
        documento=null;
        DefaultTableModel tableModel = (DefaultTableModel) datosArchivoVerificarTbl.getModel();
        
        tableModel.setRowCount(0);
       
        //Actualizamos los datos del archivo
        String[] data = new String[1];
        data[0] = "";
        tableModel.addRow(data);
        datosArchivoVerificarTbl.setModel(tableModel);
        tableModel.fireTableDataChanged();
        //datosFirmanteVerificarTbl
        
        DefaultTableModel tableModelCert = (DefaultTableModel) datosFirmanteVerificarTbl.getModel();
        
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
        datosArchivoVerificarTbl.setModel(tableModelCert);
        tableModelCert.fireTableDataChanged();
        
    }//GEN-LAST:event_resetearBTN1ActionPerformed

    private void btnResetearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetearActionPerformed
        this.resetForm();
    }//GEN-LAST:event_btnResetearActionPerformed

    private void btnFirmarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFirmarActionPerformed
        if (documento == null || !documento.getAbsolutePath().equals(rutaDocumentoFirmarTxt.getText()))
        documento = new File(rutaDocumentoFirmarTxt.getText());

        try {
            this.firmarDocumento();
            // JOptionPane.showMessageDialog(this, "Documento firmado "+ this.documentoFirmadoTXT.getText(), "Firmador", JOptionPane.INFORMATION_MESSAGE, checkIcon);
            System.out.println("Documento firmado");
            
            
        } catch (Exception ex) {
            //TODO agregar mensaje de error
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error no se pudo firmar ");
        }
    }//GEN-LAST:event_btnFirmarActionPerformed

    private void btnAbrirArchivoPSKFirmarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAbrirArchivoPSKFirmarActionPerformed
        llave = abrirArchivo();
        if (llave != null) {
            rutaLlaveFirmarTxt.setText(llave.getAbsolutePath());
        }
    }//GEN-LAST:event_btnAbrirArchivoPSKFirmarActionPerformed

    private void rutaLlaveFirmarTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rutaLlaveFirmarTxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rutaLlaveFirmarTxtActionPerformed

    private void abrirArchivoFirmarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abrirArchivoFirmarBtnActionPerformed

        // Por defecto desbloqueamos el objecto de verificar
        documento = abrirArchivo(filtros);
        if (documento != null) {
            rutaDocumentoFirmarTxt.setText(documento.getAbsolutePath());

        }
        //       try{
            /*rutaDocumentoFirmarTxt.setText(Fichero.ruta());
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }//GEN-LAST:event_abrirArchivoFirmarBtnActionPerformed

    private void rutaDocumentoFirmarTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rutaDocumentoFirmarTxtActionPerformed

    }//GEN-LAST:event_rutaDocumentoFirmarTxtActionPerformed

    private void rbFirmarTokenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbFirmarTokenActionPerformed
        System.out.println("Firmar con Token");
        this.selFirmarConToken();
    }//GEN-LAST:event_rbFirmarTokenActionPerformed

    private void rbFfirmarLlaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbFfirmarLlaveActionPerformed
        System.out.println("Firmar con llave");
        this.selFirmarConArchivo();
    }//GEN-LAST:event_rbFfirmarLlaveActionPerformed

    private void resetValidarFormBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetValidarFormBtnActionPerformed
        resetearInfoValidacionCertificado();
    }//GEN-LAST:event_resetValidarFormBtnActionPerformed

    private void validarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validarBtnActionPerformed
        Validador validador = new Validador();
        KeyStoreProvider ksp;
        try {
            if (this.validarTokenRBTN.isSelected()) {
                ks = KeyStoreProviderFactory.getKeyStore(claveTXT.getPassword().toString());
                if (ks == null) {
                    throw new TokenNoEncontrado("No se encontro token!");
                }

            } else {
                ksp = new FileKeyStoreProvider(rutaCertificadoTxt.getText());
                ks = ksp.getKeystore(certClaveTXT.getPassword());

            }
            X509Certificate cert = validador.validar(claveTXT.getPassword(), ks);
            setearInfoValidacionCertificado(cert);
            agregarValidezCertificado("Válido");
        } catch (KeyStoreException | TokenNoEncontrado | IOException | RubricaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_validarBtnActionPerformed

    private void abrirCertificadoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abrirCertificadoBtnActionPerformed
        llaveVerificar = abrirArchivo();
        if (llaveVerificar != null) {
            rutaCertificadoTxt.setText(llaveVerificar.getAbsolutePath());

        }
    }//GEN-LAST:event_abrirCertificadoBtnActionPerformed

    private void validarTokenRBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validarTokenRBTNActionPerformed
        selValidarToken();
    }//GEN-LAST:event_validarTokenRBTNActionPerformed

    private void validarLlaveRBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validarLlaveRBTNActionPerformed
        selValidarArchivo();
    }//GEN-LAST:event_validarLlaveRBTNActionPerformed

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
    private javax.swing.JButton abrirArchivoFirmarBtn;
    private javax.swing.JButton abrirCertificadoBtn;
    private javax.swing.JTextField archivoFirmadoTxt;
    private javax.swing.JLabel archivoFirmadoVerficarLbl;
    private javax.swing.JTextField archivoFirmadoVerificarTxt;
    private javax.swing.JButton btnAbrirArchivoPSKFirmar;
    private javax.swing.JButton btnFirmar;
    private javax.swing.JButton btnResetear;
    private javax.swing.JPasswordField certClaveTXT;
    private javax.swing.JLabel certificadoEnFimadorLbl;
    private javax.swing.JLabel certificadoVldCertLbl;
    private javax.swing.JLabel certificadoVldCertLbl1;
    private javax.swing.JPasswordField claveTXT;
    private javax.swing.JTable datosArchivoVerificarTbl;
    private javax.swing.JTable datosCertificadosValidarTbl;
    private javax.swing.JTable datosDelCertificadoFirmadorTbl;
    private javax.swing.JTable datosDelFirmanteFirmadorTbl;
    private javax.swing.JTable datosFirmanteVerificarTbl;
    private javax.swing.JButton examinarVerificarBtn;
    private javax.swing.JPanel firmarVerificarDocPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTabbedPane mainPanel;
    private javax.swing.JRadioButton rbFfirmarLlave;
    private javax.swing.JRadioButton rbFirmarToken;
    private javax.swing.JButton resetValidarFormBtn;
    private javax.swing.JButton resetearBTN1;
    private javax.swing.JTextField rutaCertificadoTxt;
    private javax.swing.JTextField rutaDocumentoFirmarTxt;
    private javax.swing.JTextField rutaLlaveFirmarTxt;
    private javax.swing.ButtonGroup tipoFirmaBtnGRP;
    private javax.swing.JButton validarBtn;
    private javax.swing.JPanel validarCertificadoPanel;
    private javax.swing.JRadioButton validarLlaveRBTN;
    private javax.swing.JRadioButton validarTokenRBTN;
    private javax.swing.JButton verificarBTN;
    private javax.swing.JPanel verificarDocumentoPanel;
    // End of variables declaration//GEN-END:variables
}
