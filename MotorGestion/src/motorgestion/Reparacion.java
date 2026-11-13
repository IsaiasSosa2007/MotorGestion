package motorgestion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una reparacion (orden de trabajo) del taller.
 * Trabaja con tres tablas:
 *   - "reparaciones": los datos de la orden (vehiculo, fechas, costos, estado)
 *   - "empleadoReparacion": que empleados trabajan en la reparacion
 *   - "repuestosReparacion": que repuestos se usaron y cuantos
 *
 * Al guardar, tambien descuenta del stock los repuestos usados.
 * Las ventanas (reparaciones y repAlta) NO escriben SQL: llaman a los
 * metodos de esta clase.
 */
public class Reparacion {

    // Los tres estados posibles (son los del ENUM de la tabla)
    public static final String PENDIENTE = "Pendiente";
    public static final String EN_PROCESO = "En proceso";
    public static final String ENTREGADO = "Entregado";

    /** Un repuesto usado en la reparacion: cual, cuantos y a que precio. */
    public static class RepuestoUsado {
        private final int idRepuesto;
        private final String nombre;
        private int cantidad;
        private final double precio;   // precio por unidad al momento de la reparacion

        public RepuestoUsado(int idRepuesto, String nombre, int cantidad, double precio) {
            this.idRepuesto = idRepuesto;
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.precio = precio;
        }

        public int getIdRepuesto() { return idRepuesto; }
        public String getNombre() { return nombre; }
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        public double getPrecio() { return precio; }
        public double getSubtotal() { return cantidad * precio; }
    }

    private int idReparacion;            // 0 = reparacion nueva (todavia no esta en la base)
    private int idVehiculo;
    private LocalDate fechaIngreso;
    private LocalDate fechaEgreso;       // null mientras no se entrega
    private String detalles;
    private double costoRepuestos;       // suma de los repuestos usados
    private double costoReparo;          // mano de obra
    private double total;                // costoRepuestos + costoReparo
    private String estado = PENDIENTE;

    // Solo para mostrar en el listado
    private String matricula;
    private String vehiculo;             // "Marca Modelo"
    private String nombreCliente;
    private String nombresEmpleados;     // "Ana, Juan"

    // Detalle (se carga con cargarDetalle o lo arma el formulario)
    private List<Integer> idEmpleados = new ArrayList<>();
    private List<RepuestoUsado> repuestosUsados = new ArrayList<>();

    // ------------------------------------------------------------
    // Getters y setters
    // ------------------------------------------------------------
    public int getIdReparacion() { return idReparacion; }

    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }

    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public LocalDate getFechaEgreso() { return fechaEgreso; }

    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }

    public double getCostoRepuestos() { return costoRepuestos; }

    public double getCostoReparo() { return costoReparo; }
    public void setCostoReparo(double costoReparo) { this.costoReparo = costoReparo; }

    public double getTotal() { return total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMatricula() { return matricula; }
    public String getVehiculo() { return vehiculo; }
    public String getNombreCliente() { return nombreCliente; }

    /** Nombres de los empleados a cargo, o "Sin asignar". */
    public String getNombresEmpleados() {
        return (nombresEmpleados == null) ? "Sin asignar" : nombresEmpleados;
    }

    public List<Integer> getIdEmpleados() { return idEmpleados; }
    public void setIdEmpleados(List<Integer> idEmpleados) { this.idEmpleados = idEmpleados; }

    public List<RepuestoUsado> getRepuestosUsados() { return repuestosUsados; }
    public void setRepuestosUsados(List<RepuestoUsado> repuestosUsados) { this.repuestosUsados = repuestosUsados; }

    /** Numero de orden con 4 cifras: 7 -> "0007". */
    public String getNumeroOrden() {
        return String.format("%04d", idReparacion);
    }

    /** El estado que sigue: Pendiente -> En proceso -> Entregado (null si ya se entrego). */
    public String getEstadoSiguiente() {
        if (PENDIENTE.equals(estado)) {
            return EN_PROCESO;
        }
        if (EN_PROCESO.equals(estado)) {
            return ENTREGADO;
        }
        return null;
    }

    /** Recalcula los costos a partir de los repuestos usados y la mano de obra. */
    public void calcularCostos() {
        costoRepuestos = 0;
        for (RepuestoUsado r : repuestosUsados) {
            costoRepuestos += r.getSubtotal();
        }
        total = costoRepuestos + costoReparo;
    }

    // ------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------

    /**
     * Devuelve las reparaciones cuyo vehiculo (matricula) o cliente contiene
     * el texto buscado. Las mas nuevas primero.
     * @param estado estado elegido, o null para ver todos
     */
    public static List<Reparacion> listar(String texto, String estado) throws SQLException {
        String sql = "SELECT r.*, v.matricula, v.marca, v.modelo, c.nombre AS cliente, "
                   + "(SELECT GROUP_CONCAT(e.nombre ORDER BY e.nombre SEPARATOR ', ') "
                   + "   FROM empleadoReparacion er JOIN empleado e ON e.idempleado = er.empleado_idempleado "
                   + "   WHERE er.reparaciones_idreparaciones = r.idreparaciones) AS empleados "
                   + "FROM reparaciones r "
                   + "JOIN vehiculo v ON v.idvehiculo = r.vehiculo_idvehiculo "
                   + "JOIN cliente c ON c.idcliente = v.cliente_idcliente "
                   + "WHERE (v.matricula LIKE ? OR c.nombre LIKE ?) "
                   + "AND (? IS NULL OR r.estado = ?) "
                   + "ORDER BY r.fecha_ingreso DESC, r.idreparaciones DESC";
        List<Reparacion> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String patron = "%" + texto.trim() + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, estado);
            ps.setString(4, estado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reparacion r = new Reparacion();
                    r.idReparacion = rs.getInt("idreparaciones");
                    r.idVehiculo = rs.getInt("vehiculo_idvehiculo");
                    r.fechaIngreso = rs.getDate("fecha_ingreso").toLocalDate();
                    if (rs.getDate("fecha_egreso") != null) {
                        r.fechaEgreso = rs.getDate("fecha_egreso").toLocalDate();
                    }
                    r.detalles = rs.getString("detalles");
                    r.costoRepuestos = rs.getDouble("costoRepuestos");
                    r.costoReparo = rs.getDouble("costoReparo");
                    r.total = rs.getDouble("total");
                    r.estado = rs.getString("estado");
                    r.matricula = rs.getString("matricula");
                    r.vehiculo = rs.getString("marca") + " " + rs.getString("modelo");
                    r.nombreCliente = rs.getString("cliente");
                    r.nombresEmpleados = rs.getString("empleados");   // null si no tiene
                    lista.add(r);
                }
            }
        }
        return lista;
    }

    /** Carga los empleados y los repuestos de esta reparacion (para la ficha y para editar). */
    public void cargarDetalle() throws SQLException {
        idEmpleados = new ArrayList<>();
        repuestosUsados = new ArrayList<>();
        try (Connection con = Conexion.getConexion()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT empleado_idempleado FROM empleadoReparacion WHERE reparaciones_idreparaciones = ?")) {
                ps.setInt(1, idReparacion);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        idEmpleados.add(rs.getInt(1));
                    }
                }
            }
            String sql = "SELECT rr.repuestos_idrepuestos, p.nombre, rr.cantidadRepuestos, rr.precio "
                       + "FROM repuestosReparacion rr JOIN repuestos p ON p.idrepuestos = rr.repuestos_idrepuestos "
                       + "WHERE rr.reparaciones_idreparaciones = ? ORDER BY p.nombre";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idReparacion);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        repuestosUsados.add(new RepuestoUsado(rs.getInt(1), rs.getString(2),
                                rs.getInt(3), rs.getDouble(4)));
                    }
                }
            }
        }
    }

    /**
     * Resumen para las tarjetas de arriba:
     * {pendientes, en proceso, entregadas, total cobrado de las entregadas}.
     */
    public static double[] resumen() throws SQLException {
        String sql = "SELECT COALESCE(SUM(estado = ?), 0), COALESCE(SUM(estado = ?), 0), "
                   + "COALESCE(SUM(estado = ?), 0), "
                   + "COALESCE(SUM(CASE WHEN estado = ? THEN total ELSE 0 END), 0) "
                   + "FROM reparaciones";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, PENDIENTE);
            ps.setString(2, EN_PROCESO);
            ps.setString(3, ENTREGADO);
            ps.setString(4, ENTREGADO);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new double[]{rs.getDouble(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4)};
            }
        }
    }

    /** Vehiculos para el combo: "3 - AC 845 JK · Ford Focus (Sofía Rodríguez)". */
    public static List<String> listarVehiculos() throws SQLException {
        String sql = "SELECT v.idvehiculo, v.matricula, v.marca, v.modelo, c.nombre "
                   + "FROM vehiculo v JOIN cliente c ON c.idcliente = v.cliente_idcliente "
                   + "ORDER BY v.matricula";
        List<String> vehiculos = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                vehiculos.add(rs.getInt(1) + " - " + rs.getString(2) + " · " + rs.getString(3) + " "
                        + rs.getString(4) + " (" + rs.getString(5) + ")");
            }
        }
        return vehiculos;
    }

    /** Empleados para elegir quien trabaja: "2 - Roberto Ríos". */
    public static List<String> listarEmpleados() throws SQLException {
        List<String> empleados = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement("SELECT idempleado, nombre FROM empleado ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                empleados.add(rs.getInt(1) + " - " + rs.getString(2));
            }
        }
        return empleados;
    }

    // ------------------------------------------------------------
    // Guardar, cambiar estado y eliminar
    // ------------------------------------------------------------

    /**
     * Guarda la reparacion con sus empleados y repuestos, todo en una
     * TRANSACCION: o se guarda todo, o no se guarda nada.
     * Tambien actualiza el stock de los repuestos y el "en taller" del vehiculo.
     * Si no hay stock suficiente de un repuesto, no guarda nada y avisa.
     */
    public void guardar() throws SQLException {
        boolean eraNuevo = (idReparacion == 0);
        calcularCostos();
        // La fecha de salida se pone sola cuando se entrega
        if (!ENTREGADO.equals(estado)) {
            fechaEgreso = null;
        } else if (fechaEgreso == null) {
            fechaEgreso = LocalDate.now();
        }
        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);   // empieza la transaccion
            try {
                if (!eraNuevo) {
                    devolverStock(con, idReparacion);   // lo que usaba antes vuelve al stock
                }
                guardarDatos(con);
                guardarEmpleados(con);
                guardarRepuestos(con);                   // y se descuenta lo que usa ahora
                actualizarEnTaller(con);
                con.commit();           // todo salio bien: se confirma
            } catch (SQLException ex) {
                con.rollback();         // algo fallo: se deshace todo (el stock tambien)
                if (eraNuevo) {
                    idReparacion = 0;
                }
                throw ex;
            }
        }
    }

    /** Inserta o modifica la fila de la tabla "reparaciones". */
    private void guardarDatos(Connection con) throws SQLException {
        if (idReparacion == 0) {
            // La tabla no tiene AUTO_INCREMENT: calculamos el proximo id
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COALESCE(MAX(idreparaciones), 0) + 1 FROM reparaciones");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                idReparacion = rs.getInt(1);
            }
            String sql = "INSERT INTO reparaciones (vehiculo_idvehiculo, fecha_ingreso, fecha_egreso, detalles, "
                       + "costoRepuestos, costoReparo, total, estado, idreparaciones) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                cargarParametros(ps);
                ps.executeUpdate();
            }
        } else {
            String sql = "UPDATE reparaciones SET vehiculo_idvehiculo = ?, fecha_ingreso = ?, fecha_egreso = ?, "
                       + "detalles = ?, costoRepuestos = ?, costoReparo = ?, total = ?, estado = ? "
                       + "WHERE idreparaciones = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                cargarParametros(ps);
                ps.executeUpdate();
            }
        }
    }

    /** Carga los 9 parametros, en el mismo orden para INSERT y UPDATE. */
    private void cargarParametros(PreparedStatement ps) throws SQLException {
        ps.setInt(1, idVehiculo);
        ps.setDate(2, java.sql.Date.valueOf(fechaIngreso));
        ps.setDate(3, fechaEgreso == null ? null : java.sql.Date.valueOf(fechaEgreso));
        ps.setString(4, detalles);
        ps.setDouble(5, costoRepuestos);
        ps.setDouble(6, costoReparo);
        ps.setDouble(7, total);
        ps.setString(8, estado);
        ps.setInt(9, idReparacion);
    }

    /** Borra los empleados que tenia y guarda los elegidos. */
    private void guardarEmpleados(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM empleadoReparacion WHERE reparaciones_idreparaciones = ?")) {
            ps.setInt(1, idReparacion);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO empleadoReparacion (empleado_idempleado, reparaciones_idreparaciones) VALUES (?, ?)")) {
            for (int idEmpleado : idEmpleados) {
                ps.setInt(1, idEmpleado);
                ps.setInt(2, idReparacion);
                ps.executeUpdate();
            }
        }
    }

    /** Borra los repuestos que tenia, guarda los nuevos y los descuenta del stock. */
    private void guardarRepuestos(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM repuestosReparacion WHERE reparaciones_idreparaciones = ?")) {
            ps.setInt(1, idReparacion);
            ps.executeUpdate();
        }
        String insertar = "INSERT INTO repuestosReparacion (repuestos_idrepuestos, reparaciones_idreparaciones, "
                        + "cantidadRepuestos, precio) VALUES (?, ?, ?, ?)";
        // Solo descuenta si alcanza: si no hay suficientes unidades, no modifica ninguna fila
        String descontar = "UPDATE repuestos SET cantidad = cantidad - ? WHERE idrepuestos = ? AND cantidad >= ?";
        try (PreparedStatement psInsertar = con.prepareStatement(insertar);
             PreparedStatement psDescontar = con.prepareStatement(descontar)) {
            for (RepuestoUsado r : repuestosUsados) {
                psInsertar.setInt(1, r.getIdRepuesto());
                psInsertar.setInt(2, idReparacion);
                psInsertar.setInt(3, r.getCantidad());
                psInsertar.setDouble(4, r.getPrecio());
                psInsertar.executeUpdate();

                psDescontar.setInt(1, r.getCantidad());
                psDescontar.setInt(2, r.getIdRepuesto());
                psDescontar.setInt(3, r.getCantidad());
                if (psDescontar.executeUpdate() == 0) {
                    throw new SQLException("No hay stock suficiente de \"" + r.getNombre() + "\".");
                }
            }
        }
    }

    /** Suma al stock los repuestos que tiene guardados una reparacion. */
    private static void devolverStock(Connection con, int idReparacion) throws SQLException {
        String sql = "UPDATE repuestos p JOIN repuestosReparacion rr ON rr.repuestos_idrepuestos = p.idrepuestos "
                   + "SET p.cantidad = p.cantidad + COALESCE(rr.cantidadRepuestos, 0) "
                   + "WHERE rr.reparaciones_idreparaciones = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idReparacion);
            ps.executeUpdate();
        }
    }

    /**
     * Marca "en taller" a los vehiculos que tienen alguna reparacion sin entregar,
     * y saca del taller a los demas.
     */
    private static void actualizarEnTaller(Connection con) throws SQLException {
        String sql = "UPDATE vehiculo v SET v.enTaller = ("
                   + "SELECT COUNT(*) FROM reparaciones r "
                   + "WHERE r.vehiculo_idvehiculo = v.idvehiculo AND r.estado <> ?) > 0";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ENTREGADO);
            ps.executeUpdate();
        }
    }

    /** Pasa la reparacion al estado siguiente (con fecha de salida si se entrega). */
    public static void cambiarEstado(int idReparacion, String nuevoEstado) throws SQLException {
        String sql = "UPDATE reparaciones SET estado = ?, "
                   + "fecha_egreso = (CASE WHEN ? = ? THEN CURDATE() ELSE NULL END) "
                   + "WHERE idreparaciones = ?";
        try (Connection con = Conexion.getConexion()) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nuevoEstado);
                ps.setString(2, nuevoEstado);
                ps.setString(3, ENTREGADO);
                ps.setInt(4, idReparacion);
                ps.executeUpdate();
            }
            actualizarEnTaller(con);
        }
    }

    /**
     * Elimina la reparacion. Los repuestos que usaba vuelven al stock, y
     * (por el ON DELETE CASCADE) se borran sus empleados y repuestos asignados.
     */
    public static void eliminar(int idReparacion) throws SQLException {
        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                devolverStock(con, idReparacion);
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM reparaciones WHERE idreparaciones = ?")) {
                    ps.setInt(1, idReparacion);
                    ps.executeUpdate();
                }
                actualizarEnTaller(con);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }
}
