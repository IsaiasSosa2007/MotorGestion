package motorgestion;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Cambio de pantalla: abre la nueva ventana maximizada (ocupando toda la
 * pantalla) y cierra la anterior, para que siempre haya una sola abierta.
 */
public class Navegacion {

    /**
     * @param destino la pantalla que se quiere abrir
     * @param desde cualquier componente de la pantalla actual (por ejemplo, un boton)
     */
    public static void ir(JFrame destino, Component desde) {
        Window actual = (desde instanceof Window) ? (Window) desde : SwingUtilities.getWindowAncestor(desde);
        destino.setExtendedState(JFrame.MAXIMIZED_BOTH);
        destino.setVisible(true);
        if (actual != null && actual != destino) {
            actual.dispose();
        }
    }

    /**
     * Deja una pantalla preparada para abrirse a pantalla completa.
     * El tamanio 1280x720 es el que toma si despues se la "restaura".
     */
    public static void pantallaCompleta(JFrame ventana) {
        // Espacio libre de la pantalla (sin la barra de tareas): en una
        // pantalla chica o un proyector, la ventana nunca queda mas grande que eso
        Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        ventana.setSize(Math.min(1280, area.width), Math.min(720, area.height));
        // Tamanio minimo: no se puede achicar tanto que el contenido se aplaste
        ventana.setMinimumSize(new Dimension(Math.min(1000, area.width), Math.min(620, area.height)));
        ventana.setLocationRelativeTo(null);
        ventana.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
}
