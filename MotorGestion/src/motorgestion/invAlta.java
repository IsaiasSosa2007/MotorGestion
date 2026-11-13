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
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author User
 */
public class invAlta extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(invAlta.class.getName());

    // ------------------------------------------------------------
    // Campos del formulario (se arman por codigo en armarFormulario)
    // ------------------------------------------------------------
    private static final NumberFormat PESOS = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"));

    private final JTextField txtNombre = new JTextField(22);
    private final JComboBox<String> cbxProveedor = new JComboBox<>();
    /** JSpinner: campo numerico con flechitas para subir y bajar. */
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
    private final JSpinner spnPrecio = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000000.0, 500.0));
    private final JLabel lblValor = new JLabel();

    /** Repuesto que se esta cargando (nuevo) o editando. */
    private final Repuesto repuesto;
    /** Lo que se hace despues de guardar, por ejemplo refrescar el inventario. */
    private final Runnable alGuardar;

    /** Formulario vacio, para cargar un repuesto nuevo. */
    public invAlta() {
        this(null, null);
    }

    /**
     * Formulario de repuesto.
     * @param r repuesto a editar, o null para cargar uno nuevo
     * @param alGuardar accion a ejecutar despues de guardar (puede ser null)
     */
    public invAlta(Repuesto r, Runnable alGuardar) {
        initComponents();
        this.repuesto = (r != null) ? r : new Repuesto();
        this.alGuardar = alGuardar;
        // Cerrar este formulario no tiene que cerrar todo el programa
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(r == null ? "MotorGestion - Nuevo repuesto" : "MotorGestion - Editar repuesto");
        armarFormulario(r == null ? "NUEVO REPUESTO" : "EDITAR REPUESTO");
        cargarProveedores();
        if (r != null) {
            txtNombre.setText(r.getNombre());
            spnCantidad.setValue(r.getCantidad());
            spnPrecio.setValue(r.getPrecio());
            for (int i = 0; i < cbxProveedor.getItemCount(); i++) {
                if (cbxProveedor.getItemAt(i).startsWith(r.getIdProveedor() + " - ")) {
                    cbxProveedor.setSelectedIndex(i);
                    break;
                }
            }
        }
        actualizarValor();
    }

    /** Arma los campos y botones del formulario. */
    private void armarFormulario(String titulo) {
        getContentPane().removeAll();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(22, 26, 22, 26));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 5, 6, 5);
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

        // El precio se muestra con dos decimales
        spnPrecio.setEditor(new JSpinner.NumberEditor(spnPrecio, "#,##0.00"));

        int fila = 1;
        fila = agregarFila(panel, g, fila, "Repuesto:", txtNombre);
        fila = agregarFila(panel, g, fila, "Proveedor:", cbxProveedor);
        fila = agregarFila(panel, g, fila, "Unidades en stock:", spnCantidad);
        fila = agregarFila(panel, g, fila, "Precio por unidad ($):", spnPrecio);

        // Recuadro que muestra el valor total, calculado en el momento
        lblValor.setFont(lblValor.getFont().deriveFont(Font.BOLD, 14f));
        JPanel recuadro = new JPanel(new BorderLayout());
        recuadro.setBackground(Estilo.SUPERFICIE);
        recuadro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilo.BORDE),
                new EmptyBorder(10, 12, 10, 12)));
        recuadro.add(lblValor, BorderLayout.CENTER);
        g.gridx = 0;
        g.gridy = fila++;
        g.gridwidth = 2;
        g.insets = new Insets(12, 5, 6, 5);
        panel.add(recuadro, g);

        JButton btnGuardar = new JButton("Guardar");
        Estilo.botonPrimario(btnGuardar);
        JButton btnCancelar = new JButton("Cancelar");
        Estilo.botonSecundario(btnCancelar);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnCancelar);
        botones.add(btnGuardar);
        g.gridy = fila;
        g.insets = new Insets(16, 5, 5, 5);
        panel.add(botones, g);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);

        // Cada vez que cambian la cantidad o el precio, se recalcula el valor
        spnCantidad.addChangeListener(e -> actualizarValor());
        spnPrecio.addChangeListener(e -> actualizarValor());
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

    /** Muestra "unidades x precio" en el recuadro. */
    private void actualizarValor() {
        int cantidad = ((Number) spnCantidad.getValue()).intValue();
        double precio = ((Number) spnPrecio.getValue()).doubleValue();
        lblValor.setText("Valor en stock:  " + PESOS.format(cantidad * precio));
    }

    /** Carga el combo con los proveedores de la base ("1 - MotorSpot"). */
    private void cargarProveedores() {
        try {
            for (String proveedor : Repuesto.listarProveedores()) {
                cbxProveedor.addItem(proveedor);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los proveedores:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Valida los datos, los pasa al Repuesto y lo guarda en la base. */
    private void guardar() {
        String nombre = txtNombre.getText().trim();
        int cantidad = ((Number) spnCantidad.getValue()).intValue();
        double precio = ((Number) spnPrecio.getValue()).doubleValue();

        // 1. Validaciones (el largo es el de la tabla "repuestos")
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribí el nombre del repuesto.");
            txtNombre.requestFocus();
            return;
        }
        if (nombre.length() > 50) {
            JOptionPane.showMessageDialog(this, "El nombre puede tener hasta 50 caracteres.");
            return;
        }
        if (cbxProveedor.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Elegí el proveedor.\nSi no aparece, primero cargalo en Proveedores.");
            return;
        }
        if (precio <= 0) {
            JOptionPane.showMessageDialog(this, "El precio tiene que ser mayor a cero.");
            return;
        }

        // 2. Pasar los datos al objeto
        repuesto.setNombre(nombre);
        repuesto.setCantidad(cantidad);
        repuesto.setPrecio(precio);
        repuesto.setIdProveedor(Integer.parseInt(cbxProveedor.getSelectedItem().toString().split(" - ")[0]));

        // 3. Guardar en la base
        try {
            repuesto.guardar();
            JOptionPane.showMessageDialog(this, "Repuesto guardado.");
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
        java.awt.EventQueue.invokeLater(() -> new invAlta().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
