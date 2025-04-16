# Movies and Series Share

Estamos desarrollando una App multiplataforma (Android y iOS) para crear y compartir opiniones sobre Peliculas y Series que hayas o estes viendo.
Para ello, el usuario tendra inicialmente un formulario donde podra incluir los detalles para que se genere la opinion.
Por ahora en el formulario tendra:
-Nombre de Serie o Pelicula
-Puntuacion
-Comentario
-Imagen: Ver si existe alguna api de series y peliculas para obtener imagenes de portada.
-Genero
-Plataforma

Esta informacion se guardara en una base de datos, por ahora podemos guardarla en la base de datos de la app, pero a futuro podra ser guardada en la nube.
Una vez se genere la opinion, se mostrará una nueva pantalla con un listado donde se puedan ver las opiniones de otros usuarios.

Además, el usuario podrá hacer click sobre cada una de las opiniones para ver en detalle.

## Tecnologías

Estas son las tecnologías que vamos a utilizar para cada una de las funcionalidades.
Todas las librerias y sus versiones correspondientes ya estan en el build.gradle.kts

- Es un proyecto multiplataforma(Android y iOS). Usando Compose Multiplatform (CMM). Todo la UI se escribe dentro del modulo composeApp dentro de commonMain salvo configuraciones particulares de cada plataforma
- Jetpack Compose como sistema de interfaces.
- Material 3 como sistema de diseño.
- Kotlin como lenguaje de programación.
- Los ViewModels de Jetbrains para proyectos CMM para la comunicación entre la UI y la capa de datos.
- Koin multiplataforma como inyector de dependencias.
- Para persistencia de las conversaciones, utilizaremos Room. Recuerda usar KSP (la versión actual es 2.1.20-1.0.32) y no KAPT para las dependencias que generan código, como el compiler de Room.
- La API de https://www.omdbapi.com/ para obtener las imagenes de las peliculas o series.
- Ktor como cliente HTTP.
## Arquitectura
Vamos a usar una arquitectura sencilla, donde tendremos:
- La UI en Compose Multiplatform
- Comunicación con la capa de datos mediante MVVM
- La capa de datos estará formada por repositorios, que ocultarán qué librerías concretas se están utilizando.

## Reglas extra

- Siempre que termines de generar un código, compílalo para ver que no hay problemas. Para ello, utiliza compileDebugKotlin
- Aunque pienses que los build.gradle.kts están incorrectos, los que tienes ahora mismo en el contexto son válidos. Si tienes que modificar el libs.versions o los ficheros gradle, simplemente añade lo nuevo que necesites, y no modifiques lo que ya existe.
- No incluyas comentarios solo para explicar lo que ya hace el código. Solo en caso de que haya alguna parte que se quede así porque en el futuro haya que añadir nueva funcionalidad.