# Resultados verificados de pruebas Android

Commit comprobado: `bafe4b61b9aedffbd1e0637da92522d1c517ea5e`.
Ejecución: https://github.com/KevinValdess/EA-3-TacticalTrack/actions/runs/45390946636

| Medición | Resultado |
|---|---:|
| Pruebas | 73 |
| Fallos | 0 |
| Errores | 0 |
| Omitidas | 0 |
| Cobertura general de líneas | 90.08% |
| Líneas cubiertas | 1844 |
| Líneas sin cubrir | 203 |
| Mínimo general de líneas | 80 % |

La suite incluye pruebas unitarias y pruebas locales de componentes Android con Robolectric. No necesita una API de producción ni un emulador.

## Todas las métricas del informe

| Métrica | Cubiertas | Sin cubrir | Cobertura |
|---|---:|---:|---:|
| INSTRUCTION | 21669 | 3576 | 85.83% |
| BRANCH | 1167 | 783 | 59.85% |
| LINE | 1844 | 203 | 90.08% |
| COMPLEXITY | 854 | 766 | 52.72% |
| METHOD | 533 | 111 | 82.76% |
| CLASS | 229 | 11 | 95.42% |

## Cobertura de líneas por paquete

| Paquete | Cubiertas | Sin cubrir | Cobertura |
|---|---:|---:|---:|
| cl.duoc.tacticaltrack.navigation | 143 | 11 | 92.86% |
| cl.duoc.tacticaltrack.domain | 7 | 0 | 100.00% |
| cl.duoc.tacticaltrack.ui.components | 93 | 0 | 100.00% |
| cl.duoc.tacticaltrack.data.local | 256 | 77 | 76.88% |
| cl.duoc.tacticaltrack.viewmodel | 335 | 11 | 96.82% |
| cl.duoc.tacticaltrack.data.remote | 102 | 1 | 99.03% |
| cl.duoc.tacticaltrack.util | 33 | 13 | 71.74% |
| cl.duoc.tacticaltrack.ui.screens | 604 | 46 | 92.92% |
| cl.duoc.tacticaltrack.data.repository | 110 | 4 | 96.49% |
| cl.duoc.tacticaltrack | 16 | 24 | 40.00% |
| cl.duoc.tacticaltrack.ui.theme | 14 | 0 | 100.00% |
| cl.duoc.tacticaltrack.model | 27 | 0 | 100.00% |
| cl.duoc.tacticaltrack.push | 104 | 16 | 86.67% |

La comprobación mínima corresponde al total de líneas ejecutables del informe general, no a cada paquete por separado. Las ramas y los métodos tienen contadores distintos. No se excluyen pantallas ni adaptadores de este informe. Se incluyen también las implementaciones generadas de Room y BuildConfig. Los filtros internos de JaCoCo para código sintetizado por Kotlin pueden diferir del motor de IntelliJ; por ello sus porcentajes no se deben presentar como si fueran mediciones idénticas.

El código de producción recibido en APP.zip se conserva sin cambios. Se amplían las pruebas y se ajusta su configuración. La firma, Firebase y la configuración de SDK locales no se incluyen en este paquete.

## Importar en Android Studio

El porcentaje de esta ejecución no acredita el panel de un equipo distinto. Ejecuta desde APP `gradlew.bat clean :app:prepararCoberturaIDE --no-daemon` para comprobar las 73 pruebas y exportar los datos locales. Después importa `APP/cobertura-ide/TacticalTrack.exec` en Run → Manage Coverage Reports, desmarcando las suites anteriores. El resumen local está en `APP/cobertura-ide/RESULTADO_LOCAL.txt`.
