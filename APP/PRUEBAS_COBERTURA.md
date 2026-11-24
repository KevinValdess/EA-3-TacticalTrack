# Comprobar y mostrar la cobertura en Android Studio

El HTML anterior y el panel de cobertura de Android Studio no son la misma ejecución. Un porcentaje obtenido en GitHub no demuestra por sí solo el porcentaje visible en tu equipo. La captura con 28 % no cumple el objetivo.

## Aplicar esta versión

1. Combina la carpeta APP de este ZIP con tu APP actual. Conserva google-services.json, local.properties y la carpeta de firma.
2. Abre APP en Android Studio y ejecuta Sync Project with Gradle Files.
3. Selecciona JDK 17 en Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK.

## Ejecutar todas las pruebas

En el selector de ejecución de Android Studio elige:

TacticalTrack - Todas las pruebas y cobertura general

Pulsa el triángulo verde Run normal. Esta configuración ejecuta clean y la comprobación completa mediante Gradle. Exporta los datos de esa misma ejecución; no depende de la configuración temporal anterior de pruebas.

Alternativa desde PowerShell en APP:

```powershell
.\gradlew.bat clean :app:prepararCoberturaIDE --no-daemon
```

También puedes ejecutar VERIFICAR_COBERTURA.bat.

La tarea falla si se ejecutan menos de 73 pruebas, si hay fallos, errores u omitidas, o si la cobertura general de líneas es inferior al 80 %. El resumen local queda en cobertura-ide/RESULTADO_LOCAL.txt. Su contador corresponde a tu ejecución, no al informe adjunto del servidor.

## Mostrar esos resultados en el panel Coverage

1. Abre Run → Manage Coverage Reports. Según la versión puede llamarse Show Coverage Data; puedes buscar la acción con Ctrl+Shift+A.
2. Desmarca las suites anteriores para no confundir ejecuciones.
3. Pulsa el botón para importar y selecciona APP/cobertura-ide/TacticalTrack.exec generado por el paso anterior.
4. Marca esa suite y pulsa Show Selected.

En versiones que admitan XML también puedes importar APP/cobertura-ide/TacticalTrack.xml.

La importación es la función oficial del IDE para mostrar datos externos en su panel. Los archivos se copian directamente de JaCoCo; no se editan sus contadores ni se eliminan paquetes de la app. El informe general incluye pantallas, navegación, persistencia y adaptadores. Las métricas LINE y BRANCH son diferentes.

Si el resumen local dice 73 pruebas y más de 80 % de líneas pero el panel sigue en 28 %, el panel no está representando esos mismos datos correctamente. En ese caso conserva RESULTADO_LOCAL.txt y la ventana de suites seleccionadas para diagnosticar la importación; no se debe afirmar que el panel está corregido hasta comprobarlo.

## Documentación

https://www.jetbrains.com/help/idea/code-coverage.html
