# Recetario Boliviano

Aplicación móvil Android desarrollada en Kotlin con arquitectura MVVM, Room Database y Material Design 3.

## Características

- **Pantalla de Bienvenida (Splash Screen)**: Imagen de fondo con comida boliviana y botón "Siguiente"
- **Registro de Usuario**: Nombre, departamento (de Bolivia), avatar (cámara o galería)
- **Perfil de Usuario**: Ver y cambiar avatar únicamente
- **Navegación Principal**:
  - Inicio
  - Departamentos (La Paz, Cochabamba, Santa Cruz, Oruro, Potosí, Chuquisaca, Tarija, Beni, Pando)
  - Ver Todo
  - Favoritos
  - Perfil

## Requisitos Técnicos

- Android Studio (última versión)
- Lenguaje: Kotlin
- Base de datos: Room (SQLite)
- UI: Material Design 3
- Arquitectura: MVVM
- Navegación: Navigation Component

## Funcionalidades

- CRUD completo de recetas
- Sistema de favoritos
- Búsqueda por nombre
- Filtros por categoría (Sopa, Segundo, Postre)
- Filtros por departamento
- 30+ recetas pre-cargadas de Bolivia

## Instalación

1. Clonar o extraer el proyecto
2. Abrir en Android Studio
3. Sincronizar Gradle
4. Compilar y ejecutar

## Estructura del Proyecto

```
RecetarioBoliviano/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/recetarioboliviano/
│   │   │   ├── modelo/
│   │   │   │   ├── dao/
│   │   │   │   ├── entidades/
│   │   │   │   ├── repositorio/
│   │   │   │   └── util/
│   │   │   ├── vista/
│   │   │   │   ├── actividades/
│   │   │   │   └── adaptadores/
│   │   │   └── vistamodelo/
│   │   └── res/
│   │       ├── drawable/
│   │       ├── layout/
│   │       ├── menu/
│   │       ├── values/
│   │       └── xml/
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Recetas Incluidas

- **La Paz**: Chairo Paceño, Fricasé Paceño, Plato Paceño, Helado de Canela, Queso Humo
- **Cochabamba**: Sopa de Maní, Silpancho, Pique Macho, Rosquetes de Punata, T'impiri
- **Santa Cruz**: Locro de Gallina, Majadito, Keperí, Cuñapé, Somó
- **Oruro**: Charquekan, Patarata, Api Morado
- **Potosí**: Kanka, Chicharrón de Llama, Tortica de Nata
- **Chuquisaca**: Lagua, Fritata, Mote con Huesos
- **Tarija**: Milanesa Nutria, Sango, Alfajor
- **Beni**: Masaco, Jenefarto, Mbuú
- **Pando**: Criadillas, Juane, Cocos con Miel

## Licencia

Este proyecto es software libre bajo licencia MIT.
