/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package motorgestion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author User
 */
public class clientes extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(clientes.class.getName());

    // ------------------------------------------------------------
    // Componentes del modulo (se arman por codigo en armarPantalla)
    // ------------------------------------------------------------
    /** Colores para las iniciales de cada tarjeta (se elige uno segun el cliente). */
    private static final Color[] COLORES = {
        Estilo.PRIMARIO, Estilo.ACENTO, Estilo.EXITO,
        new Color(0x5B, 0x4B, 0x8A), new Color(0x00, 0x83, 0x8F), Estilo.PRIMARIO_CLARO
    };
    /** Borde de una tarjeta normal y de la tarjeta seleccionada. */
    private static final Border BORDE_NORMAL = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Estilo.BORDE, 1), new EmptyBorder(15, 15, 15, 15));
    private static final Border BORDE_ELEGIDA = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Estilo.PRIMARIO, 2), new EmptyBorder(14, 14, 14, 14));

    private final JTextField txtBuscar = new JTextField(18);
    private final JComboBox<String> cbxOrden = new JComboBox<>(new String[]{"Nombre", "DNI"});
    private final JLabel lblCantidad = new JLabel();
    /** Panel donde van las tarjetas, de a 3 por fila. */
    private final JPanel grilla = new JPanel(new GridLayout(0, 3, 18, 18));

    /** Cliente de la tarjeta seleccionada (null si no hay ninguna). */
    private Cliente elegido = null;
    private JPanel tarjetaElegida = null;

    /**
     * Pantalla de Clientes: una agenda de tarjetas, una por cliente,
     * con sus datos de contacto y los vehiculos que tiene en el taller.
     */
    public clientes() {
        initComponents();
        setTitle("MotorGestion - Clientes");
        armarPantalla();
        cargarTarjetas();
    }

    /** Arma el contenido de la pantalla debajo del menu. */
    private void armarPantalla() {
        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panelMenu1, BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(0, 18));
        contenido.setBorder(new EmptyBorder(20, 28, 20, 28));

        // --- Encabezado: titulo a la izquierda, acciones a la derecha ---
        JLabel lblTitulo = new JLabel("CLIENTES");
        Estilo.titulo(lblTitulo);
        JLabel lblSubtitulo = new JLabel("Agenda de clientes del taller y sus vehículos");
        lblSubtitulo.setForeground(Estilo.TEXTO_SUAVE);
        JPanel titulos = new JPanel(new GridLayout(2, 1));
        titulos.add(lblTitulo);
        titulos.add(lblSubtitulo);

        JButton btnNuevo = new JButton("+ Nuevo cliente");
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

        // --- Busqueda y orden ---
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filtros.add(new JLabel("Buscar por nombre o DNI:"));
        filtros.add(txtBuscar);
        filtros.add(new JLabel("Ordenar por:"));
        filtros.add(cbxOrden);
        JPanel barraFiltros = new JPanel(new BorderLayout());
        barraFiltros.add(filtros, BorderLayout.WEST);
        barraFiltros.add(lblCantidad, BorderLayout.EAST);

        JPanel encabezado = new JPanel(new BorderLayout(0, 14));
        encabezado.add(filaTitulo, BorderLayout.NORTH);
        encabezado.add(barraFiltros, BorderLayout.CENTER);

        // --- Tarjetas (con barra de desplazamiento si son muchas) ---
        JPanel envoltorio = new JPanel(new BorderLayout());   // evita que las tarjetas se estiren hacia abajo
        envoltorio.add(grilla, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(envoltorio);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JLabel lblAyuda = new JLabel("Hacé clic en una tarjeta para elegirla, o doble clic para editarla.");
        lblAyuda.setForeground(Estilo.TEXTO_SUAVE);

        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(scroll, BorderLayout.CENTER);
        contenido.add(lblAyuda, BorderLayout.SOUTH);
        getContentPane().add(contenido, BorderLayout.CENTER);

        // --- Que hace cada control ---
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { cargarTarjetas(); }
            @Override public void removeUpdate(DocumentEvent e) { cargarTarjetas(); }
            @Override public void changedUpdate(DocumentEvent e) { cargarTarjetas(); }
        });
        cbxOrden.addActionListener(e -> cargarTarjetas());
        btnNuevo.addActionListener(e -> abrirFormulario(null));
        btnEditar.addActionListener(e -> {
            if (hayElegido()) {
                abrirFormulario(elegido);
            }
        });
        btnEliminar.addActionListener(e -> eliminarElegido());

        Navegacion.pantallaCompleta(this);   // ocupa toda la pantalla
    }

    /** Vuelve a dibujar las tarjetas segun la busqueda y el orden elegidos. */
    private void cargarTarjetas() {
        List<Cliente> lista;
        try {
            lista = Cliente.listar(txtBuscar.getText(), cbxOrden.getSelectedIndex() == 1);
        } catch (SQLException ex) {
            lista = new ArrayList<>();
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los clientes:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        elegido = null;
        tarjetaElegida = null;
        grilla.removeAll();
        for (Cliente c : lista) {
            grilla.add(crearTarjeta(c));
        }
        lblCantidad.setText(lista.size() + " cliente(s)");
        grilla.revalidate();   // avisa que cambiaron los componentes
        grilla.repaint();      // y los vuelve a dibujar
    }

    /** Crea la tarjeta de un cliente: iniciales, nombre, DNI, telefono y vehiculos. */
    private JPanel crearTarjeta(Cliente c) {
        // Cuadrado de color con las iniciales
        JLabel lblIniciales = new JLabel(c.getIniciales(), SwingConstants.CENTER);
        lblIniciales.setOpaque(true);
        lblIniciales.setBackground(COLORES[c.getIdCliente() % COLORES.length]);
        lblIniciales.setForeground(Color.WHITE);
        lblIniciales.setFont(lblIniciales.getFont().deriveFont(Font.BOLD, 18f));
        lblIniciales.setPreferredSize(new Dimension(48, 48));

        JLabel lblNombre = new JLabel(c.getNombre());
        lblNombre.setFont(lblNombre.getFont().deriveFont(Font.BOLD, 15f));
        JLabel lblDni = new JLabel("DNI " + c.getDniConPuntos());
        lblDni.setForeground(Estilo.TEXTO_SUAVE);
        JPanel nombreYDni = new JPanel(new GridLayout(2, 1));
        nombreYDni.setOpaque(false);
        nombreYDni.add(lblNombre);
        nombreYDni.add(lblDni);

        JPanel cabecera = new JPanel(new BorderLayout(12, 0));
        cabecera.setOpaque(false);
        cabecera.add(lblIniciales, BorderLayout.WEST);
        cabecera.add(nombreYDni, BorderLayout.CENTER);
        cabecera.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE),   // linea debajo
                new EmptyBorder(0, 0, 12, 0)));

        // Datos de contacto y vehiculos
        JLabel lblTelefono = new JLabel("Teléfono:  " + c.getTelefono());
        JLabel lblVehiculos;
        if (c.getCantidadVehiculos() == 0) {
            lblVehiculos = new JLabel("Sin vehículos cargados");
            lblVehiculos.setForeground(Estilo.TEXTO_SUAVE);
        } else {
            lblVehiculos = new JLabel("Vehículos (" + c.getCantidadVehiculos() + "):  " + c.getMatriculas());
        }
        JPanel datos = new JPanel(new GridLayout(2, 1, 0, 6));
        datos.setOpaque(false);
        datos.add(lblTelefono);
        datos.add(lblVehiculos);

        JPanel tarjeta = new JPanel(new BorderLayout(0, 12));
        tarjeta.setBackground(Estilo.SUPERFICIE);
        tarjeta.setBorder(BORDE_NORMAL);
        tarjeta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tarjeta.add(cabecera, BorderLayout.NORTH);
        tarjeta.add(datos, BorderLayout.CENTER);

        // Clic = elegir la tarjeta; doble clic = editar
        tarjeta.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                elegir(c, tarjeta);
                if (e.getClickCount() == 2) {
                    abrirFormulario(c);
                }
            }
        });
        return tarjeta;
    }

    /** Marca una tarjeta como elegida (borde azul) y desmarca la anterior. */
    private void elegir(Cliente c, JPanel tarjeta) {
        if (tarjetaElegida != null) {
            tarjetaElegida.setBorder(BORDE_NORMAL);
        }
        elegido = c;
        tarjetaElegida = tarjeta;
        tarjeta.setBorder(BORDE_ELEGIDA);
    }

    /** true si hay una tarjeta elegida; si no, avisa. */
    private boolean hayElegido() {
        if (elegido == null) {
            JOptionPane.showMessageDialog(this, "Primero hacé clic en la tarjeta de un cliente.");
            return false;
        }
        return true;
    }

    /** Abre el formulario: vacio si c es null (Nuevo), con datos si no (Editar). */
    private void abrirFormulario(Cliente c) {
        cliAlta formulario = new cliAlta(c, this::cargarTarjetas);
        formulario.setLocationRelativeTo(this);
        formulario.setVisible(true);
    }

    private void eliminarElegido() {
        if (!hayElegido()) {
            return;
        }
        String aviso = "¿Eliminar al cliente " + elegido.getNombre() + "?";
        if (elegido.getCantidadVehiculos() > 0) {
            aviso += "\nTambién se borran sus " + elegido.getCantidadVehiculos()
                   + " vehículo(s) y las reparaciones de esos vehículos.";
        }
        if (!Dialogo.confirmar(this, "Eliminar cliente", aviso,
                "Sí, eliminar", true)) {
            return;
        }
        try {
            Cliente.eliminar(elegido.getIdCliente());
            cargarTarjetas();
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

        jLabel1.setText("CLIENTES");

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
                        .addGap(226, 226, 226)
                        .addComponent(jLabel1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelMenu1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(47, 47, 47)
                .addComponent(jLabel1)
                .addContainerGap(53, Short.MAX_VALUE))
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
        java.awt.EventQueue.invokeLater(() -> new clientes().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private motorgestion.panelMenu panelMenu1;
    // End of variables declaration//GEN-END:variables
}
