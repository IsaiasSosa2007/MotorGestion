package motorgestion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un vehiculo del taller y contiene todas las operaciones
 * sobre la tabla "vehiculo" de la base de datos.
 *
 * Las ventanas (vehiculos y vehiAlta) NO escriben SQL: llaman a los
 * metodos de esta clase. Si cambia la tabla, se modifica solo esta clase.
 */
public class Vehiculo {

    private int idVehiculo;          // 0 = vehiculo nuevo (todavia no esta en la base)
    private String marca;
    private String modelo;
    private String color;
    private String matricula;
    private boolean enTaller;
    private String cedula;
    private boolean seguro;
    private String empresaSeguros;   // puede ser null si no tiene seguro
    private int idCliente;
    private String nombreCliente;    // solo para mostrar en el listado

    // ------------------------------------------------------------
    // Getters y setters
    // ------------------------------------------------------------
    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public boolean isEnTaller() { return enTaller; }
    public void setEnTaller(boolean enTaller) { this.enTaller = enTaller; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public boolean isSeguro() { return seguro; }
    public void setSeguro(boolean seguro) { this.seguro = seguro; }

    public String getEmpresaSeguros() { return empresaSeguros; }
    public void setEmpresaSeguros(String empresaSeguros) { this.empresaSeguros = empresaSeguros; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getNombreCliente() { return nombreCliente; }

    // ------------------------------------------------------------
    // Operaciones con la base de datos
    // ------------------------------------------------------------

    /**
     * Devuelve los vehiculos cuya matricula contiene el texto buscado,
     * junto con el nombre de su duenio. Con texto vacio devuelve todos.
     */
    public static List<Vehiculo> listar(String matriculaBuscada) throws SQLException {
        String sql = "SELECT v.*, c.nombre AS nombreCliente "
                   + "FROM vehiculo v JOIN cliente c ON c.idcliente = v.cliente_idcliente "
                   + "WHERE v.matricula LIKE ? ORDER BY v.matricula";
        List<Vehiculo> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + matriculaBuscada.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(desdeFila(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Guarda el vehiculo: si es nuevo (id 0) lo inserta, si ya existe lo modifica.
     * Lanza SQLException si, por ejemplo, la matricula o la cedula ya existen.
     */
    public void guardar() throws SQLException {
        try (Connection con = Conexion.getConexion()) {
            if (idVehiculo == 0) {
                // La tabla no tiene AUTO_INCREMENT: calculamos el proximo id
                int nuevoId;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COALESCE(MAX(idvehiculo), 0) + 1 FROM vehiculo");
                     ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    nuevoId = rs.getInt(1);
                }
                String sql = "INSERT INTO vehiculo (marca, modelo, color, matricula, enTaller, "
                           + "cedula, seguro, empresa_seguros, cliente_idcliente, idvehiculo) "
                           + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    cargarParametros(ps, nuevoId);
                    ps.executeUpdate();
                }
                // Recien ahora que se guardo, el vehiculo tiene su id
                idVehiculo = nuevoId;
            } else {
                String sql = "UPDATE vehiculo SET marca = ?, modelo = ?, color = ?, matricula = ?, "
                           + "enTaller = ?, cedula = ?, seguro = ?, empresa_seguros = ?, "
                           + "cliente_idcliente = ? WHERE idvehiculo = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    cargarParametros(ps, idVehiculo);
                    ps.executeUpdate();
                }
            }
        }
    }

    /**
     * Elimina el vehiculo. Atencion: por como esta hecha la base
     * (ON DELETE CASCADE), tambien se borran sus reparaciones.
     */
    public static void eliminar(int idVehiculo) throws SQLException {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM vehiculo WHERE idvehiculo = ?")) {
            ps.setInt(1, idVehiculo);
            ps.executeUpdate();
        }
    }

    /**
     * Lista de clientes para el combo de la ventana de alta,
     * con el formato "id - nombre" (por ejemplo "1 - Copito Ibarra").
     */
    public static List<String> listarClientes() throws SQLException {
        List<String> clientes = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT idcliente, nombre FROM cliente ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientes.add(rs.getInt("idcliente") + " - " + rs.getString("nombre"));
            }
        }
        return clientes;
    }

    // ------------------------------------------------------------
    // Metodos internos
    // ------------------------------------------------------------

    /** Arma un Vehiculo con los datos de la fila actual del ResultSet. */
    private static Vehiculo desdeFila(ResultSet rs) throws SQLException {
        Vehiculo v = new Vehiculo();
        v.idVehiculo = rs.getInt("idvehiculo");
        v.marca = rs.getString("marca");
        v.modelo = rs.getString("modelo");
        v.color = rs.getString("color");
        v.matricula = rs.getString("matricula");
        v.enTaller = rs.getBoolean("enTaller");
        v.cedula = rs.getString("cedula");
        v.seguro = rs.getBoolean("seguro");
        v.empresaSeguros = rs.getString("empresa_seguros");
        v.idCliente = rs.getInt("cliente_idcliente");
        v.nombreCliente = rs.getString("nombreCliente");
        return v;
    }

    /** Carga los 10 parametros, en el mismo orden para INSERT y UPDATE. */
    private void cargarParametros(PreparedStatement ps, int id) throws SQLException {
        ps.setString(1, marca);
        ps.setString(2, modelo);
        ps.setString(3, color);
        ps.setString(4, matricula);
        ps.setBoolean(5, enTaller);
        ps.setString(6, cedula);
        ps.setBoolean(7, seguro);
        if (seguro && empresaSeguros != null && !empresaSeguros.isBlank()) {
            ps.setString(8, empresaSeguros);
        } else {
            ps.setNull(8, java.sql.Types.VARCHAR);
        }
        ps.setInt(9, idCliente);
        ps.setInt(10, id);
    }
}
