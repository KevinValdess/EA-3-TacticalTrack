# Ejecutable Android de entrega

[Descargar el APK completo](https://github.com/KevinValdess/EA-3-TacticalTrack/releases/download/apk-7a2ca4c7/app-release.apk)

La descarga se publica después de verificar la firma Android. El APK conserva exactamente los bytes del archivo aportado por Kevin.

| Dato | Valor |
|---|---|
| Archivo | `app-release.apk` |
| Tamaño | 13.434.398 bytes |
| SHA-256 | `7a2ca4c7f5484f821ed6f2d4ac0bd150ce1012c530001182361fa3d9cc76fc53` |

## Reconstrucción desde los fuentes

El archivo está conservado en dos partes dentro de `apk-partes/` para permitir su transferencia. No son APK instalables por separado. Desde la raíz del repositorio:

```powershell
python scripts/reconstruir_apk.py
```

El resultado es `ENTREGA/app-release.apk`. El script comprueba las partes y el resultado completo mediante SHA-256. Para instalar directamente, usar el enlace de descarga del APK completo.

La verificación automática ejecuta `apksigner verify --verbose --print-certs` y comprueba el paquete `cl.duoc.tacticaltrack`. La llave original del equipo todavía debe incorporarse a la entrega del docente.
