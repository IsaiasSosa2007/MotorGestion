/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package motorgestion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
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
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author User
 */
public class empleados extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(empleados.class.getName());

    // ------------------------------------------------------------
    // Componentes del modulo (se arman por codigo en armarPantalla)
    // ------------------------------------------------------------

    private final JTextField txtBuscar = new JTextField(18);
    private final JLabel lblCantidad = new JLabel();
    private final JTabbedPane pestanias = new JTabbedPane();

    // Pestania "Personal": la tabla de empleados
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"Empleado", "DNI", "Teléfono", "Usuario", "Acceso", "Reparaciones activas", "Total"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;   // se edita desde el formulario
        }
    };
    private final JTable tblEmpleados = new JTable(modeloTabla);

    // Pestania "Carga de trabajo": una barra por empleado
    private final JPanel panelCarga = new JPanel(new GridBagLayout());

    /** Empleados que se ven en la tabla, en el mismo orden que las filas. */
    private List<Empleado> listaActual = new ArrayList<>();

    /**
     * Pantalla de Empleados, con dos pestanias: "Personal" (datos y acceso
     * al sistema) y "Carga de trabajo" (cuantas reparaciones tiene cada uno).
     */
    public empleados() {
        initComponents();
        setTitle("MotorGestion - Empleados");
        armarPantalla();
        cargarDatos();
    }

    /** Arma el contenido de la pantalla debajo del menu. */
    private void armarPantalla() {
        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panelMenu1, BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(0, 16));
        contenido.setBorder(new EmptyBorder(20, 28, 20, 28));

        // --- Encabezado: titulo a la izquierda, acciones a la derecha ---
        JLabel lblTitulo = new JLabel("EMPLEADOS");
        Estilo.titulo(lblTitulo);
        JLabel lblSubtitulo = new JLabel("Equipo del taller, acceso al sistema y carga de trabajo");
        lblSubtitulo.setForeground(Estilo.TEXTO_SUAVE);
        JPanel titulos = new JPanel(new GridLayout(2, 1));
        titulos.add(lblTitulo);
        titulos.add(lblSubtitulo);

        JButton btnNuevo = new JButton("+ Nuevo empleado");
        Estilo.botonPrimario(btnNuevo);
        JButton btnEditar = new JButton("Editar");
        Estilo.botonSecundario(btnEditar);
        JButton btnEliminar = new JButton("Eliminar");
        Estilo.botonPeligro(btnEliminar);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        acciones.add(btnNuevo);
        acciones.add(btnEditar);
        acciones.add(btnEliminar);

        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.add(titulos, BorderLayout.WEST);
        filaTitulo.add(acciones, BorderLayout.EAST);

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        busqueda.add(new JLabel("Buscar por nombre o DNI:"));
        busqueda.add(txtBuscar);
        JPanel barraBusqueda = new JPanel(new BorderLayout());
        barraBusqueda.add(busqueda, BorderLayout.WEST);
        barraBusqueda.add(lblCantidad, BorderLayout.EAST);

        JPanel encabezado = new JPanel(new BorderLayout(0, 14));
        encabezado.add(filaTitulo, BorderLayout.NORTH);
        encabezado.add(barraBusqueda, BorderLayout.CENTER);

        // --- Pestania 1: Personal ---
        Estilo.tabla(tblEmpleados);
        tblEmpleados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Texto con margen a la izquierda; DNI, telefono y numeros centrados
        DefaultTableCellRenderer conMargen = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                    boolean foco, int fila, int columna) {
                JLabel celda = (JLabel) super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
                celda.setBorder(new EmptyBorder(0, 10, 0, 0));
                return celda;
            }
        };
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        tblEmpleados.getColumnModel().getColumn(0).setCellRenderer(conMargen);
        for (int col : new int[]{1, 2, 3, 5, 6}) {
            tblEmpleados.getColumnModel().getColumn(col).setCellRenderer(centrado);
        }
        // Columna "Acceso": azul para Administrador, gris para Sin acceso
        tblEmpleados.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                    boolean foco, int fila, int columna) {
                JLabel celda = (JLabel) super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
                celda.setHorizontalAlignment(SwingConstants.CENTER);
                celda.setFont(celda.getFont().deriveFont(Font.BOLD));
                if (!seleccionada) {
                    if ("Administrador".equals(valor)) {
                        celda.setForeground(Estilo.PRIMARIO);
                    } else if ("Sin acceso".equals(valor)) {
                        celda.setForeground(Estilo.TEXTO_SUAVE);
                    } else {
                        celda.setForeground(Estilo.TEXTO);
                    }
                }
                return celda;
            }
        });
        JScrollPane scrollTabla = new JScrollPane(tblEmpleados);
        scrollTabla.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        scrollTabla.getViewport().setBackground(Estilo.SUPERFICIE);
        JPanel pestaniaPersonal = new JPanel(new BorderLayout());
        pestaniaPersonal.setBorder(new EmptyBorder(12, 12, 12, 12));
        pestaniaPersonal.add(scrollTabla, BorderLayout.CENTER);

        // --- Pestania 2: Carga de trabajo ---
        panelCarga.setBackground(Estilo.SUPERFICIE);
        JPanel envoltorio = new JPanel(new BorderLayout());   // para que las barras queden arriba
        envoltorio.setBackground(Estilo.SUPERFICIE);
        envoltorio.add(panelCarga, BorderLayout.NORTH);
        JScrollPane scrollCarga = new JScrollPane(envoltorio);
        scrollCarga.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        JLabel lblExplica = new JLabel("Reparaciones Pendientes o En proceso asignadas a cada empleado. "
                + "Sirve para repartir el trabajo nuevo.");
        lblExplica.setForeground(Estilo.TEXTO_SUAVE);
        JPanel pestaniaCarga = new JPanel(new BorderLayout(0, 10));
        pestaniaCarga.setBorder(new EmptyBorder(12, 12, 12, 12));
        pestaniaCarga.add(lblExplica, BorderLayout.NORTH);
        pestaniaCarga.add(scrollCarga, BorderLayout.CENTER);

        pestanias.addTab("Personal", pestaniaPersonal);
        pestanias.addTab("Carga de trabajo", pestaniaCarga);

        JLabel lblAyuda = new JLabel("Doble clic en un empleado para editarlo. "
                + "Solo quienes tienen usuario pueden entrar al sistema.");
        lblAyuda.setForeground(Estilo.TEXTO_SUAVE);

        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(pestanias, BorderLayout.CENTER);
        contenido.add(lblAyuda, BorderLayout.SOUTH);
        getContentPane().add(contenido, BorderLayout.CENTER);

        // --- Que hace cada control ---
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { cargarDatos(); }
            @Override public void removeUpdate(DocumentEvent e) { cargarDatos(); }
            @Override public void changedUpdate(DocumentEvent e) { cargarDatos(); }
        });
        btnNuevo.addActionListener(e -> abrirFormulario(null));
        btnEditar.addActionListener(e -> {
            Empleado emp = seleccionado();
            if (emp != null) {
                abrirFormulario(emp);
            }
        });
        btnEliminar.addActionListener(e -> eliminarSeleccionado());
        tblEmpleados.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {   // doble clic = Editar
                    Empleado emp = seleccionado();
                    if (emp != null) {
                        abrirFormulario(emp);
                    }
                }
            }
        });

        Navegacion.pantallaCompleta(this);   // ocupa toda la pantalla
    }

    /** Carga los empleados en la tabla y arma las barras de carga de trabajo. */
    private void cargarDatos() {
        try {
            listaActual = Empleado.listar(txtBuscar.getText());
        } catch (SQLException ex) {
            listaActual = new ArrayList<>();
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los empleados:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        // Tabla
        modeloTabla.setRowCount(0);
        for (Empleado emp : listaActual) {
            modeloTabla.addRow(new Object[]{
                emp.getNombre(), emp.getDni(), emp.getTelefono(),
                emp.tieneAcceso() ? emp.getUsuario() : "-",
                emp.getAccesoTexto(), emp.getReparacionesActivas(), emp.getReparacionesTotales()
            });
        }
        lblCantidad.setText(listaActual.size() + " empleado(s)");
        armarCargaDeTrabajo();
    }

    /** Una fila por empleado: nombre, barra y "N activas · M en total". */
    private void armarCargaDeTrabajo() {
        panelCarga.removeAll();
        // La barra mas llena es la del empleado con mas reparaciones activas
        int maximo = 1;
        for (Empleado emp : listaActual) {
            maximo = Math.max(maximo, emp.getReparacionesActivas());
        }
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 16, 10, 16);
        g.anchor = GridBagConstraints.WEST;
        int fila = 0;
        for (Empleado emp : listaActual) {
            JLabel lblNombre = new JLabel(emp.getNombre());
            lblNombre.setFont(lblNombre.getFont().deriveFont(Font.BOLD, 13f));
            lblNombre.setPreferredSize(new Dimension(200, 24));

            JProgressBar barra = new JProgressBar(0, maximo);
            barra.setValue(emp.getReparacionesActivas());
            barra.setPreferredSize(new Dimension(380, 18));

            JLabel lblDetalle = new JLabel(emp.getReparacionesActivas() + " activa(s)  ·  "
                    + emp.getReparacionesTotales() + " en total");
            lblDetalle.setForeground(Estilo.TEXTO_SUAVE);

            g.gridy = fila++;
            g.gridx = 0;
            g.weightx = 0;
            g.fill = GridBagConstraints.NONE;
            panelCarga.add(lblNombre, g);
            g.gridx = 1;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            panelCarga.add(barra, g);
            g.gridx = 2;
            g.weightx = 0;
            g.fill = GridBagConstraints.NONE;
            panelCarga.add(lblDetalle, g);
        }
        panelCarga.revalidate();
        panelCarga.repaint();
    }

    /** Devuelve el empleado seleccionado, o null (con aviso) si no hay ninguno. */
    private Empleado seleccionado() {
        int fila = tblEmpleados.getSelectedRow();
        if (fila == -1) {
            pestanias.setSelectedIndex(0);
            JOptionPane.showMessageDialog(this, "Seleccioná un empleado en la pestaña Personal.");
            return null;
        }
        return listaActual.get(fila);
    }

    /** Abre el formulario: vacio si emp es null (Nuevo), con datos si no (Editar). */
    private void abrirFormulario(Empleado emp) {
        empAlta formulario = new empAlta(emp, this::cargarDatos);
        formulario.setLocationRelativeTo(this);
        formulario.setVisible(true);
    }

    private void eliminarSeleccionado() {
        Empleado emp = seleccionado();
        if (emp == null) {
            return;
        }
        try {
            // No se puede dejar el sistema sin ningun administrador
            if (emp.esAdmin() && Empleado.cantidadAdmins() <= 1) {
                JOptionPane.showMessageDialog(this, emp.getNombre() + " es el único administrador.\n"
                        + "Antes de eliminarlo, dale el rol de administrador a otro empleado.");
                return;
            }
            String aviso = "¿Eliminar a " + emp.getNombre() + "?";
            if (emp.tieneAcceso()) {
                aviso += "\nTambién se borra su usuario \"" + emp.getUsuario() + "\" del sistema.";
            }
            if (Dialogo.confirmar(this, "Eliminar empleado", aviso,
                    "Sí, eliminar", true)) {
                Empleado.eliminar(emp.getIdEmpleado());
                cargarDatos();
            }
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

        jLabel1.setText("EMPLEADOS-borrenestonoseanboludos");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(panelMenu1, javax.swing.GroupLayout.PREFERRED_SIZE, 863, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(328, 328, 328)
                        .addComponent(jLabel1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelMenu1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(39, 39, 39))
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
        java.awt.EventQueue.invokeLater(() -> new empleados().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private motorgestion.panelMenu panelMenu1;
    // End of variables declaration//GEN-END:variables
}
