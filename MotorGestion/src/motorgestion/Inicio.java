/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package motorgestion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author User
 */
public class Inicio extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Inicio.class.getName());

    /**
     * Pantalla de inicio: el menu arriba y, en el centro, el logo,
     * la bienvenida y un resumen rapido del taller.
     */
    public Inicio() {
        initComponents();
        setTitle("MotorGestion - Inicio");
        armarPantalla();
        Navegacion.pantallaCompleta(this);
    }

    private void armarPantalla() {
        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panelMenu1, BorderLayout.NORTH);

        // Columna centrada: logo, bienvenida, resumen y ayuda
        JPanel columna = new JPanel();
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        columna.setOpaque(false);

        Image logo = new ImageIcon(getClass().getResource("/motorgestion/Imagenes/LogoHienaHD.png"))
                .getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH);
        JLabel lblLogo = new JLabel(new ImageIcon(logo));

        JLabel lblBienvenida = new JLabel(Sesion.hayUsuario()
                ? "Bienvenido/a, " + Sesion.getNombre()
                : "Bienvenido/a a MotorGestion");
        lblBienvenida.setFont(lblBienvenida.getFont().deriveFont(Font.BOLD, 26f));
        JLabel lblSubtitulo = new JLabel("Sistema de registro de mantenimiento y reparaciones de vehículos");
        lblSubtitulo.setForeground(Estilo.TEXTO_SUAVE);
        lblSubtitulo.setFont(lblSubtitulo.getFont().deriveFont(14f));

        JPanel tarjetas = new JPanel(new GridLayout(1, 5, 16, 0));
        tarjetas.setOpaque(false);
        tarjetas.setMaximumSize(new java.awt.Dimension(1180, 120));
        // Ancho comodo, pero se pueden achicar si la pantalla es mas chica
        tarjetas.setPreferredSize(new java.awt.Dimension(1000, 110));
        tarjetas.setMinimumSize(new java.awt.Dimension(950, 110));
        cargarResumen(tarjetas);

        JLabel lblAyuda = new JLabel("Elegí una sección en el menú de arriba para empezar.");
        lblAyuda.setForeground(Estilo.TEXTO_SUAVE);

        for (JLabel l : new JLabel[]{lblLogo, lblBienvenida, lblSubtitulo, lblAyuda}) {
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        tarjetas.setAlignmentX(Component.CENTER_ALIGNMENT);

        columna.add(lblLogo);
        columna.add(Box.createVerticalStrut(18));
        columna.add(lblBienvenida);
        columna.add(Box.createVerticalStrut(6));
        columna.add(lblSubtitulo);
        columna.add(Box.createVerticalStrut(34));
        columna.add(tarjetas);
        columna.add(Box.createVerticalStrut(28));
        columna.add(lblAyuda);

        JPanel centro = new JPanel(new GridBagLayout());   // centra la columna en la pantalla
        centro.setBorder(new EmptyBorder(20, 28, 20, 28));
        centro.add(columna);
        getContentPane().add(centro, BorderLayout.CENTER);
    }

    /** Cinco tarjetas con numeros del taller, leidos de la base de datos. */
    private void cargarResumen(JPanel tarjetas) {
        String activas = "-", clientes = "-", enTaller = "-", proveedores = "-", stockBajo = "-";
        try {
            double[] r = Reparacion.resumen();
            activas = String.valueOf((int) (r[0] + r[1]));   // pendientes + en proceso
            clientes = String.valueOf(Cliente.listar("", false).size());
            enTaller = String.valueOf(Vehiculo.listar("").stream().filter(Vehiculo::isEnTaller).count());
            proveedores = String.valueOf(Proveedor.listar("", null).size());
            stockBajo = String.valueOf((int) Repuesto.resumen()[3]);
        } catch (SQLException ex) {
            // Sin conexion: las tarjetas muestran "-" y se puede seguir usando el menu
        }
        tarjetas.add(tarjeta("REPARACIONES ACTIVAS", activas, Estilo.ACENTO));
        tarjetas.add(tarjeta("CLIENTES", clientes, Estilo.PRIMARIO));
        tarjetas.add(tarjeta("VEHÍCULOS EN EL TALLER", enTaller, Estilo.PRIMARIO));
        tarjetas.add(tarjeta("PROVEEDORES", proveedores, Estilo.PRIMARIO));
        tarjetas.add(tarjeta("REPUESTOS CON STOCK BAJO", stockBajo, Estilo.ALERTA));
    }

    /** Tarjeta de resumen: numero en el color dado y franja del mismo color arriba. */
    private JPanel tarjeta(String titulo, String numero, java.awt.Color color) {
        JLabel lblNumero = new JLabel(numero);
        lblNumero.setForeground(color);
        return Estilo.tarjetaResumen(titulo, lblNumero, color);
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
        java.awt.EventQueue.invokeLater(() -> new Inicio().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private motorgestion.panelMenu panelMenu1;
    // End of variables declaration//GEN-END:variables
}
