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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author User
 */
public class provAlta extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(provAlta.class.getName());

    // ------------------------------------------------------------
    // Campos del formulario (se arman por codigo en armarFormulario)
    // ------------------------------------------------------------
    private final JTextField txtNombre = new JTextField(22);
    private final JTextField txtTelefono = new JTextField(22);
    private final JTextField txtDireccion = new JTextField(22);
    /** Combo editable: se elige una categoria existente o se escribe una nueva. */
    private final JComboBox<String> cbxCategoria = new JComboBox<>();

    /** Proveedor que se esta cargando (nuevo) o editando. */
    private final Proveedor proveedor;
    /** Lo que se hace despues de guardar, por ejemplo refrescar la lista. */
    private final Runnable alGuardar;

    /** Formulario vacio, para cargar un proveedor nuevo. */
    public provAlta() {
        this(null, null);
    }

    /**
     * Formulario de proveedor.
     * @param p proveedor a editar, o null para cargar uno nuevo
     * @param alGuardar accion a ejecutar despues de guardar (puede ser null)
     */
    public provAlta(Proveedor p, Runnable alGuardar) {
        initComponents();
        this.proveedor = (p != null) ? p : new Proveedor();
        this.alGuardar = alGuardar;
        // Cerrar este formulario no tiene que cerrar todo el programa
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(p == null ? "MotorGestion - Nuevo proveedor" : "MotorGestion - Editar proveedor");
        armarFormulario(p == null ? "NUEVO PROVEEDOR" : "EDITAR PROVEEDOR");
        cargarCategorias();
        if (p != null) {
            txtNombre.setText(p.getNombre());
            txtTelefono.setText(p.getTelefono());
            txtDireccion.setText(p.getDireccion());
            cbxCategoria.setSelectedItem(p.getCategoria());
        } else {
            cbxCategoria.setSelectedItem("");
        }
    }

    /** Arma los campos y botones del formulario. */
    private void armarFormulario(String titulo) {
        getContentPane().removeAll();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 18f));
        lblTitulo.setForeground(Estilo.PRIMARIO);
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        panel.add(lblTitulo, g);
        g.gridwidth = 1;

        cbxCategoria.setEditable(true);
        int fila = 1;
        fila = agregarFila(panel, g, fila, "Empresa:", txtNombre);
        fila = agregarFila(panel, g, fila, "Teléfono:", txtTelefono);
        fila = agregarAyuda(panel, g, fila, "Solo números, sin espacios ni guiones (ej. 1133446543)");
        fila = agregarFila(panel, g, fila, "Dirección:", txtDireccion);
        fila = agregarAyuda(panel, g, fila, "Calle, número y localidad");
        fila = agregarFila(panel, g, fila, "Categoría:", cbxCategoria);
        fila = agregarAyuda(panel, g, fila, "Elegí una existente o escribí una nueva");

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
        g.insets = new Insets(16, 5, 5, 5);
        panel.add(botones, g);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);

        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnGuardar);   // Enter = Guardar

        // Primero setResizable y despues pack: al reves, en Windows la ventana
        // queda unos pixeles mas chica y los botones de abajo quedan pegados al borde
        setResizable(false);
        pack();
    }

    /** Agrega una fila "etiqueta + campo" y devuelve la fila siguiente. */
    private int agregarFila(JPanel panel, GridBagConstraints g, int fila, String texto, JComponent campo) {
        g.gridx = 0;
        g.gridy = fila;
        g.weightx = 0;
        panel.add(new JLabel(texto), g);
        g.gridx = 1;
        g.weightx = 1;
        panel.add(campo, g);
        return fila + 1;
    }

    /** Agrega un texto de ayuda chico y gris debajo del campo anterior. */
    private int agregarAyuda(JPanel panel, GridBagConstraints g, int fila, String texto) {
        JLabel ayuda = new JLabel(texto);
        ayuda.setForeground(Estilo.TEXTO_SUAVE);
        ayuda.setFont(ayuda.getFont().deriveFont(12f));
        Insets anteriores = g.insets;
        g.insets = new Insets(0, 5, 6, 5);
        g.gridx = 1;
        g.gridy = fila;
        panel.add(ayuda, g);
        g.insets = anteriores;
        return fila + 1;
    }

    /** Carga en el combo las categorias que ya existen. */
    private void cargarCategorias() {
        try {
            for (String categoria : Proveedor.listarCategorias()) {
                cbxCategoria.addItem(categoria);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar las categorías:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Valida los datos, los pasa al Proveedor y lo guarda en la base. */
    private void guardar() {
        String nombre = txtNombre.getText().trim();
        String telefono = txtTelefono.getText().trim().replace(" ", "").replace("-", "");
        String direccion = txtDireccion.getText().trim();
        Object elegida = cbxCategoria.getEditor().getItem();
        String categoria = elegida == null ? "" : elegida.toString().trim();

        // 1. Validaciones (los largos son los de la tabla "proveedor")
        if (nombre.isEmpty() || telefono.isEmpty() || direccion.isEmpty() || categoria.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completá empresa, teléfono, dirección y categoría.");
            return;
        }
        if (!telefono.matches("\\d{6,11}")) {
            JOptionPane.showMessageDialog(this, "El teléfono tiene que tener entre 6 y 11 números.");
            txtTelefono.requestFocus();
            return;
        }
        if (nombre.length() > 100 || direccion.length() > 100) {
            JOptionPane.showMessageDialog(this, "La empresa y la dirección pueden tener hasta 100 caracteres.");
            return;
        }
        if (categoria.length() > 45) {
            JOptionPane.showMessageDialog(this, "La categoría puede tener hasta 45 caracteres.");
            return;
        }

        // 2. Pasar los datos al objeto
        proveedor.setNombre(nombre);
        proveedor.setTelefono(telefono);
        proveedor.setDireccion(direccion);
        proveedor.setCategoria(categoria);

        // 3. Guardar en la base
        try {
            proveedor.guardar();
            JOptionPane.showMessageDialog(this, "Proveedor guardado.");
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
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
        java.awt.EventQueue.invokeLater(() -> new provAlta().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
