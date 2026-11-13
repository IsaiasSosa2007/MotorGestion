/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package motorgestion;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Formulario para cargar o editar una reparacion.
 * Izquierda: vehiculo, fecha, trabajo, estado y empleados a cargo.
 * Derecha: repuestos usados (controlando el stock) y costos, que se
 * calculan solos mientras se carga.
 *
 * @author User
 */
public class repAlta extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(repAlta.class.getName());

    // ------------------------------------------------------------
    // Campos del formulario (se arman por codigo en armarFormulario)
    // ------------------------------------------------------------
    private static final NumberFormat PESOS = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"));

    private final JComboBox<String> cbxVehiculo = new JComboBox<>();
    /** Fecha con flechitas para subir y bajar el dia. */
    private final JSpinner spnIngreso = new JSpinner(new SpinnerDateModel());
    private final JTextField txtDetalles = new JTextField(26);
    private final JComboBox<String> cbxEstado = new JComboBox<>(new String[]{
        Reparacion.PENDIENTE, Reparacion.EN_PROCESO, Reparacion.ENTREGADO});

    // Empleados: una casilla por empleado (y su id en la misma posicion)
    private final JPanel panelEmpleados = new JPanel(new GridLayout(0, 2, 8, 4));
    private final List<JCheckBox> chkEmpleados = new ArrayList<>();
    private final List<Integer> idsEmpleados = new ArrayList<>();

    // Repuestos
    private final JComboBox<String> cbxRepuesto = new JComboBox<>();
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final DefaultTableModel modeloUsados = new DefaultTableModel(
            new String[]{"Repuesto", "Cant.", "Precio unit.", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tblUsados = new JTable(modeloUsados);

    // Costos
    private final JSpinner spnManoObra = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000000.0, 1000.0));
    private final JLabel lblCostoRepuestos = new JLabel();
    private final JLabel lblTotal = new JLabel();

    /** Reparacion que se esta cargando (nueva) o editando. */
    private final Reparacion reparacion;
    /** Lo que se hace despues de guardar, por ejemplo refrescar la lista. */
    private final Runnable alGuardar;

    /** Repuestos del inventario (en el mismo orden que el combo). */
    private List<Repuesto> inventario = new ArrayList<>();
    /** Repuestos que se van agregando a esta reparacion. */
    private final List<Reparacion.RepuestoUsado> usados = new ArrayList<>();
    /**
     * Cuanto de cada repuesto ya tenia guardado esta reparacion (al editar).
     * Esas unidades vuelven al stock al guardar, asi que tambien estan disponibles.
     */
    private final Map<Integer, Integer> usadosAntes = new HashMap<>();

    /** Formulario vacio, para cargar una reparacion nueva. */
    public repAlta() {
        this(null, null);
    }

    /**
     * Formulario de reparacion.
     * @param r reparacion a editar, o null para cargar una nueva
     * @param alGuardar accion a ejecutar despues de guardar (puede ser null)
     */
    public repAlta(Reparacion r, Runnable alGuardar) {
        initComponents();
        this.reparacion = (r != null) ? r : new Reparacion();
        this.alGuardar = alGuardar;
        // Cerrar este formulario no tiene que cerrar todo el programa
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(r == null ? "MotorGestion - Nueva reparación" : "MotorGestion - Editar reparación");
        cargarListas();
        armarFormulario(r == null ? "NUEVA REPARACIÓN" : "EDITAR REPARACIÓN N° " + r.getNumeroOrden());
        if (r != null) {
            cargarDatos(r);
        }
        actualizarCostos();
    }

    /** Llena los combos y las casillas con los datos de la base. */
    private void cargarListas() {
        try {
            for (String v : Reparacion.listarVehiculos()) {
                cbxVehiculo.addItem(v);
            }
            for (String e : Reparacion.listarEmpleados()) {
                String[] partes = e.split(" - ", 2);   // "2 - Roberto Ríos" -> id y nombre
                idsEmpleados.add(Integer.parseInt(partes[0]));
                JCheckBox chk = new JCheckBox(partes[1]);
                chk.setOpaque(false);
                chkEmpleados.add(chk);
                panelEmpleados.add(chk);
            }
            inventario = Repuesto.listar("", 0, false);
            for (Repuesto rep : inventario) {
                cbxRepuesto.addItem(rep.getNombre() + "  (stock " + rep.getCantidad() + ")");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los datos:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Arma las dos columnas del formulario y los botones. */
    private void armarFormulario(String titulo) {
        getContentPane().removeAll();

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 18f));
        lblTitulo.setForeground(Estilo.PRIMARIO);

        // ---------- Columna izquierda: datos y empleados ----------
        JPanel izquierda = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 0, 5, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        spnIngreso.setEditor(new JSpinner.DateEditor(spnIngreso, "dd/MM/yyyy"));
        cbxVehiculo.setPrototypeDisplayValue("00 - AAA 000 · Volkswagen Gol Trend (Nombre Apellido)");

        int fila = 0;
        fila = agregarSeccion(izquierda, g, fila, "DATOS DE LA ORDEN");
        fila = agregarFila(izquierda, g, fila, "Vehículo:", cbxVehiculo);
        fila = agregarFila(izquierda, g, fila, "Fecha de ingreso:", spnIngreso);
        fila = agregarFila(izquierda, g, fila, "Trabajo a realizar:", txtDetalles);
        fila = agregarFila(izquierda, g, fila, "Estado:", cbxEstado);
        JLabel lblAyudaEstado = new JLabel("Al pasar a Entregado se guarda la fecha de entrega.");
        Estilo.tituloSeccion(lblAyudaEstado);
        lblAyudaEstado.setFont(lblAyudaEstado.getFont().deriveFont(Font.PLAIN));
        g.gridx = 1;
        g.gridy = fila++;
        g.insets = new Insets(0, 0, 5, 8);
        izquierda.add(lblAyudaEstado, g);

        fila = agregarSeccion(izquierda, g, fila, "EMPLEADOS A CARGO");
        panelEmpleados.setOpaque(false);
        g.gridx = 0;
        g.gridy = fila++;
        g.gridwidth = 2;
        izquierda.add(panelEmpleados, g);
        // Espacio que empuja todo hacia arriba
        g.gridy = fila;
        g.weighty = 1;
        izquierda.add(new JLabel(), g);

        // ---------- Columna derecha: repuestos y costos ----------
        JPanel derecha = new JPanel(new BorderLayout(0, 10));

        JLabel lblSeccionRep = new JLabel("REPUESTOS USADOS");
        Estilo.tituloSeccion(lblSeccionRep);
        lblSeccionRep.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE), new EmptyBorder(0, 0, 4, 0)));
        JButton btnAgregar = new JButton("Agregar");
        Estilo.botonSecundario(btnAgregar);
        JPanel agregar = new JPanel(new BorderLayout(8, 0));
        agregar.add(cbxRepuesto, BorderLayout.CENTER);
        JPanel cantidadYBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        cantidadYBoton.add(new JLabel("Cant.:"));
        spnCantidad.setPreferredSize(new Dimension(60, spnCantidad.getPreferredSize().height));
        cantidadYBoton.add(spnCantidad);
        cantidadYBoton.add(btnAgregar);
        agregar.add(cantidadYBoton, BorderLayout.EAST);
        JPanel arribaDerecha = new JPanel(new BorderLayout(0, 8));
        arribaDerecha.add(lblSeccionRep, BorderLayout.NORTH);
        arribaDerecha.add(agregar, BorderLayout.CENTER);

        Estilo.tabla(tblUsados);
        tblUsados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblUsados.getColumnModel().getColumn(0).setPreferredWidth(190);
        tblUsados.getColumnModel().getColumn(0).setCellRenderer(Estilo.celdaTexto());
        for (int col = 1; col <= 3; col++) {
            tblUsados.getColumnModel().getColumn(col).setCellRenderer(Estilo.celdaCentrada());
        }
        JScrollPane scroll = new JScrollPane(tblUsados);
        scroll.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        scroll.getViewport().setBackground(Estilo.SUPERFICIE);
        scroll.setPreferredSize(new Dimension(430, 170));
        JButton btnQuitar = new JButton("Quitar repuesto");
        Estilo.botonSecundario(btnQuitar);
        JPanel quitar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        quitar.add(btnQuitar);
        JPanel centroDerecha = new JPanel(new BorderLayout(0, 6));
        centroDerecha.add(scroll, BorderLayout.CENTER);
        centroDerecha.add(quitar, BorderLayout.SOUTH);

        // Recuadro de costos: se recalcula solo
        spnManoObra.setEditor(new JSpinner.NumberEditor(spnManoObra, "#,##0.00"));
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 18f));
        lblTotal.setForeground(Estilo.PRIMARIO);
        JPanel costos = new JPanel(new GridLayout(3, 2, 10, 6));
        costos.setBackground(Estilo.SUPERFICIE);
        costos.setBorder(Estilo.bordeTarjeta(10));
        costos.add(new JLabel("Mano de obra ($):"));
        costos.add(spnManoObra);
        costos.add(new JLabel("Repuestos:"));
        costos.add(lblCostoRepuestos);
        JLabel lblTextoTotal = new JLabel("TOTAL:");
        lblTextoTotal.setFont(lblTextoTotal.getFont().deriveFont(Font.BOLD, 14f));
        costos.add(lblTextoTotal);
        costos.add(lblTotal);

        derecha.add(arribaDerecha, BorderLayout.NORTH);
        derecha.add(centroDerecha, BorderLayout.CENTER);
        derecha.add(costos, BorderLayout.SOUTH);

        // ---------- Botones ----------
        JButton btnGuardar = new JButton("Guardar");
        Estilo.botonPrimario(btnGuardar);
        JButton btnCancelar = new JButton("Cancelar");
        Estilo.botonSecundario(btnCancelar);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnCancelar);
        botones.add(btnGuardar);

        JPanel columnas = new JPanel(new GridLayout(1, 2, 28, 0));
        columnas.add(izquierda);
        columnas.add(derecha);

        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBorder(new EmptyBorder(22, 26, 22, 26));
        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(columnas, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);

        // ---------- Que hace cada boton ----------
        btnAgregar.addActionListener(e -> agregarRepuesto());
        btnQuitar.addActionListener(e -> quitarRepuesto());
        spnManoObra.addChangeListener(e -> actualizarCostos());
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());

        // Primero setResizable y despues pack: al reves, en Windows la ventana
        // queda unos pixeles mas chica y los botones de abajo quedan pegados al borde
        setResizable(false);
        pack();
    }

    /** Titulo chico de seccion con una linea gris debajo. */
    private int agregarSeccion(JPanel panel, GridBagConstraints g, int fila, String texto) {
        JLabel seccion = new JLabel(texto);
        Estilo.tituloSeccion(seccion);
        seccion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE), new EmptyBorder(0, 0, 4, 0)));
        g.gridx = 0;
        g.gridy = fila;
        g.gridwidth = 2;
        g.insets = new Insets(fila == 0 ? 0 : 14, 0, 6, 8);
        panel.add(seccion, g);
        g.gridwidth = 1;
        g.insets = new Insets(5, 0, 5, 8);
        return fila + 1;
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

    /** Al editar: pone en el formulario los datos de la reparacion. */
    private void cargarDatos(Reparacion r) {
        try {
            r.cargarDetalle();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el detalle:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        for (int i = 0; i < cbxVehiculo.getItemCount(); i++) {
            if (cbxVehiculo.getItemAt(i).startsWith(r.getIdVehiculo() + " - ")) {
                cbxVehiculo.setSelectedIndex(i);
                break;
            }
        }
        // LocalDate -> Date, que es lo que usa el JSpinner
        spnIngreso.setValue(Date.from(r.getFechaIngreso().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        txtDetalles.setText(r.getDetalles());
        cbxEstado.setSelectedItem(r.getEstado());
        for (int i = 0; i < idsEmpleados.size(); i++) {
            chkEmpleados.get(i).setSelected(r.getIdEmpleados().contains(idsEmpleados.get(i)));
        }
        for (Reparacion.RepuestoUsado u : r.getRepuestosUsados()) {
            usados.add(u);
            usadosAntes.put(u.getIdRepuesto(), u.getCantidad());
        }
        spnManoObra.setValue(r.getCostoReparo());
        mostrarUsados();
    }

    /** Agrega el repuesto elegido a la lista, si hay stock. */
    private void agregarRepuesto() {
        int indice = cbxRepuesto.getSelectedIndex();
        if (indice == -1) {
            return;
        }
        Repuesto rep = inventario.get(indice);
        int cantidad = ((Number) spnCantidad.getValue()).intValue();

        // Si ya estaba en la lista, se suman las cantidades
        Reparacion.RepuestoUsado yaAgregado = null;
        for (Reparacion.RepuestoUsado u : usados) {
            if (u.getIdRepuesto() == rep.getIdRepuesto()) {
                yaAgregado = u;
            }
        }
        int enLista = (yaAgregado == null) ? 0 : yaAgregado.getCantidad();
        // Disponible = stock actual + lo que esta reparacion ya tenia guardado
        int disponible = rep.getCantidad() + usadosAntes.getOrDefault(rep.getIdRepuesto(), 0);
        if (enLista + cantidad > disponible) {
            JOptionPane.showMessageDialog(this, "No alcanza el stock de " + rep.getNombre() + ".\n"
                    + "Disponibles: " + disponible + " (ya agregaste " + enLista + ").\n"
                    + "Podés reponerlo desde Repuestos > Ajustar stock.");
            return;
        }
        if (yaAgregado != null) {
            yaAgregado.setCantidad(enLista + cantidad);
        } else {
            usados.add(new Reparacion.RepuestoUsado(rep.getIdRepuesto(), rep.getNombre(), cantidad, rep.getPrecio()));
        }
        spnCantidad.setValue(1);
        mostrarUsados();
    }

    /** Saca de la lista el repuesto seleccionado en la tabla. */
    private void quitarRepuesto() {
        int fila = tblUsados.getSelectedRow();
        if (fila == -1 || fila >= usados.size()) {
            JOptionPane.showMessageDialog(this, "Seleccioná en la tabla el repuesto que querés quitar.");
            return;
        }
        usados.remove(fila);
        mostrarUsados();
    }

    /** Vuelve a llenar la tabla de repuestos usados y recalcula los costos. */
    private void mostrarUsados() {
        modeloUsados.setRowCount(0);
        for (Reparacion.RepuestoUsado u : usados) {
            modeloUsados.addRow(new Object[]{u.getNombre(), u.getCantidad(),
                PESOS.format(u.getPrecio()), PESOS.format(u.getSubtotal())});
        }
        actualizarCostos();
    }

    /** Repuestos + mano de obra = total (se ve mientras se carga). */
    private void actualizarCostos() {
        double costoRepuestos = 0;
        for (Reparacion.RepuestoUsado u : usados) {
            costoRepuestos += u.getSubtotal();
        }
        double manoObra = ((Number) spnManoObra.getValue()).doubleValue();
        lblCostoRepuestos.setText(PESOS.format(costoRepuestos));
        lblTotal.setText(PESOS.format(costoRepuestos + manoObra));
    }

    /** Valida los datos, los pasa a la Reparacion y la guarda en la base. */
    private void guardar() {
        String detalles = txtDetalles.getText().trim();
        // Date -> LocalDate (solo el dia, sin la hora)
        LocalDate ingreso = ((Date) spnIngreso.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        List<Integer> elegidos = new ArrayList<>();
        for (int i = 0; i < chkEmpleados.size(); i++) {
            if (chkEmpleados.get(i).isSelected()) {
                elegidos.add(idsEmpleados.get(i));
            }
        }

        // 1. Validaciones (el largo es el de la tabla "reparaciones")
        if (cbxVehiculo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Elegí el vehículo.\nSi no aparece, primero cargalo en Vehículos.");
            return;
        }
        if (ingreso.isAfter(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "La fecha de ingreso no puede ser posterior a hoy.");
            return;
        }
        if (detalles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribí qué trabajo hay que hacer.");
            txtDetalles.requestFocus();
            return;
        }
        if (detalles.length() > 100) {
            JOptionPane.showMessageDialog(this, "El trabajo a realizar puede tener hasta 100 caracteres.");
            return;
        }
        if (elegidos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Elegí al menos un empleado a cargo.");
            return;
        }

        // 2. Pasar los datos al objeto
        reparacion.setIdVehiculo(Integer.parseInt(cbxVehiculo.getSelectedItem().toString().split(" - ")[0]));
        reparacion.setFechaIngreso(ingreso);
        reparacion.setDetalles(detalles);
        reparacion.setEstado((String) cbxEstado.getSelectedItem());
        reparacion.setIdEmpleados(elegidos);
        reparacion.setRepuestosUsados(usados);
        reparacion.setCostoReparo(((Number) spnManoObra.getValue()).doubleValue());

        // 3. Guardar en la base (descuenta el stock y actualiza "en taller")
        try {
            reparacion.guardar();
            JOptionPane.showMessageDialog(this, "Reparación N° " + reparacion.getNumeroOrden() + " guardada.");
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
        java.awt.EventQueue.invokeLater(() -> new repAlta().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
