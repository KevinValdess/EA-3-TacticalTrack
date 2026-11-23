# TacticalTrack

DSY1105 · Desarrollo de Aplicaciones Móviles · Entrega de Evaluación Parcial 4 y defensa de Evaluación Parcial 5.

Aplicación Android para registrar y verificar visitas en terreno. Permite asociar cada visita a un punto de venta y un equipo, adjuntar una fotografía y ubicación GPS, y revisar su aprobación o rechazo. Una API propia en Spring Boot guarda los registros y gestiona los avisos al supervisor mediante Firebase Cloud Messaging. El clima del lugar se consulta en Open-Meteo.

## Integrantes

| Integrante | Cuenta GitHub |
|---|---|
| Kevin Valdés | [KevinValdess](https://github.com/KevinValdess) |
| Jonathan Aliaga | [JonaSlack](https://github.com/JonaSlack) |
| Victor Salas | [victor-salas89](https://github.com/victor-salas89) |

Los aportes de la etapa anterior están vinculados en [Colaboración y procedencia](DOCUMENTACION/COLABORACION.md), con commits originales de los tres integrantes.

## Estructura

| Carpeta | Contenido |
|---|---|
| [APP](APP/) | Aplicación Kotlin con Jetpack Compose, ViewModels, Retrofit, Room y pruebas unitarias. |
| [API](API/) | Servicio Java con Spring Boot, JPA, H2, Firebase Admin y pruebas. |

El README general está en la raíz de la rama `main`. La configuración del equipo, los datos de H2, las credenciales privadas y la firma del APK se mantienen fuera de Git.

## Estado del código

La app incluye el acceso mediante `POST /api/auth/login`, la recuperación del usuario con `GET /api/auth/me` y el cierre de sesión con `POST /api/auth/logout`. Obtiene el nombre y el rol desde el servidor; el token se guarda cifrado mediante Android Keystore.

**Pendiente de sincronización:** la carpeta `API/` conserva la versión publicada en el repositorio de origen. Los cambios de usuarios, sesiones, H2 Console y Swagger realizados directamente en el servidor todavía no están en GitHub. Por eso, compilar y ejecutar esta copia de la API permite probar sus recursos de visitas y catálogo, pero no proporciona el login que necesita la app actual.

Para usar la app con el acceso completo, mantener la API actual del servidor o incorporar primero esos archivos al repositorio. Conservar el JAR y el código del servidor hasta completar la sincronización. Los permisos de usuarios incorporados allí no significan que todos los endpoints de visitas y dispositivos estén protegidos.

## Evidencias y entrega

- [Pruebas, cobertura y alcance de la medición](DOCUMENTACION/PRUEBAS.md).
- [APK, firma y preparación del ZIP de entrega](DOCUMENTACION/ENTREGA.md).
- [Colaboración e historial original](DOCUMENTACION/COLABORACION.md).
- [Verificaciones e informes automáticos](https://github.com/KevinValdess/EA-3-TacticalTrack/actions).

Los informes, el APK final, su llave de evaluación y las capturas se incorporan a la entrega con los archivos reales del equipo. El tablero Trello todavía debe añadirse.

## Requisitos

- JDK 17.
- Maven para la API.
- Android Studio, SDK 35 y Build Tools 35.0.0 para la app.
- Un teléfono o emulador con Google Play services para las notificaciones.
- Conexión a Tailscale para acceder al servidor privado del equipo.

## Obtener el proyecto

```bash
git clone https://github.com/KevinValdess/EA-3-TacticalTrack.git
cd EA-3-TacticalTrack
```

## API y base de datos

Desde `API/`:

```bash
mvn test
mvn spring-boot:run
```

La API escucha por defecto en `http://localhost:8080`. Para comprobarla:

```bash
curl http://localhost:8080/api/points
```

H2 se ejecuta dentro del proceso de la API: no hay que iniciar otro servicio de base de datos. Por defecto conserva sus datos en `API/data/tacticaltrack.mv.db` cuando se arranca desde esa carpeta. Mantener ese archivo y la misma ruta de conexión para conservar los registros.

| Variable | Valor predeterminado | Uso |
|---|---|---|
| `PORT` | `8080` | Puerto de la API. |
| `DB_URL` | `jdbc:h2:file:./data/tacticaltrack` | Ruta de la base persistente. |
| `DB_USER` | `sa` | Usuario de H2. |
| `DB_PASSWORD` | Vacía | Contraseña de H2. |
| `PUSH_ENABLED` | `false` | Activa el envío de notificaciones. |
| `FIREBASE_PROJECT_ID` | Vacío | ID del proyecto de Firebase. |
| `GOOGLE_APPLICATION_CREDENTIALS` | Sin definir | Ruta del JSON privado de la cuenta de servicio. |
| `PUSH_POLL_MS` | `15000` | Intervalo de revisión de avisos, en milisegundos. |

La instancia desplegada del equipo utiliza el puerto local `18080` y la base `jdbc:h2:file:/var/lib/tacticaltrack/tacticaltrack`, definidos en su configuración del servidor.

### API externa de clima

La app usa Retrofit para consultar `GET https://api.open-meteo.com/v1/forecast`, con los parámetros `latitude`, `longitude`, `current=temperature_2m,weather_code` y `timezone=auto`. El resultado aparece en el detalle de la visita como temperatura, código meteorológico y hora del dato. Es clima actual de referencia, no una reconstrucción del clima histórico de la visita.

### Recursos incluidos en esta copia de la API

| Recurso | Ruta |
|---|---|
| Puntos de venta | `/api/points` |
| Equipos | `/api/assets` |
| Visitas | `/api/visits` |
| Dispositivos del supervisor | `/api/devices/{installationId}` |
| Estado del aviso | `/api/notifications/{visitId}` |

Los controladores de `API/src/main/java/cl/duoc/tacticaltrack/backend/` definen los métodos y cuerpos de las solicitudes. Los archivos de `API/src/test/` incluyen ejemplos de las operaciones.

## App Android

Abrir `APP/` como proyecto en Android Studio y seleccionar JDK 17 para Gradle.

La URL del backend se define al compilar y debe terminar en `/`. El valor predeterminado `http://10.0.2.2:8080/` apunta al computador desde el emulador; para el login requiere una API que incluya el módulo de autenticación.

Para usar el servidor del equipo desde PowerShell, dentro de `APP/`:

```powershell
.\gradlew.bat assembleDebug "-PbackendUrl=https://server.tailc8b8f3.ts.net:8443/"
```

En Linux o macOS:

```bash
chmod +x gradlew
./gradlew assembleDebug -PbackendUrl=https://server.tailc8b8f3.ts.net:8443/
```

El APK de depuración se genera en `APP/app/build/outputs/apk/debug/app-debug.apk`.

El teléfono debe estar conectado a la red Tailscale correspondiente. Si el nombre del servidor no resuelve, revisar que el cliente esté utilizando el DNS de Tailscale.

### Usuarios y visitas

Los usuarios se gestionan en la API del servidor. El rol determina las opciones que muestra la app, y el nombre recibido se utiliza en el saludo y en los nuevos registros. Cambiar un nombre no modifica el autor guardado en visitas anteriores.

La app permite registrar visitas con fotografía, ubicación y estado del equipo; editar y eliminar registros; y revisar su aprobación o rechazo. Room conserva la última copia de las visitas en el teléfono. Para iniciar o recuperar una sesión se necesita conexión con la API.

## Firebase y notificaciones

### Android

1. Registrar en Firebase una app con el identificador `cl.duoc.tacticaltrack`.
2. Descargar `google-services.json` y guardarlo en `APP/app/google-services.json`.
3. Usar el mismo proyecto de Firebase que el servidor y recompilar.

Se conserva el archivo de configuración Android que ya estaba publicado en el proyecto de origen. Ese archivo configura el cliente; no sustituye la credencial privada de Firebase Admin.

### Servidor

Guardar el JSON de la cuenta de servicio fuera del repositorio y configurar su ruta. Ejemplo en Linux:

```bash
export GOOGLE_APPLICATION_CREDENTIALS="/ruta/privada/firebase-admin.json"
export FIREBASE_PROJECT_ID="id-del-proyecto"
export PUSH_ENABLED=true
cd API
mvn spring-boot:run
```

En el servidor del equipo, estas variables se administran en `/etc/tacticaltrack/api.env` y la credencial se guarda en `/etc/tacticaltrack/firebase-admin.json`.

Para comprobar el envío, iniciar sesión como supervisor, activar los avisos en Inicio y aceptar el permiso de Android. Registrar una visita desde otro dispositivo y comprobar que llega la notificación. El identificador del supervisor se obtiene de la sesión.

La API guarda el aviso junto con la visita y procesa el envío por separado. Si falla la conexión con Firebase, reintenta el envío. El historial de la app permite revisar las visitas aunque un aviso no llegue.

## APK firmado

Para actualizar la app instalada, conservar la misma llave y alias usados anteriormente. Copiar al nuevo proyecto:

- `APP/signing/tacticaltrack-evaluacion.jks`
- `APP/keystore.properties`

El archivo `APP/keystore.properties.example` muestra el formato de configuración. Ajustar `storeFile` para que apunte a la llave desde `APP/`. Si se trabaja en el mismo computador, también puede conservarse `APP/local.properties` con la ruta del SDK.

Desde `APP/`, en PowerShell:

```powershell
.\gradlew.bat assembleRelease "-PbackendUrl=https://server.tailc8b8f3.ts.net:8443/"
```

El APK firmado se genera en `APP/app/build/outputs/apk/release/app-release.apk`. La firma y sus contraseñas no se suben al repositorio.

## Pruebas

API, desde `API/`:

```bash
mvn test
```

Android, desde `APP/`, en PowerShell:

```powershell
.\gradlew.bat testDebugUnitTest logicCoverageReport verifyLogicCoverage lintVitalRelease
```

En Linux o macOS, usar `./gradlew`. El informe de cobertura queda en `APP/app/build/reports/jacoco/logicCoverageReport/html/index.html`.

Consultar [las pruebas y su alcance](DOCUMENTACION/PRUEBAS.md) y descargar los informes generados por GitHub Actions. Las pruebas de autenticación Android comprueban el contrato HTTP con respuestas simuladas. Para validar la integración completa, probar contra la API desplegada el login, la recuperación de sesión, el cierre de sesión, las visitas y las notificaciones.

## Archivos locales

Conservar fuera de Git el JSON privado de Firebase Admin, `keystore.properties`, las llaves de firma, `local.properties`, los archivos de entorno y las bases de datos. Los directorios de compilación y las cachés también están excluidos.
