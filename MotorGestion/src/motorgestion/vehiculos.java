/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package motorgestion;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author User
 */
public class vehiculos extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(vehiculos.class.getName());

    // ------------------------------------------------------------
    // Componentes del modulo (se arman por codigo en armarPantalla)
    // ------------------------------------------------------------
    private final JTextField txtBuscar = new JTextField(15);
    private final JLabel lblCantidad = new JLabel();
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"Matrícula", "Marca", "Modelo", "Color", "Cliente", "En taller", "Seguro", "Aseguradora"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;   // la tabla es solo para ver; se edita desde el formulario
        }
    };
    private final JTable tblVehiculos = new JTable(modeloTabla);

    /** Vehiculos que se ven en la tabla, en el mismo orden que las filas. */
    private List<Vehiculo> listaActual = new ArrayList<>();

    /**
     * Pantalla de Vehiculos: arriba el menu del sistema (panelMenu),
     * abajo la busqueda, la tabla y los botones Nuevo / Editar / Eliminar.
     */
    public vehiculos() {
        initComponents();
        setTitle("MotorGestion - Vehículos");
        armarPantalla();
        cargarTabla("");
    }

    /** Arma el contenido de la pantalla debajo del menu. */
    private void armarPantalla() {
        // Se reemplaza el contenido del diseñador: el menu arriba y el modulo en el centro
        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panelMenu1, BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(0, 12));
        contenido.setBorder(new EmptyBorder(16, 24, 16, 24));

        // Titulo y busqueda
        JLabel lblTitulo = new JLabel("VEHÍCULOS");
        Estilo.titulo(lblTitulo);
        JButton btnBuscar = new JButton("Buscar");
        JButton btnTodos = new JButton("Ver todos");
        Estilo.botonSecundario(btnBuscar);
        Estilo.botonSecundario(btnTodos);
        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        busqueda.add(new JLabel("Buscar por matrícula:"));
        busqueda.add(txtBuscar);
        busqueda.add(btnBuscar);
        busqueda.add(btnTodos);
        JPanel arriba = new JPanel(new BorderLayout(0, 8));
        arriba.add(lblTitulo, BorderLayout.NORTH);
        arriba.add(busqueda, BorderLayout.CENTER);

        // Tabla
        Estilo.tabla(tblVehiculos);
        tblVehiculos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Textos largos con margen a la izquierda; datos cortos centrados
        for (int col = 0; col < tblVehiculos.getColumnCount(); col++) {
            boolean esTexto = (col == 1 || col == 2 || col == 4 || col == 7);   // Marca, Modelo, Cliente, Aseguradora
            tblVehiculos.getColumnModel().getColumn(col).setCellRenderer(
                    esTexto ? Estilo.celdaTexto() : Estilo.celdaCentrada());
        }
        JScrollPane scroll = new JScrollPane(tblVehiculos);
        scroll.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        scroll.getViewport().setBackground(Estilo.SUPERFICIE);
        lblCantidad.setForeground(Estilo.TEXTO_SUAVE);

        // Botones de accion
        JButton btnNuevo = new JButton("Nuevo");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        Estilo.botonPrimario(btnNuevo);
        Estilo.botonSecundario(btnEditar);
        Estilo.botonPeligro(btnEliminar);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnNuevo);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        JPanel abajo = new JPanel(new BorderLayout());
        abajo.add(lblCantidad, BorderLayout.WEST);
        abajo.add(botones, BorderLayout.EAST);

        contenido.add(arriba, BorderLayout.NORTH);
        contenido.add(scroll, BorderLayout.CENTER);
        contenido.add(abajo, BorderLayout.SOUTH);
        getContentPane().add(contenido, BorderLayout.CENTER);

        // Que hace cada boton
        btnBuscar.addActionListener(e -> cargarTabla(txtBuscar.getText()));
        txtBuscar.addActionListener(e -> cargarTabla(txtBuscar.getText()));   // Enter en la caja = Buscar
        btnTodos.addActionListener(e -> {
            txtBuscar.setText("");
            cargarTabla("");
        });
        btnNuevo.addActionListener(e -> abrirFormulario(null));
        btnEditar.addActionListener(e -> editarSeleccionado());
        btnEliminar.addActionListener(e -> eliminarSeleccionado());
        tblVehiculos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {   // doble clic en una fila = Editar
                    editarSeleccionado();
                }
            }
        });

        Navegacion.pantallaCompleta(this);   // ocupa toda la pantalla
    }

    /** Llena la tabla con los vehiculos cuya matricula contiene el texto buscado. */
    private void cargarTabla(String matricula) {
        try {
            listaActual = Vehiculo.listar(matricula);
        } catch (SQLException ex) {
            listaActual = new ArrayList<>();
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los vehículos:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        modeloTabla.setRowCount(0);
        for (Vehiculo v : listaActual) {
            modeloTabla.addRow(new Object[]{
                v.getMatricula(), v.getMarca(), v.getModelo(), v.getColor(), v.getNombreCliente(),
                v.isEnTaller() ? "Sí" : "No",
                v.isSeguro() ? "Sí" : "No",
                v.getEmpresaSeguros() == null ? "-" : v.getEmpresaSeguros()
            });
        }
        lblCantidad.setText(listaActual.size() + " vehículo(s)");
    }

    /** Devuelve el vehiculo de la fila seleccionada, o null si no hay ninguna. */
    private Vehiculo seleccionado() {
        int fila = tblVehiculos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccioná un vehículo de la tabla.");
            return null;
        }
        return listaActual.get(fila);
    }

    /** Abre el formulario: vacio si v es null (Nuevo), con datos si no (Editar). */
    private void abrirFormulario(Vehiculo v) {
        vehiAlta formulario = new vehiAlta(v, () -> cargarTabla(txtBuscar.getText()));
        formulario.setLocationRelativeTo(this);
        formulario.setVisible(true);
    }

    private void editarSeleccionado() {
        Vehiculo v = seleccionado();
        if (v != null) {
            abrirFormulario(v);
        }
    }

    private void eliminarSeleccionado() {
        Vehiculo v = seleccionado();
        if (v == null) {
            return;
        }
        if (!Dialogo.confirmar(this, "Eliminar vehículo", "¿Eliminar el vehículo " + v.getMatricula() + "?\nTambién se borran sus reparaciones.",
                "Sí, eliminar", true)) {
            return;
        }
        try {
            Vehiculo.eliminar(v.getIdVehiculo());
            cargarTabla(txtBuscar.getText());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar:\n" + ex.getMessage(),
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

        panelMenu1 = new motorgestion.panelMenu();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("vehiculos");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelMenu1, javax.swing.GroupLayout.PREFERRED_SIZE, 863, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 12, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(398, 398, 398)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelMenu1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(jLabel1)
                .addGap(0, 77, Short.MAX_VALUE))
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
        java.awt.EventQueue.invokeLater(() -> new vehiculos().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private motorgestion.panelMenu panelMenu1;
    // End of variables declaration//GEN-END:variables
}
