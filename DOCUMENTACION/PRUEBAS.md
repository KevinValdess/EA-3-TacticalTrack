# Pruebas y cobertura

## Resultado comprobado

La ejecución [45092692942](https://github.com/KevinValdess/EA-3-TacticalTrack/actions/runs/45092692942) aprobó 42 pruebas Android, sin fallos ni errores, y midió 94,18% de cobertura de líneas de la lógica incluida. Ver el [resumen conservado](RESULTADOS_PRUEBAS.md).

## Android

Desde `APP/`, con JDK 17 y SDK 35:

```powershell
.\gradlew.bat testDebugUnitTest logicCoverageReport verifyLogicCoverage lintVitalRelease
```

Desde la raíz del repositorio:

```powershell
python scripts/resumen_cobertura.py
```

El segundo comando transforma resultados reales de JaCoCo y JUnit en `evidencias-generadas/cobertura.md`. Falla si no existen informes, si las pruebas reportan errores o si la cobertura incluida no alcanza el 80%.

| Evidencia | Ruta |
|---|---|
| Informe HTML de cobertura | `APP/app/build/reports/jacoco/logicCoverageReport/html/index.html` |
| Informe XML | `APP/app/build/reports/jacoco/logicCoverageReport/logicCoverageReport.xml` |
| Resultados JUnit | `APP/app/build/test-results/testDebugUnitTest/` |
| Resumen para la entrega | `evidencias-generadas/cobertura.md` |

## Alcance de la medición

Se incluyen `VisitViewModel`, `LoginViewModel`, `WeatherViewModel`, los repositorios `AuthRepository`, `VisitRepository`, `WeatherRepository`, `CatalogRepository`, `DeviceRegistrationRepository` y las reglas del paquete `domain`.

Se excluyen la interfaz Compose, factorías, DTO y adaptadores de plataforma. Por ejemplo, la cifra no mide el funcionamiento físico de la cámara ni la entrega real de Firebase. `UserPreferencesRepository` es un adaptador de DataStore y `SessionViewModelStore` administra el ciclo de vida. Ambos quedan fuera de esta medición.

La cobertura mide líneas ejecutadas de la lógica incluida, no el porcentaje de todos los archivos de Android. Las exclusiones deben explicarse al docente con este alcance.

## Qué se prueba y qué se simula

| Prueba | Propósito | Dependencias simuladas |
|---|---|---|
| `LoginViewModelTest` | Validación, errores, sesión y prevención de doble envío | API y preferencias con MockK |
| `AuthRepositoryTest` | Token, roles, cuentas deshabilitadas, recuperación y cierre de sesión | API y almacén de tokens en memoria |
| `AuthHttpTest` | Rutas, JSON y encabezado Bearer | Servidor HTTP local MockWebServer |
| `ViewModelsTest` | Formulario, evidencia, operaciones y manejo de errores | Repositorios y ubicación |
| `VisitRepositoryTest` | Operaciones remotas y copia local | DAO, API y almacenamiento de evidencia |
| `WeatherTest` | Datos de clima y fallos de consulta | API y repositorio según el caso |
| `CatalogAndDeviceTest` | Consistencia del catálogo y registro de dispositivos | API |

Las pruebas del login no requieren contraseñas ni cuentas de producción. Las credenciales de esos tests son datos ficticios.

## API

Desde `API/`:

```bash
mvn verify
```

Los informes quedan en `API/target/surefire-reports/`. Las pruebas usan bases H2 de prueba y simulaciones del envío push, no la base del servidor del equipo.

## Verificación automática

[GitHub Actions](https://github.com/KevinValdess/EA-3-TacticalTrack/actions) ejecuta las pruebas de API, la verificación de cobertura Android, `assembleDebug` y `lintVitalRelease`. Sus artefactos conservan los informes.

El APK de ese flujo es **debug**. El APK release se compila y firma con la llave original del equipo y se comprueba por separado.

## Integración en el teléfono

Antes de cerrar la entrega, comprobar en el último APK firmado:

1. Ingreso con supervisor y operarios, nombre recibido de la API y cierre de sesión.
2. Registro, edición, eliminación, aprobación y rechazo de una visita.
3. Consulta del clima sin afectar las operaciones del backend.
4. Recepción del aviso en el teléfono del supervisor.
5. Conservación de los registros después de reiniciar el servicio.

Registrar la versión comprobada y el resultado de cada operación. Los resultados automáticos no sustituyen esta prueba con el backend real.
