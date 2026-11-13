package motorgestion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un proveedor de repuestos y contiene todas las operaciones
 * sobre la tabla "proveedor" de la base de datos.
 *
 * Las ventanas (proveedores y provAlta) NO escriben SQL: llaman a los
 * metodos de esta clase.
 */
public class Proveedor {

    private int idProveedor;          // 0 = proveedor nuevo (todavia no esta en la base)
    private String nombre;
    private String telefono;
    private String direccion;
    private String categoria;
    private int cantidadRepuestos;    // cuantos repuestos distintos provee (solo para mostrar)

    // ------------------------------------------------------------
    // Getters y setters
    // ------------------------------------------------------------
    public int getIdProveedor() { return idProveedor; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getCantidadRepuestos() { return cantidadRepuestos; }

    // ------------------------------------------------------------
    // Operaciones con la base de datos
    // ------------------------------------------------------------

    /**
     * Devuelve los proveedores cuyo nombre contiene el texto buscado y que
     * son de la categoria elegida (null = todas), con la cantidad de
     * repuestos que provee cada uno.
     */
    public static List<Proveedor> listar(String nombreBuscado, String categoria) throws SQLException {
        String sql = "SELECT p.*, COUNT(r.idrepuestos) AS cantidadRepuestos "
                   + "FROM proveedor p LEFT JOIN repuestos r ON r.proveedor_idproveedor = p.idproveedor "
                   + "WHERE p.nombre LIKE ? AND (? IS NULL OR p.categoriaProductos = ?) "
                   + "GROUP BY p.idproveedor ORDER BY p.nombre";
        List<Proveedor> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + nombreBuscado.trim() + "%");
            ps.setString(2, categoria);
            ps.setString(3, categoria);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Proveedor p = new Proveedor();
                    p.idProveedor = rs.getInt("idproveedor");
                    p.nombre = rs.getString("nombre");
                    p.telefono = rs.getString("telefono");
                    p.direccion = rs.getString("direccion");
                    p.categoria = rs.getString("categoriaProductos");
                    p.cantidadRepuestos = rs.getInt("cantidadRepuestos");
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    /** Categorias que ya existen, sin repetir (para los combos). */
    public static List<String> listarCategorias() throws SQLException {
        List<String> categorias = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT DISTINCT categoriaProductos FROM proveedor ORDER BY categoriaProductos");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categorias.add(rs.getString(1));
            }
        }
        return categorias;
    }

    /**
     * Repuestos que provee este proveedor.
     * Cada fila es {nombre, stock, precio}.
     */
    public List<Object[]> listarRepuestos() throws SQLException {
        List<Object[]> repuestos = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT nombre, cantidad, precio FROM repuestos "
                   + "WHERE proveedor_idproveedor = ? ORDER BY nombre")) {
            ps.setInt(1, idProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    repuestos.add(new Object[]{rs.getString("nombre"), rs.getInt("cantidad"), rs.getDouble("precio")});
                }
            }
        }
        return repuestos;
    }

    /** Guarda el proveedor: si es nuevo (id 0) lo inserta, si ya existe lo modifica. */
    public void guardar() throws SQLException {
        try (Connection con = Conexion.getConexion()) {
            if (idProveedor == 0) {
                // La tabla no tiene AUTO_INCREMENT: calculamos el proximo id
                int nuevoId;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COALESCE(MAX(idproveedor), 0) + 1 FROM proveedor");
                     ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    nuevoId = rs.getInt(1);
                }
                String sql = "INSERT INTO proveedor (nombre, telefono, direccion, categoriaProductos, idproveedor) "
                           + "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    cargarParametros(ps, nuevoId);
                    ps.executeUpdate();
                }
                idProveedor = nuevoId;   // recien ahora que se guardo tiene su id
            } else {
                String sql = "UPDATE proveedor SET nombre = ?, telefono = ?, direccion = ?, "
                           + "categoriaProductos = ? WHERE idproveedor = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    cargarParametros(ps, idProveedor);
                    ps.executeUpdate();
                }
            }
        }
    }

    /**
     * Elimina el proveedor. Atencion: por como esta hecha la base
     * (ON DELETE CASCADE), tambien se borran los repuestos que provee.
     */
    public static void eliminar(int idProveedor) throws SQLException {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM proveedor WHERE idproveedor = ?")) {
            ps.setInt(1, idProveedor);
            ps.executeUpdate();
        }
    }

    /** Carga los 5 parametros, en el mismo orden para INSERT y UPDATE. */
    private void cargarParametros(PreparedStatement ps, int id) throws SQLException {
        ps.setString(1, nombre);
        ps.setString(2, telefono);
        ps.setString(3, direccion);
        ps.setString(4, categoria);
        ps.setInt(5, id);
    }
}
