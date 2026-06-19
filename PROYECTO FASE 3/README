# Adivina el Número

## Descripción del proyecto

**Adivina el Número** es un juego desarrollado en Python aplicando la metodología **TDD**.
El objetivo del jugador es adivinar un número secreto generado aleatoriamente dentro del rango de **1 a 100**.

El jugador cuenta con un máximo de **10 intentos**. Después de cada intento válido, el sistema indica si el número ingresado es:

* Muy bajo
* Muy alto
* Correcto

El juego finaliza cuando el jugador adivina correctamente el número secreto o cuando se queda sin intentos disponibles.

---

## Tecnologías utilizadas

* Python 3.x
* Pytest
* Pytest-cov

---

## Estructura del proyecto

```text
AdivinaElNumero/
│
├── adivina.py
├── main.py
├── test_adivina.py
└── README.md
```

---

## Descripción de archivos

| Archivo           | Descripción                                                                  |
| ----------------- | ---------------------------------------------------------------------------- |
| `adivina.py`      | Contiene la lógica principal del juego y la clase `AdivinaElNumero`.         |
| `main.py`         | Contiene la ejecución del juego por consola.                                 |
| `test_adivina.py` | Contiene la suite de pruebas automatizadas desarrollada con `pytest`.        |
| `README.md`       | Contiene las instrucciones de instalación, ejecución y pruebas del proyecto. |

---

## Requisitos previos

Antes de ejecutar el proyecto, se debe contar con:

```text
Python 3.x
pip
```

Para verificar si Python está instalado, se puede ejecutar:

```bash
python --version
```

O también:

```bash
py --version
```

---

## Instalación

Primero, se debe abrir una terminal en la carpeta del proyecto:

```bash
cd AdivinaElNumero
```

Luego, se instalan las dependencias necesarias:

```bash
python -m pip install pytest pytest-cov
```

En caso de usar `py` en Windows, también se puede ejecutar:

```bash
py -m pip install pytest pytest-cov
```

---

## Ejecución del juego

Para iniciar el juego por consola, se debe ejecutar el archivo `main.py`:

```bash
python main.py
```

O en Windows:

```bash
py main.py
```

Al ejecutar el programa, se mostrará la interfaz por consola. El usuario deberá ingresar números entre **1 y 100** hasta adivinar el número secreto o hasta quedarse sin intentos.

---

## Ejecución de pruebas

Para ejecutar la suite completa de pruebas automatizadas, se utiliza el siguiente comando:

```bash
python -m pytest test_adivina.py
```

O en Windows:

```bash
py -m pytest test_adivina.py
```

Si todas las pruebas se ejecutan correctamente, la terminal debe mostrar un resultado similar a:

```text
15 passed
```

---

## Cobertura de código

Para medir la cobertura de código se utilizó la herramienta `pytest-cov`.

La cobertura se aplica sobre el archivo `adivina.py`, ya que este contiene la lógica principal del juego. La ejecución por consola se encuentra separada en `main.py`, lo cual permite medir de forma más precisa la cobertura de los métodos principales del sistema.

Para ejecutar las pruebas con cobertura de código, se utiliza el siguiente comando:

```bash
python -m pytest test_adivina.py --cov=adivina --cov-report=term-missing
```

O en Windows:

```bash
py -m pytest test_adivina.py --cov=adivina --cov-report=term-missing
```

El resultado esperado debe ser similar al siguiente:

```text
Name         Stmts   Miss  Cover   Missing
------------------------------------------
adivina.py      XX      0   100%
------------------------------------------
TOTAL           XX      0   100%

15 passed
```

El resultado demuestra que las pruebas automatizadas cubren el **100% de la lógica principal del juego** contenida en el archivo `adivina.py`.

---

## Funcionalidades principales

El juego cumple con las siguientes funcionalidades:

* Manejo de un número secreto dentro del rango de 1 a 100.
* Validación de intentos ingresados por el usuario.
* Rechazo de intentos inválidos.
* Comparación del intento con el número secreto.
* Mensajes de retroalimentación: “muy bajo”, “muy alto” o “correcto”.
* Inicio de partida con 10 intentos disponibles.
* Reducción de intentos únicamente cuando el intento es válido.
* Detección de victoria.
* Detección de derrota.
* Verificación del estado final del juego.

---

## Aplicación de TDD

El desarrollo del juego se realizó aplicando el ciclo TDD:

### Red

Se escribieron primero las pruebas automatizadas en el archivo `test_adivina.py`, tomando como base los requerimientos funcionales del juego.

### Green

Se implementó el código mínimo necesario en `adivina.py` para que las pruebas pasaran correctamente.

### Refactor

Se mejoró la estructura del código, separando la lógica principal del juego en `adivina.py` y la ejecución por consola en `main.py`, manteniendo todas las pruebas aprobadas.

---

## Conclusión

El proyecto **Adivina el Número** permite demostrar la aplicación práctica de TDD en el desarrollo de un juego sencillo por consola. La separación entre la lógica principal y la interfaz de ejecución facilita el mantenimiento del código y permite obtener una cobertura completa de pruebas sobre los métodos principales del sistema.
