# StoreApp - CRUD Dinámico con Java y PostgreSQL 

Una aplicación de escritorio robusta construida en Java Swing que permite gestionar cualquier tabla de una base de datos PostgreSQL de forma dinámica. El programa detecta automáticamente las columnas, tipos de datos y genera la interfaz de usuario (tablas y formularios) en tiempo real.

## Características

* **Interfaz Dinámica:** No necesitas crear un formulario por cada tabla; el sistema lee los metadatos de PostgreSQL y construye los campos automáticamente.
* **CRUD Completo:** Operaciones de lectura, inserción, actualización y borrado.
* **Búsqueda en Tiempo Real:** Filtro integrado para localizar registros rápidamente.
* **Gestión de Tipos:** Manejo inteligente de tipos de datos (Strings, Integers, BigDecimals).
* **UI Moderna:** Incluye iconos y una disposición limpia usando `GridBagLayout`.

## Requisitos Técnicos

* **Java JDK:** 8 o superior (Compatible con versiones modernas).
* **Base de Datos:** PostgreSQL.
* **Driver JDBC:** `postgresql-42.7.9.jar` (incluido en el repositorio).

## Instalación y Configuración

### 1. Preparar la Base de Datos
Crea una base de datos llamada `tienda` y las tablas que desees. Por ejemplo:
```sql
CREATE DATABASE tienda;

CREATE TABLE productos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100),
    precio NUMERIC(10,2),
    stock INT
);
