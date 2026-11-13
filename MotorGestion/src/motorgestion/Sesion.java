package motorgestion;

/**
 * Guarda quien entro al sistema (nombre y rol) mientras el programa esta abierto.
 * La completa la pantalla de ingreso (Logueo) y la usa el menu para saber
 * que botones mostrar.
 */
public class Sesion {

    private static String nombre = null;   // nombre del empleado que entro
    private static String rol = null;      // "admin" o "empleado"

    /** Se llama cuando el usuario y la contrasena son correctos. */
    public static void iniciar(String nombreEmpleado, String rolUsuario) {
        nombre = nombreEmpleado;
        rol = rolUsuario;
    }

    /** Se llama al cerrar sesion. */
    public static void cerrar() {
        nombre = null;
        rol = null;
    }

    public static boolean hayUsuario() {
        return nombre != null;
    }

    public static String getNombre() {
        return nombre;
    }

    /**
     * true si el que entro es administrador.
     * Si no entro nadie (por ejemplo, al probar una pantalla suelta con
     * Shift+F6 en NetBeans) se muestra todo, para poder probar.
     */
    public static boolean esAdmin() {
        return rol == null || Empleado.ROL_ADMIN.equals(rol);
    }

    /** "Administrador" o "Empleado", para mostrar. */
    public static String getRolTexto() {
        return esAdmin() ? "Administrador" : "Empleado";
    }
}
