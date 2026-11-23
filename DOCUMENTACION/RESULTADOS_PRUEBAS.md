# Pruebas y cobertura Android

Versión comprobada: `7ec13c61302feeab0918a1034b9b59a2053888d2`.

| Medición | Resultado |
|---|---:|
| Líneas cubiertas | 372 |
| Líneas sin cubrir | 23 |
| Cobertura de la lógica incluida | 94.18% |
| Mínimo exigido | 80% |
| Pruebas ejecutadas | 42 |
| Fallos | 0 |
| Errores | 0 |
| Omitidas | 0 |

| Paquete | Cubiertas | Sin cubrir | Cobertura |
|---|---:|---:|---:|
| cl.duoc.tacticaltrack.domain | 7 | 0 | 100.00% |
| cl.duoc.tacticaltrack.data.repository | 93 | 7 | 93.00% |
| cl.duoc.tacticaltrack.viewmodel | 272 | 16 | 94.44% |

**Alcance:** ViewModels de visitas, login y clima; repositorios de autenticación, visitas, catálogo, clima y dispositivos; reglas de dominio.

La medición excluye Compose, DTO, factorías y adaptadores de plataforma. No representa el porcentaje de todos los archivos de Android ni sustituye las pruebas de integración en el teléfono.

Resultado: APROBADO.

Ejecución verificada: https://github.com/KevinValdess/EA-3-TacticalTrack/actions/runs/45092692942

El resultado corresponde al commit indicado. Los cambios posteriores de documentación no modifican el código de la app medido.

## API publicada

La misma ejecución completó `mvn verify`: 24 pruebas, 0 fallos, 0 errores y 0 omitidas. Este resultado corresponde a la API publicada antes de incorporar los fuentes del módulo de usuarios del servidor.
