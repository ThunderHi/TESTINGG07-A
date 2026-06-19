import random
from adivina import AdivinaElNumero


def jugar():
    numero_secreto = random.randint(
        AdivinaElNumero.MINIMO,
        AdivinaElNumero.MAXIMO
    )

    juego = AdivinaElNumero(numero_secreto)

    print("=" * 40)
    print(" ADIVINA EL NÚMERO")
    print("=" * 40)
    print("Debes adivinar un número entre 1 y 100.")
    print("Tienes 10 intentos.\n")

    while not juego.juego_terminado():
        print(f"Intentos restantes: {juego.intentos_restantes}")
        entrada = input("Ingresa tu intento: ")

        try:
            intento = int(entrada)
        except ValueError:
            print("Debes ingresar un número entero.\n")
            continue

        if not juego.validar_intento(intento):
            print("Número fuera del rango permitido.\n")
            continue

        resultado = juego.comparar(intento)
        juego.registrar_intento(intento)

        if resultado == juego.MUY_BAJO:
            print("Muy bajo.\n")
        elif resultado == juego.MUY_ALTO:
            print("Muy alto.\n")
        elif resultado == juego.CORRECTO:
            print("\n¡Ganaste! Adivinaste el número.")

    if juego.es_derrota():
        print(f"\nPerdiste. El número era {juego.numero_secreto}.")


if __name__ == "__main__":
    jugar()