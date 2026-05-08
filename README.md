# Sistema de Asistencia Docente - Java Swing

## Descripción

Este proyecto consiste en un **módulo de control y seguimiento de asistencia de estudiantes**, desarrollado en **Java** utilizando **Swing** para la interfaz gráfica.

El sistema permite gestionar sesiones de clase, registrar asistencias, controlar tardanzas, justificar inasistencias, visualizar estadísticas y generar reportes en formato CSV. Además, incluye persistencia básica de datos mediante serialización, permitiendo conservar la información aunque el programa se cierre.

Este módulo está inspirado en el funcionamiento del **sistema de asistencia docente del Aula Virtual de la Universidad Católica de Santa María**, adaptado como proyecto académico para el curso de **Testing, Implantación y Mantenimiento de Sistemas**.

> **Nota:** Este proyecto no está conectado al sistema real de la universidad. Es una versión académica y simplificada desarrollada con fines educativos.

---

## Características principales

- Inicio de sesión para docente y estudiantes.
- Registro de asistencia por parte del docente.
- Marcado de asistencia por parte del estudiante.
- Gestión de fechas o sesiones de clase.
- Bloqueo y desbloqueo de sesiones.
- Habilitación del marcado para alumnos.
- Registro de estados de asistencia:
  - Presente
  - Tardanza
  - Ausencia Injustificada
  - Ausencia Justificada
- Visualización del historial de asistencia.
- Estadísticas generales del curso.
- Estadísticas individuales por estudiante.
- Exportación de reportes en formato CSV.
- Persistencia de datos usando `ObjectOutputStream` y `ObjectInputStream`.
- Validaciones básicas de código, fechas y entradas de texto.

---

## Tecnologías utilizadas

- Java
- Java Swing
- Programación Orientada a Objetos
- Serialización de objetos
- Archivos CSV

---

## Requisitos para ejecutar el proyecto

Antes de ejecutar el sistema, se debe contar con:

- Java JDK instalado.
- Un editor o IDE como:
  - Eclipse
  - NetBeans
  - IntelliJ IDEA
  - Visual Studio Code

También puede ejecutarse directamente desde la terminal.

---

## Datos de prueba

El sistema incluye datos de prueba para facilitar la ejecución inicial.

### Usuario docente

```text
Código: 1234567890
Contraseña: 1234567890
```

### Usuarios estudiantes

```text
Código: 2023203021
Contraseña: 72807827
```

```text
Código: 2021601981
Contraseña: 72807827
```

```text
Código: 2023803011
Contraseña: 72807827
```

---

## Pasos para ejecutar el proyecto

### Opción 1: Ejecutar desde un IDE

1. Descargar o clonar este repositorio.

```bash
git clone https://github.com/usuario/nombre-del-repositorio.git
```

2. Abrir el proyecto en el IDE de preferencia.

3. Ubicar el archivo principal:

```text
SistemaAsistencia.java
```

4. Ejecutar la clase `SistemaAsistencia`.

5. Iniciar sesión usando las credenciales de prueba.

---

### Opción 2: Ejecutar desde la terminal

1. Abrir una terminal en la carpeta donde se encuentra el archivo:

```text
SistemaAsistencia.java
```

2. Compilar el archivo con el siguiente comando:

```bash
javac SistemaAsistencia.java
```

3. Ejecutar el programa con:

```bash
java SistemaAsistencia
```

4. Iniciar sesión como docente o estudiante usando los datos de prueba.

---

## Funcionamiento general del sistema

Al iniciar el programa, se muestra una ventana de acceso donde el usuario debe ingresar su código y contraseña.

Dependiendo del usuario, el sistema muestra una de las siguientes vistas:

---

## Panel Docente

Desde esta vista, el docente puede:

- Crear nuevas fechas de asistencia.
- Eliminar sesiones.
- Desbloquear o bloquear una sesión.
- Habilitar el marcado para estudiantes.
- Registrar asistencia manualmente.
- Visualizar estadísticas del curso.
- Descargar un reporte general en CSV.
- Cerrar sesión.

---

## Panel Estudiante

Desde esta vista, el estudiante puede:

- Marcar su asistencia en sesiones habilitadas.
- Revisar su historial de asistencia.
- Ver sus estadísticas personales.
- Descargar su reporte individual en CSV.
- Cerrar sesión.

---

## Persistencia de datos

El sistema utiliza serialización para guardar la información en un archivo local llamado:

```text
datos_asistencia.dat
```

En este archivo se almacenan:

- Estudiantes.
- Registros de asistencia.
- Fechas o sesiones configuradas.

Gracias a esto, los datos no se pierden al cerrar el programa.

---

## Reportes generados

El sistema permite generar reportes en formato CSV.

### Reporte del docente

```text
Reporte_Docente_G07.csv
```

Este reporte incluye el resumen general de asistencia de todos los estudiantes.

### Reporte del estudiante

```text
Reporte_CODIGO.csv
```

Ejemplo:

```text
Reporte_2023203021.csv
```

Este reporte incluye el historial personal de asistencia del estudiante.

---

## Estructura básica del proyecto

```text
SistemaAsistencia.java
README.md
datos_asistencia.dat
Reporte_Docente_G07.csv
Reporte_2023203021.csv
```

El archivo `datos_asistencia.dat` y los reportes CSV se generan automáticamente durante la ejecución del programa.

---

## Validaciones implementadas

El sistema incluye validaciones para evitar errores comunes, tales como:

- El código de usuario debe tener exactamente 10 dígitos.
- Las fechas deben tener el formato `dd/MM/yyyy`.
- No se permiten fechas duplicadas.
- No se puede marcar asistencia si la sesión está bloqueada.
- Un estudiante no puede marcar dos veces la misma sesión.
- Se limpian espacios extra y caracteres invisibles en las entradas de texto.
- Los datos exportados a CSV se limpian para evitar errores por comas o caracteres especiales.

---

## Objetivo académico

El objetivo de este proyecto es aplicar conceptos de desarrollo, validación y testing de software mediante la construcción de un módulo funcional de asistencia.

El sistema permite practicar:

- Desarrollo de interfaces gráficas con Java Swing.
- Manejo de estructuras de datos en memoria.
- Persistencia básica con archivos.
- Validación de entradas.
- Diseño de casos de prueba.
- Exportación de reportes.
- Simulación de un módulo académico inspirado en un sistema real.

---

## Créditos

Proyecto desarrollado como parte del curso:

```text
Testing, Implantación y Mantenimiento de Sistemas
Universidad Católica de Santa María
Escuela Profesional de Ingeniería de Sistemas
```

### Grupo A

```text
Huacallo Inga Thunder Jesus
Urbiola Urquizo Hugo Raul
Rojas Luna Kevin Jostin
```

---

## Estado del proyecto

Proyecto académico funcional en fase de desarrollo y pruebas.

Actualmente cuenta con las funcionalidades principales implementadas para el control de asistencia docente y estudiantil.
