# Preparación de la entrega

## Archivos requeridos

- Código de `APP/` y de la API compatible que funciona con el login actual.
- README general con integrantes, funcionalidades, endpoints y ejecución.
- Informes de pruebas y cobertura de al menos el 80% de la lógica medida.
- APK release firmado y su llave `.jks` de evaluación.
- Capturas del APK instalado, la generación o verificación de firma y la llave.
- Tablero Trello y evidencias de aportes técnicos del equipo.

## Firma original

Conservar en la máquina de compilación:

- `APP/signing/tacticaltrack-evaluacion.jks`.
- `APP/keystore.properties`, con la configuración real de firma.

Desde `APP/`:

```powershell
.\gradlew.bat assembleRelease "-PbackendUrl=https://server.tailc8b8f3.ts.net:8443/"
```

El resultado es `APP/app/build/outputs/apk/release/app-release.apk`.

Desde la raíz, con Python y el JDK disponibles:

```powershell
python scripts/preparar_entrega.py --apksigner "$env:LOCALAPPDATA\Android\Sdk\build-tools\35.0.0\apksigner.bat"
```

El script pide la contraseña de la llave sin mostrarla, verifica el certificado del APK y comprueba que coincide con el del `.jks`. Crea `entrega-local/TacticalTrack_entrega.zip`, con las fuentes versionadas, el APK, la llave y el resultado de verificación. La contraseña no se escribe en el ZIP. Para otro nombre de salida, usar `--out ruta.zip`.

Este ZIP se prepara para el docente con la llave de evaluación solicitada por la pauta. La credencial privada de Firebase Admin, los archivos `.env`, la base de datos y `keystore.properties` se mantienen fuera del repositorio y del ZIP.

Guardar las capturas reales en `capturas-entrega/` antes de ejecutar el script. Los PNG, JPEG y WebP de esa carpeta se incluyen en `ENTREGA/capturas/` dentro del ZIP para el docente. La carpeta queda fuera de Git. El script no confirma una instalación: adjuntar una captura del último APK probado.

## Copia del código de la API desplegada

Desde el servidor:

```bash
cd ~/tacticaltrack-servidor
zip -r ~/TacticalTrack_API_servidor.zip API/src API/pom.xml
```

La copia contiene los fuentes y las dependencias, sin `target`, datos de H2 ni credenciales privadas de Firebase. Es la versión que se debe comparar y sincronizar con `API/` antes de dar por cerrada la entrega.

## Cierre en AVA

La pauta exige entrega grupal en AVA y activación individual con nombre, equipo y aplicación. El docente debe poder acceder al repositorio público y a los archivos solicitados.

El plazo de entrega es el indicado por el docente. Revisar todo antes de su vencimiento: la pauta establece calificación 1,0 ante cambios posteriores al límite. No hay tareas automáticas que publiquen cambios en este repositorio.
