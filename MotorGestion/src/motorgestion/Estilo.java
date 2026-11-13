package motorgestion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Paleta de colores, tipografia y estilos de MotorGestion.
 *
 * Todas las pantallas toman los colores de aca: si se quiere cambiar
 * un color del sistema, se cambia en un solo lugar.
 */
public class Estilo {

    // ------------------------------------------------------------
    // PALETA DE COLORES
    // ------------------------------------------------------------
    /** Azul petroleo: barra del menu, encabezados de tablas y botones principales. */
    public static final Color PRIMARIO = new Color(0x1F, 0x3A, 0x5F);
    /** Azul un poco mas claro: cuando el mouse pasa por encima. */
    public static final Color PRIMARIO_CLARO = new Color(0x2E, 0x52, 0x80);
    /** Naranja (el del logo de la hiena): lo que hay que destacar. */
    public static final Color ACENTO = new Color(0xE6, 0x7E, 0x22);
    /** Naranja claro: textos destacados sobre el fondo azul (se lee mejor que el naranja comun). */
    public static final Color ACENTO_CLARO = new Color(0xF7, 0xA8, 0x55);
    /** Blanco grisado: fondo de todas las pantallas (asi resaltan las tarjetas y tablas blancas). */
    public static final Color FONDO = new Color(0xE6, 0xEA, 0xEF);
    /** Blanco: tarjetas, tablas y formularios. */
    public static final Color SUPERFICIE = Color.WHITE;
    /** Gris claro: bordes y lineas separadoras. */
    public static final Color BORDE = new Color(0xCF, 0xD6, 0xDF);
    /** Gris medio: lineas de filas y columnas de las tablas (bien visibles sobre el blanco). */
    public static final Color LINEA_TABLA = new Color(0xAE, 0xB9, 0xC6);
    /** Casi negro: texto principal. */
    public static final Color TEXTO = new Color(0x1E, 0x29, 0x3B);
    /** Gris azulado: subtitulos, ayudas y datos secundarios. */
    public static final Color TEXTO_SUAVE = new Color(0x52, 0x60, 0x72);
    /** Celeste grisaceo: textos secundarios sobre el fondo azul (barra del menu, login). */
    public static final Color TEXTO_CLARO = new Color(0xD5, 0xDF, 0xEB);
    /** Gris azulado: "sombra" debajo de las tarjetas, para que se vean como cajas. */
    public static final Color SOMBRA = new Color(0xC4, 0xCC, 0xD6);
    /** Celeste: fila seleccionada en las tablas. */
    public static final Color SELECCION = new Color(0xDC, 0xE7, 0xF3);

    // Colores de estado (siempre con el mismo significado en todo el sistema)
    /** Verde: todo bien (stock OK, dato valido). */
    public static final Color EXITO = new Color(0x2E, 0x7D, 0x32);
    /** Ambar: atencion (stock bajo). */
    public static final Color ALERTA = new Color(0xD9, 0x77, 0x06);
    /** Rojo: problema o accion peligrosa (sin stock, eliminar, dato invalido). */
    public static final Color PELIGRO = new Color(0xC6, 0x28, 0x28);

    // ------------------------------------------------------------
    // TIPOGRAFIA
    // ------------------------------------------------------------
    /** "Segoe UI" (la letra de Windows); si no esta, la letra sin serifa de Java. */
    public static final String LETRA = elegirLetra();
    // Tamanios pensados para que se lean bien tambien en un proyector
    public static final Font NORMAL = new Font(LETRA, Font.PLAIN, 14);
    public static final Font NEGRITA = new Font(LETRA, Font.BOLD, 14);
    public static final Font CHICA = new Font(LETRA, Font.BOLD, 12);
    public static final Font TITULO = new Font(LETRA, Font.BOLD, 22);
    public static final Font NUMERO_GRANDE = new Font(LETRA, Font.BOLD, 26);

    private static String elegirLetra() {
        List<String> instaladas = Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        return instaladas.contains("Segoe UI") ? "Segoe UI" : Font.SANS_SERIF;
    }

    // ------------------------------------------------------------
    // ESTILO GENERAL (se aplica una sola vez, al arrancar el programa)
    // ------------------------------------------------------------
    /**
     * Cambia los valores por defecto de Swing para que TODOS los componentes
     * (incluidos los mensajes de JOptionPane) usen la paleta y la letra.
     * Tiene que llamarse antes de crear la primera ventana.
     */
    public static void aplicarGlobal() {
        // Misma letra para todo
        FontUIResource letra = new FontUIResource(NORMAL);
        for (Object clave : UIManager.getLookAndFeelDefaults().keySet().toArray()) {
            if (clave.toString().endsWith(".font")) {
                UIManager.put(clave, letra);
            }
        }
        // Fondos y textos
        UIManager.put("Panel.background", FONDO);
        UIManager.put("OptionPane.background", FONDO);
        UIManager.put("CheckBox.background", FONDO);
        UIManager.put("TabbedPane.background", FONDO);
        UIManager.put("Viewport.background", SUPERFICIE);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("CheckBox.foreground", TEXTO);
        // Tablas
        UIManager.put("Table.selectionBackground", SELECCION);
        UIManager.put("Table.selectionForeground", TEXTO);
        UIManager.put("Table.gridColor", LINEA_TABLA);
        // Botones comunes: planos y blancos (sin el degradado celeste de Java)
        UIManager.put("Button.background", SUPERFICIE);
        UIManager.put("Button.foreground", TEXTO);
        UIManager.put("Button.gradient", Arrays.asList(0f, 0f, SUPERFICIE, SUPERFICIE, SUPERFICIE));
        UIManager.put("Button.select", SELECCION);
        // Pestanias y barras de progreso
        UIManager.put("TabbedPane.selected", SUPERFICIE);
        UIManager.put("TabbedPane.contentAreaColor", SUPERFICIE);
        UIManager.put("TabbedPane.borderHightlightColor", BORDE);
        UIManager.put("TabbedPane.focus", SUPERFICIE);
        UIManager.put("ProgressBar.foreground", PRIMARIO);
        UIManager.put("ProgressBar.background", BORDE);
        // Mensajes (JOptionPane): letra mas grande para que se lean bien
        UIManager.put("OptionPane.messageFont", new FontUIResource(NORMAL.deriveFont(15f)));
        UIManager.put("OptionPane.buttonFont", new FontUIResource(NEGRITA));
    }

    // ------------------------------------------------------------
    // BOTONES
    // ------------------------------------------------------------
    /** Boton de la accion principal (Nuevo, Guardar, Ingresar): azul con letra blanca. */
    public static void botonPrimario(JButton b) {
        pintar(b, PRIMARIO, PRIMARIO_CLARO, Color.WHITE, BorderFactory.createEmptyBorder(9, 18, 9, 18));
    }

    /** Boton comun (Editar, Cancelar, Buscar): blanco con borde gris. */
    public static void botonSecundario(JButton b) {
        pintar(b, SUPERFICIE, SELECCION, TEXTO, BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE), BorderFactory.createEmptyBorder(8, 17, 8, 17)));
    }

    /** Boton de una accion peligrosa (Eliminar): blanco con letra y borde rojos. */
    public static void botonPeligro(JButton b) {
        pintar(b, SUPERFICIE, new Color(0xFD, 0xEC, 0xEA), PELIGRO, BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PELIGRO), BorderFactory.createEmptyBorder(8, 17, 8, 17)));
    }

    /** Boton de la barra del menu (sobre el fondo azul). */
    public static void botonMenu(JButton b, boolean pantallaActual) {
        if (pantallaActual) {
            pintar(b, ACENTO, ACENTO, Color.WHITE, BorderFactory.createEmptyBorder(8, 9, 8, 9));
        } else {
            pintar(b, PRIMARIO, PRIMARIO_CLARO, Color.WHITE, BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARIO_CLARO), BorderFactory.createEmptyBorder(7, 8, 7, 8)));
        }
        b.setFont(NEGRITA.deriveFont(13.5f));   // un poco mas chica: son 7 botones en una fila
    }

    /**
     * Pinta un boton plano con un color de fondo, otro cuando el mouse
     * pasa por encima, el color de la letra y el borde.
     */
    private static void pintar(JButton b, Color fondo, Color fondoMouse, Color letra, Border borde) {
        b.setUI(new BasicButtonUI());   // boton plano, sin el degradado de Java
        b.putClientProperty("fondo", fondo);             // se guardan los dos colores en el boton
        b.putClientProperty("fondoMouse", fondoMouse);
        b.setBackground(fondo);
        b.setForeground(letra);
        b.setFont(NEGRITA);
        b.setBorder(borde);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Cambia de color cuando el mouse pasa por encima (el aviso se agrega una sola vez,
        // asi un boton se puede volver a pintar con otros colores, como el del menu)
        if (b.getClientProperty("conEstilo") == null) {
            b.putClientProperty("conEstilo", true);
            b.getModel().addChangeListener(e -> b.setBackground((Color) b.getClientProperty(
                    b.getModel().isRollover() ? "fondoMouse" : "fondo")));
        }
    }

    // ------------------------------------------------------------
    // TEXTOS
    // ------------------------------------------------------------
    /** Titulo grande de cada pantalla. */
    public static void titulo(JLabel l) {
        l.setFont(TITULO);
        l.setForeground(PRIMARIO);
    }

    /** Subtitulo gris debajo del titulo. */
    public static void subtitulo(JLabel l) {
        l.setFont(NORMAL);
        l.setForeground(TEXTO_SUAVE);
    }

    /** Titulo chico de seccion o de tarjeta (en mayusculas, gris). */
    public static void tituloSeccion(JLabel l) {
        l.setFont(CHICA);
        l.setForeground(TEXTO_SUAVE);
    }

    // ------------------------------------------------------------
    // TARJETAS Y TABLAS
    // ------------------------------------------------------------
    /**
     * Tarjeta de resumen (las de Inicio, Repuestos y Reparaciones): caja blanca
     * con una franja de color arriba, borde gris y una "sombra" abajo,
     * un titulo chico y un numero grande.
     */
    public static JPanel tarjetaResumen(String titulo, JLabel numero, Color color) {
        JLabel lblTitulo = new JLabel(titulo);
        tituloSeccion(lblTitulo);
        numero.setFont(NUMERO_GRANDE);
        JPanel tarjeta = new JPanel(new BorderLayout(0, 4));
        tarjeta.setBackground(SUPERFICIE);
        Border franja = BorderFactory.createMatteBorder(5, 0, 0, 0, color);              // franja de color
        Border contorno = BorderFactory.createMatteBorder(0, 1, 1, 1, LINEA_TABLA);     // borde gris
        Border sombra = BorderFactory.createMatteBorder(0, 0, 3, 0, SOMBRA);           // sombra abajo
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(sombra, BorderFactory.createCompoundBorder(franja, contorno)),
                new EmptyBorder(12, 18, 12, 18)));
        tarjeta.add(lblTitulo, BorderLayout.NORTH);
        tarjeta.add(numero, BorderLayout.CENTER);
        return tarjeta;
    }

    /**
     * Borde de las cajas grandes (listas y fichas): borde gris, "sombra" abajo
     * y espacio interno. Las dos cajas de una pantalla usan el mismo, asi se ven parejas.
     */
    public static Border bordeCaja() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 3, 0, SOMBRA),
                        BorderFactory.createLineBorder(LINEA_TABLA)),
                new EmptyBorder(14, 18, 14, 18));
    }

    /** Borde de las tarjetas blancas: linea gris fina y espacio interno. */
    public static Border bordeTarjeta(int espacio) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE), new EmptyBorder(espacio, espacio + 4, espacio, espacio + 4));
    }

    /**
     * Da a una tabla el estilo del sistema: encabezado azul con letra blanca,
     * filas altas, lineas grises en filas y columnas y seleccion celeste.
     */
    public static void tabla(JTable t) {
        t.setRowHeight(30);
        t.setFont(NORMAL);
        t.setForeground(TEXTO);
        t.setGridColor(LINEA_TABLA);
        t.setShowHorizontalLines(true);   // linea entre filas
        t.setShowVerticalLines(true);     // linea entre columnas
        t.setSelectionBackground(SELECCION);
        t.setSelectionForeground(TEXTO);
        t.setIntercellSpacing(new Dimension(1, 1));   // deja 1 pixel para dibujar cada linea

        JTableHeader encabezado = t.getTableHeader();
        encabezado.setReorderingAllowed(false);
        encabezado.setPreferredSize(new Dimension(0, 34));
        encabezado.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                    boolean foco, int fila, int columna) {
                JLabel celda = (JLabel) super.getTableCellRendererComponent(tabla, valor, false, false, fila, columna);
                celda.setBackground(PRIMARIO);
                celda.setForeground(Color.WHITE);
                celda.setFont(NEGRITA);
                celda.setHorizontalAlignment(SwingConstants.CENTER);
                celda.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, PRIMARIO_CLARO));
                celda.setOpaque(true);
                return celda;
            }
        });
    }

    /** Renderer para texto con un margen a la izquierda (nombres, descripciones). */
    public static DefaultTableCellRenderer celdaTexto() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                    boolean foco, int fila, int columna) {
                JLabel celda = (JLabel) super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
                celda.setBorder(new EmptyBorder(0, 10, 0, 0));
                return celda;
            }
        };
    }

    /** Renderer para datos cortos o numeros, centrados en su casillero. */
    public static DefaultTableCellRenderer celdaCentrada() {
        DefaultTableCellRenderer centrada = new DefaultTableCellRenderer();
        centrada.setHorizontalAlignment(SwingConstants.CENTER);
        return centrada;
    }
}
