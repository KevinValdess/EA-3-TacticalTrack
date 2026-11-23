"""Reconstruye el APK de entrega sin cambiar sus bytes y comprueba SHA-256."""
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DIRECTORY = ROOT / 'ENTREGA' / 'apk-partes'
OUTPUT = ROOT / 'ENTREGA' / 'app-release.apk'


def reconstruct(directory=DIRECTORY, output=OUTPUT):
    manifest = json.loads((directory / 'manifest.json').read_text(encoding='utf-8'))
    payload = bytearray()
    for part in manifest['parts']:
        if Path(part['path']).name != part['path']:
            raise ValueError('Nombre de parte inválido.')
        content = (directory / part['path']).read_bytes()
        if len(content) != part['size'] or hashlib.sha256(content).hexdigest() != part['sha256']:
            raise ValueError('Parte incompleta o modificada: ' + part['path'])
        payload.extend(content)
    if len(payload) != manifest['size'] or hashlib.sha256(payload).hexdigest() != manifest['sha256']:
        raise ValueError('El APK reconstruido no coincide con el original.')
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_bytes(payload)
    print('APK original reconstruido: ' + str(output))
    print('SHA-256: ' + manifest['sha256'])
    return output


if __name__ == '__main__':
    reconstruct()
