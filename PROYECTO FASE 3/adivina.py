import random

# ============================================================
# BLUE / REFACTOR
# Clase principal del juego.
# ============================================================

class AdivinaElNumero:
    # Constantes del juego.
    # Evitan usar números sueltos como 1, 100 o 10 dentro del código.
    MINIMO = 1
    MAXIMO = 100
    INTENTOS_INICIALES = 10

    # Mensajes que devuelve la comparación.
    MUY_BAJO = "muy bajo"
    MUY_ALTO = "muy alto"
    CORRECTO = "correcto"

    def __init__(self, numero_secreto):
        # Guarda el número secreto que el jugador debe adivinar.
        self.numero_secreto = numero_secreto

        # El juego inicia con 10 intentos disponibles.
        self.intentos_restantes = self.INTENTOS_INICIALES

        # Indica si el jugador ganó o no.
        self.victoria = False

    def validar_intento(self, intento):
        # Verifica que el intento esté dentro del rango permitido: 1 a 100.
        return self.MINIMO <= intento <= self.MAXIMO

    def comparar(self, intento):
        # Compara el intento con el número secreto.
        if intento < self.numero_secreto:
            # Si el intento es menor al número secreto.
            return self.MUY_BAJO

        if intento > self.numero_secreto:
            # Si el intento es mayor al número secreto.
            return self.MUY_ALTO

        # Si no es menor ni mayor, entonces es correcto.
        return self.CORRECTO

    def registrar_intento(self, intento):
        # Si el intento no está dentro del rango, no se descuenta.
        if not self.validar_intento(intento):
            return

        # Se compara el intento con el número secreto.
        resultado = self.comparar(intento)

        # Todo intento válido reduce en 1 los intentos restantes.
        self._reducir_intento()

        # Si el intento fue correcto, se marca la victoria.
        if resultado == self.CORRECTO:
            self._marcar_victoria()

    def _reducir_intento(self):
        # Método interno para descontar un intento válido.
        self.intentos_restantes -= 1

    def _marcar_victoria(self):
        # Método interno para cambiar el estado del juego a victoria.
        self.victoria = True

    def es_victoria(self):
        # Retorna True si el jugador adivinó el número.
        return self.victoria

    def es_derrota(self):
        # Retorna True si ya no quedan intentos y no hubo victoria.
        return self.intentos_restantes == 0 and not self.victoria

    def juego_terminado(self):
        # El juego termina si hay victoria o derrota.
        return self.es_victoria() or self.es_derrota()