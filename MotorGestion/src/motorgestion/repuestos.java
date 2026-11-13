/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package motorgestion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
public class repuestos extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(repuestos.class.getName());

    // ------------------------------------------------------------
    // Componentes del modulo (se arman por codigo en armarPantalla)
    // ------------------------------------------------------------
    private static final String TODOS = "Todos los proveedores";
    private static final NumberFormat PESOS = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"));

    // Tarjetas de resumen (arriba)
    private final JLabel lblTotalRepuestos = numeroTarjeta();
    private final JLabel lblTotalUnidades = numeroTarjeta();
    private final JLabel lblValorTotal = numeroTarjeta();
    private final JLabel lblStockBajo = numeroTarjeta();

    // Filtros
    private final JTextField txtBuscar = new JTextField(16);
    private final JComboBox<String> cbxProveedor = new JComboBox<>();
    private final JCheckBox chkSoloBajo = new JCheckBox("Solo stock bajo");
    private final JLabel lblCantidad = new JLabel();

    // Tabla
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"Repuesto", "Proveedor", "Stock", "Precio unitario", "Valor en stock", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;   // se edita desde el formulario
        }
    };
    private final JTable tblRepuestos = new JTable(modeloTabla);

    /** Repuestos que se ven en la tabla, en el mismo orden que las filas. */
    private List<Repuesto> listaActual = new ArrayList<>();

    /**
     * Pantalla de Inventario de repuestos: arriba un resumen en tarjetas,
     * despues los filtros, la tabla con el estado del stock y las acciones.
     */
    public repuestos() {
        initComponents();
        setTitle("MotorGestion - Inventario de repuestos");
        armarPantalla();
        cargarProveedores();
        actualizar();
    }

    /** Crea el numero grande de una tarjeta de resumen. */
    private static JLabel numeroTarjeta() {
        JLabel numero = new JLabel("-");
        numero.setFont(numero.getFont().deriveFont(Font.BOLD, 26f));
        numero.setForeground(Estilo.PRIMARIO);
        return numero;
    }

    /** Crea una tarjeta de resumen (el diseño de la tarjeta esta en Estilo). */
    private static JPanel tarjeta(String titulo, JLabel numero, java.awt.Color color) {
        return Estilo.tarjetaResumen(titulo, numero, color);
    }

    /** Arma el contenido de la pantalla debajo del menu. */
    private void armarPantalla() {
        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panelMenu1, BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(0, 18));
        contenido.setBorder(new EmptyBorder(20, 28, 20, 28));

        // --- Titulo ---
        JLabel lblTitulo = new JLabel("INVENTARIO DE REPUESTOS");
        Estilo.titulo(lblTitulo);
        JLabel lblSubtitulo = new JLabel("Stock, precios y alertas de reposición");
        lblSubtitulo.setForeground(Estilo.TEXTO_SUAVE);

        // --- Tarjetas de resumen ---
        JPanel tarjetas = new JPanel(new GridLayout(1, 4, 16, 0));
        tarjetas.add(tarjeta("REPUESTOS DISTINTOS", lblTotalRepuestos, Estilo.PRIMARIO));
        tarjetas.add(tarjeta("UNIDADES EN STOCK", lblTotalUnidades, Estilo.PRIMARIO));
        tarjetas.add(tarjeta("VALOR DEL INVENTARIO", lblValorTotal, Estilo.ACENTO));
        tarjetas.add(tarjeta("CON STOCK BAJO", lblStockBajo, Estilo.ALERTA));

        JPanel cabecera = new JPanel();
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjetas.setAlignmentX(Component.LEFT_ALIGNMENT);
        cabecera.add(lblTitulo);
        cabecera.add(lblSubtitulo);
        cabecera.add(javax.swing.Box.createVerticalStrut(16));
        cabecera.add(tarjetas);

        // --- Filtros ---
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filtros.add(new JLabel("Buscar:"));
        filtros.add(txtBuscar);
        filtros.add(new JLabel("Proveedor:"));
        filtros.add(cbxProveedor);
        filtros.add(chkSoloBajo);
        JPanel barraFiltros = new JPanel(new BorderLayout());
        barraFiltros.add(filtros, BorderLayout.WEST);
        barraFiltros.add(lblCantidad, BorderLayout.EAST);

        // --- Tabla ---
        Estilo.tabla(tblRepuestos);
        tblRepuestos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Textos con un margen a la izquierda; numeros centrados en su casillero
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int col = 0; col <= 1; col++) {
            tblRepuestos.getColumnModel().getColumn(col).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                        boolean foco, int fila, int columna) {
                    JLabel celda = (JLabel) super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
                    celda.setBorder(new EmptyBorder(0, 10, 0, 0));   // margen a la izquierda
                    return celda;
                }
            });
        }
        for (int col = 2; col <= 4; col++) {
            tblRepuestos.getColumnModel().getColumn(col).setCellRenderer(centrado);
        }
        // La columna Estado se pinta de verde, naranja o rojo
        tblRepuestos.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                    boolean foco, int fila, int columna) {
                JLabel celda = (JLabel) super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
                celda.setHorizontalAlignment(SwingConstants.CENTER);
                celda.setFont(celda.getFont().deriveFont(Font.BOLD));
                if (!seleccionada) {
                    if ("Sin stock".equals(valor)) {
                        celda.setForeground(Estilo.PELIGRO);
                    } else if ("Stock bajo".equals(valor)) {
                        celda.setForeground(Estilo.ALERTA);
                    } else {
                        celda.setForeground(Estilo.EXITO);
                    }
                }
                return celda;
            }
        });
        JScrollPane scroll = new JScrollPane(tblRepuestos);
        scroll.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        scroll.getViewport().setBackground(Estilo.SUPERFICIE);

        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.add(barraFiltros, BorderLayout.NORTH);
        centro.add(scroll, BorderLayout.CENTER);

        // --- Acciones ---
        JLabel lblLeyenda = new JLabel("Stock bajo: " + Repuesto.STOCK_MINIMO + " unidades o menos");
        lblLeyenda.setForeground(Estilo.TEXTO_SUAVE);
        JButton btnNuevo = new JButton("+ Nuevo repuesto");
        Estilo.botonPrimario(btnNuevo);
        JButton btnAjustar = new JButton("Ajustar stock");
        Estilo.botonSecundario(btnAjustar);
        JButton btnEditar = new JButton("Editar");
        Estilo.botonSecundario(btnEditar);
        JButton btnEliminar = new JButton("Eliminar");
        Estilo.botonPeligro(btnEliminar);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.add(btnNuevo);
        botones.add(btnAjustar);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        JPanel acciones = new JPanel(new BorderLayout());
        acciones.add(lblLeyenda, BorderLayout.WEST);
        acciones.add(botones, BorderLayout.EAST);

        contenido.add(cabecera, BorderLayout.NORTH);
        contenido.add(centro, BorderLayout.CENTER);
        contenido.add(acciones, BorderLayout.SOUTH);
        getContentPane().add(contenido, BorderLayout.CENTER);

        // --- Que hace cada control ---
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { cargarTabla(); }
            @Override public void removeUpdate(DocumentEvent e) { cargarTabla(); }
            @Override public void changedUpdate(DocumentEvent e) { cargarTabla(); }
        });
        cbxProveedor.addActionListener(e -> cargarTabla());
        chkSoloBajo.addActionListener(e -> cargarTabla());
        btnNuevo.addActionListener(e -> abrirFormulario(null));
        btnAjustar.addActionListener(e -> ajustarStock());
        btnEditar.addActionListener(e -> {
            Repuesto r = seleccionado();
            if (r != null) {
                abrirFormulario(r);
            }
        });
        btnEliminar.addActionListener(e -> eliminarSeleccionado());
        tblRepuestos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {   // doble clic = Editar
                    Repuesto r = seleccionado();
                    if (r != null) {
                        abrirFormulario(r);
                    }
                }
            }
        });

        Navegacion.pantallaCompleta(this);   // ocupa toda la pantalla
    }

    /** Llena el combo de proveedores (el primero es "Todos"). */
    private void cargarProveedores() {
        cbxProveedor.addItem(TODOS);
        try {
            for (String proveedor : Repuesto.listarProveedores()) {
                cbxProveedor.addItem(proveedor);
            }
        } catch (SQLException ex) {
            mostrarError("No se pudieron cargar los proveedores", ex);
        }
    }

    /** Actualiza las tarjetas y la tabla (despues de cualquier cambio). */
    private void actualizar() {
        cargarResumen();
        cargarTabla();
    }

    /** Llena las tarjetas de resumen con los datos de todo el inventario. */
    private void cargarResumen() {
        try {
            double[] r = Repuesto.resumen();
            lblTotalRepuestos.setText(String.valueOf((int) r[0]));
            lblTotalUnidades.setText(String.valueOf((int) r[1]));
            lblValorTotal.setText(PESOS.format(r[2]));
            lblStockBajo.setText(String.valueOf((int) r[3]));
            lblStockBajo.setForeground(r[3] > 0 ? Estilo.ALERTA : Estilo.EXITO);
        } catch (SQLException ex) {
            mostrarError("No se pudo calcular el resumen", ex);
        }
    }

    /** Llena la tabla segun los filtros elegidos. */
    private void cargarTabla() {
        int idProveedor = 0;   // 0 = todos
        Object elegido = cbxProveedor.getSelectedItem();
        if (elegido != null && !elegido.equals(TODOS)) {
            idProveedor = Integer.parseInt(elegido.toString().split(" - ")[0]);
        }
        try {
            listaActual = Repuesto.listar(txtBuscar.getText(), idProveedor, chkSoloBajo.isSelected());
        } catch (SQLException ex) {
            listaActual = new ArrayList<>();
            mostrarError("No se pudieron cargar los repuestos", ex);
        }
        modeloTabla.setRowCount(0);
        for (Repuesto r : listaActual) {
            modeloTabla.addRow(new Object[]{
                r.getNombre(), r.getNombreProveedor(), r.getCantidad(),
                PESOS.format(r.getPrecio()), PESOS.format(r.getValorEnStock()), r.getEstado()
            });
        }
        lblCantidad.setText(listaActual.size() + " repuesto(s) en la lista");
    }

    /** Devuelve el repuesto seleccionado, o null (con aviso) si no hay ninguno. */
    private Repuesto seleccionado() {
        int fila = tblRepuestos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccioná un repuesto de la tabla.");
            return null;
        }
        return listaActual.get(fila);
    }

    /** Abre el formulario: vacio si r es null (Nuevo), con datos si no (Editar). */
    private void abrirFormulario(Repuesto r) {
        invAlta formulario = new invAlta(r, this::actualizar);
        formulario.setLocationRelativeTo(this);
        formulario.setVisible(true);
    }

    /** Suma o resta unidades al stock del repuesto seleccionado. */
    private void ajustarStock() {
        Repuesto r = seleccionado();
        if (r == null) {
            return;
        }
        String respuesta = JOptionPane.showInputDialog(this,
                r.getNombre() + "  (stock actual: " + r.getCantidad() + ")\n\n"
                + "Unidades que ENTRAN (ej. 5) o que SALEN (ej. -2):",
                "Ajustar stock", JOptionPane.QUESTION_MESSAGE);
        if (respuesta == null || respuesta.isBlank()) {
            return;   // se cancelo
        }
        int unidades;
        try {
            unidades = Integer.parseInt(respuesta.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Escribí un número entero, por ejemplo 5 o -2.");
            return;
        }
        if (unidades == 0) {
            return;
        }
        try {
            if (Repuesto.ajustarStock(r.getIdRepuesto(), unidades)) {
                actualizar();
            } else {
                JOptionPane.showMessageDialog(this, "No hay stock suficiente: hay " + r.getCantidad() + " unidad(es).");
            }
        } catch (SQLException ex) {
            mostrarError("No se pudo ajustar el stock", ex);
        }
    }

    private void eliminarSeleccionado() {
        Repuesto r = seleccionado();
        if (r == null) {
            return;
        }
        if (!Dialogo.confirmar(this, "Eliminar repuesto", "¿Eliminar el repuesto " + r.getNombre() + "?\n"
                + "También se quita de las reparaciones donde se usó.",
                "Sí, eliminar", true)) {
            return;
        }
        try {
            Repuesto.eliminar(r.getIdRepuesto());
            actualizar();
        } catch (SQLException ex) {
            mostrarError("No se pudo eliminar", ex);
        }
    }

    private void mostrarError(String mensaje, SQLException ex) {
        JOptionPane.showMessageDialog(this, mensaje + ":\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

        jLabel1.setText("jLabel1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelMenu1, javax.swing.GroupLayout.PREFERRED_SIZE, 863, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 12, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(362, 362, 362)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelMenu1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 69, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(37, 37, 37))
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
        java.awt.EventQueue.invokeLater(() -> new repuestos().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private motorgestion.panelMenu panelMenu1;
    // End of variables declaration//GEN-END:variables
}
