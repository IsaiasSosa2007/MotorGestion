package motorgestion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * Ventana de confirmacion con el estilo del sistema (reemplaza a la
 * cajita gris de JOptionPane): franja azul con el titulo, el mensaje
 * en letra grande y dos botones. Aparece centrada sobre la pantalla.
 */
public class Dialogo {

    /** Lo que eligio el usuario (se guarda aca porque la ventana es modal). */
    private static boolean respuesta;

    /**
     * Pregunta algo y espera la respuesta.
     * @param padre      componente de la pantalla que pregunta (para centrar la ventana)
     * @param titulo     texto de la franja azul
     * @param mensaje    pregunta; "\n" corta el renglon
     * @param textoSi    texto del boton que acepta (ej. "Sí, eliminar")
     * @param peligroso  true si la accion borra algo (el boton se pinta de rojo)
     * @return true si eligio el boton de aceptar
     */
    public static boolean confirmar(Component padre, String titulo, String mensaje,
                                    String textoSi, boolean peligroso) {
        Window ventana = (padre instanceof Window) ? (Window) padre : SwingUtilities.getWindowAncestor(padre);
        // Modal: mientras esta abierta no se puede tocar la pantalla de atras
        JDialog dialogo = new JDialog(ventana, titulo, JDialog.ModalityType.APPLICATION_MODAL);
        dialogo.setUndecorated(true);   // sin la barra de Windows: la reemplaza la franja azul
        respuesta = false;

        // --- Franja de arriba: titulo en blanco y linea naranja ---
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Estilo.NEGRITA.deriveFont(16f));
        lblTitulo.setForeground(Color.WHITE);
        JPanel franja = new JPanel(new BorderLayout());
        franja.setBackground(Estilo.PRIMARIO);
        franja.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, peligroso ? Estilo.PELIGRO : Estilo.ACENTO),
                new EmptyBorder(12, 20, 12, 20)));
        franja.add(lblTitulo, BorderLayout.WEST);

        // --- Mensaje: <html> permite cortar renglones con <br> ---
        JLabel lblMensaje = new JLabel("<html>" + mensaje.replace("\n", "<br>") + "</html>");
        lblMensaje.setFont(Estilo.NORMAL.deriveFont(15f));
        lblMensaje.setForeground(Estilo.TEXTO);
        lblMensaje.setBorder(new EmptyBorder(22, 24, 22, 24));

        // --- Botones ---
        JButton btnNo = new JButton("Cancelar");
        Estilo.botonSecundario(btnNo);
        JButton btnSi = new JButton(textoSi);
        if (peligroso) {
            Estilo.botonPeligro(btnSi);
        } else {
            Estilo.botonPrimario(btnSi);
        }
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        botones.setBorder(new EmptyBorder(0, 20, 18, 20));
        botones.add(btnNo);
        botones.add(btnSi);

        JPanel cuerpo = new JPanel(new BorderLayout());
        cuerpo.setBackground(Estilo.SUPERFICIE);
        cuerpo.setBorder(BorderFactory.createLineBorder(Estilo.PRIMARIO, 1));   // marco fino
        cuerpo.add(franja, BorderLayout.NORTH);
        cuerpo.add(lblMensaje, BorderLayout.CENTER);
        cuerpo.add(botones, BorderLayout.SOUTH);
        dialogo.setContentPane(cuerpo);

        // --- Que hace cada boton ---
        btnSi.addActionListener(e -> {
            respuesta = true;
            dialogo.dispose();
        });
        btnNo.addActionListener(e -> dialogo.dispose());
        dialogo.getRootPane().setDefaultButton(btnSi);   // Enter = aceptar
        // Escape = cancelar
        dialogo.getRootPane().registerKeyboardAction(e -> dialogo.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialogo.pack();
        if (dialogo.getWidth() < 420) {
            dialogo.setSize(420, dialogo.getHeight());   // ancho minimo para que no quede apretada
        }
        dialogo.setLocationRelativeTo(ventana);   // centrada sobre la pantalla
        dialogo.setVisible(true);                 // espera aca hasta que se cierre
        return respuesta;
    }
}
