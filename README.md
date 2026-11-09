# BibleSearchUM 📖

Aplicación nativa para Android desarrollada con **Jetpack Compose**, **Retrofit 2** y arquitectura **MVVM (Model-View-ViewModel)** que consume la API REST abierta de [Bolls Bible](https://bolls.life/) para la consulta interactiva y reactiva de libros, capítulos y versículos bíblicos.

---

## 🚀 Características Principales

- **Consumo de API REST Asíncrono:** Integración con Retrofit 2, Gson y Kotlin Coroutines para la obtención de datos remotos sin bloquear el hilo de la interfaz de usuario.
- **Arquitectura MVVM Limpia:** Separación estricta de responsabilidades entre Vista (Compose), Modelo (Data classes) y Lógica de Negocio (ViewModel con StateFlow inmutable).
- **Menús Desplegables Dependientes:** Implementación de selectores en cascada (*Traducción*, *Libro*, *Capítulo*, *Versículo*) utilizando el componente `ExposedDropdownMenuBox` de Material 3.
- **Gestión Reactiva de Estados:** Manejo explícito y declarativo de estados de carga (`isLoading`), éxito (`currentVerseContent`) y error (`errorMessage`).
- **Diseño Moderno Material 3:** Interfaz pulida con soporte de temas claro y dinámico, tarjetas elevadas, tipografía con serifa para lectura bíblica y `TopAppBar` centrada.

---

## 🌟 Retos Adicionales Implementados (Puntos Extra)

| Reto | Descripción | Implementación |
| :--- | :--- | :--- |
| **Reto 1: Compartir Versículo** | Botón de acción y FAB para compartir la cita y texto bíblico hacia otras apps. | `Intent.ACTION_SEND` implícito con `Intent.createChooser` tanto en la tarjeta del versículo como en un Floating Action Button. |
| **Reto 2: Persistencia Local de Favoritos** | Guardar versículos favoritos y visualizarlos en cualquier momento. | Implementado con **Jetpack Preferences DataStore** reactivo con `Flow`, con un modal desplegable (`ModalBottomSheet`) para listar, consultar y eliminar versículos guardados. |
| **Reto 3: Selector Dinámico de Traducciones** | Cambio dinámico entre diferentes traducciones disponibles en el API de Bolls Bible. | Soporte multiversión (RV1960, NVI, LBLA, NTBT) con endpoints REST dinámicos en Retrofit y normalizador de slugs. |

---

## 🛠️ Tecnologías y Librerías

- **Lenguaje:** Kotlin 2.x
- **UI:** Jetpack Compose con Compose BOM `2024.02.01` & Material 3
- **Ciclo de Vida & Estado:** `androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0`, `lifecycle-runtime-compose:2.7.0`
- **Red:** `Retrofit 2.9.0` + `converter-gson` + `OkHttp3 Logging Interceptor`
- **Persistencia:** `androidx.datastore:datastore-preferences:1.1.1`
- **Corrutinas:** `kotlinx.coroutines-android:1.7.3`
- **Testing:** `JUnit 4`, `kotlinx-coroutines-test`

---

## 📂 Estructura del Proyecto

```text
app/src/main/java/com/example/blesearchum/
├── MainActivity.kt                      # Activity principal con edge-to-edge
├── data/
│   └── FavoritesRepository.kt           # Repositorio DataStore para versículos favoritos (Reto 2)
├── model/
│   └── BibleModels.kt                   # Clases de datos (BibleBook, BibleVerse, FavoriteVerse, etc.)
├── network/
│   └── BibleApiService.kt               # Interfaz Retrofit, interceptores y RetrofitInstance
├── ui/
│   ├── BibleUiState.kt                  # Data class inmutable que modela todo el estado de la UI
│   ├── BibleViewModel.kt                # ViewModel con lógica de llamadas, estado y reactividad
│   ├── BibleSearchScreen.kt             # Pantalla Compose principal con selectores y tarjeta de pasaje
│   ├── components/
│   │   └── DropdownSelector.kt          # Componente reutilizable con ExposedDropdownMenuBox M3
│   └── theme/
│       ├── Color.kt                     # Paleta de colores Material 3
│       ├── Theme.kt                     # Configuración de tema claro/oscuro dinámico
│       └── Type.kt                      # Tipografía de la aplicación
```

---

## 📋 Requisitos para Ejecutar

- **Android Studio:** Jellyfish | Koala | Ladybug o superior.
- **JDK:** Java 17 o 21.
- **Android SDK:**
  - Min SDK: 26 (Android 8.0 Oreo)
  - Target/Compile SDK: 34

---

## 🧪 Pruebas Unitarias

El proyecto incluye pruebas unitarias para `BibleViewModel` en `app/src/test/java/com/example/blesearchum/BibleViewModelTest.kt`, cubriendo:
- Carga inicial asíncrona de libros.
- Generación reactiva de capítulos según el libro seleccionado.
- Población del rango de versículos dependientes.
- Manejo de respuestas y visualización del pasaje bíblico.
- Reseteo consistente de selecciones mediante `clearSelection()`.
