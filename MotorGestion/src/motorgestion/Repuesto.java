package motorgestion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un repuesto del inventario del taller y contiene todas las
 * operaciones sobre la tabla "repuestos" de la base de datos.
 *
 * Las ventanas (repuestos e invAlta) NO escriben SQL: llaman a los
 * metodos de esta clase.
 */
public class Repuesto {

    /** Con esta cantidad o menos, el repuesto se marca como "Stock bajo". */
    public static final int STOCK_MINIMO = 3;

    private int idRepuesto;          // 0 = repuesto nuevo (todavia no esta en la base)
    private String nombre;
    private int cantidad;            // unidades en stock
    private double precio;           // precio por unidad
    private int idProveedor;
    private String nombreProveedor;  // solo para mostrar en el listado

    // ------------------------------------------------------------
    // Getters y setters
    // ------------------------------------------------------------
    public int getIdRepuesto() { return idRepuesto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }

    public String getNombreProveedor() { return nombreProveedor; }

    /** Valor de todo el stock de este repuesto (unidades x precio). */
    public double getValorEnStock() {
        return cantidad * precio;
    }

    /** "Sin stock", "Stock bajo" u "OK", segun la cantidad. */
    public String getEstado() {
        if (cantidad == 0) {
            return "Sin stock";
        }
        if (cantidad <= STOCK_MINIMO) {
            return "Stock bajo";
        }
        return "OK";
    }

    // ------------------------------------------------------------
    // Operaciones con la base de datos
    // ------------------------------------------------------------

    /**
     * Devuelve los repuestos que coinciden con los filtros.
     * @param nombreBuscado texto que debe contener el nombre ("" = todos)
     * @param idProveedor proveedor elegido (0 = todos)
     * @param soloStockBajo true para ver solo los que hay que reponer
     */
    public static List<Repuesto> listar(String nombreBuscado, int idProveedor, boolean soloStockBajo)
            throws SQLException {
        String sql = "SELECT r.*, p.nombre AS nombreProveedor "
                   + "FROM repuestos r JOIN proveedor p ON p.idproveedor = r.proveedor_idproveedor "
                   + "WHERE r.nombre LIKE ? "
                   + "AND (? = 0 OR r.proveedor_idproveedor = ?) "
                   + "AND (? = FALSE OR r.cantidad <= ?) "
                   + "ORDER BY r.nombre";
        List<Repuesto> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + nombreBuscado.trim() + "%");
            ps.setInt(2, idProveedor);
            ps.setInt(3, idProveedor);
            ps.setBoolean(4, soloStockBajo);
            ps.setInt(5, STOCK_MINIMO);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Repuesto r = new Repuesto();
                    r.idRepuesto = rs.getInt("idrepuestos");
                    r.nombre = rs.getString("nombre");
                    r.cantidad = rs.getInt("cantidad");
                    r.precio = rs.getDouble("precio");
                    r.idProveedor = rs.getInt("proveedor_idproveedor");
                    r.nombreProveedor = rs.getString("nombreProveedor");
                    lista.add(r);
                }
            }
        }
        return lista;
    }

    /**
     * Resumen de todo el inventario, para las tarjetas de arriba:
     * {repuestos distintos, unidades en stock, valor total, repuestos con stock bajo}.
     */
    public static double[] resumen() throws SQLException {
        String sql = "SELECT COUNT(*), COALESCE(SUM(cantidad), 0), "
                   + "COALESCE(SUM(cantidad * precio), 0), COALESCE(SUM(cantidad <= ?), 0) "
                   + "FROM repuestos";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, STOCK_MINIMO);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new double[]{rs.getDouble(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4)};
            }
        }
    }

    /** Lista de proveedores para los combos, con el formato "id - nombre". */
    public static List<String> listarProveedores() throws SQLException {
        List<String> proveedores = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT idproveedor, nombre FROM proveedor ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                proveedores.add(rs.getInt("idproveedor") + " - " + rs.getString("nombre"));
            }
        }
        return proveedores;
    }

    /** Guarda el repuesto: si es nuevo (id 0) lo inserta, si ya existe lo modifica. */
    public void guardar() throws SQLException {
        try (Connection con = Conexion.getConexion()) {
            if (idRepuesto == 0) {
                // La tabla no tiene AUTO_INCREMENT: calculamos el proximo id
                int nuevoId;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COALESCE(MAX(idrepuestos), 0) + 1 FROM repuestos");
                     ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    nuevoId = rs.getInt(1);
                }
                String sql = "INSERT INTO repuestos (nombre, cantidad, precio, proveedor_idproveedor, idrepuestos) "
                           + "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    cargarParametros(ps, nuevoId);
                    ps.executeUpdate();
                }
                idRepuesto = nuevoId;   // recien ahora que se guardo tiene su id
            } else {
                String sql = "UPDATE repuestos SET nombre = ?, cantidad = ?, precio = ?, "
                           + "proveedor_idproveedor = ? WHERE idrepuestos = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    cargarParametros(ps, idRepuesto);
                    ps.executeUpdate();
                }
            }
        }
    }

    /**
     * Suma (o resta, si es negativo) unidades al stock.
     * Devuelve false si el stock quedaria por debajo de cero (y no lo cambia).
     */
    public static boolean ajustarStock(int idRepuesto, int unidades) throws SQLException {
        String sql = "UPDATE repuestos SET cantidad = cantidad + ? "
                   + "WHERE idrepuestos = ? AND cantidad + ? >= 0";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, unidades);
            ps.setInt(2, idRepuesto);
            ps.setInt(3, unidades);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Elimina el repuesto. Atencion: por como esta hecha la base
     * (ON DELETE CASCADE), tambien se borra de las reparaciones donde se uso.
     */
    public static void eliminar(int idRepuesto) throws SQLException {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM repuestos WHERE idrepuestos = ?")) {
            ps.setInt(1, idRepuesto);
            ps.executeUpdate();
        }
    }

    /** Carga los 5 parametros, en el mismo orden para INSERT y UPDATE. */
    private void cargarParametros(PreparedStatement ps, int id) throws SQLException {
        ps.setString(1, nombre);
        ps.setInt(2, cantidad);
        ps.setDouble(3, precio);
        ps.setInt(4, idProveedor);
        ps.setInt(5, id);
    }
}
