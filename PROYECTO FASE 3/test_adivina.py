import pytest 
from adivina import AdivinaElNumero

class TestGeneracion:
    # RF01: El sistema debe manejar un número secreto dentro del rango de 1 a 100.
    def test_numero_secreto_dentro_de_rango(self):
        juego = AdivinaElNumero(numero_secreto=50)
        assert 1 <= juego.numero_secreto <= 100


class TestValidacion:
    # RF02: El sistema debe permitir validar si un intento está dentro del rango permitido.
    def test_entrada_valida_retorna_true(self):
        juego = AdivinaElNumero(numero_secreto=50)
        assert juego.validar_intento(45) == True

    # RF03: El sistema debe rechazar intentos mayores a 100.
    def test_entrada_fuera_de_rango_retorna_false(self):
        juego = AdivinaElNumero(numero_secreto=50)
        assert juego.validar_intento(200) == False

    # RF03: El sistema debe rechazar intentos menores a 1.
    def test_entrada_cero_retorna_false(self):
        juego = AdivinaElNumero(numero_secreto=50)
        assert juego.validar_intento(0) == False

    # RF03: El sistema no debe reducir intentos cuando el intento es inválido.
    def test_intento_invalido_no_reduce_contador(self):
        juego = AdivinaElNumero(numero_secreto=50)
        juego.registrar_intento(200)
        assert juego.intentos_restantes == 10

class TestComparacion:
    # RF04 y RF05: Si el intento es menor que el número secreto, debe indicar "muy bajo".
    def test_intento_muy_bajo(self):
        juego = AdivinaElNumero(numero_secreto=50)
        assert juego.comparar(30) == "muy bajo"

    # RF04 y RF05: Si el intento es mayor que el número secreto, debe indicar "muy alto".
    def test_intento_muy_alto(self):
        juego = AdivinaElNumero(numero_secreto=50)
        assert juego.comparar(80) == "muy alto"

    # RF04 y RF05: Si el intento es igual al número secreto, debe indicar "correcto".
    def test_intento_correcto(self):
        juego = AdivinaElNumero(numero_secreto=50)
        assert juego.comparar(50) == "correcto"

class TestIntentos:
    # RF06: El sistema debe iniciar cada partida con 10 intentos disponibles.
    def test_intentos_iniciales_son_10(self):
        juego = AdivinaElNumero(numero_secreto=50)
        assert juego.intentos_restantes == 10

    # RF07: El sistema debe reducir en 1 los intentos cuando se registra un intento válido.
    def test_intento_reduce_contador(self):
        juego = AdivinaElNumero(numero_secreto=50)
        juego.registrar_intento(30)
        assert juego.intentos_restantes == 9

    # RF09: El sistema debe identificar la derrota cuando se terminan los intentos.
    def test_sin_intentos_es_derrota(self):
        juego = AdivinaElNumero(numero_secreto=50)
        for _ in range(10):
            juego.registrar_intento(30)
        assert juego.es_derrota() == True

class TestEstadoJuego:
    # RF08: El sistema debe identificar la victoria cuando el jugador adivina el número.
    def test_adivinar_marca_victoria(self):
        juego = AdivinaElNumero(numero_secreto=50)
        juego.registrar_intento(50)
        assert juego.es_victoria() == True

    # RF10: El sistema debe indicar que el juego no ha terminado si aún hay intentos y no hay victoria.
    def test_juego_no_terminado_si_hay_intentos(self):
        juego = AdivinaElNumero(numero_secreto=50)
        juego.registrar_intento(30)
        assert juego.juego_terminado() == False

    # RF10: El sistema debe indicar que el juego terminó cuando hay victoria.
    def test_juego_terminado_si_hay_victoria(self):
        juego = AdivinaElNumero(numero_secreto=50)
        juego.registrar_intento(50)
        assert juego.juego_terminado() == True

    # RF10: El sistema debe indicar que el juego terminó cuando hay derrota.
    def test_juego_terminado_si_hay_derrota(self):
        juego = AdivinaElNumero(numero_secreto=50)
        for _ in range(10):
            juego.registrar_intento(30)
        assert juego.juego_terminado() == True