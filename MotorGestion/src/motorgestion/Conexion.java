package motorgestion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centraliza la conexión a la base de datos.
 * Si cambia el servidor, el usuario o la clave, se modifica solo esta clase.
 */
public class Conexion {

private static final String URL = "jdbc:mysql://localhost:3307/MotorGestionV1";
private static final String USUARIO = "motorgestion";
private static final String CLAVE = "motor2026";

    /** Devuelve una conexión abierta. Quien la pide la cierra (con try). */
    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}