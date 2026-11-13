/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package motorgestion;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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
 * Pantalla de Reparaciones: el centro del sistema.
 * Arriba un resumen por estado, a la izquierda la lista de ordenes
 * y a la derecha la ficha de la orden elegida (vehiculo, empleados,
 * repuestos usados y costos), con el boton para avanzar de estado.
 *
 * @author User
 */
public class reparaciones extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(reparaciones.class.getName());

    // ------------------------------------------------------------
    // Componentes del modulo (se arman por codigo en armarPantalla)
    // ------------------------------------------------------------
    private static final String TODOS = "Todos los estados";
    private static final NumberFormat PESOS = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"));
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM/yy");

    // Tarjetas de resumen (arriba)
    private final JLabel lblPendientes = numeroTarjeta(Estilo.ALERTA);
    private final JLabel lblEnProceso = numeroTarjeta(Estilo.PRIMARIO);
    private final JLabel lblEntregadas = numeroTarjeta(Estilo.EXITO);
    private final JLabel lblCobrado = numeroTarjeta(Estilo.PRIMARIO);

    // Filtros
    private final JTextField txtBuscar = new JTextField(16);
    private final JComboBox<String> cbxEstado = new JComboBox<>(new String[]{
        TODOS, Reparacion.PENDIENTE, Reparacion.EN_PROCESO, Reparacion.ENTREGADO});
    private final JLabel lblCantidad = new JLabel();

    // Lista de reparaciones
    private final DefaultTableModel modeloTabla = soloLectura("N°", "Ingreso", "Matrícula", "Cliente", "Estado", "Total");
    private final JTable tblReparaciones = new JTable(modeloTabla);

    // Ficha de la reparacion elegida
    private final CardLayout tarjetasFicha = new CardLayout();
    private final JPanel panelFicha = new JPanel(tarjetasFicha);
    private final JLabel lblOrden = new JLabel();
    private final JLabel lblEstado = new JLabel();
    private final JLabel lblVehiculo = new JLabel();
    private final JLabel lblCliente = new JLabel();
    private final JLabel lblFechas = new JLabel();
    private final JLabel lblDetalles = new JLabel();
    private final JLabel lblEmpleados = new JLabel();
    private final DefaultTableModel modeloRepuestos = soloLectura("Repuesto", "Cant.", "Precio unit.", "Subtotal");
    private final JTable tblRepuestos = new JTable(modeloRepuestos);
    private final JLabel lblCostoRepuestos = new JLabel();
    private final JLabel lblManoObra = new JLabel();
    private final JLabel lblTotal = new JLabel();
    private final JButton btnAvanzar = new JButton();

    /** Reparaciones que se ven en la tabla, en el mismo orden que las filas. */
    private List<Reparacion> listaActual = new ArrayList<>();

    public reparaciones() {
        initComponents();
        setTitle("MotorGestion - Reparaciones");
        armarPantalla();
        actualizar();
    }

    /** Crea un modelo de tabla que no se puede editar escribiendo en las celdas. */
    private static DefaultTableModel soloLectura(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    /** Numero grande de una tarjeta de resumen, en el color de su estado. */
    private static JLabel numeroTarjeta(Color color) {
        JLabel numero = new JLabel("-");
        numero.setFont(numero.getFont().deriveFont(Font.BOLD, 24f));
        numero.setForeground(color);
        return numero;
    }

    /** Tarjeta de resumen con la franja del color del estado (el diseño esta en Estilo). */
    private static JPanel tarjeta(String titulo, JLabel numero, Color color) {
        return Estilo.tarjetaResumen(titulo, numero, color);
    }

    /** Color que identifica a cada estado en todo el sistema. */
    public static Color colorEstado(String estado) {
        if (Reparacion.PENDIENTE.equals(estado)) {
            return Estilo.ALERTA;
        }
        if (Reparacion.EN_PROCESO.equals(estado)) {
            return Estilo.PRIMARIO;
        }
        return Estilo.EXITO;
    }

    /** Titulo chico de una seccion de la ficha. */
    private static JLabel tituloSeccion(String texto) {
        JLabel titulo = new JLabel(texto);
        Estilo.tituloSeccion(titulo);
        return titulo;
    }

    /** Arma el contenido de la pantalla debajo del menu. */
    private void armarPantalla() {
        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panelMenu1, BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(0, 16));
        contenido.setBorder(new EmptyBorder(20, 28, 16, 28));

        // --- Titulo y acciones ---
        JLabel lblTitulo = new JLabel("REPARACIONES");
        Estilo.titulo(lblTitulo);
        JLabel lblSubtitulo = new JLabel("Órdenes de trabajo del taller: qué se le hace a cada vehículo, quién lo hace y cuánto cuesta");
        Estilo.subtitulo(lblSubtitulo);
        JPanel titulos = new JPanel(new GridLayout(2, 1));
        titulos.add(lblTitulo);
        titulos.add(lblSubtitulo);

        JButton btnNuevo = new JButton("+ Nueva reparación");
        Estilo.botonPrimario(btnNuevo);
        JButton btnEditar = new JButton("Editar");
        Estilo.botonSecundario(btnEditar);
        JButton btnEliminar = new JButton("Eliminar");
        Estilo.botonPeligro(btnEliminar);
        btnEliminar.setVisible(Sesion.esAdmin());   // eliminar una orden solo puede el administrador
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.add(btnNuevo);
        acciones.add(btnEditar);
        acciones.add(btnEliminar);

        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.add(titulos, BorderLayout.WEST);
        filaTitulo.add(acciones, BorderLayout.EAST);

        // --- Tarjetas de resumen ---
        JPanel tarjetas = new JPanel(new GridLayout(1, 4, 16, 0));
        tarjetas.add(tarjeta("PENDIENTES", lblPendientes, Estilo.ALERTA));
        tarjetas.add(tarjeta("EN PROCESO", lblEnProceso, Estilo.PRIMARIO));
        tarjetas.add(tarjeta("ENTREGADAS", lblEntregadas, Estilo.EXITO));
        tarjetas.add(tarjeta("COBRADO EN ENTREGADAS", lblCobrado, Estilo.ACENTO));

        JPanel cabecera = new JPanel(new BorderLayout(0, 16));
        cabecera.add(filaTitulo, BorderLayout.NORTH);
        cabecera.add(tarjetas, BorderLayout.CENTER);

        // --- Filtros ---
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filtros.add(new JLabel("Buscar por matrícula o cliente:"));
        filtros.add(txtBuscar);
        filtros.add(new JLabel("Estado:"));
        filtros.add(cbxEstado);
        lblCantidad.setForeground(Estilo.TEXTO_SUAVE);
        JPanel barraFiltros = new JPanel(new BorderLayout());
        barraFiltros.add(filtros, BorderLayout.WEST);
        barraFiltros.add(lblCantidad, BorderLayout.EAST);

        // --- Izquierda: lista de reparaciones ---
        Estilo.tabla(tblReparaciones);
        tblReparaciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // (el vehiculo completo se ve en la ficha de la derecha)
        int[] anchos = {55, 85, 105, 170, 110, 125};
        for (int col = 0; col < anchos.length; col++) {
            tblReparaciones.getColumnModel().getColumn(col).setPreferredWidth(anchos[col]);
            boolean esTexto = (col == 3);   // el Cliente con margen; el resto centrado
            tblReparaciones.getColumnModel().getColumn(col).setCellRenderer(
                    esTexto ? Estilo.celdaTexto() : Estilo.celdaCentrada());
        }
        // La columna Estado se pinta con el color de cada estado
        tblReparaciones.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                    boolean foco, int fila, int columna) {
                JLabel celda = (JLabel) super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
                celda.setHorizontalAlignment(SwingConstants.CENTER);
                celda.setFont(celda.getFont().deriveFont(Font.BOLD));
                celda.setForeground(colorEstado(String.valueOf(valor)));
                return celda;
            }
        });
        JScrollPane scrollLista = new JScrollPane(tblReparaciones);
        scrollLista.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        scrollLista.getViewport().setBackground(Estilo.SUPERFICIE);
        // La lista va en una caja igual a la de la ficha, asi las dos se ven parejas
        JPanel lista = new JPanel(new BorderLayout(0, 10));
        lista.setBackground(Estilo.SUPERFICIE);
        lista.setBorder(Estilo.bordeCaja());
        lista.add(tituloSeccion("ÓRDENES DE TRABAJO"), BorderLayout.NORTH);
        lista.add(scrollLista, BorderLayout.CENTER);
        lista.setPreferredSize(new Dimension(520, 200));

        // --- Derecha: ficha de la reparacion ---
        JLabel lblVacio = new JLabel("Elegí una reparación de la lista para ver su detalle.", SwingConstants.CENTER);
        lblVacio.setForeground(Estilo.TEXTO_SUAVE);
        panelFicha.setBackground(Estilo.SUPERFICIE);
        panelFicha.setBorder(Estilo.bordeCaja());
        panelFicha.add(lblVacio, "vacio");
        panelFicha.add(armarFicha(), "ficha");
        panelFicha.setPreferredSize(new Dimension(400, 200));

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, lista, panelFicha);
        division.setResizeWeight(0.6);   // el espacio que sobra se reparte 60% / 40%
        // Cuando cambia el tamanio de la ventana, la lista vuelve a ocupar el 55% del ancho
        // (asi en una pantalla chica la lista no queda aplastada)
        division.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                division.setDividerLocation(0.55);
            }
        });
        division.setBorder(null);
        division.setDividerSize(16);

        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.add(barraFiltros, BorderLayout.NORTH);
        centro.add(division, BorderLayout.CENTER);

        contenido.add(cabecera, BorderLayout.NORTH);
        contenido.add(centro, BorderLayout.CENTER);
        getContentPane().add(contenido, BorderLayout.CENTER);

        // --- Que hace cada control ---
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { cargarTabla(); }
            @Override public void removeUpdate(DocumentEvent e) { cargarTabla(); }
            @Override public void changedUpdate(DocumentEvent e) { cargarTabla(); }
        });
        cbxEstado.addActionListener(e -> cargarTabla());
        tblReparaciones.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarFicha();
            }
        });
        tblReparaciones.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {   // doble clic = Editar
                    editarSeleccionada();
                }
            }
        });
        btnNuevo.addActionListener(e -> abrirFormulario(null));
        btnEditar.addActionListener(e -> editarSeleccionada());
        btnEliminar.addActionListener(e -> eliminarSeleccionada());
        btnAvanzar.addActionListener(e -> avanzarEstado());

        Navegacion.pantallaCompleta(this);   // ocupa toda la pantalla
    }

    /** Agrega componentes uno debajo del otro, alineados a la izquierda. */
    private static JPanel columna(Component... componentes) {
        JPanel columna = new JPanel();
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        columna.setOpaque(false);
        for (Component c : componentes) {
            ((javax.swing.JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
            columna.add(c);
        }
        return columna;
    }

    /**
     * Acomoda una parte de un panel segun el ancho disponible: si "fija" y "movil"
     * entran en un renglon, "movil" va en lugarAncho (al costado); si no, va en
     * lugarAngosto (abajo). Asi en una pantalla chica nada se encima ni se corta.
     */
    private static void acomodarSegunAncho(JPanel panel, JComponent fija, JComponent movil,
                                           String lugarAncho, String lugarAngosto) {
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int necesario = fija.getPreferredSize().width + movil.getPreferredSize().width + 8;
                String lugar = (panel.getWidth() >= necesario) ? lugarAncho : lugarAngosto;
                if (!lugar.equals(((BorderLayout) panel.getLayout()).getConstraints(movil))) {
                    panel.remove(movil);
                    panel.add(movil, lugar);
                    panel.revalidate();
                }
            }
        });
    }

    /** Arma la ficha (lado derecho) con sus tres partes: datos, repuestos y costos. */
    private JPanel armarFicha() {
        // 1) Datos de la orden: a la izquierda el vehiculo, a la derecha el trabajo
        lblOrden.setFont(lblOrden.getFont().deriveFont(Font.BOLD, 20f));
        lblOrden.setForeground(Estilo.PRIMARIO);
        lblEstado.setOpaque(true);   // "etiqueta" de color con el estado
        lblEstado.setForeground(Color.WHITE);
        lblEstado.setFont(Estilo.CHICA);
        lblEstado.setBorder(new EmptyBorder(4, 10, 4, 10));
        JPanel filaOrden = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaOrden.setOpaque(false);
        filaOrden.add(lblOrden);
        filaOrden.add(Box.createHorizontalStrut(12));
        filaOrden.add(lblEstado);

        // Botones de la orden, a la derecha del numero
        Estilo.botonPrimario(btnAvanzar);
        JButton btnHistorial = new JButton("Historial");
        btnHistorial.setToolTipText("Ver todas las reparaciones de este vehículo");
        Estilo.botonSecundario(btnHistorial);
        btnHistorial.addActionListener(e -> verHistorial());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnHistorial);
        botones.add(btnAvanzar);

        JPanel encabezadoFicha = new JPanel(new BorderLayout(0, 8));
        encabezadoFicha.setOpaque(false);
        encabezadoFicha.setBorder(new EmptyBorder(0, 0, 10, 0));
        encabezadoFicha.add(filaOrden, BorderLayout.WEST);
        encabezadoFicha.add(botones, BorderLayout.EAST);
        // Si no entran al lado del numero, los botones bajan a un segundo renglon
        acomodarSegunAncho(encabezadoFicha, filaOrden, botones, BorderLayout.EAST, BorderLayout.SOUTH);

        lblVehiculo.setFont(lblVehiculo.getFont().deriveFont(Font.BOLD, 14f));
        JPanel vehiculo = columna(lblVehiculo, Box.createVerticalStrut(4), lblCliente,
                Box.createVerticalStrut(4), lblFechas);
        JPanel trabajo = columna(tituloSeccion("TRABAJO A REALIZAR"), Box.createVerticalStrut(2), lblDetalles,
                Box.createVerticalStrut(8), tituloSeccion("EMPLEADOS A CARGO"), Box.createVerticalStrut(2), lblEmpleados);
        JPanel dosColumnas = new JPanel(new GridLayout(1, 2, 16, 0));
        dosColumnas.setOpaque(false);
        dosColumnas.add(vehiculo);
        dosColumnas.add(trabajo);
        JPanel datos = new JPanel(new BorderLayout());
        datos.setOpaque(false);
        datos.add(encabezadoFicha, BorderLayout.NORTH);
        datos.add(dosColumnas, BorderLayout.CENTER);
        datos.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE),   // linea que separa las partes
                new EmptyBorder(0, 0, 14, 0)));

        // 2) Repuestos usados
        Estilo.tabla(tblRepuestos);
        tblRepuestos.setRowSelectionAllowed(false);
        tblRepuestos.setFocusable(false);
        tblRepuestos.getColumnModel().getColumn(0).setCellRenderer(Estilo.celdaTexto());
        for (int col = 1; col <= 3; col++) {
            tblRepuestos.getColumnModel().getColumn(col).setCellRenderer(Estilo.celdaCentrada());
        }
        JScrollPane scrollRepuestos = new JScrollPane(tblRepuestos);
        scrollRepuestos.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        scrollRepuestos.getViewport().setBackground(Estilo.SUPERFICIE);
        JPanel bloqueRepuestos = new JPanel(new BorderLayout(0, 6));
        bloqueRepuestos.setOpaque(false);
        bloqueRepuestos.add(tituloSeccion("REPUESTOS USADOS"), BorderLayout.NORTH);
        bloqueRepuestos.add(scrollRepuestos, BorderLayout.CENTER);

        // 3) Costos: repuestos + mano de obra = total
        lblCostoRepuestos.setFont(Estilo.NEGRITA);
        lblManoObra.setFont(Estilo.NEGRITA);
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 20f));
        lblTotal.setForeground(Estilo.PRIMARIO);
        JPanel suma = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        suma.setOpaque(false);
        suma.add(new JLabel("Repuestos"));
        suma.add(lblCostoRepuestos);
        suma.add(new JLabel(" + Mano de obra"));
        suma.add(lblManoObra);
        JPanel total = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        total.setOpaque(false);
        total.add(new JLabel(" = TOTAL"));
        total.add(lblTotal);

        JPanel pie = new JPanel(new BorderLayout(0, 2));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.BORDE),
                new EmptyBorder(10, 0, 0, 0)));
        pie.add(suma, BorderLayout.CENTER);
        pie.add(total, BorderLayout.EAST);
        // Si no entra en un renglon, el total baja al segundo
        acomodarSegunAncho(pie, suma, total, BorderLayout.EAST, BorderLayout.SOUTH);

        JPanel ficha = new JPanel(new BorderLayout(0, 12));
        ficha.setBackground(Estilo.SUPERFICIE);
        ficha.add(datos, BorderLayout.NORTH);
        ficha.add(bloqueRepuestos, BorderLayout.CENTER);
        ficha.add(pie, BorderLayout.SOUTH);
        return ficha;
    }

    /** Actualiza las tarjetas y la tabla (despues de cualquier cambio). */
    private void actualizar() {
        cargarResumen();
        cargarTabla();
    }

    /** Llena las tarjetas de resumen. */
    private void cargarResumen() {
        try {
            double[] r = Reparacion.resumen();
            lblPendientes.setText(String.valueOf((int) r[0]));
            lblEnProceso.setText(String.valueOf((int) r[1]));
            lblEntregadas.setText(String.valueOf((int) r[2]));
            lblCobrado.setText(PESOS.format(r[3]));
        } catch (SQLException ex) {
            mostrarError("No se pudo calcular el resumen", ex);
        }
    }

    /** Llena la tabla segun la busqueda y el estado elegido. */
    private void cargarTabla() {
        String estado = TODOS.equals(cbxEstado.getSelectedItem()) ? null : (String) cbxEstado.getSelectedItem();
        try {
            listaActual = Reparacion.listar(txtBuscar.getText(), estado);
        } catch (SQLException ex) {
            listaActual = new ArrayList<>();
            mostrarError("No se pudieron cargar las reparaciones", ex);
        }
        modeloTabla.setRowCount(0);
        for (Reparacion r : listaActual) {
            modeloTabla.addRow(new Object[]{
                r.getNumeroOrden(), r.getFechaIngreso().format(FECHA_CORTA), r.getMatricula(),
                r.getNombreCliente(), r.getEstado(), PESOS.format(r.getTotal())
            });
        }
        lblCantidad.setText(listaActual.size() + " reparación(es)  ·  doble clic para editar");
        if (!listaActual.isEmpty()) {
            tblReparaciones.setRowSelectionInterval(0, 0);   // se muestra la primera
        } else {
            tarjetasFicha.show(panelFicha, "vacio");
        }
    }

    /** Muestra en la ficha la reparacion seleccionada en la tabla. */
    private void mostrarFicha() {
        int fila = tblReparaciones.getSelectedRow();
        if (fila == -1) {
            tarjetasFicha.show(panelFicha, "vacio");
            return;
        }
        Reparacion r = listaActual.get(fila);
        try {
            r.cargarDetalle();   // empleados y repuestos de esta reparacion
        } catch (SQLException ex) {
            mostrarError("No se pudo cargar el detalle", ex);
            return;
        }
        lblOrden.setText("ORDEN N° " + r.getNumeroOrden());
        lblEstado.setText(r.getEstado().toUpperCase());
        lblEstado.setBackground(colorEstado(r.getEstado()));
        lblVehiculo.setText(r.getVehiculo() + "  ·  " + r.getMatricula());
        lblCliente.setText("Cliente: " + r.getNombreCliente());
        lblFechas.setText("Ingresó el " + r.getFechaIngreso().format(FECHA)
                + (r.getFechaEgreso() != null ? "   ·   Se entregó el " + r.getFechaEgreso().format(FECHA) : ""));
        // <html> hace que el texto largo baje de renglon en vez de cortarse
        lblDetalles.setText("<html>" + r.getDetalles() + "</html>");
        lblEmpleados.setText(r.getNombresEmpleados());

        modeloRepuestos.setRowCount(0);
        for (Reparacion.RepuestoUsado u : r.getRepuestosUsados()) {
            modeloRepuestos.addRow(new Object[]{u.getNombre(), u.getCantidad(),
                PESOS.format(u.getPrecio()), PESOS.format(u.getSubtotal())});
        }
        if (r.getRepuestosUsados().isEmpty()) {
            modeloRepuestos.addRow(new Object[]{"(todavía no se usaron repuestos)", "", "", ""});
        }
        lblCostoRepuestos.setText(PESOS.format(r.getCostoRepuestos()));
        lblManoObra.setText(PESOS.format(r.getCostoReparo()));
        lblTotal.setText(PESOS.format(r.getTotal()));

        String siguiente = r.getEstadoSiguiente();
        btnAvanzar.setVisible(siguiente != null);
        if (siguiente != null) {
            btnAvanzar.setText(Reparacion.ENTREGADO.equals(siguiente) ? "Entregar" : "Empezar trabajo");
        }
        tarjetasFicha.show(panelFicha, "ficha");
    }

    /** Devuelve la reparacion seleccionada, o null (con aviso) si no hay ninguna. */
    private Reparacion seleccionada() {
        int fila = tblReparaciones.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccioná una reparación de la lista.");
            return null;
        }
        return listaActual.get(fila);
    }

    /** Abre el formulario: vacio si r es null (Nueva), con datos si no (Editar). */
    private void abrirFormulario(Reparacion r) {
        repAlta formulario = new repAlta(r, this::actualizar);
        formulario.setLocationRelativeTo(this);
        formulario.setVisible(true);
    }

    private void editarSeleccionada() {
        Reparacion r = seleccionada();
        if (r != null) {
            abrirFormulario(r);
        }
    }

    /** Pendiente -> En proceso -> Entregado. */
    private void avanzarEstado() {
        Reparacion r = seleccionada();
        if (r == null || r.getEstadoSiguiente() == null) {
            return;
        }
        String siguiente = r.getEstadoSiguiente();
        if (!Dialogo.confirmar(this, "Cambiar estado", "¿Pasar la orden N° " + r.getNumeroOrden() + " (" + r.getMatricula() + ") a \"" + siguiente + "\"?"
                + (Reparacion.ENTREGADO.equals(siguiente) ? "\nSe registra la fecha de hoy como fecha de entrega." : ""),
                "Sí, cambiar", false)) {
            return;
        }
        try {
            Reparacion.cambiarEstado(r.getIdReparacion(), siguiente);
            int fila = tblReparaciones.getSelectedRow();
            actualizar();
            if (fila < tblReparaciones.getRowCount()) {
                tblReparaciones.setRowSelectionInterval(fila, fila);   // se queda en la misma orden
            }
        } catch (SQLException ex) {
            mostrarError("No se pudo cambiar el estado", ex);
        }
    }

    /** Muestra todas las reparaciones del vehiculo elegido (su historial). */
    private void verHistorial() {
        Reparacion r = seleccionada();
        if (r != null) {
            cbxEstado.setSelectedItem(TODOS);
            txtBuscar.setText(r.getMatricula());   // la busqueda filtra sola
        }
    }

    private void eliminarSeleccionada() {
        Reparacion r = seleccionada();
        if (r == null) {
            return;
        }
        if (!Dialogo.confirmar(this, "Eliminar reparación", "¿Eliminar la orden N° " + r.getNumeroOrden() + " de " + r.getMatricula() + "?\n"
                + "Los repuestos que usaba vuelven al stock.",
                "Sí, eliminar", true)) {
            return;
        }
        try {
            Reparacion.eliminar(r.getIdReparacion());
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(panelMenu1, javax.swing.GroupLayout.PREFERRED_SIZE, 863, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelMenu1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(241, Short.MAX_VALUE))
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
        java.awt.EventQueue.invokeLater(() -> new reparaciones().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private motorgestion.panelMenu panelMenu1;
    // End of variables declaration//GEN-END:variables
}
