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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author User
 */
public class empAlta extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(empAlta.class.getName());

    // ------------------------------------------------------------
    // Campos del formulario (se arman por codigo en armarFormulario)
    // ------------------------------------------------------------
    private static final String TEXTO_ADMIN = "Administrador";
    private static final String TEXTO_EMPLEADO = "Empleado";

    // Seccion "Datos personales"
    private final JTextField txtNombre = new JTextField(22);
    private final JTextField txtDni = new JTextField(22);
    private final JTextField txtTelefono = new JTextField(22);
    // Seccion "Acceso al sistema"
    private final JCheckBox chkAcceso = new JCheckBox("Puede ingresar al sistema");
    private final JTextField txtUsuario = new JTextField(22);
    private final JComboBox<String> cbxRol = new JComboBox<>(new String[]{TEXTO_EMPLEADO, TEXTO_ADMIN});
    private final JPasswordField txtClave = new JPasswordField(22);
    private final JPasswordField txtRepetir = new JPasswordField(22);
    private final JLabel lblAyudaClave = new JLabel();

    /** Empleado que se esta cargando (nuevo) o editando. */
    private final Empleado empleado;
    /** Lo que se hace despues de guardar, por ejemplo refrescar la lista. */
    private final Runnable alGuardar;
    /** Si al abrir el formulario el empleado era administrador. */
    private final boolean eraAdmin;

    /** Formulario vacio, para cargar un empleado nuevo. */
    public empAlta() {
        this(null, null);
    }

    /**
     * Formulario de empleado.
     * @param emp empleado a editar, o null para cargar uno nuevo
     * @param alGuardar accion a ejecutar despues de guardar (puede ser null)
     */
    public empAlta(Empleado emp, Runnable alGuardar) {
        initComponents();
        this.empleado = (emp != null) ? emp : new Empleado();
        this.alGuardar = alGuardar;
        this.eraAdmin = emp != null && emp.esAdmin();
        // Cerrar este formulario no tiene que cerrar todo el programa
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(emp == null ? "MotorGestion - Nuevo empleado" : "MotorGestion - Editar empleado");
        armarFormulario(emp == null ? "NUEVO EMPLEADO" : "EDITAR EMPLEADO");
        if (emp != null) {
            txtNombre.setText(emp.getNombre());
            txtDni.setText(emp.getDni());
            txtTelefono.setText(emp.getTelefono());
            chkAcceso.setSelected(emp.tieneAcceso());
            if (emp.tieneAcceso()) {
                txtUsuario.setText(emp.getUsuario());
                cbxRol.setSelectedItem(emp.esAdmin() ? TEXTO_ADMIN : TEXTO_EMPLEADO);
            }
        }
        actualizarSeccionAcceso();
    }

    /** Arma las dos secciones del formulario y los botones. */
    private void armarFormulario(String titulo) {
        getContentPane().removeAll();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(22, 26, 22, 26));
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 18f));
        lblTitulo.setForeground(Estilo.PRIMARIO);
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        g.insets = new Insets(0, 5, 10, 5);
        panel.add(lblTitulo, g);

        int fila = 1;
        fila = agregarSeccion(panel, g, fila, "DATOS PERSONALES");
        fila = agregarFila(panel, g, fila, "Nombre y apellido:", txtNombre);
        fila = agregarFila(panel, g, fila, "DNI:", txtDni);
        fila = agregarFila(panel, g, fila, "Teléfono:", txtTelefono);

        fila = agregarSeccion(panel, g, fila, "ACCESO AL SISTEMA");
        g.gridx = 0;
        g.gridy = fila++;
        g.gridwidth = 2;
        g.insets = new Insets(4, 5, 4, 5);
        panel.add(chkAcceso, g);
        g.gridwidth = 1;
        fila = agregarFila(panel, g, fila, "Usuario:", txtUsuario);
        fila = agregarFila(panel, g, fila, "Rol:", cbxRol);
        fila = agregarFila(panel, g, fila, "Contraseña:", txtClave);
        fila = agregarFila(panel, g, fila, "Repetir contraseña:", txtRepetir);
        lblAyudaClave.setForeground(Estilo.TEXTO_SUAVE);
        lblAyudaClave.setFont(lblAyudaClave.getFont().deriveFont(12f));
        g.gridx = 1;
        g.gridy = fila++;
        g.insets = new Insets(0, 5, 4, 5);
        panel.add(lblAyudaClave, g);

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

        chkAcceso.addActionListener(e -> actualizarSeccionAcceso());
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnGuardar);   // Enter = Guardar

        // Primero setResizable y despues pack (ver comentario en vehiAlta)
        setResizable(false);
        pack();
    }

    /** Titulo chico y gris de una seccion, con una linea debajo. */
    private int agregarSeccion(JPanel panel, GridBagConstraints g, int fila, String texto) {
        JLabel seccion = new JLabel(texto);
        seccion.setFont(seccion.getFont().deriveFont(Font.BOLD, 12f));
        seccion.setForeground(Estilo.TEXTO_SUAVE);
        seccion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE),
                new EmptyBorder(0, 0, 4, 0)));
        g.gridx = 0;
        g.gridy = fila;
        g.gridwidth = 2;
        g.insets = new Insets(14, 5, 6, 5);
        panel.add(seccion, g);
        g.gridwidth = 1;
        return fila + 1;
    }

    /** Agrega una fila "etiqueta + campo" y devuelve la fila siguiente. */
    private int agregarFila(JPanel panel, GridBagConstraints g, int fila, String texto, JComponent campo) {
        g.insets = new Insets(5, 5, 5, 10);
        g.gridx = 0;
        g.gridy = fila;
        g.weightx = 0;
        panel.add(new JLabel(texto), g);
        g.gridx = 1;
        g.weightx = 1;
        panel.add(campo, g);
        return fila + 1;
    }

    /** Los campos de acceso solo se pueden usar si se tilda "Puede ingresar". */
    private void actualizarSeccionAcceso() {
        boolean acceso = chkAcceso.isSelected();
        txtUsuario.setEnabled(acceso);
        cbxRol.setEnabled(acceso);
        txtClave.setEnabled(acceso);
        txtRepetir.setEnabled(acceso);
        if (!acceso) {
            lblAyudaClave.setText("Sin usuario: no podrá entrar al programa.");
        } else if (empleado.tieneAcceso()) {
            lblAyudaClave.setText("Dejá la contraseña vacía para no cambiarla.");
        } else {
            lblAyudaClave.setText("Mínimo 6 caracteres. Se guarda cifrada.");
        }
    }

    /** Valida los datos, los pasa al Empleado y lo guarda en la base. */
    private void guardar() {
        String nombre = txtNombre.getText().trim();
        String dni = txtDni.getText().trim();
        String telefono = txtTelefono.getText().trim();
        boolean acceso = chkAcceso.isSelected();
        String usuario = txtUsuario.getText().trim();
        boolean admin = TEXTO_ADMIN.equals(cbxRol.getSelectedItem());
        String clave = new String(txtClave.getPassword());
        String repetir = new String(txtRepetir.getPassword());

        // 1. Datos personales (largos de la tabla "empleado")
        if (nombre.length() < 3 || nombre.length() > 100) {
            JOptionPane.showMessageDialog(this, "Escribí nombre y apellido (hasta 100 caracteres).");
            return;
        }
        if (!dni.matches("\\d{7,8}")) {
            JOptionPane.showMessageDialog(this, "El DNI tiene que tener 7 u 8 números, sin puntos.");
            return;
        }
        if (!telefono.matches("\\d{6,11}")) {
            JOptionPane.showMessageDialog(this, "El teléfono tiene que tener entre 6 y 11 números.");
            return;
        }

        // 2. Acceso al sistema
        String claveNueva = null;   // null = no se cambia la contrasena
        if (acceso) {
            if (!usuario.matches("[A-Za-z0-9._]{3,45}")) {
                JOptionPane.showMessageDialog(this, "El usuario debe tener entre 3 y 45 caracteres,\n"
                        + "sin espacios (letras, números, punto o guion bajo).");
                return;
            }
            boolean necesitaClave = !empleado.tieneAcceso();   // usuario nuevo: la clave es obligatoria
            if (necesitaClave || !clave.isEmpty()) {
                if (clave.length() < 6) {
                    JOptionPane.showMessageDialog(this, "La contraseña debe tener al menos 6 caracteres.");
                    return;
                }
                if (!clave.equals(repetir)) {
                    JOptionPane.showMessageDialog(this, "Las dos contraseñas no coinciden.");
                    txtRepetir.setText("");
                    return;
                }
                claveNueva = clave;
            }
        }

        try {
            if (acceso && Empleado.usuarioOcupado(usuario, empleado.getIdEmpleado())) {
                JOptionPane.showMessageDialog(this, "El usuario \"" + usuario + "\" ya lo usa otro empleado.");
                return;
            }
            // No se puede dejar el sistema sin ningun administrador
            boolean sigueAdmin = acceso && admin;
            if (eraAdmin && !sigueAdmin && Empleado.cantidadAdmins() <= 1) {
                JOptionPane.showMessageDialog(this, "Es el único administrador: no puede dejar de serlo.\n"
                        + "Primero dale el rol de administrador a otro empleado.");
                return;
            }

            // 3. Pasar los datos al objeto y guardar
            empleado.setNombre(nombre);
            empleado.setDni(dni);
            empleado.setTelefono(telefono);
            empleado.setUsuario(acceso ? usuario : null);
            empleado.setRol(acceso ? (admin ? Empleado.ROL_ADMIN : Empleado.ROL_EMPLEADO) : null);
            empleado.guardar(claveNueva);

            JOptionPane.showMessageDialog(this, "Empleado guardado.");
            if (alGuardar != null) {
                alGuardar.run();
            }
            dispose();
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

        jPanel1 = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jComboBox2 = new javax.swing.JComboBox<>();
        jComboBox3 = new javax.swing.JComboBox<>();
        jComboBox4 = new javax.swing.JComboBox<>();
        jComboBox5 = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Empleados", "Altas", "Bajas", "Modificaciones", "Consulta" }));
        jComboBox1.addActionListener(this::jComboBox1ActionPerformed);

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Clientes", "Altas", "Bajas", "Modificaciones", "Consulta" }));

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Vehiculos", "Altas", "Bajas", "Modificaciones", "Consultas" }));

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Proveedores", "Altas", "Bajas", "Modificaciones", "Consultas" }));

        jComboBox5.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Inventario", "Altas", "Bajas", "Modificaciones", "Consultas" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(241, 241, 241)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(63, 63, 63)
                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(63, 63, 63)
                .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(67, 67, 67)
                .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(72, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(56, 56, 56))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(6, 6, 6))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(135, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new empAlta().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JComboBox<String> jComboBox5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
