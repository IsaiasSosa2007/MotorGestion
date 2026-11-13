/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package motorgestion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author User
 */
public class panelMenu extends javax.swing.JPanel {

    // Botones que no estaban en el diseño original
    private final JButton btnInicio = new JButton("Inicio");
    private final JButton btnReparaciones = new JButton("Reparaciones");
    private final JButton btnSalir = new JButton("Cerrar sesión");
    // Las dos partes del menu (se guardan para poder reacomodarlas)
    private JPanel marca;
    private JPanel centrado;
    /** true cuando el menu esta en dos filas (ventana angosta). */
    private boolean enDosFilas = false;

    /**
     * Barra de menu que aparece arriba de todas las pantallas:
     * a la izquierda el logo y el nombre del sistema, a la derecha
     * los botones para ir a cada seccion.
     */
    public panelMenu() {
        initComponents();
        armarMenu();
    }

    /** Ordena el logo y los botones en una barra azul de alto fijo. */
    private void armarMenu() {
        removeAll();
        setLayout(new BorderLayout(24, 0));
        setBackground(Estilo.PRIMARIO);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, Estilo.ACENTO),   // linea naranja abajo
                new EmptyBorder(10, 24, 10, 24)));

        // --- Izquierda: logo chico + nombre del sistema + quien entro ---
        Image logo = new ImageIcon(getClass().getResource("/motorgestion/Imagenes/LogoHiena3.png"))
                .getImage().getScaledInstance(72, 72, Image.SCALE_SMOOTH);
        lblimagen.setIcon(new ImageIcon(logo));
        lblimagen.setText(null);

        JLabel lblSistema = new JLabel("MotorGestion");
        lblSistema.setFont(lblSistema.getFont().deriveFont(Font.BOLD, 23f));
        lblSistema.setForeground(Color.WHITE);
        JLabel lblTaller = new JLabel("Gestión de talleres · AlfaMotors");
        lblTaller.setForeground(Estilo.TEXTO_CLARO);
        lblTaller.setFont(Estilo.NORMAL.deriveFont(15f));
        // Letras grandes y claras sobre el azul: tienen que leerse en un proyector
        JLabel lblUsuario = new JLabel(Sesion.hayUsuario()
                ? Sesion.getNombre() + "  ·  " + Sesion.getRolTexto()
                : " ");
        lblUsuario.setForeground(Estilo.ACENTO_CLARO);
        lblUsuario.setFont(Estilo.NEGRITA);

        JPanel textos = new JPanel(new GridLayout(3, 1));
        textos.setOpaque(false);
        textos.add(lblSistema);
        textos.add(lblTaller);
        textos.add(lblUsuario);

        marca = new JPanel(new BorderLayout(12, 0));
        marca.setOpaque(false);
        marca.add(lblimagen, BorderLayout.WEST);
        marca.add(textos, BorderLayout.CENTER);

        // --- Derecha: botones de navegacion, todos del mismo tamaño ---
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        for (JButton b : new JButton[]{btnInicio, btnReparaciones, btnClientes, btnVehiculos, btnProveedores, btnRepuestos, btnEmpleados}) {
            prepararBoton(b, 104);
            botones.add(b);
        }
        prepararBoton(btnReparaciones, 116);   // el texto mas largo necesita un poco mas
        prepararBoton(btnSalir, 124);
        botones.add(javax.swing.Box.createHorizontalStrut(12));   // separa "Cerrar sesion" del resto
        botones.add(btnSalir);

        // Empleados solo lo ve el administrador
        btnEmpleados.setVisible(Sesion.esAdmin());
        // Cerrar sesion solo tiene sentido si alguien entro
        btnSalir.setVisible(Sesion.hayUsuario());

        centrado = new JPanel(new GridBagLayout());   // centra los botones en vertical
        centrado.setOpaque(false);
        centrado.add(botones);

        add(marca, BorderLayout.WEST);
        add(centrado, BorderLayout.EAST);
        // Cada vez que cambia el ancho de la ventana, el menu se reacomoda
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ajustarAlAncho();
            }
        });

        btnInicio.addActionListener(e -> {
            if (!(SwingUtilities.getWindowAncestor(this) instanceof Inicio)) {
                Navegacion.ir(new Inicio(), this);
            }
        });
        btnReparaciones.addActionListener(e -> {
            if (!(SwingUtilities.getWindowAncestor(this) instanceof reparaciones)) {
                Navegacion.ir(new reparaciones(), this);
            }
        });
        btnSalir.addActionListener(e -> cerrarSesion());
    }

    /** Mismo tamaño y estilo para todos los botones, sin el recuadro punteado del foco. */
    /**
     * Si el logo y los botones no entran en una sola fila (ventana chica,
     * otra resolucion o un proyector), los botones pasan a una segunda fila
     * debajo del logo, en vez de taparlo.
     */
    private void ajustarAlAncho() {
        int necesario = marca.getPreferredSize().width + centrado.getPreferredSize().width + 24 + 48;
        boolean dosFilas = getWidth() < necesario;
        if (dosFilas == enDosFilas) {
            return;   // ya esta bien acomodado
        }
        enDosFilas = dosFilas;
        remove(centrado);
        if (dosFilas) {
            add(centrado, BorderLayout.SOUTH);
            centrado.setBorder(new EmptyBorder(10, 0, 0, 0));   // espacio entre las dos filas
        } else {
            add(centrado, BorderLayout.EAST);
            centrado.setBorder(null);
        }
        revalidate();
        repaint();
    }

    private void prepararBoton(JButton b, int ancho) {
        Estilo.botonMenu(b, false);
        b.setPreferredSize(new Dimension(ancho, 36));
        b.setFocusable(false);
    }

    /** Cuando la barra ya esta dentro de una ventana, resalta el boton de esa pantalla. */
    @Override
    public void addNotify() {
        super.addNotify();
        Window w = SwingUtilities.getWindowAncestor(this);
        JButton actual = null;
        if (w instanceof Inicio) actual = btnInicio;
        else if (w instanceof reparaciones) actual = btnReparaciones;
        else if (w instanceof clientes) actual = btnClientes;
        else if (w instanceof vehiculos) actual = btnVehiculos;
        else if (w instanceof proveedores) actual = btnProveedores;
        else if (w instanceof repuestos) actual = btnRepuestos;
        else if (w instanceof empleados) actual = btnEmpleados;
        if (actual != null) {
            Estilo.botonMenu(actual, true);   // la pantalla actual queda en naranja
        }
    }

    /** Pregunta, borra la sesion y vuelve a la pantalla de ingreso. */
    private void cerrarSesion() {
        if (!Dialogo.confirmar(this, "Cerrar sesión", "¿Cerrar la sesión de " + Sesion.getNombre() + "?",
                "Cerrar sesión", false)) {
            return;
        }
        Sesion.cerrar();
        Window actual = SwingUtilities.getWindowAncestor(this);
        new Logueo().setVisible(true);
        if (actual != null) {
            actual.dispose();
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

        lblimagen = new javax.swing.JLabel();
        btnEmpleados = new javax.swing.JButton();
        btnClientes = new javax.swing.JButton();
        btnVehiculos = new javax.swing.JButton();
        btnProveedores = new javax.swing.JButton();
        btnRepuestos = new javax.swing.JButton();

        lblimagen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorgestion/Imagenes/LogoHiena3.png"))); // NOI18N

        btnEmpleados.setText("Empleados");
        btnEmpleados.addActionListener(this::btnEmpleadosActionPerformed);

        btnClientes.setText("Clientes");
        btnClientes.addActionListener(this::btnClientesActionPerformed);

        btnVehiculos.setText("Vehiculos");
        btnVehiculos.addActionListener(this::btnVehiculosActionPerformed);

        btnProveedores.setText("Proveedores");
        btnProveedores.addActionListener(this::btnProveedoresActionPerformed);

        btnRepuestos.setText("Repuestos");
        btnRepuestos.addActionListener(this::btnRepuestosActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblimagen, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 71, Short.MAX_VALUE)
                .addComponent(btnEmpleados, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(btnClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(btnVehiculos, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(btnProveedores)
                .addGap(30, 30, 30)
                .addComponent(btnRepuestos, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblimagen, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 13, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(72, 72, 72)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEmpleados, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRepuestos, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnProveedores, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnVehiculos, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnVehiculosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVehiculosActionPerformed
        java.awt.Window actual = javax.swing.SwingUtilities.getWindowAncestor(this);

        // Si ya estamos en esta pantalla, no hacer nada
        if (actual instanceof vehiculos) {
            return;
        }

        // Crear la nueva pantalla
        vehiculos destino = new vehiculos();

        // Copiar estado, tamaño y posición de la ventana actual
        if (actual instanceof javax.swing.JFrame) {
            int estado = ((javax.swing.JFrame) actual).getExtendedState();
            destino.setExtendedState(estado);

            if ((estado & javax.swing.JFrame.MAXIMIZED_BOTH) != javax.swing.JFrame.MAXIMIZED_BOTH) {
                destino.setSize(actual.getSize());
                destino.setLocation(actual.getLocation());
            }
        } else {
            destino.setLocationRelativeTo(null);
        }

        // Mostrar la nueva y cerrar la anterior
        destino.setVisible(true);
        if (actual != null) {
            actual.dispose();
        }
    }//GEN-LAST:event_btnVehiculosActionPerformed

    private void btnEmpleadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEmpleadosActionPerformed
        java.awt.Window actual = javax.swing.SwingUtilities.getWindowAncestor(this);

        // Si ya estamos en esta pantalla, no hacer nada
        if (actual instanceof empleados) {
            return;
        }

        // Crear la nueva pantalla
        empleados destino = new empleados();

        // Copiar estado, tamaño y posición de la ventana actual
        if (actual instanceof javax.swing.JFrame) {
            int estado = ((javax.swing.JFrame) actual).getExtendedState();
            destino.setExtendedState(estado);

            if ((estado & javax.swing.JFrame.MAXIMIZED_BOTH) != javax.swing.JFrame.MAXIMIZED_BOTH) {
                destino.setSize(actual.getSize());
                destino.setLocation(actual.getLocation());
            }
        } else {
            destino.setLocationRelativeTo(null);
        }

        // Mostrar la nueva y cerrar la anterior
        destino.setVisible(true);
        if (actual != null) {
            actual.dispose();
        }
    }//GEN-LAST:event_btnEmpleadosActionPerformed

    private void btnClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClientesActionPerformed
        java.awt.Window actual = javax.swing.SwingUtilities.getWindowAncestor(this);

        // Si ya estamos en esta pantalla, no hacer nada
        if (actual instanceof clientes) {
            return;
        }

        // Crear la nueva pantalla
        clientes destino = new clientes();

        // Copiar estado, tamaño y posición de la ventana actual
        if (actual instanceof javax.swing.JFrame) {
            int estado = ((javax.swing.JFrame) actual).getExtendedState();
            destino.setExtendedState(estado);

            if ((estado & javax.swing.JFrame.MAXIMIZED_BOTH) != javax.swing.JFrame.MAXIMIZED_BOTH) {
                destino.setSize(actual.getSize());
                destino.setLocation(actual.getLocation());
            }
        } else {
            destino.setLocationRelativeTo(null);
        }

        // Mostrar la nueva y cerrar la anterior
        destino.setVisible(true);
        if (actual != null) {
            actual.dispose();
        }
    }//GEN-LAST:event_btnClientesActionPerformed

    private void btnProveedoresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProveedoresActionPerformed
        java.awt.Window actual = javax.swing.SwingUtilities.getWindowAncestor(this);

        // Si ya estamos en esta pantalla, no hacer nada
        if (actual instanceof proveedores) {
            return;
        }

        // Crear la nueva pantalla
        proveedores destino = new proveedores();

        // Copiar estado, tamaño y posición de la ventana actual
        if (actual instanceof javax.swing.JFrame) {
            int estado = ((javax.swing.JFrame) actual).getExtendedState();
            destino.setExtendedState(estado);

            if ((estado & javax.swing.JFrame.MAXIMIZED_BOTH) != javax.swing.JFrame.MAXIMIZED_BOTH) {
                destino.setSize(actual.getSize());
                destino.setLocation(actual.getLocation());
            }
        } else {
            destino.setLocationRelativeTo(null);
        }

        // Mostrar la nueva y cerrar la anterior
        destino.setVisible(true);
        if (actual != null) {
            actual.dispose();
        }
    }//GEN-LAST:event_btnProveedoresActionPerformed

    private void btnRepuestosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRepuestosActionPerformed
        java.awt.Window actual = javax.swing.SwingUtilities.getWindowAncestor(this);

        // Si ya estamos en esta pantalla, no hacer nada
        if (actual instanceof repuestos) {
            return;
        }

        // Crear la nueva pantalla
        repuestos destino = new repuestos();

        // Copiar estado, tamaño y posición de la ventana actual
        if (actual instanceof javax.swing.JFrame) {
            int estado = ((javax.swing.JFrame) actual).getExtendedState();
            destino.setExtendedState(estado);

            if ((estado & javax.swing.JFrame.MAXIMIZED_BOTH) != javax.swing.JFrame.MAXIMIZED_BOTH) {
                destino.setSize(actual.getSize());
                destino.setLocation(actual.getLocation());
            }
        } else {
            destino.setLocationRelativeTo(null);
        }

        // Mostrar la nueva y cerrar la anterior
        destino.setVisible(true);
        if (actual != null) {
            actual.dispose();
        }
    }//GEN-LAST:event_btnRepuestosActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClientes;
    private javax.swing.JButton btnEmpleados;
    private javax.swing.JButton btnProveedores;
    private javax.swing.JButton btnRepuestos;
    private javax.swing.JButton btnVehiculos;
    private javax.swing.JLabel lblimagen;
    // End of variables declaration//GEN-END:variables
}
