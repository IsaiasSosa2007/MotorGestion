-- ============================================================
--  MOTORGESTION - AlfaMotors
--  Base de datos completa (modelo: BDD/Modelo.mwb)
--
--  Como usarlo: MySQL Workbench > File > Open SQL Script >
--  elegir este archivo > ejecutar con el rayito.
--  Se puede ejecutar mas de una vez: borra y vuelve a crear todo.
-- ============================================================

-- 1) Borrar la base vieja de prueba y la anterior de este modelo
DROP DATABASE IF EXISTS MotorGestionV1;
DROP DATABASE IF EXISTS MotorGestion;

CREATE DATABASE MotorGestion DEFAULT CHARACTER SET utf8mb4;
USE MotorGestion;

-- ------------------------------------------------------------
-- 2) Tablas
-- ------------------------------------------------------------
CREATE TABLE cliente (
  idcliente INT NOT NULL,
  nombre    VARCHAR(100) NOT NULL,
  telefono  VARCHAR(11)  NOT NULL,
  dni       VARCHAR(8)   NOT NULL,
  PRIMARY KEY (idcliente),
  UNIQUE INDEX dni_UNIQUE (dni)
) ENGINE = InnoDB;

CREATE TABLE empleado (
  idempleado INT NOT NULL,
  nombre     VARCHAR(100) NOT NULL,
  telefono   VARCHAR(11)  NOT NULL,
  dni        VARCHAR(8)   NOT NULL,
  PRIMARY KEY (idempleado)
) ENGINE = InnoDB;

CREATE TABLE proveedor (
  idproveedor        INT NOT NULL,
  nombre             VARCHAR(100) NOT NULL,
  telefono           VARCHAR(11)  NOT NULL,
  direccion          VARCHAR(100) NOT NULL,
  categoriaProductos VARCHAR(45)  NOT NULL,
  PRIMARY KEY (idproveedor)
) ENGINE = InnoDB;

CREATE TABLE repuestos (
  idrepuestos           INT NOT NULL,
  nombre                VARCHAR(50) NOT NULL,
  cantidad              INT NOT NULL,
  precio                DOUBLE NOT NULL,
  proveedor_idproveedor INT NOT NULL,
  PRIMARY KEY (idrepuestos),
  INDEX fk_repuestos_proveedor1_idx (proveedor_idproveedor),
  CONSTRAINT fk_repuestos_proveedor1 FOREIGN KEY (proveedor_idproveedor)
    REFERENCES proveedor (idproveedor) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE vehiculo (
  idvehiculo        INT NOT NULL,
  marca             VARCHAR(45) NOT NULL,
  modelo            VARCHAR(45) NOT NULL,
  color             VARCHAR(45) NOT NULL,
  matricula         VARCHAR(10) NOT NULL,
  enTaller          TINYINT NOT NULL,
  cedula            VARCHAR(45) NOT NULL,
  seguro            TINYINT NOT NULL,
  empresa_seguros   VARCHAR(45) NULL,
  cliente_idcliente INT NOT NULL,
  PRIMARY KEY (idvehiculo),
  UNIQUE INDEX matricula_UNIQUE (matricula),
  UNIQUE INDEX cedula_UNIQUE (cedula),
  INDEX fk_vehiculo_cliente1_idx (cliente_idcliente),
  CONSTRAINT fk_vehiculo_cliente1 FOREIGN KEY (cliente_idcliente)
    REFERENCES cliente (idcliente) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE reparaciones (
  idreparaciones      INT NOT NULL,
  vehiculo_idvehiculo INT NOT NULL,
  fecha_ingreso       DATE NOT NULL,
  fecha_egreso        DATE NULL,
  detalles            VARCHAR(100) NOT NULL,
  costoRepuestos      DOUBLE NOT NULL,
  costoReparo         DOUBLE NOT NULL,
  total               DOUBLE NOT NULL,
  estado              ENUM('Pendiente', 'En proceso', 'Entregado') NOT NULL,
  PRIMARY KEY (idreparaciones),
  INDEX fk_reparaciones_vehiculo1_idx (vehiculo_idvehiculo),
  CONSTRAINT fk_reparaciones_vehiculo1 FOREIGN KEY (vehiculo_idvehiculo)
    REFERENCES vehiculo (idvehiculo) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- clave es VARCHAR(64) (en el modelo decia 45) porque la contrasena
-- se guarda cifrada con SHA-256, que ocupa 64 caracteres.
CREATE TABLE usuarios (
  idusuarios          INT NOT NULL AUTO_INCREMENT,
  nombre              VARCHAR(45) NOT NULL,
  tipoUsuario         VARCHAR(20) NULL,
  clave               VARCHAR(64) NOT NULL,
  empleado_idempleado INT NOT NULL,
  PRIMARY KEY (idusuarios),
  INDEX fk_usuarios_empleado1_idx (empleado_idempleado),
  CONSTRAINT fk_usuarios_empleado1 FOREIGN KEY (empleado_idempleado)
    REFERENCES empleado (idempleado) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE repuestosReparacion (
  repuestos_idrepuestos       INT NOT NULL,
  reparaciones_idreparaciones INT NOT NULL,
  cantidadRepuestos           INT NULL,
  precio                      DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (repuestos_idrepuestos, reparaciones_idreparaciones),
  INDEX fk_repuestosReparacion_repuestos1_idx (repuestos_idrepuestos),
  INDEX fk_repuestosReparacion_reparaciones1_idx (reparaciones_idreparaciones),
  CONSTRAINT fk_repuestosReparacion_repuestos1 FOREIGN KEY (repuestos_idrepuestos)
    REFERENCES repuestos (idrepuestos) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT fk_repuestosReparacion_reparaciones1 FOREIGN KEY (reparaciones_idreparaciones)
    REFERENCES reparaciones (idreparaciones) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE empleadoReparacion (
  empleado_idempleado         INT NOT NULL,
  reparaciones_idreparaciones INT NOT NULL,
  PRIMARY KEY (empleado_idempleado, reparaciones_idreparaciones),
  INDEX fk_empleado_has_reparaciones_reparaciones1_idx (reparaciones_idreparaciones),
  INDEX fk_empleado_has_reparaciones_empleado1_idx (empleado_idempleado),
  CONSTRAINT fk_empleado_has_reparaciones_empleado1 FOREIGN KEY (empleado_idempleado)
    REFERENCES empleado (idempleado) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT fk_empleado_has_reparaciones_reparaciones1 FOREIGN KEY (reparaciones_idreparaciones)
    REFERENCES reparaciones (idreparaciones) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- 3) Datos de ejemplo: 8 clientes, 10 vehiculos, 5 proveedores,
--    15 repuestos, 4 empleados y 9 reparaciones.
--    En cada reparacion: costoRepuestos = suma de (cantidad x precio)
--    de los repuestos usados, y total = costoRepuestos + costoReparo.
--    Los vehiculos con reparaciones sin entregar quedan "en taller".
-- ------------------------------------------------------------
INSERT INTO cliente (idcliente, nombre, telefono, dni) VALUES
(1, 'Lucía Fernández', '1156781234', '30123456'),
(2, 'Martín Gómez', '1143219876', '28765432'),
(3, 'Sofía Rodríguez', '1167894521', '35432109'),
(4, 'Diego Pereyra', '1132456789', '25987654'),
(5, 'Valentina López', '1178563412', '38456123'),
(6, 'Javier Acosta', '1149873210', '22345678'),
(7, 'Camila Benítez', '1161237894', '40123987'),
(8, 'Ricardo Molina', '1158796432', '18456789');

INSERT INTO empleado (idempleado, nombre, telefono, dni) VALUES
(1, 'Carlos Méndez', '1166667788', '24777222'),
(2, 'Roberto Ríos', '1147647788', '27788222'),
(3, 'Martín Suárez', '1164467488', '31799222'),
(4, 'Nicolás Herrera', '1159034412', '39120456');

INSERT INTO proveedor (idproveedor, nombre, telefono, direccion, categoriaProductos) VALUES
(1, 'MotorSpot', '1133446543', 'Av. Mitre 1520, Avellaneda', 'Piezas de motor'),
(2, 'Suspensiones del Sur', '1145678321', 'Av. H. Yrigoyen 3480, Lanús', 'Suspensión y dirección'),
(3, 'Frenos Lanús', '1152347788', 'Margarita Weild 1210, Lanús', 'Frenos'),
(4, 'ElectroAuto Quilmes', '1142256190', 'Av. Calchaquí 845, Quilmes', 'Electricidad del automotor'),
(5, 'Lubricentro Gerli', '1168903344', 'Casacuberta 774, Gerli', 'Lubricantes y filtros');

INSERT INTO repuestos (idrepuestos, nombre, cantidad, precio, proveedor_idproveedor) VALUES
(1, 'Alternador 12V 90A', 2, 185000.00, 4),
(2, 'Batería 12V 65Ah', 5, 120000.00, 4),
(3, 'Bujía de encendido', 20, 6500.00, 4),
(4, 'Juego de pistones', 3, 210000.00, 1),
(5, 'Kit de distribución', 4, 95000.00, 1),
(6, 'Bomba de agua', 0, 68000.00, 1),
(7, 'Amortiguador delantero', 6, 78000.00, 2),
(8, 'Rótula de dirección', 8, 24000.00, 2),
(9, 'Extremo de dirección', 1, 19500.00, 2),
(10, 'Pastillas de freno delanteras', 10, 32000.00, 3),
(11, 'Disco de freno ventilado', 4, 54000.00, 3),
(12, 'Líquido de frenos DOT4 500 ml', 2, 9800.00, 3),
(13, 'Aceite 10W40 x 4 L', 15, 38000.00, 5),
(14, 'Filtro de aceite', 18, 8500.00, 5),
(15, 'Filtro de aire', 0, 11000.00, 5);

INSERT INTO vehiculo (idvehiculo, marca, modelo, color, matricula, enTaller, cedula, seguro, empresa_seguros, cliente_idcliente) VALUES
(1, 'Peugeot', '208', 'Blanco', 'AE 123 BC', TRUE, '45.218.307', TRUE, 'La Caja', 1),
(2, 'Volkswagen', 'Gol Trend', 'Gris', 'NKL 482', FALSE, '31.904.552', TRUE, 'Federación Patronal', 2),
(3, 'Ford', 'Focus', 'Azul', 'AC 845 JK', TRUE, '44.567.120', TRUE, 'Sancor Seguros', 3),
(4, 'Chevrolet', 'Corsa', 'Rojo', 'HGT 317', FALSE, '27.330.981', FALSE, NULL, 4),
(5, 'Toyota', 'Etios', 'Plata', 'AD 556 FR', TRUE, '46.002.775', TRUE, 'Rivadavia Seguros', 5),
(6, 'Fiat', 'Cronos', 'Negro', 'AF 210 MN', TRUE, '47.881.046', TRUE, 'Mercantil Andina', 6),
(7, 'Renault', 'Kangoo', 'Blanco', 'OPQ 904', TRUE, '35.112.468', FALSE, NULL, 6),
(8, 'Citroën', 'C3', 'Celeste', 'AB 774 TT', TRUE, '43.659.214', TRUE, 'San Cristóbal', 7),
(9, 'Ford', 'Ranger', 'Gris', 'AA 991 KD', FALSE, '40.275.833', TRUE, 'La Caja', 8),
(10, 'Volkswagen', 'Suran', 'Bordó', 'MRT 627', FALSE, '32.748.590', FALSE, NULL, 2);

INSERT INTO reparaciones (idreparaciones, vehiculo_idvehiculo, fecha_ingreso, fecha_egreso, detalles, costoRepuestos, costoReparo, total, estado) VALUES
(1, 2, '2026-10-06', '2026-10-09', 'Cambio de kit de distribución y bomba de agua', 163000.00, 120000.00, 283000.00, 'Entregado'),
(2, 4, '2026-10-14', '2026-10-15', 'Cambio de pastillas y discos de freno delanteros', 140000.00, 45000.00, 185000.00, 'Entregado'),
(3, 9, '2026-10-20', '2026-10-23', 'Service completo: aceite, filtros y bujías', 121500.00, 60000.00, 181500.00, 'Entregado'),
(4, 1, '2026-11-03', NULL, 'Falla de carga: cambio de alternador', 185000.00, 70000.00, 255000.00, 'En proceso'),
(5, 3, '2026-11-05', NULL, 'Ruido en tren delantero: amortiguadores y rótulas', 204000.00, 95000.00, 299000.00, 'En proceso'),
(6, 6, '2026-11-09', NULL, 'Pérdida de compresión: cambio de pistones', 210000.00, 350000.00, 560000.00, 'En proceso'),
(7, 8, '2026-11-10', NULL, 'No arranca a la mañana: revisar batería', 0.00, 25000.00, 25000.00, 'Pendiente'),
(8, 5, '2026-11-11', NULL, 'Dirección dura y juego en el volante', 0.00, 40000.00, 40000.00, 'Pendiente'),
(9, 7, '2026-11-12', NULL, 'Cambio de líquido de frenos y revisión general', 9800.00, 30000.00, 39800.00, 'Pendiente');

INSERT INTO empleadoReparacion (empleado_idempleado, reparaciones_idreparaciones) VALUES
(2, 1),
(3, 1),
(3, 2),
(2, 3),
(2, 4),
(3, 5),
(4, 5),
(1, 6),
(3, 6),
(4, 7),
(2, 8),
(4, 9);

INSERT INTO repuestosReparacion (repuestos_idrepuestos, reparaciones_idreparaciones, cantidadRepuestos, precio) VALUES
(5, 1, 1, 95000.00),
(6, 1, 1, 68000.00),
(10, 2, 1, 32000.00),
(11, 2, 2, 54000.00),
(13, 3, 2, 38000.00),
(14, 3, 1, 8500.00),
(15, 3, 1, 11000.00),
(3, 3, 4, 6500.00),
(1, 4, 1, 185000.00),
(7, 5, 2, 78000.00),
(8, 5, 2, 24000.00),
(4, 6, 1, 210000.00),
(12, 9, 1, 9800.00);

-- Usuarios para entrar al programa (contrasena cifrada con SHA-256)
--   admin    / admin123     -> Carlos Méndez (administrador)
--   empleado / empleado123  -> Roberto Ríos (empleado)
--   Martín Suárez y Nicolás Herrera no tienen usuario.
INSERT INTO usuarios (nombre, tipoUsuario, clave, empleado_idempleado) VALUES
('admin',    'admin',    SHA2('admin123', 256),    1),
('empleado', 'empleado', SHA2('empleado123', 256), 2);

-- ------------------------------------------------------------
-- 4) Usuario de MySQL que usa el programa Java (Conexion.java)
-- ------------------------------------------------------------
CREATE USER IF NOT EXISTS 'motorgestion'@'localhost' IDENTIFIED BY 'motor2026';
GRANT ALL PRIVILEGES ON MotorGestion.* TO 'motorgestion'@'localhost';
FLUSH PRIVILEGES;

-- Puerto de este MySQL (tiene que coincidir con Conexion.java)
SELECT @@port AS puerto;
