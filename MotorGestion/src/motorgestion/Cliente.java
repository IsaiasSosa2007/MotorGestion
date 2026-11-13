package motorgestion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un cliente del taller y contiene todas las operaciones
 * sobre la tabla "cliente" de la base de datos.
 *
 * Las ventanas (clientes y cliAlta) NO escriben SQL: llaman a los
 * metodos de esta clase.
 */
public class Cliente {

    private int idCliente;            // 0 = cliente nuevo (todavia no esta en la base)
    private String nombre;
    private String telefono;
    private String dni;
    private int cantidadVehiculos;    // solo para mostrar
    private String matriculas;        // matriculas de sus vehiculos, separadas por " · "

    // ------------------------------------------------------------
    // Getters y setters
    // ------------------------------------------------------------
    public int getIdCliente() { return idCliente; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public int getCantidadVehiculos() { return cantidadVehiculos; }

    public String getMatriculas() { return matriculas; }

    /** DNI con puntos para mostrar: 23456789 -> 23.456.789 */
    public String getDniConPuntos() {
        StringBuilder conPuntos = new StringBuilder(dni);
        for (int i = conPuntos.length() - 3; i > 0; i -= 3) {
            conPuntos.insert(i, '.');
        }
        return conPuntos.toString();
    }

    /** Iniciales del nombre, por ejemplo "Copito Ibarra" -> "CI". */
    public String getIniciales() {
        StringBuilder iniciales = new StringBuilder();
        for (String palabra : nombre.trim().split("\\s+")) {
            if (!palabra.isEmpty() && iniciales.length() < 2) {
                iniciales.append(Character.toUpperCase(palabra.charAt(0)));
            }
        }
        return iniciales.toString();
    }

    // ------------------------------------------------------------
    // Operaciones con la base de datos
    // ------------------------------------------------------------

    /**
     * Devuelve los clientes cuyo nombre o DNI contiene el texto buscado,
     * con la cantidad y las matriculas de sus vehiculos.
     * @param ordenarPorDni true para ordenar por DNI, false por nombre
     */
    public static List<Cliente> listar(String texto, boolean ordenarPorDni) throws SQLException {
        String sql = "SELECT c.*, COUNT(v.idvehiculo) AS cantidadVehiculos, "
                   + "GROUP_CONCAT(v.matricula ORDER BY v.matricula SEPARATOR ' · ') AS matriculas "
                   + "FROM cliente c LEFT JOIN vehiculo v ON v.cliente_idcliente = c.idcliente "
                   + "WHERE c.nombre LIKE ? OR c.dni LIKE ? "
                   + "GROUP BY c.idcliente "
                   + (ordenarPorDni ? "ORDER BY c.dni" : "ORDER BY c.nombre");
        List<Cliente> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String patron = "%" + texto.trim() + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cliente c = new Cliente();
                    c.idCliente = rs.getInt("idcliente");
                    c.nombre = rs.getString("nombre");
                    c.telefono = rs.getString("telefono");
                    c.dni = rs.getString("dni");
                    c.cantidadVehiculos = rs.getInt("cantidadVehiculos");
                    c.matriculas = rs.getString("matriculas");   // null si no tiene vehiculos
                    lista.add(c);
                }
            }
        }
        return lista;
    }

    /**
     * Guarda el cliente: si es nuevo (id 0) lo inserta, si ya existe lo modifica.
     * Si el DNI ya existe, lanza SQLIntegrityConstraintViolationException.
     */
    public void guardar() throws SQLException {
        try (Connection con = Conexion.getConexion()) {
            if (idCliente == 0) {
                // La tabla no tiene AUTO_INCREMENT: calculamos el proximo id
                int nuevoId;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COALESCE(MAX(idcliente), 0) + 1 FROM cliente");
                     ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    nuevoId = rs.getInt(1);
                }
                String sql = "INSERT INTO cliente (nombre, telefono, dni, idcliente) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    cargarParametros(ps, nuevoId);
                    ps.executeUpdate();
                }
                idCliente = nuevoId;   // recien ahora que se guardo tiene su id
            } else {
                String sql = "UPDATE cliente SET nombre = ?, telefono = ?, dni = ? WHERE idcliente = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    cargarParametros(ps, idCliente);
                    ps.executeUpdate();
                }
            }
        }
    }

    /**
     * Elimina el cliente. Atencion: por como esta hecha la base
     * (ON DELETE CASCADE), tambien se borran sus vehiculos y las
     * reparaciones de esos vehiculos.
     */
    public static void eliminar(int idCliente) throws SQLException {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM cliente WHERE idcliente = ?")) {
            ps.setInt(1, idCliente);
            ps.executeUpdate();
        }
    }

    /** Carga los 4 parametros, en el mismo orden para INSERT y UPDATE. */
    private void cargarParametros(PreparedStatement ps, int id) throws SQLException {
        ps.setString(1, nombre);
        ps.setString(2, telefono);
        ps.setString(3, dni);
        ps.setInt(4, id);
    }
}
