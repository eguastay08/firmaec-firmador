/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.firmadigital.firmador;

import java.awt.GridLayout;
import java.util.List;
import javax.swing.JFileChooser;
import java.io.File; 
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author jdcalle
 */
public class Main extends javax.swing.JFrame {
    
    
    private File documento;
    private File llave;
    private File ultimaCarpeta;
    private String mensajeError;
    private static String RUTA_IMG = "src/main/resources/images/";
    private static String CHECK_IMG = "CheckIcon.png";
    private static String DELETE_IMG = "Deleteicon.png";
    private final List<String> extensionesPermitidas; 
    private final FileNameExtensionFilter filtros = new FileNameExtensionFilter("Documentos de Oficina", "pdf","docx","xlsx","pptx","odt","ods","odp");
    private static String OS = System.getProperty("os.name").toLowerCase();
  
    
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
    
    private File abrirArchivo(FileNameExtensionFilter filtro){
        final JFileChooser fileChooser = new JFileChooser();
        File archivo;
        if(filtro != null){
            fileChooser.setFileFilter(filtro);
        }
        fileChooser.setCurrentDirectory(ultimaCarpeta);
        fileChooser.setFileFilter(filtros); 
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            archivo = fileChooser.getSelectedFile();
            if(archivo.exists())
                System.out.println("existe");
            System.out.println("entro");
            ultimaCarpeta = fileChooser.getCurrentDirectory();
            return archivo;
        } 
        return null;
    }
    
    private File abrirArchivo(){
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
    
    private void resetForm(){
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
        
    }
    
    private void selFirmarConArchivo(){
        this.firmarBTN.setEnabled(true);
        this.rutaLlaveTXT.setEnabled(true);
        this.abrirArchivoPSKBtn.setEnabled(true);
        // Si es windows no hay que habilitar el campo de contraseña
        if(!esWindows()){
            this.claveTXT.setEnabled(true);
        }else{
            this.claveTXT.setEnabled(false);
        }
    }
    
    private void selFirmarConToken(){
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
    private boolean validacionPreFirmar(){
        //Revisamos si existe el documento a firmar
        // TODO no hacer un return directamente, se podria validar todos los parametros e ir aumentando los errores
        if(!documento.exists()){
            this.mensajeError = "El documento no existe en la ruta";
            return false;
        }
        if(firmarLlaveRBTN.isSelected() && !llave.exists()){
            this.mensajeError = "La llave no existe en la ruta";
            return false;
        }
        // Si firma con token debe
        if(firmarTokenRBTN.isSelected() && !hayToken()){
            this.mensajeError = "No hay token conectado";
            return false;
        }
        return tipoDeDocumentPermitido(documento.getAbsolutePath());
        
    }
    
    // Si existe el archivo
    private boolean archivoEnRutaExiste(JTextField jtextField){
        if(jtextField == null || jtextField.getText().isEmpty() ){
            return false;
        }
        File archivo = new File(jtextField.getText());
        
        return archivo.exists();
    }
    
    //Revisa si hay token conectado al usb
    private boolean hayToken(){
        // TODO conectar a rubica para validar si hay token
        return true;
    }
    
    /*
    Vemos si el documento existe
    */
    private boolean verificarDocumento(){
        // Vemos si existe
        if(documento == null || !documento.exists() )
            return false;
        // Vemos si es un documento permitido primero
        if(!tipoDeDocumentPermitido(documento.getAbsolutePath()))
            return false;
        
        // Vemos los procesos por documento
        String extDocumento = FilenameUtils.getExtension(documento.getAbsolutePath());
      
        // TODO arreglar manejo de errores
        switch(extDocumento){
            case "pdf":
                return verificarPDF(documento);
            case "docx":
            case "xlsx":
            case "pptx":
                return verificarMSOffice(documento);
            case "odt":
            case "ods":
            case "odp":
                return verificarLibreOffice(documento);
            default:
                return false;
        }
        
    }
    
    private boolean verificarPDF(File documento){
        // TODO conectar a rubica, recibir informacion
        for(int i=0;i<10; i++){
            this.verificadoPorJPL.add(new JLabel("Certificado por: "));
            this.verificadoPorJPL.add(new JLabel("Unidad Certificadora Uno"));
            this.verificadoPorJPL.add(new JLabel("Fecha:"));
            this.verificadoPorJPL.add(new JLabel("2012/10/6"));
        }
        return true;
    }
    
    private boolean verificarMSOffice(File documento){
        // TODO conectar a rubica, recibir informacion
        return true;
    }
    private boolean verificarLibreOffice(File documento){
        // TODO conectar a rubica, recibir informacion
        return true;
    }
    
    // Se podria verificar el mimetype
    private boolean tipoDeDocumentPermitido(String rutaDocumento){
        String extDocumento = FilenameUtils.getExtension(rutaDocumento);
        return extensionesPermitidas.stream().anyMatch((extension) -> (extension.equals(extDocumento)));
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
        jLabel5 = new javax.swing.JLabel();
        fechaVerificacionTXT = new javax.swing.JTextField();
        verificadoPorSPL = new javax.swing.JScrollPane();
        verificadoPorJPL = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Verificador - Firmador");

        jLabel1.setText("Documento");

        tipoFirmaBtnGRP.add(firmarLlaveRBTN);
        firmarLlaveRBTN.setText("Firmar con Archivo PSK");
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        jLabel5.setText("Fecha Verificación");

        fechaVerificacionTXT.setEnabled(false);
        fechaVerificacionTXT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fechaVerificacionTXTActionPerformed(evt);
            }
        });

        verificadoPorJPL.setName(""); // NOI18N
        verificadoPorJPL.setLayout(new java.awt.GridLayout(1, 2));
        verificadoPorSPL.setViewportView(verificadoPorJPL);

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
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(rutaLlaveTXT)
                                    .addComponent(claveTXT)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(fechaVerificacionTXT, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 294, Short.MAX_VALUE))
                                    .addComponent(verificadoPorSPL)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(54, 54, 54)
                                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(fechaVerificacionTXT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(verificadoPorSPL, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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

    private void fechaVerificacionTXTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fechaVerificacionTXTActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fechaVerificacionTXTActionPerformed

    private void abrirArchivoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abrirArchivoBtnActionPerformed
        // Por defecto desbloqueamos el objecto de verificar
        documento = abrirArchivo(filtros);
        if(documento != null){
            rutaDocumentoTXT.setText(documento.getAbsolutePath());
            verificarBTN.setEnabled(true);
        }
    }//GEN-LAST:event_abrirArchivoBtnActionPerformed

    private void abrirArchivoPSKBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abrirArchivoPSKBtnActionPerformed
        llave = abrirArchivo();
        if(llave != null){
            rutaLlaveTXT.setText(llave.getAbsolutePath());
        }        
    }//GEN-LAST:event_abrirArchivoPSKBtnActionPerformed

    private void verificarBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verificarBTNActionPerformed
        // Si lo del documento es distinto a lo de la ruta seteamos la ruta
        // del documento a la del textfield
        if(!documento.getAbsolutePath().equals(rutaDocumentoTXT.getText())){
            documento = new File(rutaDocumentoTXT.getText());
        }
        verificarDocumento();
    }//GEN-LAST:event_verificarBTNActionPerformed

    private void verificarBTN1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verificarBTN1ActionPerformed
        this.resetForm();
    }//GEN-LAST:event_verificarBTN1ActionPerformed

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
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton abrirArchivoBtn;
    private javax.swing.JButton abrirArchivoPSKBtn;
    private javax.swing.JPasswordField claveTXT;
    private javax.swing.JTextField fechaVerificacionTXT;
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
    private javax.swing.JPanel verificadoPorJPL;
    private javax.swing.JScrollPane verificadoPorSPL;
    private javax.swing.JButton verificarBTN;
    private javax.swing.JButton verificarBTN1;
    // End of variables declaration//GEN-END:variables
}
