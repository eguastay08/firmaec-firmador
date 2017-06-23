/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.firmadigital.firmador;

import ec.gob.firmadigital.cliente.FirmaDigital;
import ec.gob.firmadigital.cliente.VerificadorDigital;
import ec.gob.firmadigital.exceptions.DocumentoNoExistenteException;
import ec.gob.firmadigital.exceptions.DocumentoNoPermitido;
import ec.gob.firmadigital.exceptions.TokenNoConectadoException;
import ec.gob.firmadigital.exceptions.TokenNoEncontrado;
import ec.gob.firmadigital.utils.FirmadorFileUtils;
import java.awt.Component;
import java.util.List;
import javax.swing.JFileChooser;
import java.io.File;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
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
import io.rubrica.keystore.KeyStoreProviderFactory;import io.rubrica.ocsp.OcspValidationException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
;

/**
 *
 * @author jdcalle
 */
public class Main extends javax.swing.JFrame {

    private KeyStore ks;
    private File documento;
    private File llave;
    private File ultimaCarpeta;
    private String mensajeError;
    private static final String RUTA_IMG = "images/";
    private static final String CHECK_IMG = "CheckIcon.png";
    private static final String NOTCHECK_IMG = "DeleteIcon.png";
    private final List<String> extensionesPermitidas;
    private final FileNameExtensionFilter filtros = new FileNameExtensionFilter("Documentos de Oficina", "pdf", "docx", "xlsx", "pptx", "odt", "ods", "odp");
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private final ImageIcon checkIcon = new ImageIcon(ClassLoader.getSystemResource(RUTA_IMG + CHECK_IMG));
    private final ImageIcon notCheckIcon = new ImageIcon(ClassLoader.getSystemResource(RUTA_IMG + NOTCHECK_IMG));

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
    private boolean verificarDocumento() throws DocumentoNoPermitido, IOException, KeyStoreException, OcspValidationException, SignatureException {
        // Vemos si existe
        System.out.println("Verificando Docs");
        if (documento == null || !documento.exists()) {
            return false;
        }
        // Vemos si es un documento permitido primero
        tipoDeDocumentPermitido(documento);
        
        VerificadorDigital verificadorDigital = new VerificadorDigital();
        
        List<Certificado> certs = verificadorDigital.verificar(documento);
        
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
                     * y que pertenezca a una entidad reconocida
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tipoFirmaBtnGRP = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        firmarLlaveRBTN = new javax.swing.JRadioButton();
        firmarTokenRBTN = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        rutaDocumentoTXT = new javax.swing.JTextField();
        abrirArchivoBtn = new javax.swing.JButton();
        rutaLlaveTXT = new javax.swing.JTextField();
        abrirArchivoPSKBtn = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        claveTXT = new javax.swing.JPasswordField();
        jPanel1 = new javax.swing.JPanel();
        firmarBTN = new javax.swing.JButton();
        verificarBTN = new javax.swing.JButton();
        verificarBTN1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        verificadoPorSPL = new javax.swing.JScrollPane();
        certificadosJTR = new javax.swing.JTree();
        jLabel5 = new javax.swing.JLabel();
        documentoFirmadoTXT = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Verificador - Firmador");

        jLabel1.setText("Documento");

        tipoFirmaBtnGRP.add(firmarLlaveRBTN);
        firmarLlaveRBTN.setText("Firmar con Llave PKC");
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

        jLabel2.setText("Llave");

        rutaDocumentoTXT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rutaDocumentoTXTActionPerformed(evt);
            }
        });

        abrirArchivoBtn.setText("Abrir");
        abrirArchivoBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abrirArchivoBtnActionPerformed(evt);
            }
        });

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(firmarBTN)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(verificarBTN)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(verificarBTN1)
                .addContainerGap(128, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(35, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(firmarBTN)
                    .addComponent(verificarBTN)
                    .addComponent(verificarBTN1))
                .addContainerGap())
        );

        jLabel4.setText("Verificado Por");

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Firmas");
        certificadosJTR.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        verificadoPorSPL.setViewportView(certificadosJTR);

        jLabel5.setText("Doc. Firmado");

        documentoFirmadoTXT.setEditable(false);
        documentoFirmadoTXT.setEnabled(false);
        documentoFirmadoTXT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                documentoFirmadoTXTActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rutaDocumentoTXT))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel5))
                                .addGap(39, 39, 39)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(rutaLlaveTXT)
                                    .addComponent(claveTXT)
                                    .addComponent(verificadoPorSPL)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(54, 54, 54)
                                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(documentoFirmadoTXT))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(abrirArchivoBtn)
                            .addComponent(abrirArchivoPSKBtn)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(firmarLlaveRBTN)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(firmarTokenRBTN)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(rutaDocumentoTXT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(abrirArchivoBtn)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(firmarLlaveRBTN)
                    .addComponent(firmarTokenRBTN))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(rutaLlaveTXT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(abrirArchivoPSKBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(claveTXT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(documentoFirmadoTXT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(verificadoPorSPL, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
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
    private javax.swing.JTree certificadosJTR;
    private javax.swing.JPasswordField claveTXT;
    private javax.swing.JTextField documentoFirmadoTXT;
    private javax.swing.JButton firmarBTN;
    private javax.swing.JRadioButton firmarLlaveRBTN;
    private javax.swing.JRadioButton firmarTokenRBTN;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField rutaDocumentoTXT;
    private javax.swing.JTextField rutaLlaveTXT;
    private javax.swing.ButtonGroup tipoFirmaBtnGRP;
    private javax.swing.JScrollPane verificadoPorSPL;
    private javax.swing.JButton verificarBTN;
    private javax.swing.JButton verificarBTN1;
    // End of variables declaration//GEN-END:variables
}
