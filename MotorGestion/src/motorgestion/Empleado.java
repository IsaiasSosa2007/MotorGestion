package motorgestion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un empleado del taller y su acceso al sistema.
 * Trabaja con dos tablas: "empleado" (datos personales) y
 * "usuarios" (usuario, contrasena y rol para entrar al programa).
 *
 * Las ventanas (empleados y empAlta) NO escriben SQL: llaman a los
 * metodos de esta clase.
 */
public class Empleado {

    public static final String ROL_ADMIN = "admin";
    public static final String ROL_EMPLEADO = "empleado";

    private int idEmpleado;             // 0 = empleado nuevo (todavia no esta en la base)
    private String nombre;
    private String telefono;
    private String dni;
    private String usuario;             // null = no puede entrar al sistema
    private String rol;                 // "admin" o "empleado" (null si no tiene usuario)
    private int reparacionesActivas;    // reparaciones Pendientes o En proceso
    private int reparacionesTotales;    // todas las reparaciones en las que trabajo

    // ------------------------------------------------------------
    // Getters y setters
    // ------------------------------------------------------------
    public int getIdEmpleado() { return idEmpleado; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public int getReparacionesActivas() { return reparacionesActivas; }
    public int getReparacionesTotales() { return reparacionesTotales; }

    public boolean tieneAcceso() {
        return usuario != null;
    }

    public boolean esAdmin() {
        return ROL_ADMIN.equals(rol);
    }

    /** Texto para mostrar: "Administrador", "Empleado" o "Sin acceso". */
    public String getAccesoTexto() {
        if (!tieneAcceso()) {
            return "Sin acceso";
        }
        return esAdmin() ? "Administrador" : "Empleado";
    }

    // ------------------------------------------------------------
    // Operaciones con la base de datos
    // ------------------------------------------------------------

    /**
     * Devuelve los empleados cuyo nombre o DNI contiene el texto buscado,
     * con su usuario del sistema y cuantas reparaciones tiene.
     */
    public static List<Empleado> listar(String texto) throws SQLException {
        String sql = "SELECT e.*, u.nombre AS usuario, u.tipoUsuario, "
                   + "(SELECT COUNT(*) FROM empleadoReparacion er "
                   + "   JOIN reparaciones r ON r.idreparaciones = er.reparaciones_idreparaciones "
                   + "   WHERE er.empleado_idempleado = e.idempleado AND r.estado <> 'Entregado') AS activas, "
                   + "(SELECT COUNT(*) FROM empleadoReparacion er "
                   + "   WHERE er.empleado_idempleado = e.idempleado) AS totales "
                   + "FROM empleado e LEFT JOIN usuarios u ON u.empleado_idempleado = e.idempleado "
                   + "WHERE e.nombre LIKE ? OR e.dni LIKE ? "
                   + "ORDER BY e.nombre";
        List<Empleado> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String patron = "%" + texto.trim() + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Empleado e = new Empleado();
                    e.idEmpleado = rs.getInt("idempleado");
                    e.nombre = rs.getString("nombre");
                    e.telefono = rs.getString("telefono");
                    e.dni = rs.getString("dni");
                    e.usuario = rs.getString("usuario");        // null si no tiene
                    e.rol = rs.getString("tipoUsuario");
                    e.reparacionesActivas = rs.getInt("activas");
                    e.reparacionesTotales = rs.getInt("totales");
                    lista.add(e);
                }
            }
        }
        return lista;
    }

    /** true si ese nombre de usuario ya lo usa OTRO empleado. */
    public static boolean usuarioOcupado(String usuario, int idEmpleado) throws SQLException {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) FROM usuarios WHERE nombre = ? AND empleado_idempleado <> ?")) {
            ps.setString(1, usuario);
            ps.setInt(2, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** Cuantos administradores hay (para no dejar el sistema sin ninguno). */
    public static int cantidadAdmins() throws SQLException {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) FROM usuarios WHERE tipoUsuario = ?")) {
            ps.setString(1, ROL_ADMIN);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * Guarda el empleado y su acceso al sistema, todo junto en una TRANSACCION:
     * o se guarda todo, o no se guarda nada.
     * @param claveNueva contrasena nueva, o null para dejar la que tenia
     */
    public void guardar(String claveNueva) throws SQLException {
        boolean eraNuevo = (idEmpleado == 0);
        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);   // empieza la transaccion
            try {
                guardarDatosPersonales(con);
                guardarAcceso(con, claveNueva);
                con.commit();           // todo salio bien: se confirma
            } catch (SQLException ex) {
                con.rollback();         // algo fallo: se deshace todo
                if (eraNuevo) {
                    idEmpleado = 0;     // como se deshizo, sigue siendo nuevo
                }
                throw ex;
            }
        }
    }

    /** Inserta o modifica la fila de la tabla "empleado". */
    private void guardarDatosPersonales(Connection con) throws SQLException {
        if (idEmpleado == 0) {
            // La tabla no tiene AUTO_INCREMENT: calculamos el proximo id
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COALESCE(MAX(idempleado), 0) + 1 FROM empleado");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                idEmpleado = rs.getInt(1);
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO empleado (nombre, telefono, dni, idempleado) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, nombre);
                ps.setString(2, telefono);
                ps.setString(3, dni);
                ps.setInt(4, idEmpleado);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE empleado SET nombre = ?, telefono = ?, dni = ? WHERE idempleado = ?")) {
                ps.setString(1, nombre);
                ps.setString(2, telefono);
                ps.setString(3, dni);
                ps.setInt(4, idEmpleado);
                ps.executeUpdate();
            }
        }
    }

    /** Crea, modifica o borra la fila de la tabla "usuarios" de este empleado. */
    private void guardarAcceso(Connection con, String claveNueva) throws SQLException {
        if (usuario == null) {
            // Sin acceso al sistema: se borra su usuario si tenia
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM usuarios WHERE empleado_idempleado = ?")) {
                ps.setInt(1, idEmpleado);
                ps.executeUpdate();
            }
            return;
        }
        // Primero se intenta modificar el usuario que ya tenia
        String sql = (claveNueva == null)
                ? "UPDATE usuarios SET nombre = ?, tipoUsuario = ? WHERE empleado_idempleado = ?"
                : "UPDATE usuarios SET nombre = ?, tipoUsuario = ?, clave = SHA2(?, 256) WHERE empleado_idempleado = ?";
        int modificados;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, rol);
            if (claveNueva == null) {
                ps.setInt(3, idEmpleado);
            } else {
                ps.setString(3, claveNueva);
                ps.setInt(4, idEmpleado);
            }
            modificados = ps.executeUpdate();
        }
        // Si no tenia usuario, se crea (la contrasena se guarda cifrada)
        if (modificados == 0) {
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO usuarios (nombre, tipoUsuario, clave, empleado_idempleado) "
                  + "VALUES (?, ?, SHA2(?, 256), ?)")) {
                ps.setString(1, usuario);
                ps.setString(2, rol);
                ps.setString(3, claveNueva);
                ps.setInt(4, idEmpleado);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Elimina el empleado. Por como esta hecha la base (ON DELETE CASCADE),
     * tambien se borra su usuario y su asignacion a reparaciones.
     */
    public static void eliminar(int idEmpleado) throws SQLException {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM empleado WHERE idempleado = ?")) {
            ps.setInt(1, idEmpleado);
            ps.executeUpdate();
        }
    }
}
