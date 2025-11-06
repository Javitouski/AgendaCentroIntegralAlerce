# AgendaCentroIntegralAlerce
Esta aplicacion es una agenda mensual y semanal, con sistema de LogIn a base de Firebase Autenticator y un sistema para agendar citas, crear usuarios (teniendo los permisos), entre otras funciones de diseño para una excelente UX.

---

## 👥 Integrantes del Equipo

* Darien Sifert    
* Gillermo Contreras
* Javier Rebolledo
* Pedro Soto
* Benjamin Lineros

---

## 📋 Información General

### Estructura de Evaluación del Proyecto

| Evaluación | Porcentaje | Fecha Límite | Descripción |
| :--- | :---: | :---: | :--- |
| **N2** | 15% | 06 de Octubre | Primera entrega: Planificación, Diseño y Prototipo |
| **N3** | 20% | 20 de Octubre | Segunda entrega: Control de avance |
| **N4** | 25% | 03 de Noviembre| Tercera entrega: Control de avance, Conexión BD |
| **N5** | 30% | 19 de Noviembre| Cuarta entrega: Entrega Final |

> **Nota:** La evaluación N1 es independiente. Las evaluaciones N2, N3, N4 y N5 corresponden al proyecto de desarrollo.

---

## 🎯 Requisitos Generales del Proyecto

### 1. Requisitos Técnicos Obligatorios

* **Plataforma y Tecnologías:**
    * Desarrollo en **Android Studio** (`Kotlin` o `Java`).
    * Base de datos: **Firebase** (`Firestore`, `Realtime Database` o ambas).
    * Control de versiones: **Git y GitHub** (repositorio del equipo).
    * Publicación: **Google Play Console** (en modo prueba/interno).
* **Funcionalidades Obligatorias:**
    * Sistema de autenticación de usuarios (`Firebase Authentication`).
    * Sistema de roles/perfiles (mínimo 2 roles: `admin` y `usuario`).
    * Operaciones **CRUD** completas sobre al menos una entidad principal.
    * Alertas y notificaciones (diálogos, toasts, snackbars, notificaciones push).
    * Permisos de usuario según roles (acceso diferenciado a funcionalidades).
* **Organización del Equipo:**
    * Equipos de máximo 6 integrantes.
    * Uso obligatorio de `Git`/`GitHub` para colaboración.
    * Planificación de tareas con asignación clara de responsabilidades.
    * Commits frecuentes y descriptivos.

### 2. Componentes Adicionales Recomendados

* Aplicación web complementaria conectada a la misma base de datos Firebase.
* Uso de permisos del sistema Android (cámara, ubicación, almacenamiento, etc.).
* Implementación de patrones de diseño (`MVVM`, `Repository`, etc.).

---

## 📦 Entregas y Pautas de Evaluación

### N2 - Primera Entrega (15%)
* **Fecha límite:** 06 de Octubre
* **Tema:** Planificación, Diseño y Prototipo

#### Componentes a entregar:
* Ficha de inscripción completada y firmada.
* Documento de planificación del proyecto.
* Prototipo visual (Figma, Adobe XD, etc.).
* Repositorio GitHub creado y compartido con el docente.

#### Rúbrica de Evaluación N2 (100 puntos)
| Criterio | Puntaje | Descripción |
| :--- | :---: | :--- |
| **Ficha de inscripción** | 5 pts | Completitud y formalidad del documento. |
| **Documento de planificación**| 35 pts | Claridad de objetivos (10), descripción detallada (10), funcionalidades bien definidas (10), distribución de tareas (5). |
| **Diseño del prototipo** | 35 pts | Completitud de pantallas (15), coherencia visual (10), flujo de navegación lógico (10). |
| **Definición de roles** | 10 pts | Roles claramente especificados y diferenciados. |
| **Repositorio GitHub** | 10 pts | Estructura adecuada (5), README descriptivo (5). |
| **Presentación general** | 5 pts | Calidad general de la documentación. |

### N3 - Segunda Entrega (20%)
* **Fecha límite:** 20 de Octubre
* **Tema:** Primer Control de Avance

#### Componentes a entregar:
* Aplicación funcional con implementación inicial (40-50%).
* Sistema de autenticación operativo.
* Código en GitHub con commits regulares de todos los integrantes.
* Informe de avance breve.

#### Rúbrica de Evaluación N3 (100 puntos)
| Criterio | Puntaje | Descripción |
| :--- | :---: | :--- |
| **Funcionalidad implementada** | 40 pts | Porcentaje de funciones operativas (20), navegación funcional (10), estabilidad de la app (10). |
| **Sistema de autenticación** | 20 pts | Login/registro funcional (10), integración con Firebase (10). |
| **Calidad del código** | 15 pts | Estructura organizada (8), código limpio y comentado (7). |
| **Uso de GitHub** | 15 pts | Commits frecuentes (8), participación de todos los integrantes (7). |
| **Informe de avance** | 10 pts | Claridad y honestidad sobre el progreso. |

### N4 - Tercera Entrega (25%)
* **Fecha límite:** 03 de Noviembre
* **Tema:** Conexión a Base de Datos y CRUD

#### Componentes a entregar:
* Aplicación avanzada (70-80% de funcionalidades).
* Integración completa con Firebase (CRUD operativo).
* Implementación de permisos por rol.
* APK de prueba instalable.
* Actualización en GitHub con toda la funcionalidad.

#### Rúbrica de Evaluación N4 (100 puntos)
| Criterio | Puntaje | Descripción |
| :--- | :---: | :--- |
| **Funcionalidad avanzada** | 30 pts | Progreso sustancial (15), CRUD completo (15). |
| **Integración Firebase** | 25 pts | Conexión estable (10), operaciones CRUD en BD (10), sincronización (5). |
| **Sistema de roles/permisos** | 20 pts | Roles implementados (10), restricciones funcionando (10). |
| **Alertas y notificaciones** | 10 pts | Implementación apropiada de alertas/notificaciones. |
| **Calidad técnica** | 10 pts | Manejo de errores (5), validaciones (5). |
| **APK funcional** | 5 pts | App instalable y ejecutable sin errores críticos. |

### N5 - Entrega Final (30%)
* **Fecha límite:** 19 de Noviembre
* **Tema:** Entrega Final del Proyecto

#### Componentes a entregar:
* Aplicación completa y funcional (100%).
* APK final optimizado.
* Publicación en Google Play Console (modo prueba/interno).
* Código fuente final, limpio y documentado en GitHub.
* Video demostración (3-5 minutos).
* Manual de usuario (PDF).
* Aplicación web complementaria (si aplica).

#### Rúbrica de Evaluación N5 (100 puntos)
| Criterio | Puntaje | Descripción |
| :--- | :---: | :--- |
| **Funcionalidad completa** | 30 pts | Todas las funciones operativas (15), estabilidad y rendimiento (10), manejo de errores (5). |
| **Calidad de la interfaz** | 15 pts | Diseño profesional (8), experiencia de usuario (7). |
| **Integración técnica** | 15 pts | Firebase bien implementado (8), roles/permisos robustos (7). |
| **Video demostración** | 10 pts | Claridad (5), cobertura de funcionalidades (5). |
| **Manual de usuario** | 10 pts | Completitud (5), claridad y calidad (5). |
| **Publicación Google Play** | 5 pts | Configuración correcta en modo prueba. |
| **GitHub - Código final** | 10 pts | Código limpio y documentado (5), README completo (5). |
| **App web (Opcional)** | 5 pts | Si está implementada y funcional (valorado). |

---

## 📌 Consideraciones Importantes

### Formato de Entrega
* **Documentos:** PDF o formato editable (`.docx`).
* **Código:** Repositorio GitHub con acceso al docente.
* **APK:** Subir a una carpeta compartida o a los `releases` de GitHub.
* **Video:** Link de YouTube, Google Drive o similar.

### Criterios de Aprobación
* Cada entrega debe cumplir con un mínimo de **60 puntos**.
* El proyecto debe demostrar trabajo colaborativo real (visible en los `commits`).
* La aplicación debe ser funcional y estable.

### Penalizaciones
* **Entrega fuera de plazo:** -10 puntos por día de atraso.
* **Falta de trabajo colaborativo:** -20 puntos si no se evidencia trabajo en equipo en GitHub.
* **Código desorganizado:** -15 puntos si el código no tiene comentarios o está mal estructurado.
* **Plagio:** Nota 1.0.

### Recomendaciones
* Realizar `commits` frecuentes y descriptivos.
* Probar la aplicación en diferentes dispositivos.
* Implementar validaciones y manejo de excepciones.
* Seguir los principios de diseño de **Material Design**.
* Consultar dudas oportunamente con el docente.
