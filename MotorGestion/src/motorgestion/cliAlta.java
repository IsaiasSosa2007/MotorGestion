/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package motorgestion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author User
 */
public class cliAlta extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(cliAlta.class.getName());

    // ------------------------------------------------------------
    // Campos del formulario (se arman por codigo en armarFormulario)
    // ------------------------------------------------------------

    private final JTextField txtNombre = new JTextField(24);
    private final JTextField txtDni = new JTextField(24);
    private final JTextField txtTelefono = new JTextField(24);
    // Mensajes debajo de cada campo: se ponen verdes o rojos mientras se escribe
    private final JLabel lblAvisoNombre = new JLabel();
    private final JLabel lblAvisoDni = new JLabel();
    private final JLabel lblAvisoTelefono = new JLabel();

    /** Cliente que se esta cargando (nuevo) o editando. */
    private final Cliente cliente;
    /** Lo que se hace despues de guardar, por ejemplo redibujar las tarjetas. */
    private final Runnable alGuardar;

    /** Formulario vacio, para cargar un cliente nuevo. */
    public cliAlta() {
        this(null, null);
    }

    /**
     * Formulario de cliente.
     * @param c cliente a editar, o null para cargar uno nuevo
     * @param alGuardar accion a ejecutar despues de guardar (puede ser null)
     */
    public cliAlta(Cliente c, Runnable alGuardar) {
        initComponents();
        this.cliente = (c != null) ? c : new Cliente();
        this.alGuardar = alGuardar;
        // Cerrar este formulario no tiene que cerrar todo el programa
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(c == null ? "MotorGestion - Nuevo cliente" : "MotorGestion - Editar cliente");
        armarFormulario(c == null ? "NUEVO CLIENTE" : "EDITAR CLIENTE");
        if (c != null) {
            txtNombre.setText(c.getNombre());
            txtDni.setText(c.getDni());
            txtTelefono.setText(c.getTelefono());
        }
        validarEnVivo();
    }

    /** Arma los campos y botones del formulario. */
    private void armarFormulario(String titulo) {
        getContentPane().removeAll();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 18f));
        lblTitulo.setForeground(Estilo.PRIMARIO);
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        g.insets = new Insets(0, 5, 14, 5);
        panel.add(lblTitulo, g);
        g.gridwidth = 1;

        int fila = 1;
        fila = agregarCampo(panel, g, fila, "Nombre y apellido:", txtNombre, lblAvisoNombre);
        fila = agregarCampo(panel, g, fila, "DNI:", txtDni, lblAvisoDni);
        fila = agregarCampo(panel, g, fila, "Teléfono:", txtTelefono, lblAvisoTelefono);

        JButton btnGuardar = new JButton("Guardar");
        Estilo.botonPrimario(btnGuardar);
        JButton btnCancelar = new JButton("Cancelar");
        Estilo.botonSecundario(btnCancelar);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnCancelar);
        botones.add(btnGuardar);
        g.gridx = 0;
        g.gridy = fila;
        g.gridwidth = 2;
        g.insets = new Insets(18, 5, 4, 5);
        panel.add(botones, g);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);

        // Cada vez que se escribe en un campo, se revisan los datos
        DocumentListener alEscribir = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { validarEnVivo(); }
            @Override public void removeUpdate(DocumentEvent e) { validarEnVivo(); }
            @Override public void changedUpdate(DocumentEvent e) { validarEnVivo(); }
        };
        txtNombre.getDocument().addDocumentListener(alEscribir);
        txtDni.getDocument().addDocumentListener(alEscribir);
        txtTelefono.getDocument().addDocumentListener(alEscribir);

        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnGuardar);   // Enter = Guardar

        // Primero setResizable y despues pack: al reves, en Windows la ventana
        // queda unos pixeles mas chica y los botones de abajo quedan pegados al borde
        setResizable(false);
        pack();
    }

    /** Agrega "etiqueta + campo" y debajo el mensaje de aviso. Devuelve la fila siguiente. */
    private int agregarCampo(JPanel panel, GridBagConstraints g, int fila, String texto,
            JTextField campo, JLabel aviso) {
        g.insets = new Insets(5, 5, 0, 10);
        g.gridx = 0;
        g.gridy = fila;
        g.weightx = 0;
        panel.add(new JLabel(texto), g);
        g.gridx = 1;
        g.weightx = 1;
        panel.add(campo, g);

        aviso.setFont(aviso.getFont().deriveFont(12f));
        // Un espacio para que el renglon ya ocupe su alto: si arranca vacio, al
        // escribir el aviso la ventana crece y los botones quedan pegados al borde
        aviso.setText(" ");
        g.insets = new Insets(2, 5, 8, 5);
        g.gridy = fila + 1;
        panel.add(aviso, g);
        return fila + 2;
    }

    // ------------------------------------------------------------
    // Validaciones (los largos son los de la tabla "cliente")
    // ------------------------------------------------------------
    private boolean nombreValido() {
        String nombre = txtNombre.getText().trim();
        return nombre.length() >= 3 && nombre.length() <= 100;
    }

    private boolean dniValido() {
        return txtDni.getText().trim().matches("\\d{7,8}");
    }

    private boolean telefonoValido() {
        return txtTelefono.getText().trim().matches("\\d{6,11}");
    }

    /** Pinta cada aviso de verde (bien) o rojo (falta algo) mientras se escribe. */
    private void validarEnVivo() {
        avisar(lblAvisoNombre, nombreValido(), "Nombre completo", "Escribí nombre y apellido");
        avisar(lblAvisoDni, dniValido(), "DNI válido", "7 u 8 números, sin puntos");
        avisar(lblAvisoTelefono, telefonoValido(), "Teléfono válido", "Entre 6 y 11 números, sin espacios");
    }

    private void avisar(JLabel aviso, boolean bien, String textoBien, String textoMal) {
        aviso.setText(bien ? textoBien : textoMal);
        aviso.setForeground(bien ? Estilo.EXITO : Estilo.PELIGRO);
    }

    /** Si todo es valido, pasa los datos al Cliente y lo guarda en la base. */
    private void guardar() {
        if (!nombreValido() || !dniValido() || !telefonoValido()) {
            JOptionPane.showMessageDialog(this, "Revisá los campos marcados en rojo.");
            return;
        }
        cliente.setNombre(txtNombre.getText().trim());
        cliente.setDni(txtDni.getText().trim());
        cliente.setTelefono(txtTelefono.getText().trim());
        try {
            cliente.guardar();
            JOptionPane.showMessageDialog(this, "Cliente guardado.");
            if (alGuardar != null) {
                alGuardar.run();
            }
            dispose();
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "Ya hay un cliente con ese DNI.",
                    "Dato repetido", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
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

        panelMenu2 = new motorgestion.panelMenu();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("NASHEEEEEEEEEEE");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(panelMenu2, javax.swing.GroupLayout.PREFERRED_SIZE, 863, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(208, 208, 208)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelMenu2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 57, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new cliAlta().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private motorgestion.panelMenu panelMenu2;
    // End of variables declaration//GEN-END:variables
}
