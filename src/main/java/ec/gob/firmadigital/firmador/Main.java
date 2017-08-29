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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import ec.gob.firmadigital.cliente.FirmaDigital;
import ec.gob.firmadigital.crl.ServicioCRL;
import ec.gob.firmadigital.exceptions.DocumentoNoExistenteException;
import ec.gob.firmadigital.exceptions.DocumentoNoPermitido;
import ec.gob.firmadigital.exceptions.TokenNoConectadoException;
import ec.gob.firmadigital.exceptions.TokenNoEncontrado;
import ec.gob.firmadigital.lectorpdf.CustomPageDrawer;
import ec.gob.firmadigital.utils.FirmadorFileUtils;
import io.rubrica.certificate.CrlUtils;
import io.rubrica.certificate.ValidationResult;
import io.rubrica.certificate.ec.bce.BceSubTestCert;
import io.rubrica.certificate.ec.cj.ConsejoJudicaturaSubCert;
import io.rubrica.certificate.ec.securitydata.SecurityDataSubCaCert;
import io.rubrica.core.RubricaException;
import io.rubrica.core.Util;
import io.rubrica.keystore.FileKeyStoreProvider;
import io.rubrica.keystore.KeyStoreProvider;
import io.rubrica.keystore.KeyStoreProviderFactory;
import io.rubrica.ocsp.OcspValidationException;
import io.rubrica.ocsp.ValidadorOCSP;
import io.rubrica.sign.InvalidFormatException;
import io.rubrica.util.CertificateUtils;
import java.security.cert.Certificate;
import java.time.Clock;
import java.time.LocalDateTime;
import javax.swing.JTable;
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
    private final ImageIcon checkIcon = new ImageIcon(ClassLoader.getSystemResource(RUTA_IMG + CHECK_IMG));
    private final ImageIcon notCheckIcon = new ImageIcon(ClassLoader.getSystemResource(RUTA_IMG + NOTCHECK_IMG));
    private PDDocument pdfDocument;
    private PDFRenderer pdfRenderer;

    /**
     * Creates new form Main
     */
    public Main() {
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
        this.firmarBTN.setEnabled(false);
        this.verificarBTN.setEnabled(false);
        this.claveTXT.setText("");
        this.rutaDocumentoFirmarTxt.setText("");
        this.claveTXT.setEnabled(false);
        this.documento = null;
        this.llave = null;
        this.rutaLlaveFirmarTxt.setEnabled(false);
        this.rutaLlaveFirmarTxt.setText("");
        //this.certificadosJTR.getModel().
        resetDatosTablaDeArchivoFirmador();
        resetDatosTabladeFirmante();
        resetDatosTablaCertificadoFirmador();

    }

    private void selFirmarConArchivo() {
        this.firmarBTN.setEnabled(true);
        this.rutaLlaveFirmarTxt.setEnabled(true);
        this.abrirArchivoPSKFirmarBtn.setEnabled(true);
        // Si es windows no hay que habilitar el campo de contraseña
        if (!esWindows()) {
            this.claveTXT.setEnabled(true);
        } else {
            this.claveTXT.setEnabled(false);
        }
    }

    private void selFirmarConToken() {
        this.firmarBTN.setEnabled(true);
        this.rutaLlaveFirmarTxt.setEnabled(false);
        this.claveTXT.setEnabled(false);
        this.abrirArchivoPSKFirmarBtn.setEnabled(false);
    }

    private boolean esWindows() {
        return (OS.contains("win"));// .indexOf("win") >= 0);
    }

    /*
    Valida que esten los campos necesarios para firmar
     */
    private void validacionPreFirmar() throws Exception{
        //Revisamos si existe el documento a firmar
        // TODO no hacer un return directamente, se podria validar todos los parametros e ir aumentando los errores
        if (documento ==null )
            throw new DocumentoNoExistenteException("Documento "+this.rutaDocumentoFirmarTxt.getText() + " no existe");

        if(!documento.exists()) 
            throw new DocumentoNoExistenteException("Documento "+documento.getAbsolutePath() + " no existe");
        
        if(llave == null){
            throw new DocumentoNoExistenteException("No hay llave seleccionada");
        }    
        
        if (firmarLlaveRBTN.isSelected() && !llave.exists()) {
            throw new DocumentoNoExistenteException("La llave "+llave.getAbsolutePath() + " no existe");
        }
        // Si firma con token debe
        if (firmarTokenRBTN.isSelected() && !hayToken()) {
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
    private void verificarDocumento() throws DocumentoNoPermitido, IOException, KeyStoreException, OcspValidationException, SignatureException, InvalidFormatException, RubricaException {
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
            String[] dataCert = new String[7];
            dataCert[0] = cert.getIssuedTo();
            dataCert[1] = cert.getIssuedBy();
            dataCert[2] = format1.format(cert.getValidFrom().getTime());
            dataCert[3] = format1.format(cert.getValidTo().getTime());
            dataCert[4] = format1.format(cert.getGenerated().getTime());
           // dataCert[5] = 
            dataCert[6] = cert.getIssuedTo();
            tableModelCertificados.addRow(dataCert);
        }
        
        datosFirmanteVerificarTbl.setModel(tableModelCertificados);
        tableModelCertificados.fireTableDataChanged();

           /* curCert.add(new DefaultMutableTreeNode("Emitido a: " + cert.getIssuedTo()));
            curCert.add(new DefaultMutableTreeNode("Emitido por: " + cert.getIssuedBy()));
            curCert.add(new DefaultMutableTreeNode("Válido desde: " + format1.format(cert.getValidFrom().getTime())));
            curCert.add(new DefaultMutableTreeNode("Válido hasta: " + format1.format(cert.getValidTo().getTime())));
            curCert.add(new DefaultMutableTreeNode("Fecha utilizado: " + format1.format(cert.getGenerated().getTime())));
            curCert.add(new DefaultMutableTreeNode("Validado: " + cert.getValidated()));
            curCert.add(new DefaultMutableTreeNode("Revocado: " + cert.getRevocated()));*/
    
        

       
        
        //return false;
    }
    

    
    // Se podria verificar el mimetype
    // Talvez eliminar el if
    private void tipoDeDocumentPermitido(File documento) throws DocumentoNoPermitido {
        String extDocumento = FirmadorFileUtils.getFileExtension(documento);
        if(!extensionesPermitidas.stream().anyMatch((extension) -> (extension.equals(extDocumento))))
            throw new DocumentoNoPermitido("Extensión ." + extDocumento + " no permitida");
    }

    //TODO botar exceptions en vez de return false
    private boolean firmarDocumento() throws Exception {
        // Vemos si es un documento permitido primero
        validacionPreFirmar();

        validarFirma();

        FirmaDigital firmaDigital = new FirmaDigital();
        
        byte[] docSigned = firmaDigital.firmar(ks, documento, claveTXT.getPassword());
        String nombreDocFirmado = crearNombreFirmado(documento);
        
        FirmadorFileUtils.saveByteArrayToDisc(docSigned, nombreDocFirmado);
        
        agregarDatosTablaDeArchivoFirmador(documento.getAbsolutePath(),nombreDocFirmado);
        
        //Obtenemos el certificado firmante para obtener los datos de usuarios
        String alias = ks.aliases().nextElement();
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
        
        String nombre = FirmaDigital.getNombreCA(cert);
        
        System.out.println("Nombre: " +nombre);
        
        DatosUsuario datosUsuario = FirmaDigital.getDatosUsuarios(cert);
        
        //datosDelFirmanteFirmadorTbl
        agregarDatosTabladeFirmante(datosUsuario);
        
        agregarDatosTablaCertificadoFirmador(cert, datosUsuario);

        return true;
    }
    
    private void agregarDatosTablaDeArchivoFirmador(String documentoOriginalStr, String documentoFirmadoStr) {
        DefaultTableModel tableModel = (DefaultTableModel) datosArchivoFirmarTbl.getModel();

        tableModel.setRowCount(0);

        //Actualizamos los datos del archivo
        String[] data = new String[1];
        data[0] = "Documento Original: " + documentoOriginalStr;
        tableModel.addRow(data);
        data[0] = "Documento Firmado:" + documentoFirmadoStr;
        tableModel.addRow(data);
        
        datosArchivoFirmarTbl.setModel(tableModel);
        tableModel.fireTableDataChanged();
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
    
    private void resetDatosTablaDeArchivoFirmador() {
        DefaultTableModel tableModel = (DefaultTableModel) datosArchivoFirmarTbl.getModel();

        tableModel.setRowCount(0);
        String[] data = new String[1];
        data[0] = "";
        tableModel.addRow(data);
        data[0] = "";
        tableModel.addRow(data);
        
        datosArchivoFirmarTbl.setModel(tableModel);
        tableModel.fireTableDataChanged();
    }
    
    private void resetDatosTabladeFirmante() {
        DefaultTableModel tableModel = (DefaultTableModel) datosDelFirmanteFirmadorTbl.getModel();
        
        DatosUsuario datosUsuario = new DatosUsuario();
        

        tableModel.setRowCount(0);

        //Actualizamos los datos del archivo
        String[] data = new String[5];
        data[0] = datosUsuario.getCedula();
        String nombres = datosUsuario.getNombre()+ " " +datosUsuario.getApellido();
        data[1] = nombres;
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
     private void validarFirma() throws Exception  {
        System.out.println("Validar Firma");
        if (this.firmarTokenRBTN.isSelected()) {
            ks = KeyStoreProviderFactory.getKeyStore(claveTXT.getPassword().toString());
            if (ks == null) {
                //JOptionPane.showMessageDialog(frame, "No se encontro un token!");
                throw new TokenNoEncontrado("No se encontro token!");
            }

        } else {
            KeyStoreProvider ksp = new FileKeyStoreProvider(rutaLlaveFirmarTxt.getText());
            ks = ksp.getKeystore(claveTXT.getPassword());
            
        }
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
            certificadoEmitidoATxt.setText(Util.getCN(cert));
            String emisor = FirmaDigital.getNombreCA(cert);
            certificadoEmitidoPorTxt.setText(emisor);
            certificadoValidoDesdeTxt.setText(cert.getNotBefore().toString());
            certificadoValidoHastaTxt.setText(cert.getNotAfter().toString());
        }
        //TOdo botar error si es null
    }
    
    private void resetearInfoValidacionCertificado() {
        rutaCertificadoTxt.setText("");
        certificadoEmitidoATxt.setText("");
        certificadoEmitidoPorTxt.setText("");
        certificadoValidoDesdeTxt.setText("");
        certificadoValidoHastaTxt.setText("");
        validarCRLTxtArea.setText("");
        validarOCSPTxtArea.setText("");
        llaveVerificar = null;
        certClaveTXT.setText("");
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
        rutaDocumentoFirmarTxt = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        abrirArchivoFirmarBtn = new javax.swing.JButton();
        firmarLlaveRBTN = new javax.swing.JRadioButton();
        firmarTokenRBTN = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        rutaLlaveFirmarTxt = new javax.swing.JTextField();
        abrirArchivoPSKFirmarBtn = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        claveTXT = new javax.swing.JPasswordField();
        firmarBTN = new javax.swing.JButton();
        resetearBTN = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        formatoFirmaFirmarCbx = new javax.swing.JComboBox<>();
        jScrollPane5 = new javax.swing.JScrollPane();
        datosArchivoFirmarTbl = new javax.swing.JTable();
        jScrollPane6 = new javax.swing.JScrollPane();
        datosDelFirmanteFirmadorTbl = new javax.swing.JTable();
        jScrollPane7 = new javax.swing.JScrollPane();
        datosDelCertificadoFirmadorTbl = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        verificarDocumentoPanel = new javax.swing.JPanel();
        verificarBTN = new javax.swing.JButton();
        archivoFirmadoVerficarLbl = new javax.swing.JLabel();
        archivoFirmadoVerificarTxt = new javax.swing.JTextField();
        examinarVerificarBtn = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        datosFirmanteVerificarTbl = new javax.swing.JTable();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        datosArchivoVerificarTbl = new javax.swing.JTable();
        resetearBTN1 = new javax.swing.JButton();
        validarCertificadoPanel = new javax.swing.JPanel();
        certificadoVldCertLbl = new javax.swing.JLabel();
        rutaCertificadoTxt = new javax.swing.JTextField();
        abrirCertificadoBtn = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        certificadoEmitidoATxt = new javax.swing.JTextField();
        certificadoEmitidoPorTxt = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        certificadoValidoDesdeTxt = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        certificadoValidoHastaTxt = new javax.swing.JTextField();
        validarCRLBtn = new javax.swing.JButton();
        validarOCSPBtn = new javax.swing.JButton();
        resetValidarFormBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        validarCRLTxtArea = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        validarOCSPTxtArea = new javax.swing.JTextArea();
        certificadoVldCertLbl1 = new javax.swing.JLabel();
        certClaveTXT = new javax.swing.JPasswordField();
        firmarLlaveRBTN1 = new javax.swing.JRadioButton();
        firmarTokenRBTN1 = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Verificador - Firmador");

        firmarVerificarDocPanel.setName(""); // NOI18N

        rutaDocumentoFirmarTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rutaDocumentoFirmarTxtActionPerformed(evt);
            }
        });

        jLabel1.setText("Documento");

        abrirArchivoFirmarBtn.setText("Abrir");
        abrirArchivoFirmarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abrirArchivoFirmarBtnActionPerformed(evt);
            }
        });

        tipoFirmaBtnGRP.add(firmarLlaveRBTN);
        firmarLlaveRBTN.setText("Archivo");
        firmarLlaveRBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firmarLlaveRBTNActionPerformed(evt);
            }
        });

        tipoFirmaBtnGRP.add(firmarTokenRBTN);
        firmarTokenRBTN.setText("Token");
        firmarTokenRBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firmarTokenRBTNActionPerformed(evt);
            }
        });

        jLabel2.setText("Certificado");

        rutaLlaveFirmarTxt.setEnabled(false);
        rutaLlaveFirmarTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rutaLlaveFirmarTxtActionPerformed(evt);
            }
        });

        abrirArchivoPSKFirmarBtn.setText("Abrir");
        abrirArchivoPSKFirmarBtn.setEnabled(false);
        abrirArchivoPSKFirmarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abrirArchivoPSKFirmarBtnActionPerformed(evt);
            }
        });

        jLabel3.setText("Contraseña");

        claveTXT.setText("jPasswordField1");
        claveTXT.setEnabled(false);

        firmarBTN.setText("Firmar");
        firmarBTN.setEnabled(false);
        firmarBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firmarBTNActionPerformed(evt);
            }
        });

        resetearBTN.setText("Resetear");
        resetearBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetearBTNActionPerformed(evt);
            }
        });

        jLabel10.setText("Formato de Firma");

        formatoFirmaFirmarCbx.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "PDF", "OOXML (MS OFFICE)", "ODT", "XML" }));
        formatoFirmaFirmarCbx.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatoFirmaFirmarCbxActionPerformed(evt);
            }
        });

        datosArchivoFirmarTbl.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null}
            },
            new String [] {
                "Datos de Archivo"
            }
        ));
        jScrollPane5.setViewportView(datosArchivoFirmarTbl);

        datosDelFirmanteFirmadorTbl.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null}
            },
            new String [] {
                "Cédula", "Nombres", "Institución", "Cargo", "Fecha"
            }
        ));
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
        ));
        jScrollPane7.setViewportView(datosDelCertificadoFirmadorTbl);

        jLabel5.setText("Certificado en:");

        jLabel11.setText("Datos del Firmante");

        javax.swing.GroupLayout firmarVerificarDocPanelLayout = new javax.swing.GroupLayout(firmarVerificarDocPanel);
        firmarVerificarDocPanel.setLayout(firmarVerificarDocPanelLayout);
        firmarVerificarDocPanelLayout.setHorizontalGroup(
            firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, firmarVerificarDocPanelLayout.createSequentialGroup()
                        .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, firmarVerificarDocPanelLayout.createSequentialGroup()
                                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel5))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                                        .addComponent(firmarLlaveRBTN)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(firmarTokenRBTN)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(formatoFirmaFirmarCbx, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, firmarVerificarDocPanelLayout.createSequentialGroup()
                                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3))
                                .addGap(36, 36, 36)
                                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel11)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                                        .addComponent(firmarBTN)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(resetearBTN))
                                    .addComponent(rutaLlaveFirmarTxt)
                                    .addComponent(claveTXT)
                                    .addComponent(rutaDocumentoFirmarTxt, javax.swing.GroupLayout.Alignment.TRAILING))))
                        .addGap(18, 18, 18)
                        .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(abrirArchivoPSKFirmarBtn)
                            .addComponent(abrirArchivoFirmarBtn))
                        .addGap(38, 38, 38))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, firmarVerificarDocPanelLayout.createSequentialGroup()
                        .addGap(0, 96, Short.MAX_VALUE)
                        .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE)
                            .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE)
                                .addComponent(jScrollPane6)))
                        .addGap(101, 101, 101))))
        );
        firmarVerificarDocPanelLayout.setVerticalGroup(
            firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(firmarLlaveRBTN)
                    .addComponent(firmarTokenRBTN)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(formatoFirmaFirmarCbx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rutaDocumentoFirmarTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(abrirArchivoFirmarBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16)
                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(rutaLlaveFirmarTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(abrirArchivoPSKFirmarBtn))
                .addGap(18, 18, 18)
                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(claveTXT, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(31, 31, 31)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel11)
                .addGap(1, 1, 1)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(firmarBTN)
                    .addComponent(resetearBTN))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        mainPanel.addTab("Firmar Electrónica De Documento", firmarVerificarDocPanel);

        verificarBTN.setText("Verificar Archivo");
        verificarBTN.setEnabled(false);
        verificarBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verificarBTNActionPerformed(evt);
            }
        });

        archivoFirmadoVerficarLbl.setText("Archivo Firmado:");

        examinarVerificarBtn.setText("Examinar");
        examinarVerificarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                examinarVerificarBtnActionPerformed(evt);
            }
        });

        jLabel12.setText("Resultados de la Verificación del Archivo Firmado Electrónicamente");

        datosFirmanteVerificarTbl.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null}
            },
            new String [] {
                "Emitido A", "Emitido Por", "Válido Desde", "Válido Hasta", "Fecha Firmado"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                true, true, true, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(datosFirmanteVerificarTbl);

        jLabel13.setText("Datos De Los Firmantes");

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

        resetearBTN1.setText("Resetear");
        resetearBTN1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetearBTN1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout verificarDocumentoPanelLayout = new javax.swing.GroupLayout(verificarDocumentoPanel);
        verificarDocumentoPanel.setLayout(verificarDocumentoPanelLayout);
        verificarDocumentoPanelLayout.setHorizontalGroup(
            verificarDocumentoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(verificarDocumentoPanelLayout.createSequentialGroup()
                .addGroup(verificarDocumentoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(verificarDocumentoPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator1))
                    .addGroup(verificarDocumentoPanelLayout.createSequentialGroup()
                        .addGroup(verificarDocumentoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(verificarDocumentoPanelLayout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(archivoFirmadoVerficarLbl)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(archivoFirmadoVerificarTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 484, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(examinarVerificarBtn))
                            .addGroup(verificarDocumentoPanelLayout.createSequentialGroup()
                                .addGap(311, 311, 311)
                                .addComponent(verificarBTN))
                            .addGroup(verificarDocumentoPanelLayout.createSequentialGroup()
                                .addGap(160, 160, 160)
                                .addComponent(jLabel12)))
                        .addGap(0, 25, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(verificarDocumentoPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(verificarDocumentoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addGroup(verificarDocumentoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 722, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(verificarDocumentoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 722, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(resetearBTN1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        verificarDocumentoPanelLayout.setVerticalGroup(
            verificarDocumentoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(verificarDocumentoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(verificarDocumentoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(archivoFirmadoVerficarLbl)
                    .addComponent(archivoFirmadoVerificarTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(examinarVerificarBtn))
                .addGap(18, 18, 18)
                .addComponent(verificarBTN)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(verificarDocumentoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(verificarDocumentoPanelLayout.createSequentialGroup()
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel14))
                    .addGroup(verificarDocumentoPanelLayout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addGap(39, 39, 39)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(resetearBTN1)
                .addGap(173, 173, 173))
        );

        mainPanel.addTab("Verificar Documento", verificarDocumentoPanel);

        validarCertificadoPanel.setName(""); // NOI18N

        certificadoVldCertLbl.setText("Certificado");

        abrirCertificadoBtn.setText("Abrir");
        abrirCertificadoBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abrirCertificadoBtnActionPerformed(evt);
            }
        });

        jLabel6.setText("Emitido A");

        jLabel7.setText("Emitido Por");

        certificadoEmitidoATxt.setEditable(false);
        certificadoEmitidoATxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                certificadoEmitidoATxtActionPerformed(evt);
            }
        });

        certificadoEmitidoPorTxt.setEditable(false);
        certificadoEmitidoPorTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                certificadoEmitidoPorTxtActionPerformed(evt);
            }
        });

        jLabel8.setText("Válido desde");

        certificadoValidoDesdeTxt.setEditable(false);
        certificadoValidoDesdeTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                certificadoValidoDesdeTxtActionPerformed(evt);
            }
        });

        jLabel9.setText("Válido hasta");

        certificadoValidoHastaTxt.setEditable(false);
        certificadoValidoHastaTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                certificadoValidoHastaTxtActionPerformed(evt);
            }
        });

        validarCRLBtn.setText("Validar por CRL");
        validarCRLBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validarCRLBtnActionPerformed(evt);
            }
        });

        validarOCSPBtn.setText("Validar por OCSP");
        validarOCSPBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validarOCSPBtnActionPerformed(evt);
            }
        });

        resetValidarFormBtn.setText("Resetear");
        resetValidarFormBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetValidarFormBtnActionPerformed(evt);
            }
        });

        validarCRLTxtArea.setColumns(20);
        validarCRLTxtArea.setRows(5);
        jScrollPane1.setViewportView(validarCRLTxtArea);

        validarOCSPTxtArea.setColumns(20);
        validarOCSPTxtArea.setRows(5);
        jScrollPane2.setViewportView(validarOCSPTxtArea);

        certificadoVldCertLbl1.setText("Contraseña");

        certClaveTXT.setText("jPasswordField1");

        tipoFirmaBtnGRP.add(firmarLlaveRBTN1);
        firmarLlaveRBTN1.setText("Archivo");
        firmarLlaveRBTN1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firmarLlaveRBTN1ActionPerformed(evt);
            }
        });

        tipoFirmaBtnGRP.add(firmarTokenRBTN1);
        firmarTokenRBTN1.setText("Firmar con Token");
        firmarTokenRBTN1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firmarTokenRBTN1ActionPerformed(evt);
            }
        });

        jLabel4.setText("Certificados en:");

        javax.swing.GroupLayout validarCertificadoPanelLayout = new javax.swing.GroupLayout(validarCertificadoPanel);
        validarCertificadoPanel.setLayout(validarCertificadoPanelLayout);
        validarCertificadoPanelLayout.setHorizontalGroup(
            validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, validarCertificadoPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(resetValidarFormBtn)
                .addGap(332, 332, 332))
            .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                        .addGap(104, 104, 104)
                        .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(certificadoValidoDesdeTxt, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(certificadoEmitidoATxt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(validarCRLBtn))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                                .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel9))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 86, Short.MAX_VALUE)
                                .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(certificadoValidoHastaTxt)
                                    .addComponent(certificadoEmitidoPorTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                                .addComponent(validarOCSPBtn)
                                .addGap(30, 30, 30))))
                    .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                        .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(certificadoVldCertLbl)
                            .addComponent(jLabel6)
                            .addComponent(jLabel8)
                            .addComponent(certificadoVldCertLbl1)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                                .addComponent(firmarLlaveRBTN1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(firmarTokenRBTN1)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                                .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(certClaveTXT)
                                    .addComponent(rutaCertificadoTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 453, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(abrirCertificadoBtn)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 58, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
            .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        validarCertificadoPanelLayout.setVerticalGroup(
            validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(firmarLlaveRBTN1)
                    .addComponent(firmarTokenRBTN1)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(certificadoVldCertLbl)
                    .addComponent(rutaCertificadoTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(abrirCertificadoBtn))
                .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(certificadoVldCertLbl1))
                    .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(certClaveTXT, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(certificadoEmitidoATxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(certificadoEmitidoPorTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(certificadoValidoDesdeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(certificadoValidoHastaTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(validarCRLBtn)
                    .addComponent(validarOCSPBtn))
                .addGap(18, 18, 18)
                .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(64, 64, 64)
                .addComponent(resetValidarFormBtn)
                .addContainerGap(82, Short.MAX_VALUE))
        );

        mainPanel.addTab("Validar Certificado De Firma Electrónica", validarCertificadoPanel);

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

    private void certificadoValidoHastaTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_certificadoValidoHastaTxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_certificadoValidoHastaTxtActionPerformed

    private void certificadoValidoDesdeTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_certificadoValidoDesdeTxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_certificadoValidoDesdeTxtActionPerformed

    private void certificadoEmitidoPorTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_certificadoEmitidoPorTxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_certificadoEmitidoPorTxtActionPerformed

    private void certificadoEmitidoATxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_certificadoEmitidoATxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_certificadoEmitidoATxtActionPerformed

    private void abrirCertificadoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abrirCertificadoBtnActionPerformed
        llaveVerificar = abrirArchivo();
        if (llaveVerificar != null) {
            rutaCertificadoTxt.setText(llaveVerificar.getAbsolutePath());
            
        }
    }//GEN-LAST:event_abrirCertificadoBtnActionPerformed

    private void validarOCSPBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validarOCSPBtnActionPerformed
        System.out.println("Validar OCSP");
        validarOCSPTxtArea.setText("");
        validarOCSPTxtArea.append("Abriendo el certificado " + rutaCertificadoTxt.getText() + "\n");
        KeyStoreProvider ksp = new FileKeyStoreProvider(rutaCertificadoTxt.getText());
        try {
            ks = ksp.getKeystore(certClaveTXT.getPassword());
            String alias = ks.aliases().nextElement();
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
            Certificate [] cadenaCerts =  ks.getCertificateChain(alias);
            System.out.println("cad " +cadenaCerts.length);
            List<X509Certificate> cadena = new ArrayList<>();
            for(int i=0; i< cadenaCerts.length; i++){
                cadena.add((X509Certificate) cadenaCerts[i]);
                System.out.println(FirmaDigital.getNombreCA(cadena.get(i)));
            }
            
            setearInfoValidacionCertificado(cert);
            
            List<String> ocspUrls = CertificateUtils.getAuthorityInformationAccess(cert);
            for (String ocsp : ocspUrls) {
                validarOCSPTxtArea.append("OCSP=" + ocsp + "\n");
                System.out.println("OCSP=" + ocsp);
            }
            System.out.println("OCSPUrls "+ ocspUrls.size());
            
            ValidadorOCSP validadorOCSP = new ValidadorOCSP();
            X509Certificate certRoot = cadena.get(1); //FirmaDigital.getRootCertificate(cert);
                        
            validadorOCSP.validar(cert, certRoot, ocspUrls);
            
            boolean validezCert = true; //= OcspUtils.isValidCertificate(cert);
            //System.out.println("Valid? " +OCSPRespStatus.MALFORMED_REQUEST );
            String resultStr;
            if(validezCert)
                resultStr = "Válido";
            else
                resultStr = "Inválido";
                
            validarOCSPTxtArea.append("Certificado: " + resultStr + "\n");
            
        } catch (IOException | RubricaException | OcspValidationException | KeyStoreException ex) {
            //TODO botar error
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            validarOCSPTxtArea.append("Certificado: No se pudo validar \n");
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }//GEN-LAST:event_validarOCSPBtnActionPerformed

    private void validarCRLBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validarCRLBtnActionPerformed
        System.out.println("Validar CRL");
        String msgValidarCRL;
        validarCRLTxtArea.setText("");
        validarCRLTxtArea.append("Abriendo el certificado " + rutaCertificadoTxt.getText() + "\n");    
        
        KeyStoreProvider ksp = new FileKeyStoreProvider(rutaCertificadoTxt.getText());
        
        
        try {
            ks = ksp.getKeystore(certClaveTXT.getPassword());
            X509Certificate cert = (X509Certificate) ks.getCertificate(ks.aliases().nextElement());
            
            setearInfoValidacionCertificado(cert);
            
            for (String url : CertificateUtils.getCrlDistributionPoints(cert)) {
                System.out.println("url=" + url);
            }
            
            String nombreCA = FirmaDigital.getNombreCA(cert);
            validarCRLTxtArea.append("Validando CRL contra "+ nombreCA + "\n");
            
            String urlCrl = this.obtenerUrlCRL(CertificateUtils.getCrlDistributionPoints(cert));
            ValidationResult result;
            switch(nombreCA){
                case "Banco Central del Ecuador":
                    //TODO quemado hasta que arreglen en el banco central
                    urlCrl = ServicioCRL.BCE_CRL;
                    result = CrlUtils.verifyCertificateCRLs(cert, new BceSubTestCert().getPublicKey(),
                            Arrays.asList(urlCrl));
                    System.out.println("Validation result: " + result);
                    
                    validarCRLTxtArea.append("Certificado: " + resultadosCRL(result));
                    break;
                case "Consejo de la Judicatura":
                    result = CrlUtils.verifyCertificateCRLs(cert, new ConsejoJudicaturaSubCert().getPublicKey(),
                    Arrays.asList(urlCrl));
                    System.out.println("Validation result: " + result);
                    validarCRLTxtArea.append("Certificado: " + resultadosCRL(result));
                    break;
                case "SecurityData":
                    result = CrlUtils.verifyCertificateCRLs(cert, new SecurityDataSubCaCert().getPublicKey(),
                            Arrays.asList(urlCrl));
                    System.out.println("Validation result: " + result);
                    validarCRLTxtArea.append("Certificado: " + resultadosCRL(result));
                    break;
                default:
                    validarCRLTxtArea.append("Error entidad no reconocida \n");
                    break;
            }
            

            System.out.println(CertificateUtils.getCN(cert));
        } catch (KeyStoreException | IOException | RubricaException  ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            
        }             
        
    }//GEN-LAST:event_validarCRLBtnActionPerformed

    private void resetValidarFormBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetValidarFormBtnActionPerformed
        resetearInfoValidacionCertificado();
    }//GEN-LAST:event_resetValidarFormBtnActionPerformed

    private void resetearBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetearBTNActionPerformed
        this.resetForm();
    }//GEN-LAST:event_resetearBTNActionPerformed

    private void firmarBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firmarBTNActionPerformed
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
    }//GEN-LAST:event_firmarBTNActionPerformed

    private void abrirArchivoPSKFirmarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abrirArchivoPSKFirmarBtnActionPerformed
        llave = abrirArchivo();
        if (llave != null) {
            rutaLlaveFirmarTxt.setText(llave.getAbsolutePath());
        }
    }//GEN-LAST:event_abrirArchivoPSKFirmarBtnActionPerformed

    private void rutaLlaveFirmarTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rutaLlaveFirmarTxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rutaLlaveFirmarTxtActionPerformed

    private void firmarTokenRBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firmarTokenRBTNActionPerformed
        System.out.println("Firmar con Token");
        this.selFirmarConToken();
    }//GEN-LAST:event_firmarTokenRBTNActionPerformed

    private void firmarLlaveRBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firmarLlaveRBTNActionPerformed
        System.out.println("Firmar con llave");
        this.selFirmarConArchivo();
    }//GEN-LAST:event_firmarLlaveRBTNActionPerformed

    private void abrirArchivoFirmarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abrirArchivoFirmarBtnActionPerformed
        // Por defecto desbloqueamos el objecto de verificar
        documento = abrirArchivo(filtros);
        if (documento != null) {
            rutaDocumentoFirmarTxt.setText(documento.getAbsolutePath());

        }
        
        formatoFirmaFirmarCbx.setSelectedItem(OS);
    }//GEN-LAST:event_abrirArchivoFirmarBtnActionPerformed

    private void rutaDocumentoFirmarTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rutaDocumentoFirmarTxtActionPerformed

    }//GEN-LAST:event_rutaDocumentoFirmarTxtActionPerformed

    private void firmarLlaveRBTN1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firmarLlaveRBTN1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_firmarLlaveRBTN1ActionPerformed

    private void firmarTokenRBTN1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firmarTokenRBTN1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_firmarTokenRBTN1ActionPerformed

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

    private void formatoFirmaFirmarCbxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatoFirmaFirmarCbxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_formatoFirmaFirmarCbxActionPerformed

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
    private javax.swing.JButton abrirArchivoPSKFirmarBtn;
    private javax.swing.JButton abrirCertificadoBtn;
    private javax.swing.JLabel archivoFirmadoVerficarLbl;
    private javax.swing.JTextField archivoFirmadoVerificarTxt;
    private javax.swing.JPasswordField certClaveTXT;
    private javax.swing.JTextField certificadoEmitidoATxt;
    private javax.swing.JTextField certificadoEmitidoPorTxt;
    private javax.swing.JTextField certificadoValidoDesdeTxt;
    private javax.swing.JTextField certificadoValidoHastaTxt;
    private javax.swing.JLabel certificadoVldCertLbl;
    private javax.swing.JLabel certificadoVldCertLbl1;
    private javax.swing.JPasswordField claveTXT;
    private javax.swing.JTable datosArchivoFirmarTbl;
    private javax.swing.JTable datosArchivoVerificarTbl;
    private javax.swing.JTable datosDelCertificadoFirmadorTbl;
    private javax.swing.JTable datosDelFirmanteFirmadorTbl;
    private javax.swing.JTable datosFirmanteVerificarTbl;
    private javax.swing.JButton examinarVerificarBtn;
    private javax.swing.JButton firmarBTN;
    private javax.swing.JRadioButton firmarLlaveRBTN;
    private javax.swing.JRadioButton firmarLlaveRBTN1;
    private javax.swing.JRadioButton firmarTokenRBTN;
    private javax.swing.JRadioButton firmarTokenRBTN1;
    private javax.swing.JPanel firmarVerificarDocPanel;
    private javax.swing.JComboBox<String> formatoFirmaFirmarCbx;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane mainPanel;
    private javax.swing.JButton resetValidarFormBtn;
    private javax.swing.JButton resetearBTN;
    private javax.swing.JButton resetearBTN1;
    private javax.swing.JTextField rutaCertificadoTxt;
    private javax.swing.JTextField rutaDocumentoFirmarTxt;
    private javax.swing.JTextField rutaLlaveFirmarTxt;
    private javax.swing.ButtonGroup tipoFirmaBtnGRP;
    private javax.swing.JButton validarCRLBtn;
    private javax.swing.JTextArea validarCRLTxtArea;
    private javax.swing.JPanel validarCertificadoPanel;
    private javax.swing.JButton validarOCSPBtn;
    private javax.swing.JTextArea validarOCSPTxtArea;
    private javax.swing.JButton verificarBTN;
    private javax.swing.JPanel verificarDocumentoPanel;
    // End of variables declaration//GEN-END:variables
}
