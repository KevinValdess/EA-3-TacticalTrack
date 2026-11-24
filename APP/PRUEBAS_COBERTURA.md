# Pruebas y cobertura general de TacticalTrack

## Aplicar la actualización

Antes de reemplazar archivos, cierra las ejecuciones activas de pruebas. Conserva tu configuración local de Firebase, firma y SDK.

Extrae el ZIP y copia su carpeta APP sobre tu carpeta APP actual, aceptando reemplazar los archivos. Combina las carpetas; no borres previamente el proyecto. El paquete no incluye google-services.json, local.properties ni las claves de firma, por lo que debes conservar esos archivos de tu instalación.

La actualización contiene el código recibido, archivos de Gradle y pruebas. También incorpora un informe HTML de la ejecución verificada. No requiere actualizar la API del servidor.

## Ejecutar en Windows

En Android Studio selecciona JDK 17 para Gradle en Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK. La verificación automática utiliza esa versión.

Después, desde PowerShell en la carpeta APP:

```powershell
.\gradlew.bat clean testDebugUnitTest fullCoverageReport verifyFullCoverage
```

El primer proceso descarga las dependencias de pruebas. Robolectric ejecuta componentes Android y Compose dentro de la JVM; no necesita un emulador ni llamar a la API de producción.

El informe general se encuentra en:

```text
app/build/reports/jacoco/fullCoverageReport/html/index.html
```

Abre ese archivo desde el Explorador de Windows. Mantén toda la carpeta html para conservar sus enlaces y estilos.

## Interpretar el resultado

La comprobación verifyFullCoverage exige al menos 80 % de líneas ejecutables en el alcance general. El informe incluye pantallas, navegación, persistencia y adaptadores Android. También muestra métodos, instrucciones y ramas, que son métricas diferentes.

La instrumentación y los filtros internos de JaCoCo difieren de los del motor de cobertura de IntelliJ. Sus denominadores pueden ser distintos. No se debe comparar un porcentaje de líneas con uno de ramas ni presentar el antiguo informe filtrado como cobertura de toda la app.

La tarea logicCoverageReport permanece para comparar la lógica seleccionada anteriormente, pero no es el informe usado para aprobar el mínimo general.

Si una prueba falla, consulta app/build/reports/tests/testDebugUnitTest/index.html. No debe declararse aprobada una entrega solo porque su cobertura supera el umbral.
