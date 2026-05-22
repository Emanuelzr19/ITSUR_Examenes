# ITSUR — Sistema de Exámenes

Aplicación Kotlin Multiplatform (KMP) con Compose Multiplatform para Android y Desktop.

## Credenciales por defecto

| Rol   | Email                  | Contraseña |
|-------|------------------------|------------|
| Admin | admin@itsur.edu.mx     | admin123   |

## Arquitectura

```
ITSURExamenes/
├── shared/           ← Lógica compartida (Android + Desktop)
│   └── commonMain/
│       ├── data/     ← Repositorios + SQLDelight
│       ├── domain/   ← Modelos, casos de uso
│       ├── presentation/ ← ViewModels (Voyager ScreenModel)
│       └── ui/       ← Screens y Componentes (Compose)
├── androidApp/       ← Entry point Android
└── desktopApp/       ← Entry point Desktop (JVM)
```

## Stack tecnológico

| Librería              | Versión | Uso                       |
|-----------------------|---------|---------------------------|
| Kotlin Multiplatform  | 2.0.0   | Código compartido          |
| Compose Multiplatform | 1.6.11  | UI declarativa            |
| SQLDelight            | 2.0.2   | Base de datos local       |
| Koin                  | 3.5.6   | Inyección de dependencias |
| Voyager               | 1.0.0   | Navegación                |
| QRose                 | 1.0.1   | Generación de QR          |

## Cómo ejecutar

### Android
```bash
./gradlew :androidApp:installDebug
```

### Desktop
```bash
./gradlew :desktopApp:run
```

### Generar APK de release
```bash
./gradlew :androidApp:assembleRelease
```

### Generar instalador Desktop
```bash
./gradlew :desktopApp:createDistributable
```

## Funcionalidades

### Rol Administrador
- ✅ Gestión de exámenes (crear/editar/eliminar)
- ✅ Constructor dinámico de preguntas (opción múltiple, V/F, abierta)
- ✅ Gestión de alumnos (registrar/eliminar)
- ✅ Reportes por examen con estadísticas
- ✅ Ver calificaciones individuales

### Rol Alumno
- ✅ Ver exámenes disponibles
- ✅ Tomar examen con temporizador de progreso
- ✅ Navegación entre preguntas
- ✅ Resultado con calificación y QR de verificación
- ✅ Historial de resultados propios

## Tema visual
- **Color primario:** Verde lima (#8BC34A)
- **Acento:** Amarillo dorado (#FFC107)
- **Navy:** Azul oscuro (#1A237E)
- **Fuente:** System default optimizada para legibilidad

