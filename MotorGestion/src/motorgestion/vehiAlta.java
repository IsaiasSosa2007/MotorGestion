/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package motorgestion;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
public class vehiAlta extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(vehiAlta.class.getName());

    // ------------------------------------------------------------
    // Campos del formulario (se arman por codigo en armarFormulario)
    // ------------------------------------------------------------
    private final JTextField txtMatricula = new JTextField(18);
    private final JTextField txtMarca = new JTextField(18);
    private final JTextField txtModelo = new JTextField(18);
    private final JTextField txtColor = new JTextField(18);
    private final JTextField txtCedula = new JTextField(18);
    private final JComboBox<String> cbxCliente = new JComboBox<>();
    private final JCheckBox chkEnTaller = new JCheckBox("En taller");
    private final JCheckBox chkSeguro = new JCheckBox("Tiene seguro");
    private final JTextField txtAseguradora = new JTextField(18);

    /** Vehiculo que se esta cargando (nuevo) o editando. */
    private final Vehiculo vehiculo;
    /** Lo que se hace despues de guardar, por ejemplo refrescar la tabla. */
    private final Runnable alGuardar;

    /** Formulario vacio, para cargar un vehiculo nuevo. */
    public vehiAlta() {
        this(null, null);
    }

    /**
     * Formulario de vehiculo.
     * @param v vehiculo a editar, o null para cargar uno nuevo
     * @param alGuardar accion a ejecutar despues de guardar (puede ser null)
     */
    public vehiAlta(Vehiculo v, Runnable alGuardar) {
        initComponents();
        this.vehiculo = (v != null) ? v : new Vehiculo();
        this.alGuardar = alGuardar;
        // Cerrar este formulario no tiene que cerrar todo el programa
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(v == null ? "MotorGestion - Nuevo vehículo" : "MotorGestion - Editar vehículo");
        armarFormulario(v == null ? "NUEVO VEHÍCULO" : "EDITAR VEHÍCULO");
        cargarClientes();
        if (v != null) {
            mostrarDatos();
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

        int fila = 1;
        fila = agregarFila(panel, g, fila, "Matrícula:", txtMatricula);
        fila = agregarFila(panel, g, fila, "Marca:", txtMarca);
        fila = agregarFila(panel, g, fila, "Modelo:", txtModelo);
        fila = agregarFila(panel, g, fila, "Color:", txtColor);
        fila = agregarFila(panel, g, fila, "Cédula verde:", txtCedula);
        fila = agregarFila(panel, g, fila, "Cliente:", cbxCliente);
        g.gridx = 1;
        g.gridy = fila++;
        panel.add(chkEnTaller, g);
        g.gridy = fila++;
        panel.add(chkSeguro, g);
        fila = agregarFila(panel, g, fila, "Aseguradora:", txtAseguradora);

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

        // Que hace cada boton
        chkSeguro.addActionListener(e -> actualizarAseguradora());
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnGuardar);   // Enter = Guardar

        actualizarAseguradora();
        // Primero setResizable y despues pack: al reves, en Windows la ventana
        // queda unos pixeles mas chica y los botones de abajo quedan pegados al borde
        setResizable(false);
        pack();
    }

    /** Agrega una fila "etiqueta + campo" al formulario y devuelve la fila siguiente. */
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

    /** La aseguradora solo se puede escribir si el vehiculo tiene seguro. */
    private void actualizarAseguradora() {
        txtAseguradora.setEnabled(chkSeguro.isSelected());
        if (!chkSeguro.isSelected()) {
            txtAseguradora.setText("");
        }
    }

    /** Carga el combo con los clientes de la base ("1 - Copito Ibarra"). */
    private void cargarClientes() {
        try {
            for (String cliente : Vehiculo.listarClientes()) {
                cbxCliente.addItem(cliente);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los clientes:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Muestra en el formulario los datos del vehiculo que se edita. */
    private void mostrarDatos() {
        txtMatricula.setText(vehiculo.getMatricula());
        txtMarca.setText(vehiculo.getMarca());
        txtModelo.setText(vehiculo.getModelo());
        txtColor.setText(vehiculo.getColor());
        txtCedula.setText(vehiculo.getCedula());
        for (int i = 0; i < cbxCliente.getItemCount(); i++) {
            if (cbxCliente.getItemAt(i).startsWith(vehiculo.getIdCliente() + " - ")) {
                cbxCliente.setSelectedIndex(i);
                break;
            }
        }
        chkEnTaller.setSelected(vehiculo.isEnTaller());
        chkSeguro.setSelected(vehiculo.isSeguro());
        actualizarAseguradora();
        if (vehiculo.getEmpresaSeguros() != null) {
            txtAseguradora.setText(vehiculo.getEmpresaSeguros());
        }
    }

    /** Valida los datos, los pasa al Vehiculo y lo guarda en la base. */
    private void guardar() {
        String matricula = txtMatricula.getText().trim().toUpperCase();
        String marca = txtMarca.getText().trim();
        String modelo = txtModelo.getText().trim();
        String color = txtColor.getText().trim();
        String cedula = txtCedula.getText().trim();
        String aseguradora = txtAseguradora.getText().trim();

        // 1. Validaciones
        if (matricula.isEmpty() || marca.isEmpty() || modelo.isEmpty() || color.isEmpty() || cedula.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completá matrícula, marca, modelo, color y cédula verde.");
            return;
        }
        if (matricula.length() > 10) {
            JOptionPane.showMessageDialog(this, "La matrícula puede tener hasta 10 caracteres.");
            return;
        }
        if (marca.length() > 45 || modelo.length() > 45 || color.length() > 45
                || cedula.length() > 45 || aseguradora.length() > 45) {
            JOptionPane.showMessageDialog(this, "Marca, modelo, color, cédula y aseguradora pueden tener hasta 45 caracteres.");
            return;
        }
        if (cbxCliente.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Elegí el cliente dueño del vehículo.\nSi no aparece, primero cargalo en Clientes.");
            return;
        }
        if (chkSeguro.isSelected() && aseguradora.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Indicá la aseguradora o destildá \"Tiene seguro\".");
            return;
        }

        // 2. Pasar los datos al objeto
        int idCliente = Integer.parseInt(cbxCliente.getSelectedItem().toString().split(" - ")[0]);
        vehiculo.setMatricula(matricula);
        vehiculo.setMarca(marca);
        vehiculo.setModelo(modelo);
        vehiculo.setColor(color);
        vehiculo.setCedula(cedula);
        vehiculo.setIdCliente(idCliente);
        vehiculo.setEnTaller(chkEnTaller.isSelected());
        vehiculo.setSeguro(chkSeguro.isSelected());
        vehiculo.setEmpresaSeguros(chkSeguro.isSelected() ? aseguradora : null);

        // 3. Guardar en la base
        try {
            vehiculo.guardar();
            JOptionPane.showMessageDialog(this, "Vehículo guardado.");
            if (alGuardar != null) {
                alGuardar.run();
            }
            dispose();
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "Ya existe un vehículo con esa matrícula o esa cédula verde.",
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
        java.awt.EventQueue.invokeLater(() -> new vehiAlta().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
