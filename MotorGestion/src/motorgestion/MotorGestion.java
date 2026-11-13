package motorgestion;
public class MotorGestion {
    public static void main(String[] args) {
        Estilo.aplicarGlobal();          // colores y letra de todo el sistema (ver Estilo.java)
        new Logueo().setVisible(true);   // abre la ventana de login
    }
}