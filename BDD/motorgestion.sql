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
-- 3) Datos de prueba (los del grupo, adaptados al modelo)
-- ------------------------------------------------------------
INSERT INTO cliente (idcliente, nombre, telefono, dni) VALUES
(1, 'Copito Ibarra',  '1167678989', '23456789'),
(2, 'Junior Sosa',    '1188889999', '23456780'),
(3, 'Advincula Keke', '1168978989', '27756789');

INSERT INTO empleado (idempleado, nombre, telefono, dni) VALUES
(1, 'Carlos Papou',     '1166667788', '24777222'),
(2, 'Roberto Ruiz',     '1147647788', '24788222'),
(3, 'Martin Advincula', '1164467488', '24799222');

INSERT INTO proveedor (idproveedor, nombre, telefono, direccion, categoriaProductos) VALUES
(1, 'MotorSpot',   '1133446543', 'Pasteur 373, Sarandí',    'Piezas de motor'),
(2, 'Sus Pension', '1155567778', 'Casacuberta 774, Gerli',  'Piezas de suspension y direccion'),
(3, 'Meca Copito', '1154346343', 'Pila 3277, Sarandí',      'Herramientas');

-- El modelo pide un proveedor por repuesto (no estaba en los inserts originales)
INSERT INTO repuestos (idrepuestos, nombre, cantidad, precio, proveedor_idproveedor) VALUES
(1, 'Alternador', 3,  50000.00, 1),
(2, 'Parabrisas', 4,  30000.00, 2),
(3, 'Pistones',   12, 5000.00,  1);

INSERT INTO vehiculo (idvehiculo, marca, modelo, color, matricula, enTaller, cedula, seguro, empresa_seguros, cliente_idcliente) VALUES
(1, 'Peugeot',    '206',    'Azul', 'ICJ 067', TRUE, '12.334.445', TRUE,  'Sancor Seguros', 1),
(2, 'Citroen',    'Twingo', 'Gris', 'DRE 187', TRUE, '12.067.187', TRUE,  'Juan Seguros',   2),
(3, 'Alfa Romeo', '156',    'Rojo', 'DOC 420', TRUE, '12.420.669', FALSE, NULL,             3);

-- En el modelo, la reparacion ya no guarda empleado ni repuesto:
-- eso va en empleadoReparacion y repuestosReparacion (mas abajo).
INSERT INTO reparaciones (idreparaciones, vehiculo_idvehiculo, fecha_ingreso, fecha_egreso, detalles, costoRepuestos, costoReparo, total, estado) VALUES
(1, 2, '2026-09-24', NULL, 'Se tuvieron que reemplazar los pistones', 20000.00, 300000.00, 320000.00, 'En proceso'),
(2, 3, '2026-10-01', NULL, 'Se rompió el parabrisas en un choque',    50000.00, 250000.00, 300000.00, 'Pendiente'),
(3, 1, '2026-10-09', NULL, 'No funcionaba el alternador',             50000.00, 400000.00, 450000.00, 'En proceso');

INSERT INTO empleadoReparacion (empleado_idempleado, reparaciones_idreparaciones) VALUES
(3, 1),
(2, 2),
(1, 3);

INSERT INTO repuestosReparacion (repuestos_idrepuestos, reparaciones_idreparaciones, cantidadRepuestos, precio) VALUES
(3, 1, 4, 5000.00),
(2, 2, 1, 50000.00),
(1, 3, 1, 50000.00);

-- Usuarios para entrar al programa (contrasena cifrada con SHA-256)
--   admin    / admin123     -> Carlos Papou
--   empleado / empleado123  -> Roberto Ruiz
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
