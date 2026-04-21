# Desafio de Reaccion y Atencion - Entrega individual

## Objetivo de la app
La aplicacion propone un juego local para entrenar reaccion y atencion. El usuario configura la partida, juega una secuencia de rondas dividida en 3 niveles y al finalizar se guardan los resultados en el dispositivo para mostrar un ranking local por jugador.

## Requerimientos cubiertos
- Inicio con configuracion completa: nombre del jugador, dificultad, tipo de estimulos, iteraciones por nivel, tiempo maximo de respuesta, modo inverso y sonidos.
- Juego con 3 niveles progresivos.
- Cada partida arranca con 3 vidas.
- Si el jugador responde mal o deja pasar un estimulo obligatorio, pierde una vida.
- Si se queda sin vidas, la partida termina en derrota.
- Si completa las rondas de los 3 niveles, gana la partida.
- Modo entrenamiento, facil, medio y dificil.
- Estimulos por colores, numeros, palabras o modo mixto.
- Modo inverso opcional.
- Puntaje, tiempos de reaccion y resumen final.
- Persistencia local con SharedPreferences.
- Ranking local con mejores resultados por jugador.
- Sonidos simples generados con ToneGenerator.

## Arquitectura propuesta
Se uso una arquitectura simple MVVM:
- `MainActivity`: crea el ViewModel y la navegacion.
- `ReactionGameViewModel`: concentra la logica del juego, niveles, vidas, puntaje, temporizador y guardado.
- `ScoreRepository`: persiste y recupera partidas desde `SharedPreferences`.
- `StimulusFactory`: genera estimulos segun modo, nivel y modo inverso.
- `ui/screens`: pantallas Compose separadas por responsabilidad.
- `model`: enums y data classes de configuracion, estado y resultados.

## Pantallas implementadas
1. `SetupScreen`: pantalla de inicio y configuracion.
2. `GameScreen`: juego en tiempo real.
3. `ResultScreen`: resumen final, victoria o derrota.
4. `ScoreboardScreen`: ranking local y ultimas partidas guardadas.

## Persistencia
Los resultados se guardan como JSON dentro de `SharedPreferences`. El ranking muestra el mejor resultado competitivo de cada jugador. Las partidas de entrenamiento tambien se guardan en historial, pero no compiten en el ranking principal.

## Decision de diseno
- Se eligio Jetpack Compose para construir una interfaz clara y rapida de mantener.
- La logica se centralizo en el ViewModel para no mezclar reglas del juego con la UI.
- Se agregaron feedbacks visuales y sonoros para reforzar la experiencia ludica.
- El ranking se ordena por puntaje y, ante empate, por mejor desempeno.

## Como ejecutar
1. Clonar el repositorio y abrirlo en Android Studio.
2. Esperar sincronizacion de Gradle.
3. Ejecutar la app en un emulador o dispositivo Android.
4. Probar partidas con distintas configuraciones.

## Archivos principales
- `app/src/main/java/com/uade/desafioreaccion/MainActivity.kt`
- `app/src/main/java/com/uade/desafioreaccion/viewmodel/ReactionGameViewModel.kt`
- `app/src/main/java/com/uade/desafioreaccion/data/ScoreRepository.kt`
- `app/src/main/java/com/uade/desafioreaccion/data/StimulusFactory.kt`
