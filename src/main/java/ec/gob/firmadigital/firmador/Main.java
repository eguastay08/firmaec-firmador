/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.firmadigital.firmador;

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
import io.rubrica.certificate.ec.bce.CertificadoBancoCentralFactory;
import io.rubrica.certificate.ec.cj.CertificadoConsejoJudicaturaDataFactory;
import io.rubrica.certificate.ec.cj.ConsejoJudicaturaSubCert;
import io.rubrica.certificate.ec.securitydata.CertificadoSecurityDataFactory;
import io.rubrica.certificate.ec.securitydata.SecurityDataSubCaCert;
import io.rubrica.core.RubricaException;
import io.rubrica.core.Util;
import java.awt.Component;
import java.util.List;
import javax.swing.JFileChooser;
import java.io.File;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import io.rubrica.keystore.FileKeyStoreProvider;
import io.rubrica.keystore.KeyStoreProvider;
import io.rubrica.keystore.KeyStoreProviderFactory;
import io.rubrica.ocsp.OcspValidationException;
import io.rubrica.sign.InvalidFormatException;
import io.rubrica.util.CertificateUtils;
import io.rubrica.util.OcspUtils;
import java.awt.CardLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.SignatureException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
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
        visorPdfPanel.setVisible(false);
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
        DefaultTreeModel model = (DefaultTreeModel) certificadosJTR.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        
        //Borramos los viejos nodos
        root.removeAllChildren(); //this removes all nodes
        model.reload();
        
        this.documento = null;
        this.tipoFirmaBtnGRP.clearSelection();
        this.firmarBTN.setEnabled(false);
        this.verificarBTN.setEnabled(false);
        this.claveTXT.setText("");
        this.rutaDocumentoTXT.setText("");
        this.claveTXT.setEnabled(false);
        this.documento = null;
        this.llave = null;
        this.rutaLlaveTXT.setEnabled(false);
        this.rutaLlaveTXT.setText("");
        this.documentoFirmadoTXT.setText("");
        //this.certificadosJTR.getModel().

    }

    private void selFirmarConArchivo() {
        this.firmarBTN.setEnabled(true);
        this.rutaLlaveTXT.setEnabled(true);
        this.abrirArchivoPSKBtn.setEnabled(true);
        // Si es windows no hay que habilitar el campo de contraseña
        if (!esWindows()) {
            this.claveTXT.setEnabled(true);
        } else {
            this.claveTXT.setEnabled(false);
        }
    }

    private void selFirmarConToken() {
        this.firmarBTN.setEnabled(true);
        this.rutaLlaveTXT.setEnabled(false);
        this.claveTXT.setEnabled(false);
        this.abrirArchivoPSKBtn.setEnabled(false);
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
            throw new DocumentoNoExistenteException("Documento "+this.rutaDocumentoTXT.getText() + " no existe");

        if(!documento.exists()) 
            throw new DocumentoNoExistenteException("Documento "+documento.getAbsolutePath() + " no existe");
        
            
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
    private boolean verificarDocumento() throws DocumentoNoPermitido, IOException, KeyStoreException, OcspValidationException, SignatureException, InvalidFormatException {
        // Vemos si existe
        System.out.println("Verificando Docs");
        if (documento == null || !documento.exists()) {
            return false;
        }
        // Vemos si es un documento permitido primero
        tipoDeDocumentPermitido(documento);
        
        FirmaDigital firmaDigital = new FirmaDigital();
        
        List<Certificado> certs = firmaDigital.verificar(documento);
        
        DefaultTreeModel model = (DefaultTreeModel) certificadosJTR.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        
        certificadosJTR.setCellRenderer(new DefaultTreeCellRenderer() {

            @Override
            public Component getTreeCellRendererComponent(JTree tree,
                    Object value, boolean selected, boolean expanded,
                    boolean isLeaf, int row, boolean focused) {
                Component c = super.getTreeCellRendererComponent(tree, value,
                        selected, expanded, isLeaf, row, focused);
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                //TODO esto se podria hacer mas bonito
                String s = node.getUserObject().toString();
                
                if (node.getLevel()==1){
                    /**
                     * Solo si es un certificado valido, que no ha sido revocado 
                     * y que pertenezca a una entidad reconocida da un check
                     * TODO usar constantes para las entidad
                     */
                    if(node.getChildAt(5).toString().toLowerCase().contains("true") && 
                            node.getChildAt(6).toString().toLowerCase().contains("false")  &&
                            !node.getChildAt(1).toString().toLowerCase().contains("entidad no reconocidad") )
                        setIcon(checkIcon);
                    else
                        setIcon(notCheckIcon);
                }else if (node.getLevel()==2 && s.toLowerCase().contains("validado")){
                    // String s = node.getUserObject().toString();
                    
                    if(s.contains("true"))
                        setIcon(checkIcon);
                    else
                        setIcon(notCheckIcon);
                }else if (node.getLevel()==2 && s.toLowerCase().contains("revocado")){
                    // String s = node.getUserObject().toString();
                    if(s.contains("true"))
                        setIcon(notCheckIcon);
                    else
                        setIcon(checkIcon);
                }
                
                return c;
            }
        });
        //Borramos los viejos nodos
        root.removeAllChildren(); //this removes all nodes
        model.reload();
        
        int cont = 0;
        
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
       
        
        for(Certificado cert: certs){
   
            DefaultMutableTreeNode curCert = new DefaultMutableTreeNode("Certificado " + ++cont+ ": "+cert.getIssuedTo());    
 
            root.add(curCert);
            
            curCert.add(new DefaultMutableTreeNode("Emitido a: " + cert.getIssuedTo()));
            curCert.add(new DefaultMutableTreeNode("Emitido por: " + cert.getIssuedBy()));
            curCert.add(new DefaultMutableTreeNode("Válido desde: " + format1.format(cert.getValidFrom().getTime())));
            curCert.add(new DefaultMutableTreeNode("Válido hasta: " + format1.format(cert.getValidTo().getTime())));
            curCert.add(new DefaultMutableTreeNode("Fecha utilizado: " + format1.format(cert.getGenerated().getTime())));
            curCert.add(new DefaultMutableTreeNode("Validado: " + cert.getValidated()));
            curCert.add(new DefaultMutableTreeNode("Revocado: " + cert.getRevocated()));
        }
        
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        certificadosJTR.setModel(treeModel);
        //model.insertNodeInto(new DefaultMutableTreeNode("another_child"), root, root.getChildCount());
        
        return false;
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
        
        this.documentoFirmadoTXT.setText(nombreDocFirmado);

        return true;
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
            KeyStoreProvider ksp = new FileKeyStoreProvider(rutaLlaveTXT.getText());
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
        mainPanel = new javax.swing.JPanel();
        firmarVerificarDocPanel = new javax.swing.JPanel();
        rutaDocumentoTXT = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        abrirArchivoBtn = new javax.swing.JButton();
        firmarLlaveRBTN = new javax.swing.JRadioButton();
        firmarTokenRBTN = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        rutaLlaveTXT = new javax.swing.JTextField();
        abrirArchivoPSKBtn = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        claveTXT = new javax.swing.JPasswordField();
        jLabel5 = new javax.swing.JLabel();
        documentoFirmadoTXT = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        verificadoPorSPL = new javax.swing.JScrollPane();
        certificadosJTR = new javax.swing.JTree();
        firmarBTN = new javax.swing.JButton();
        verificarBTN = new javax.swing.JButton();
        verificarBTN1 = new javax.swing.JButton();
        visorPdfPanel = new javax.swing.JPanel();
        scrollPaneVisorPDF = new javax.swing.JScrollPane();
        panelVisorPDF = new javax.swing.JPanel();
        scrollPaneMenuPDF = new javax.swing.JScrollPane();
        panelMenuPDF = new javax.swing.JPanel();
        opciones = new javax.swing.JButton();
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
        infoDocumentos = new javax.swing.JButton();
        validarCertificados = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Verificador - Firmador");

        mainPanel.setLayout(new java.awt.CardLayout());

        firmarVerificarDocPanel.setName(""); // NOI18N

        rutaDocumentoTXT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rutaDocumentoTXTActionPerformed(evt);
            }
        });

        jLabel1.setText("Documento");

        abrirArchivoBtn.setText("Abrir");
        abrirArchivoBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abrirArchivoBtnActionPerformed(evt);
            }
        });

        tipoFirmaBtnGRP.add(firmarLlaveRBTN);
        firmarLlaveRBTN.setText("Firmar con Certificado PKC");
        firmarLlaveRBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firmarLlaveRBTNActionPerformed(evt);
            }
        });

        tipoFirmaBtnGRP.add(firmarTokenRBTN);
        firmarTokenRBTN.setText("Firmar con Token");
        firmarTokenRBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firmarTokenRBTNActionPerformed(evt);
            }
        });

        jLabel2.setText("Certificado");

        rutaLlaveTXT.setEnabled(false);
        rutaLlaveTXT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rutaLlaveTXTActionPerformed(evt);
            }
        });

        abrirArchivoPSKBtn.setText("Abrir");
        abrirArchivoPSKBtn.setEnabled(false);
        abrirArchivoPSKBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abrirArchivoPSKBtnActionPerformed(evt);
            }
        });

        jLabel3.setText("Contraseña");

        claveTXT.setText("jPasswordField1");
        claveTXT.setEnabled(false);

        jLabel5.setText("Doc. Firmado");

        documentoFirmadoTXT.setEditable(false);
        documentoFirmadoTXT.setEnabled(false);
        documentoFirmadoTXT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                documentoFirmadoTXTActionPerformed(evt);
            }
        });

        jLabel4.setText("Verificado Por");

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Firmas");
        certificadosJTR.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        verificadoPorSPL.setViewportView(certificadosJTR);

        firmarBTN.setText("Firmar");
        firmarBTN.setEnabled(false);
        firmarBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firmarBTNActionPerformed(evt);
            }
        });

        verificarBTN.setText("Verificar");
        verificarBTN.setEnabled(false);
        verificarBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verificarBTNActionPerformed(evt);
            }
        });

        verificarBTN1.setText("Resetear");
        verificarBTN1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verificarBTN1ActionPerformed(evt);
            }
        });

        panelVisorPDF.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelVisorPDF.setLayout(new java.awt.BorderLayout());
        scrollPaneVisorPDF.setViewportView(panelVisorPDF);

        scrollPaneMenuPDF.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panelMenuPDF.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelMenuPDF.setEnabled(false);
        panelMenuPDF.setName("menupaginas"); // NOI18N
        panelMenuPDF.setLayout(new javax.swing.BoxLayout(panelMenuPDF, javax.swing.BoxLayout.Y_AXIS));
        scrollPaneMenuPDF.setViewportView(panelMenuPDF);

        javax.swing.GroupLayout visorPdfPanelLayout = new javax.swing.GroupLayout(visorPdfPanel);
        visorPdfPanel.setLayout(visorPdfPanelLayout);
        visorPdfPanelLayout.setHorizontalGroup(
            visorPdfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(visorPdfPanelLayout.createSequentialGroup()
                .addComponent(scrollPaneMenuPDF, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(scrollPaneVisorPDF))
        );
        visorPdfPanelLayout.setVerticalGroup(
            visorPdfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollPaneMenuPDF, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
            .addComponent(scrollPaneVisorPDF)
        );

        opciones.setText("Opciones");
        opciones.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opcionesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout firmarVerificarDocPanelLayout = new javax.swing.GroupLayout(firmarVerificarDocPanel);
        firmarVerificarDocPanel.setLayout(firmarVerificarDocPanelLayout);
        firmarVerificarDocPanelLayout.setHorizontalGroup(
            firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(visorPdfPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                        .addComponent(firmarLlaveRBTN)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(firmarTokenRBTN)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, firmarVerificarDocPanelLayout.createSequentialGroup()
                        .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(36, 36, 36)
                                .addComponent(rutaDocumentoTXT))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, firmarVerificarDocPanelLayout.createSequentialGroup()
                                .addGap(164, 164, 164)
                                .addComponent(firmarBTN)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(verificarBTN)
                                .addGap(18, 18, 18)
                                .addComponent(verificarBTN1)
                                .addGap(0, 186, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, firmarVerificarDocPanelLayout.createSequentialGroup()
                                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                                            .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel5)
                                                .addComponent(jLabel3))
                                            .addGap(44, 44, 44))
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, firmarVerificarDocPanelLayout.createSequentialGroup()
                                            .addComponent(jLabel2)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                    .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addGap(37, 37, 37)))
                                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(verificadoPorSPL)
                                    .addComponent(rutaLlaveTXT)
                                    .addComponent(claveTXT)
                                    .addComponent(documentoFirmadoTXT))))
                        .addGap(18, 18, 18)
                        .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(abrirArchivoPSKBtn)
                            .addComponent(opciones, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(abrirArchivoBtn))))
                .addContainerGap())
        );
        firmarVerificarDocPanelLayout.setVerticalGroup(
            firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(rutaDocumentoTXT, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(abrirArchivoBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(firmarLlaveRBTN)
                    .addComponent(firmarTokenRBTN))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(abrirArchivoPSKBtn)
                    .addComponent(rutaLlaveTXT, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jLabel3))
                    .addGroup(firmarVerificarDocPanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(claveTXT, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(documentoFirmadoTXT, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(verificadoPorSPL, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(opciones))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(firmarVerificarDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(firmarBTN)
                    .addComponent(verificarBTN)
                    .addComponent(verificarBTN1))
                .addGap(34, 34, 34)
                .addComponent(visorPdfPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        mainPanel.add(firmarVerificarDocPanel, "firmadorVerificador");

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

        javax.swing.GroupLayout validarCertificadoPanelLayout = new javax.swing.GroupLayout(validarCertificadoPanel);
        validarCertificadoPanel.setLayout(validarCertificadoPanelLayout);
        validarCertificadoPanelLayout.setHorizontalGroup(
            validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, validarCertificadoPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(resetValidarFormBtn)
                .addGap(85, 85, 85))
            .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                        .addGap(116, 116, 116)
                        .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(certificadoValidoDesdeTxt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                                .addComponent(certificadoEmitidoATxt, javax.swing.GroupLayout.Alignment.LEADING))
                            .addComponent(validarCRLBtn)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, validarCertificadoPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                        .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(certificadoValidoHastaTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                            .addComponent(certificadoEmitidoPorTxt))
                        .addGap(74, 74, 74))
                    .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                        .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(validarOCSPBtn)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 104, Short.MAX_VALUE))))
            .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(certificadoVldCertLbl)
                    .addComponent(jLabel6)
                    .addComponent(jLabel8)
                    .addComponent(certificadoVldCertLbl1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(certClaveTXT)
                    .addComponent(rutaCertificadoTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(abrirCertificadoBtn)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        validarCertificadoPanelLayout.setVerticalGroup(
            validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(validarCertificadoPanelLayout.createSequentialGroup()
                .addContainerGap()
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
                .addGap(42, 42, 42)
                .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(validarCRLBtn)
                    .addComponent(validarOCSPBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(validarCertificadoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                .addComponent(resetValidarFormBtn)
                .addGap(24, 24, 24))
        );

        mainPanel.add(validarCertificadoPanel, "validador");

        infoDocumentos.setText("Firmar Verificar Documentos");
        infoDocumentos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoDocumentosActionPerformed(evt);
            }
        });

        validarCertificados.setText("Validar Certificados");
        validarCertificados.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validarCertificadosActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(infoDocumentos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(validarCertificados)
                .addGap(194, 194, 194))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(validarCertificados, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(infoDocumentos, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void firmarLlaveRBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firmarLlaveRBTNActionPerformed
        System.out.println("Firmar con llave");
        this.selFirmarConArchivo();
    }//GEN-LAST:event_firmarLlaveRBTNActionPerformed

    private void rutaDocumentoTXTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rutaDocumentoTXTActionPerformed

    }//GEN-LAST:event_rutaDocumentoTXTActionPerformed

    private void firmarTokenRBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firmarTokenRBTNActionPerformed
        System.out.println("Firmar con Token");
        this.selFirmarConToken();
    }//GEN-LAST:event_firmarTokenRBTNActionPerformed

    private void rutaLlaveTXTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rutaLlaveTXTActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rutaLlaveTXTActionPerformed

    private void abrirArchivoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abrirArchivoBtnActionPerformed
        // Por defecto desbloqueamos el objecto de verificar
        documento = abrirArchivo(filtros);
        if (documento != null) {
            rutaDocumentoTXT.setText(documento.getAbsolutePath());
            verificarBTN.setEnabled(true);

            // TODO de momento no abrirmos nada
            //Si el documento es PDF abrirlo para desplegarlo
            String ext = FirmadorFileUtils.getFileExtension(documento);
            System.out.println("Extension " + ext);
            switch (ext.toLowerCase() + "xx") {
                case "pdf":
                    System.out.println("Abrimos el visor de pdfs " + ext);
                     {
                        // TODO revisar como manejar este error a futuro
                        try {
                            //TODO pasar esto a una clase
                            //TODO ver como revisamos esto de las paginas
                            pdfDocument = PDDocument.load(documento);
                            pdfRenderer = CustomPageDrawer.renderizar(pdfDocument);

                            // pdfDocument.getNumberOfPages()
                            // Limite de 3 para no sobrecargar, se debe hacer un lazyloading
                            for (int i = 0; i < 3; i++) {
                                BufferedImage image = pdfRenderer.renderImage(i);
                                int anchoPanel = panelMenuPDF.getWidth();
                                int anchoImagen = image.getWidth();
                                int altoImagen = image.getHeight();
                                int altoPic = (altoImagen / anchoImagen) * anchoPanel;
                                JLabel imgMenu = new JLabel(new ImageIcon(image.getScaledInstance(anchoPanel, altoPic, Image.SCALE_SMOOTH)));

                                imgMenu.addMouseListener(new MouseAdapter() {
                                    public void mouseClicked(MouseEvent e) {
                                        System.out.println("Cambio de imagen ");
                                        JLabel paginaVisorPDF = new JLabel(new ImageIcon(image));
                                        panelVisorPDF.add(paginaVisorPDF);
                                        panelVisorPDF.revalidate();
                                        panelVisorPDF.repaint();
                                    }
                                });
                                //imgMenu.setH
                                panelMenuPDF.add(imgMenu);
                                panelMenuPDF.add(new JLabel("Pag. " + (i + 1)));
                                //panelMenuPDF.add(new JSeparator(JSeparator.VERTICAL));
                                if (i == 0) {
                                    JLabel paginaVisorPDF = new JLabel(new ImageIcon(image));
                                    panelVisorPDF.add(paginaVisorPDF);
                                    panelVisorPDF.revalidate();
                                    panelVisorPDF.repaint();
                                }

                                //ImageIO.write(image, "PNG", new File("/tmp/test-"+i+".png"));
                            }
                            System.out.println("Acabo");
                            panelMenuPDF.revalidate();
                            panelMenuPDF.repaint();
                            //revalidate();
                            //repaint();
                        } catch (IOException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    break;

            }
        }
    }//GEN-LAST:event_abrirArchivoBtnActionPerformed

    private void abrirArchivoPSKBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abrirArchivoPSKBtnActionPerformed
        llave = abrirArchivo();
        if (llave != null) {
            rutaLlaveTXT.setText(llave.getAbsolutePath());
        }
    }//GEN-LAST:event_abrirArchivoPSKBtnActionPerformed

    private void verificarBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verificarBTNActionPerformed
        // Si lo del documento es distinto a lo de la ruta seteamos la ruta
        // del documento a la del textfield
        if (!documento.getAbsolutePath().equals(rutaDocumentoTXT.getText())) 
            documento = new File(rutaDocumentoTXT.getText());
        
        try {
            verificarDocumento();
        } catch (Exception ex) {
            //TODO agregar mensaje de error
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error no se pudo verificar ");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_verificarBTNActionPerformed

    private void verificarBTN1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verificarBTN1ActionPerformed
        this.resetForm();
    }//GEN-LAST:event_verificarBTN1ActionPerformed

    private void firmarBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firmarBTNActionPerformed
        if (documento == null || !documento.getAbsolutePath().equals(rutaDocumentoTXT.getText())) 
            documento = new File(rutaDocumentoTXT.getText());
        
        try {
            this.firmarDocumento();
            JOptionPane.showMessageDialog(this, "Documento firmado "+ this.documentoFirmadoTXT.getText(), "Firmador", JOptionPane.INFORMATION_MESSAGE, checkIcon);
            System.out.println("Documento firmado");
        } catch (Exception ex) {
            //TODO agregar mensaje de error
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error no se pudo firmar ");
        }
    }//GEN-LAST:event_firmarBTNActionPerformed

    private void documentoFirmadoTXTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_documentoFirmadoTXTActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_documentoFirmadoTXTActionPerformed

    private void opcionesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_opcionesActionPerformed
        // TODO add your handling code here:
        JOptionPane.showMessageDialog(this, "aa", "Opciones", JOptionPane.OK_CANCEL_OPTION);
    }//GEN-LAST:event_opcionesActionPerformed

    private void infoDocumentosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoDocumentosActionPerformed
        // TODO add your handling code here:
        CardLayout card = (CardLayout)mainPanel.getLayout();
        System.out.println("Cambiar a firmadorVerificador ");
        card.show(mainPanel, "firmadorVerificador");
    }//GEN-LAST:event_infoDocumentosActionPerformed

    private void validarCertificadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validarCertificadosActionPerformed
        CardLayout card = (CardLayout)mainPanel.getLayout();
        System.out.println("Cambiar a validador ");
        card.show(mainPanel, "validador");
    }//GEN-LAST:event_validarCertificadosActionPerformed

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
            X509Certificate cert = (X509Certificate) ks.getCertificate(ks.aliases().nextElement());
            
            setearInfoValidacionCertificado(cert);
            
            List<String> ocspUrls = CertificateUtils.getAuthorityInformationAccess(cert);
            for (String ocsp : ocspUrls) {
                validarOCSPTxtArea.append("OCSP=" + ocsp + "\n");
                System.out.println("OCSP=" + ocsp);
            }
            boolean validezCert = OcspUtils.isValidCertificate(cert);
            System.out.println("Valid? " +validezCert );
            String resultStr;
            if(validezCert)
                resultStr = "Válido";
            else
                resultStr = "Inválido";
                
            validarOCSPTxtArea.append("Certificado: " + resultStr + "\n");
            
        } catch (KeyStoreException  | IOException | RubricaException ex) {
            //TODO botar error
            System.err.println("Error keysStore ");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
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
    private javax.swing.JButton abrirArchivoBtn;
    private javax.swing.JButton abrirArchivoPSKBtn;
    private javax.swing.JButton abrirCertificadoBtn;
    private javax.swing.JPasswordField certClaveTXT;
    private javax.swing.JTextField certificadoEmitidoATxt;
    private javax.swing.JTextField certificadoEmitidoPorTxt;
    private javax.swing.JTextField certificadoValidoDesdeTxt;
    private javax.swing.JTextField certificadoValidoHastaTxt;
    private javax.swing.JLabel certificadoVldCertLbl;
    private javax.swing.JLabel certificadoVldCertLbl1;
    private javax.swing.JTree certificadosJTR;
    private javax.swing.JPasswordField claveTXT;
    private javax.swing.JTextField documentoFirmadoTXT;
    private javax.swing.JButton firmarBTN;
    private javax.swing.JRadioButton firmarLlaveRBTN;
    private javax.swing.JRadioButton firmarTokenRBTN;
    private javax.swing.JPanel firmarVerificarDocPanel;
    private javax.swing.JButton infoDocumentos;
    private javax.swing.JLabel jLabel1;
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
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton opciones;
    private javax.swing.JPanel panelMenuPDF;
    private javax.swing.JPanel panelVisorPDF;
    private javax.swing.JButton resetValidarFormBtn;
    private javax.swing.JTextField rutaCertificadoTxt;
    private javax.swing.JTextField rutaDocumentoTXT;
    private javax.swing.JTextField rutaLlaveTXT;
    private javax.swing.JScrollPane scrollPaneMenuPDF;
    private javax.swing.JScrollPane scrollPaneVisorPDF;
    private javax.swing.ButtonGroup tipoFirmaBtnGRP;
    private javax.swing.JButton validarCRLBtn;
    private javax.swing.JTextArea validarCRLTxtArea;
    private javax.swing.JPanel validarCertificadoPanel;
    private javax.swing.JButton validarCertificados;
    private javax.swing.JButton validarOCSPBtn;
    private javax.swing.JTextArea validarOCSPTxtArea;
    private javax.swing.JScrollPane verificadoPorSPL;
    private javax.swing.JButton verificarBTN;
    private javax.swing.JButton verificarBTN1;
    private javax.swing.JPanel visorPdfPanel;
    // End of variables declaration//GEN-END:variables
}
