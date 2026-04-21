# Desafio de Reaccion y Atencion

Aplicacion Android desarrollada en **Kotlin** con **Jetpack Compose** como parte de una entrega individual. El proyecto propone un juego local de reaccion y atencion en el que el jugador debe identificar el estimulo correcto en el momento justo, evitando errores, falsas salidas y tiempos agotados.

## Descripcion general

La app permite configurar una partida antes de jugar y despues guarda los resultados de forma local en el dispositivo. Durante la partida se trabajan tanto la velocidad de reaccion como la atencion visual, ya que el jugador no solo tiene que tocar rapido, sino tambien interpretar correctamente la regla activa de cada ronda.

La experiencia incluye distintos tipos de estimulos, cambio de dificultad, sistema de vidas, puntaje, medicion de tiempos de reaccion y una pantalla final con resumen de rendimiento.

## Funcionalidades implementadas

- Configuracion inicial de partida
- Ingreso del nombre del jugador
- Seleccion de dificultad: entrenamiento, facil, medio y dificil
- Seleccion de estimulos: colores, numeros, palabras o modo mixto
- Configuracion de iteraciones por nivel
- Tiempo maximo de respuesta configurable
- Activacion o desactivacion de sonidos
- Modo inverso opcional
- Sistema de 3 niveles progresivos
- Sistema de vidas
- Puntaje en tiempo real
- Medicion de tiempos de reaccion
- Resumen final de la partida
- Ranking local por jugador
- Historial reciente de partidas
- Persistencia local sin uso de internet

## Como funciona el juego

En cada ronda aparece una secuencia de estimulos visuales. Segun la configuracion elegida, el jugador debe reaccionar solo cuando corresponde.

En el modo normal, el objetivo es tocar unicamente cuando aparece el estimulo correcto.  
En el modo inverso, la logica cambia y el jugador debe inhibir la respuesta en determinados casos, lo que agrega una capa extra de atencion y control.

Si el jugador toca cuando no corresponde, reacciona antes de tiempo o deja pasar una respuesta obligatoria, pierde una vida. Si completa correctamente las rondas de los niveles, gana la partida.

## Requerimientos cubiertos

- Juego local, sin conexion a internet
- Configuracion de partida antes de comenzar
- Dificultades multiples
- Distintos tipos de estimulos
- Progresion por niveles
- Control de vidas
- Registro de puntaje
- Estadisticas finales
- Persistencia local
- Ranking de resultados

## Arquitectura del proyecto

Se utilizo una arquitectura **MVVM** sencilla y clara, separando la logica del juego de la interfaz.

### Componentes principales

- `MainActivity`: punto de entrada de la app y configuracion de navegacion
- `ReactionGameViewModel`: maneja el estado general de la partida, reglas, niveles, puntaje, vidas y tiempos
- `StimulusFactory`: genera los estimulos segun el modo de juego, el nivel y la configuracion elegida
- `ScoreRepository`: guarda y recupera resultados locales usando `SharedPreferences`
- `model`: contiene enums y data classes del juego
- `ui/screens`: pantallas desarrolladas con Compose

## Pantallas implementadas

### SetupScreen
Pantalla inicial donde el usuario configura toda la partida antes de empezar.

### GameScreen
Pantalla principal del juego, donde se muestran los estimulos, el progreso, las vidas, el puntaje y la informacion de cada ronda.

### ResultScreen
Pantalla final con resumen de desempeno, incluyendo puntaje, aciertos, errores, tiempos de reaccion y resultado de la partida.

### ScoreboardScreen
Pantalla de ranking local con los mejores resultados por jugador y las ultimas partidas guardadas.

## Persistencia local

Los resultados se almacenan localmente en el dispositivo usando `SharedPreferences`.  
Se guarda tanto el historial reciente como los mejores puntajes por jugador para construir el ranking local. Esto permite cumplir con la consigna de mantener la app completamente offline.

## Decisiones de diseno

- Se uso **Jetpack Compose** para construir una interfaz moderna, clara y facil de mantener
- La logica principal se concentro en el **ViewModel** para evitar mezclar reglas del juego con la UI
- Se implementaron modos de juego variados para hacer la experiencia mas entretenida
- Se agrego persistencia local para que el usuario pueda comparar resultados entre partidas
- Se incluyeron feedbacks visuales y sonoros para reforzar la interaccion del juego

## Tecnologias utilizadas

- Kotlin
- Android Studio
- Jetpack Compose
- Material 3
- ViewModel
- SharedPreferences
- ToneGenerator

## Como ejecutar el proyecto

1. Clonar o descargar este repositorio
2. Abrir el proyecto en Android Studio
3. Esperar la sincronizacion de Gradle
4. Ejecutar la app en un emulador o dispositivo Android
5. Probar distintas configuraciones de partida

## Estructura general del proyecto

- `app/src/main/java/com/uade/desafioreaccion/MainActivity.kt`
- `app/src/main/java/com/uade/desafioreaccion/viewmodel/ReactionGameViewModel.kt`
- `app/src/main/java/com/uade/desafioreaccion/data/StimulusFactory.kt`
- `app/src/main/java/com/uade/desafioreaccion/data/ScoreRepository.kt`
- `app/src/main/java/com/uade/desafioreaccion/model/`
- `app/src/main/java/com/uade/desafioreaccion/ui/screens/`

## Posibles mejoras futuras

- Agregar animaciones mas avanzadas
- Incorporar mas tipos de estimulos
- Mostrar estadisticas historicas mas completas
- Exportar resultados
- Incluir modos de juego adicionales
