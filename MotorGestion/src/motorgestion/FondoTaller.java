package motorgestion;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Polygon;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Panel con el fondo de la pantalla de ingreso: un azul con un degrade
 * suave y un dibujo de tuercas (hexagonos con un circulo adentro) muy
 * transparente, para que tenga relacion con el taller sin distraer.
 */
public class FondoTaller extends JPanel {

    private static final int SEPARACION = 96;   // distancia entre tuercas (pixeles)
    private static final int RADIO = 15;        // tamanio de cada tuerca

    public FondoTaller(LayoutManager layout) {
        super(layout);
    }

    /** Swing llama a este metodo cada vez que hay que dibujar el panel. */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);   // bordes suaves

        // 1) Degrade: arriba un poco mas claro, abajo un poco mas oscuro
        Color arriba = Estilo.PRIMARIO_CLARO;
        Color abajo = Estilo.PRIMARIO.darker();
        g2.setPaint(new GradientPaint(0, 0, arriba, 0, getHeight(), abajo));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // 2) Tuercas: blanco casi transparente (el ultimo numero es la transparencia, de 0 a 255)
        g2.setColor(new Color(255, 255, 255, 20));
        g2.setStroke(new BasicStroke(2f));
        int fila = 0;
        for (int y = SEPARACION / 2; y < getHeight() + SEPARACION; y += SEPARACION) {
            // Las filas impares se corren media separacion, como un panal
            int corrimiento = (fila % 2 == 0) ? 0 : SEPARACION / 2;
            for (int x = corrimiento; x < getWidth() + SEPARACION; x += SEPARACION) {
                dibujarTuerca(g2, x, y);
            }
            fila++;
        }
    }

    /** Dibuja una tuerca: un hexagono y un circulo en el centro (x, y). */
    private void dibujarTuerca(Graphics2D g2, int x, int y) {
        Polygon hexagono = new Polygon();
        for (int i = 0; i < 6; i++) {
            double angulo = Math.toRadians(60 * i);   // 6 puntas, cada 60 grados
            hexagono.addPoint(x + (int) Math.round(RADIO * Math.cos(angulo)),
                              y + (int) Math.round(RADIO * Math.sin(angulo)));
        }
        g2.drawPolygon(hexagono);
        int r = RADIO / 2;
        g2.drawOval(x - r, y - r, r * 2, r * 2);
    }
}
