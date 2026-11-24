# Pruebas y cobertura de la app entregada

## Informe de Android Studio incluido en el ZIP

Fuente: `APP/htmlReport/index.html` y sus páginas de detalle.

| Métrica | Cubiertas | Total | Porcentaje del informe |
|---|---:|---:|---:|
| Clases | 180 | 193 | 93,3 % |
| Métodos | 316 | 376 | 84 % |
| Líneas | 1524 | 1843 | 82,7 % |
| Ramas | 978 | 2643 | 37 % |

La cobertura general de líneas del informe adjunto supera el mínimo del 80 %. La cobertura de ramas es otra métrica y no alcanza ese porcentaje.

## Resultados JUnit adjuntos

Los archivos `APP/VERIFICACION_PRUEBAS/TEST-*.xml` contienen 73 pruebas, 0 fallos, 0 errores y 0 omitidas. Se conservan como fueron recibidos. Sus resultados y el HTML son evidencias adjuntas; no se afirma que se hayan generado en la misma ejecución ni que esta revisión haya ejecutado las pruebas en el computador del usuario.

## Reproducir la comprobación general

Desde APP, con JDK 17:

```powershell
.\gradlew.bat clean testDebugUnitTest fullCoverageReport verifyFullCoverage
```

El reporte JaCoCo queda en `app/build/reports/jacoco/fullCoverageReport/html/index.html`. Su denominador y sus filtros internos de código generado pueden diferir de los del motor de Android Studio. El resultado de esta ejecución nueva debe leerse directamente de su reporte, sin reemplazarlo por el porcentaje del HTML adjunto.

Las configuraciones antiguas que invocaban `prepararCoberturaIDE` se retiran porque esa tarea no forma parte de la versión recibida.
