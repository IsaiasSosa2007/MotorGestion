/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package motorgestion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author User
 */
public class proveedores extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(proveedores.class.getName());

    // ------------------------------------------------------------
    // Componentes del modulo (se arman por codigo en armarPantalla)
    // ------------------------------------------------------------
    private static final String TODAS = "Todas las categorías";
    private static final NumberFormat PESOS = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"));

    private final JTextField txtBuscar = new JTextField(16);
    private final JComboBox<String> cbxCategoria = new JComboBox<>();
    private final JLabel lblCantidad = new JLabel();

    // Tabla de la izquierda: la lista de proveedores
    private final DefaultTableModel modeloProveedores = tablaSoloLectura("Proveedor", "Categoría", "Teléfono", "Repuestos");
    private final JTable tblProveedores = new JTable(modeloProveedores);

    // Ficha de la derecha: los datos del proveedor elegido y sus repuestos
    private final JLabel lblNombre = new JLabel();
    private final JLabel lblCategoria = new JLabel();
    private final JLabel lblTelefono = new JLabel();
    private final JLabel lblDireccion = new JLabel();
    private final DefaultTableModel modeloRepuestos = tablaSoloLectura("Repuesto", "Stock", "Precio");
    private final JTable tblRepuestos = new JTable(modeloRepuestos);

    /** Proveedores que se ven en la tabla, en el mismo orden que las filas. */
    private List<Proveedor> listaActual = new ArrayList<>();
    /** Evita que el combo dispare el filtro mientras se esta llenando. */
    private boolean cargandoCategorias = false;

    /**
     * Pantalla de Proveedores: a la izquierda la lista (con filtro por nombre
     * y por categoria) y a la derecha la ficha del proveedor elegido, con los
     * repuestos que nos vende.
     */
    public proveedores() {
        initComponents();
        setTitle("MotorGestion - Proveedores");
        armarPantalla();
        cargarCategorias();
        filtrar();
    }

    /** Titulo chico, gris y en negrita para cada seccion de la ficha. */
    private static JLabel tituloSeccion(String texto) {
        JLabel titulo = new JLabel(texto);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 12f));
        titulo.setForeground(Estilo.TEXTO_SUAVE);
        return titulo;
    }

    /** Crea un modelo de tabla que no se puede editar escribiendo en las celdas. */
    private static DefaultTableModel tablaSoloLectura(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    /** Arma el contenido de la pantalla debajo del menu. */
    private void armarPantalla() {
        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panelMenu1, BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(0, 12));
        contenido.setBorder(new EmptyBorder(16, 24, 16, 24));

        // --- Encabezado: titulo, filtros y acciones ---
        JLabel lblTitulo = new JLabel("PROVEEDORES");
        Estilo.titulo(lblTitulo);
        JLabel lblSubtitulo = new JLabel("Empresas que le venden repuestos y herramientas al taller");
        lblSubtitulo.setForeground(Estilo.TEXTO_SUAVE);
        JPanel titulos = new JPanel(new GridLayout(2, 1));
        titulos.add(lblTitulo);
        titulos.add(lblSubtitulo);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.add(new JLabel("Buscar:"));
        filtros.add(txtBuscar);
        filtros.add(new JLabel("Categoría:"));
        filtros.add(cbxCategoria);

        JButton btnNuevo = new JButton("+ Nuevo proveedor");
        Estilo.botonPrimario(btnNuevo);
        JButton btnEditar = new JButton("Editar");
        Estilo.botonSecundario(btnEditar);
        JButton btnEliminar = new JButton("Eliminar");
        Estilo.botonPeligro(btnEliminar);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.add(btnNuevo);
        acciones.add(btnEditar);
        acciones.add(btnEliminar);

        JPanel barra = new JPanel(new BorderLayout());
        barra.add(filtros, BorderLayout.WEST);
        barra.add(acciones, BorderLayout.EAST);

        JPanel encabezado = new JPanel(new BorderLayout(0, 10));
        encabezado.add(titulos, BorderLayout.NORTH);
        encabezado.add(barra, BorderLayout.CENTER);

        // --- Izquierda: lista de proveedores ---
        Estilo.tabla(tblProveedores);
        tblProveedores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblProveedores.getColumnModel().getColumn(3).setMaxWidth(90);
        // Nombre y categoria con margen a la izquierda; telefono y cantidad centrados
        tblProveedores.getColumnModel().getColumn(0).setCellRenderer(Estilo.celdaTexto());
        tblProveedores.getColumnModel().getColumn(1).setCellRenderer(Estilo.celdaTexto());
        tblProveedores.getColumnModel().getColumn(2).setCellRenderer(Estilo.celdaCentrada());
        tblProveedores.getColumnModel().getColumn(3).setCellRenderer(Estilo.celdaCentrada());
        JScrollPane scrollLista = new JScrollPane(tblProveedores);
        scrollLista.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        scrollLista.getViewport().setBackground(Estilo.SUPERFICIE);
        // La lista va en una caja igual a la de la ficha, asi las dos se ven parejas
        JPanel lista = new JPanel(new BorderLayout(0, 10));
        lista.setBackground(Estilo.SUPERFICIE);
        lista.setBorder(Estilo.bordeCaja());
        lista.add(tituloSeccion("PROVEEDORES CARGADOS"), BorderLayout.NORTH);
        lista.add(scrollLista, BorderLayout.CENTER);

        // --- Derecha: ficha del proveedor ---
        // Una "tarjeta" blanca con un solo borde fino. Adentro, dos secciones
        // separadas por una linea: los datos y la tabla de repuestos.
        lblNombre.setFont(lblNombre.getFont().deriveFont(Font.BOLD, 20f));
        JPanel datos = new JPanel(new GridLayout(5, 1, 0, 6));
        datos.setOpaque(false);
        datos.add(tituloSeccion("FICHA DEL PROVEEDOR"));
        datos.add(lblNombre);
        datos.add(lblCategoria);
        datos.add(lblTelefono);
        datos.add(lblDireccion);
        // Linea gris debajo de los datos (separa las dos secciones)
        datos.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE),
                new EmptyBorder(0, 0, 14, 0)));

        Estilo.tabla(tblRepuestos);
        tblRepuestos.setFocusable(false);
        tblRepuestos.getColumnModel().getColumn(0).setCellRenderer(Estilo.celdaTexto());
        tblRepuestos.getColumnModel().getColumn(1).setCellRenderer(Estilo.celdaCentrada());
        tblRepuestos.getColumnModel().getColumn(2).setCellRenderer(Estilo.celdaCentrada());
        tblRepuestos.setRowSelectionAllowed(false);
        JScrollPane scrollRepuestos = new JScrollPane(tblRepuestos);
        scrollRepuestos.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        scrollRepuestos.getViewport().setBackground(Estilo.SUPERFICIE);

        JPanel bloqueRepuestos = new JPanel(new BorderLayout(0, 8));
        bloqueRepuestos.setOpaque(false);
        bloqueRepuestos.add(tituloSeccion("REPUESTOS QUE NOS PROVEE"), BorderLayout.NORTH);
        bloqueRepuestos.add(scrollRepuestos, BorderLayout.CENTER);

        JPanel ficha = new JPanel(new BorderLayout(0, 14));
        ficha.setBackground(Estilo.SUPERFICIE);
        ficha.setBorder(Estilo.bordeCaja());
        ficha.add(datos, BorderLayout.NORTH);
        ficha.add(bloqueRepuestos, BorderLayout.CENTER);

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, lista, ficha);
        division.setResizeWeight(0.55);
        division.setBorder(null);
        division.setDividerSize(16);

        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(division, BorderLayout.CENTER);
        contenido.add(lblCantidad, BorderLayout.SOUTH);
        getContentPane().add(contenido, BorderLayout.CENTER);

        // --- Que hace cada control ---
        // El filtro se aplica mientras se escribe (no hace falta un boton Buscar)
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        cbxCategoria.addActionListener(e -> {
            if (!cargandoCategorias) {
                filtrar();
            }
        });
        tblProveedores.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarFicha();
            }
        });
        btnNuevo.addActionListener(e -> abrirFormulario(null));
        btnEditar.addActionListener(e -> {
            Proveedor p = seleccionado();
            if (p != null) {
                abrirFormulario(p);
            }
        });
        btnEliminar.addActionListener(e -> eliminarSeleccionado());

        Navegacion.pantallaCompleta(this);   // ocupa toda la pantalla
    }

    /** Llena el combo de categorias con las que existen en la base. */
    private void cargarCategorias() {
        Object elegida = cbxCategoria.getSelectedItem();
        cargandoCategorias = true;
        cbxCategoria.removeAllItems();
        cbxCategoria.addItem(TODAS);
        try {
            for (String categoria : Proveedor.listarCategorias()) {
                cbxCategoria.addItem(categoria);
            }
        } catch (SQLException ex) {
            mostrarError("No se pudieron cargar las categorías", ex);
        }
        cbxCategoria.setSelectedItem(elegida != null ? elegida : TODAS);
        cargandoCategorias = false;
    }

    /** Vuelve a llenar la lista segun el texto buscado y la categoria elegida. */
    private void filtrar() {
        String categoria = (String) cbxCategoria.getSelectedItem();
        if (categoria == null || categoria.equals(TODAS)) {
            categoria = null;
        }
        try {
            listaActual = Proveedor.listar(txtBuscar.getText(), categoria);
        } catch (SQLException ex) {
            listaActual = new ArrayList<>();
            mostrarError("No se pudieron cargar los proveedores", ex);
        }
        modeloProveedores.setRowCount(0);
        for (Proveedor p : listaActual) {
            modeloProveedores.addRow(new Object[]{
                p.getNombre(), p.getCategoria(), p.getTelefono(), p.getCantidadRepuestos()
            });
        }
        lblCantidad.setText(listaActual.size() + " proveedor(es)");
        if (!listaActual.isEmpty()) {
            tblProveedores.setRowSelectionInterval(0, 0);   // muestra la ficha del primero
        } else {
            mostrarFicha();
        }
    }

    /** Muestra a la derecha los datos y repuestos del proveedor seleccionado. */
    private void mostrarFicha() {
        modeloRepuestos.setRowCount(0);
        int fila = tblProveedores.getSelectedRow();
        if (fila == -1) {
            lblNombre.setText("Seleccioná un proveedor de la lista");
            lblCategoria.setText(" ");
            lblTelefono.setText(" ");
            lblDireccion.setText(" ");
            return;
        }
        Proveedor p = listaActual.get(fila);
        lblNombre.setText(p.getNombre());
        lblCategoria.setText("Categoría:  " + p.getCategoria());
        lblTelefono.setText("Teléfono:  " + p.getTelefono());
        lblDireccion.setText("Dirección:  " + p.getDireccion());
        try {
            for (Object[] r : p.listarRepuestos()) {
                modeloRepuestos.addRow(new Object[]{r[0], r[1], PESOS.format((double) r[2])});
            }
        } catch (SQLException ex) {
            mostrarError("No se pudieron cargar los repuestos", ex);
        }
        if (modeloRepuestos.getRowCount() == 0) {
            modeloRepuestos.addRow(new Object[]{"(sin repuestos cargados)", "", ""});
        }
    }

    /** Devuelve el proveedor seleccionado, o null (con aviso) si no hay ninguno. */
    private Proveedor seleccionado() {
        int fila = tblProveedores.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccioná un proveedor de la lista.");
            return null;
        }
        return listaActual.get(fila);
    }

    /** Abre el formulario: vacio si p es null (Nuevo), con datos si no (Editar). */
    private void abrirFormulario(Proveedor p) {
        provAlta formulario = new provAlta(p, () -> {
            cargarCategorias();   // por si se escribio una categoria nueva
            filtrar();
        });
        formulario.setLocationRelativeTo(this);
        formulario.setVisible(true);
    }

    private void eliminarSeleccionado() {
        Proveedor p = seleccionado();
        if (p == null) {
            return;
        }
        String aviso = "¿Eliminar al proveedor " + p.getNombre() + "?";
        if (p.getCantidadRepuestos() > 0) {
            aviso += "\nTambién se borran los " + p.getCantidadRepuestos() + " repuesto(s) que provee.";
        }
        if (!Dialogo.confirmar(this, "Eliminar proveedor", aviso,
                "Sí, eliminar", true)) {
            return;
        }
        try {
            Proveedor.eliminar(p.getIdProveedor());
            cargarCategorias();
            filtrar();
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

        jLabel1.setText("porveedores");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(panelMenu1, javax.swing.GroupLayout.PREFERRED_SIZE, 863, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(313, 313, 313))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelMenu1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 58, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(42, 42, 42))
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
        java.awt.EventQueue.invokeLater(() -> new proveedores().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private motorgestion.panelMenu panelMenu1;
    // End of variables declaration//GEN-END:variables
}
