-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema motorgestion
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema motorgestion
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `motorgestion` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `motorgestion` ;

-- -----------------------------------------------------
-- Table `motorgestion`.`cliente`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `motorgestion`.`cliente` (
  `idcliente` INT NOT NULL,
  `nombre` VARCHAR(100) NOT NULL,
  `telefono` VARCHAR(11) NOT NULL,
  `dni` VARCHAR(8) NOT NULL,
  PRIMARY KEY (`idcliente`),
  UNIQUE INDEX `dni_UNIQUE` (`dni` ASC) VISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `motorgestion`.`empleado`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `motorgestion`.`empleado` (
  `idempleado` INT NOT NULL,
  `nombre` VARCHAR(100) NOT NULL,
  `telefono` VARCHAR(11) NOT NULL,
  `dni` VARCHAR(8) NOT NULL,
  PRIMARY KEY (`idempleado`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `motorgestion`.`vehiculo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `motorgestion`.`vehiculo` (
  `idvehiculo` INT NOT NULL,
  `marca` VARCHAR(45) NOT NULL,
  `modelo` VARCHAR(45) NOT NULL,
  `color` VARCHAR(45) NOT NULL,
  `matricula` VARCHAR(10) NOT NULL,
  `enTaller` TINYINT NOT NULL,
  `cedula` VARCHAR(45) NOT NULL,
  `seguro` TINYINT NOT NULL,
  `empresa_seguros` VARCHAR(45) NULL DEFAULT NULL,
  `cliente_idcliente` INT NOT NULL,
  PRIMARY KEY (`idvehiculo`),
  UNIQUE INDEX `matricula_UNIQUE` (`matricula` ASC) VISIBLE,
  UNIQUE INDEX `cedula_UNIQUE` (`cedula` ASC) VISIBLE,
  INDEX `fk_vehiculo_cliente1_idx` (`cliente_idcliente` ASC) VISIBLE,
  CONSTRAINT `fk_vehiculo_cliente1`
    FOREIGN KEY (`cliente_idcliente`)
    REFERENCES `motorgestion`.`cliente` (`idcliente`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `motorgestion`.`reparaciones`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `motorgestion`.`reparaciones` (
  `idreparaciones` INT NOT NULL,
  `vehiculo_idvehiculo` INT NOT NULL,
  `fecha_ingreso` DATE NOT NULL,
  `fecha_egreso` DATE NULL DEFAULT NULL,
  `detalles` VARCHAR(100) NOT NULL,
  `costoRepuestos` DOUBLE NOT NULL,
  `costoReparo` DOUBLE NOT NULL,
  `total` DOUBLE NOT NULL,
  `estado` ENUM('Pendiente', 'En proceso', 'Entregado') NOT NULL,
  PRIMARY KEY (`idreparaciones`),
  INDEX `fk_reparaciones_vehiculo1_idx` (`vehiculo_idvehiculo` ASC) VISIBLE,
  CONSTRAINT `fk_reparaciones_vehiculo1`
    FOREIGN KEY (`vehiculo_idvehiculo`)
    REFERENCES `motorgestion`.`vehiculo` (`idvehiculo`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `motorgestion`.`empleadoreparacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `motorgestion`.`empleadoreparacion` (
  `empleado_idempleado` INT NOT NULL,
  `reparaciones_idreparaciones` INT NOT NULL,
  PRIMARY KEY (`empleado_idempleado`, `reparaciones_idreparaciones`),
  INDEX `fk_empleado_has_reparaciones_reparaciones1_idx` (`reparaciones_idreparaciones` ASC) VISIBLE,
  INDEX `fk_empleado_has_reparaciones_empleado1_idx` (`empleado_idempleado` ASC) VISIBLE,
  CONSTRAINT `fk_empleado_has_reparaciones_empleado1`
    FOREIGN KEY (`empleado_idempleado`)
    REFERENCES `motorgestion`.`empleado` (`idempleado`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_empleado_has_reparaciones_reparaciones1`
    FOREIGN KEY (`reparaciones_idreparaciones`)
    REFERENCES `motorgestion`.`reparaciones` (`idreparaciones`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `motorgestion`.`proveedor`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `motorgestion`.`proveedor` (
  `idproveedor` INT NOT NULL,
  `nombre` VARCHAR(100) NOT NULL,
  `telefono` VARCHAR(11) NOT NULL,
  `direccion` VARCHAR(100) NOT NULL,
  `categoriaProductos` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`idproveedor`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `motorgestion`.`repuestos`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `motorgestion`.`repuestos` (
  `idrepuestos` INT NOT NULL,
  `nombre` VARCHAR(50) NOT NULL,
  `cantidad` INT NOT NULL,
  `precio` DOUBLE NOT NULL,
  `proveedor_idproveedor` INT NOT NULL,
  PRIMARY KEY (`idrepuestos`),
  INDEX `fk_repuestos_proveedor1_idx` (`proveedor_idproveedor` ASC) VISIBLE,
  CONSTRAINT `fk_repuestos_proveedor1`
    FOREIGN KEY (`proveedor_idproveedor`)
    REFERENCES `motorgestion`.`proveedor` (`idproveedor`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `motorgestion`.`repuestosreparacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `motorgestion`.`repuestosreparacion` (
  `repuestos_idrepuestos` INT NOT NULL,
  `reparaciones_idreparaciones` INT NOT NULL,
  `cantidadRepuestos` INT NULL DEFAULT NULL,
  `precio` DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`repuestos_idrepuestos`, `reparaciones_idreparaciones`),
  INDEX `fk_repuestosReparacion_repuestos1_idx` (`repuestos_idrepuestos` ASC) VISIBLE,
  INDEX `fk_repuestosReparacion_reparaciones1_idx` (`reparaciones_idreparaciones` ASC) VISIBLE,
  CONSTRAINT `fk_repuestosReparacion_reparaciones1`
    FOREIGN KEY (`reparaciones_idreparaciones`)
    REFERENCES `motorgestion`.`reparaciones` (`idreparaciones`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_repuestosReparacion_repuestos1`
    FOREIGN KEY (`repuestos_idrepuestos`)
    REFERENCES `motorgestion`.`repuestos` (`idrepuestos`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `motorgestion`.`usuarios`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `motorgestion`.`usuarios` (
  `idusuarios` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45) NOT NULL,
  `tipoUsuario` VARCHAR(20) NULL DEFAULT NULL,
  `clave` VARCHAR(64) NOT NULL,
  `empleado_idempleado` INT NOT NULL,
  PRIMARY KEY (`idusuarios`),
  INDEX `fk_usuarios_empleado1_idx` (`empleado_idempleado` ASC) VISIBLE,
  CONSTRAINT `fk_usuarios_empleado1`
    FOREIGN KEY (`empleado_idempleado`)
    REFERENCES `motorgestion`.`empleado` (`idempleado`)
    ON DELETE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 3
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
