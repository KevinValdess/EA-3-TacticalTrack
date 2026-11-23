"""Crea el ZIP de entrega con fuentes, APK y la llave original, verificando su firma."""
import argparse
import base64
import getpass
import hashlib
import os
from pathlib import Path
import re
import shutil
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]


def run(command, **kwargs):
    result = subprocess.run(command, capture_output=True, text=True, **kwargs)
    if result.returncode:
        raise ValueError('Falló la herramienta de verificación. Revisa las rutas, el alias y la contraseña.')
    return result.stdout + result.stderr


def locate_apksigner(explicit):
    if explicit:
        return str(Path(explicit).resolve())
    tool = shutil.which('apksigner') or shutil.which('apksigner.bat')
    if tool:
        return tool
    for name in ('ANDROID_HOME', 'ANDROID_SDK_ROOT'):
        sdk = os.environ.get(name)
        if sdk:
            for executable in ('apksigner', 'apksigner.bat'):
                path = Path(sdk) / 'build-tools' / '35.0.0' / executable
                if path.is_file():
                    return str(path)
    raise ValueError('Indica --apksigner con la ruta a build-tools/35.0.0/apksigner o apksigner.bat.')


def verify_certificate(apk, keystore, alias, apksigner, password):
    signer = locate_apksigner(apksigner)
    info = run([signer, 'verify', '--verbose', '--print-certs', str(apk)])
    match = re.search(r'Signer #1 certificate SHA-256 digest:\s*([0-9a-fA-F]+)', info)
    if not match:
        raise ValueError('No se encontró el certificado del APK firmado.')
    keytool = shutil.which('keytool')
    if not keytool and os.environ.get('JAVA_HOME'):
        keytool = str(Path(os.environ['JAVA_HOME']) / 'bin' / ('keytool.exe' if os.name == 'nt' else 'keytool'))
    if not keytool:
        raise ValueError('No se encontró keytool. Usa JDK 17 en JAVA_HOME o PATH.')
    env = os.environ.copy()
    env['TACTICALTRACK_DELIVERY_STORE_PASSWORD'] = password
    cert = run([keytool, '-exportcert', '-rfc', '-alias', alias, '-keystore', str(keystore),
                '-storepass:env', 'TACTICALTRACK_DELIVERY_STORE_PASSWORD'], env=env)
    block = re.search(r'-----BEGIN CERTIFICATE-----(.*?)-----END CERTIFICATE-----', cert, re.S)
    if not block:
        raise ValueError('No se pudo leer el certificado de la llave.')
    digest = hashlib.sha256(base64.b64decode(block.group(1))).hexdigest()
    if digest.lower() != match.group(1).lower():
        raise ValueError('La llave y el APK tienen certificados distintos. Usa el .jks con el que firmaste ese APK.')
    return info, digest


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apk', type=Path, default=ROOT / 'APP/app/build/outputs/apk/release/app-release.apk')
    parser.add_argument('--keystore', type=Path, default=ROOT / 'APP/signing/tacticaltrack-evaluacion.jks')
    parser.add_argument('--alias', default='tacticaltrack-evaluacion')
    parser.add_argument('--capturas', type=Path, default=ROOT / 'capturas-entrega')
    parser.add_argument('--apksigner')
    parser.add_argument('--out', type=Path, default=ROOT / 'entrega-local/TacticalTrack_entrega.zip')
    args = parser.parse_args()
    apk, key = args.apk.resolve(), args.keystore.resolve()
    if not apk.is_file() or not key.is_file():
        parser.error('Falta el APK release o la llave original. Copia los archivos y compila primero.')
    if args.out.exists():
        parser.error('El ZIP ya existe. Elige otro nombre con --out para conservar la entrega anterior.')
    password = getpass.getpass('Contraseña del almacén de claves (no se guardará en el ZIP): ')
    signature, cert_hash = verify_certificate(apk, key, args.alias, args.apksigner, password)
    tracked = run(['git', '-C', str(ROOT), 'ls-files', '-z']).rstrip('\n').split('\0')
    tracked = [p for p in tracked if p]
    if not tracked:
        raise ValueError('No se encontraron los archivos versionados del proyecto.')
    args.out.parent.mkdir(parents=True, exist_ok=True)
    manifest = ['# Entrega TacticalTrack', '', 'Integrantes: Kevin Valdés, Jonathan Aliaga y Victor Salas.', '',
                f'Certificado SHA-256: `{cert_hash}`.', '',
                f'APK SHA-256: `{hashlib.sha256(apk.read_bytes()).hexdigest()}`.', '',
                f'Alias de la llave: `{args.alias}`.', '',
                'El APK y la llave incluidos comparten certificado. La contraseña se entrega al docente por el canal acordado.',
                'Las capturas de la firma, el APK instalado y Trello deben acompañar la entrega.', '']
    with zipfile.ZipFile(args.out, 'w', compression=zipfile.ZIP_DEFLATED) as archive:
        for relative in tracked:
            path = ROOT / relative
            if path.is_file():
                archive.write(path, 'TacticalTrack/' + relative.replace('\\', '/'))
        if args.capturas.is_dir():
            for image in sorted(args.capturas.iterdir()):
                if image.is_file() and not image.is_symlink() and image.suffix.lower() in {'.png', '.jpg', '.jpeg', '.webp'}:
                    archive.write(image, 'TacticalTrack/ENTREGA/capturas/' + image.name)
        archive.write(apk, 'TacticalTrack/ENTREGA/app-release.apk')
        archive.write(key, 'TacticalTrack/ENTREGA/signing/tacticaltrack-evaluacion.jks')
        archive.writestr('TacticalTrack/ENTREGA/FIRMA_VERIFICADA.txt', signature)
        archive.writestr('TacticalTrack/ENTREGA/LEEME.md', '\n'.join(manifest))
    print(f'Entrega creada: {args.out.resolve()}')
    print('Firma verificada. El ZIP incluye el APK y su llave original para la entrega al docente.')


if __name__ == '__main__':
    try:
        main()
    except (ValueError, OSError) as error:
        raise SystemExit(str(error))
