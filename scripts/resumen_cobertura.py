"""Resume JaCoCo y JUnit sin modificar el repositorio ni los resultados."""
import argparse
from pathlib import Path
import xml.etree.ElementTree as ET


def summarize(xml_path, tests_dir, minimum=0.80):
    root = ET.parse(xml_path).getroot()
    counter = next((c for c in root.findall('counter') if c.get('type') == 'LINE'), None)
    if counter is None:
        raise ValueError('El informe no contiene el contador LINE.')
    covered, missed = int(counter.get('covered')), int(counter.get('missed'))
    total = covered + missed
    if total == 0:
        raise ValueError('El informe no contiene líneas ejecutables.')
    coverage = covered / total
    suites = list(Path(tests_dir).glob('TEST-*.xml'))
    if not suites:
        raise ValueError('No se encontraron resultados JUnit de Android.')
    totals = dict(tests=0, failures=0, errors=0, skipped=0)
    for path in suites:
        suite = ET.parse(path).getroot()
        for key in totals:
            totals[key] += int(suite.get(key, 0))
    rows = []
    for package in root.findall('package'):
        lines = next((c for c in package.findall('counter') if c.get('type') == 'LINE'), None)
        if lines is not None:
            n, m = int(lines.get('covered')), int(lines.get('missed'))
            rows.append((package.get('name').replace('/', '.'), n, m, n / (n + m) if n + m else 0))
    passed = coverage >= minimum and not totals['failures'] and not totals['errors']
    return covered, missed, coverage, totals, rows, passed


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--xml', default='APP/app/build/reports/jacoco/logicCoverageReport/logicCoverageReport.xml')
    parser.add_argument('--tests', default='APP/app/build/test-results/testDebugUnitTest')
    parser.add_argument('--out', default='evidencias-generadas/cobertura.md')
    parser.add_argument('--commit', default='Comprobación local')
    args = parser.parse_args()
    covered, missed, coverage, totals, rows, passed = summarize(args.xml, args.tests)
    lines = ['# Pruebas y cobertura Android', '', f'Versión comprobada: `{args.commit}`.', '',
             '| Medición | Resultado |', '|---|---:|',
             f'| Líneas cubiertas | {covered} |', f'| Líneas sin cubrir | {missed} |',
             f'| Cobertura de la lógica incluida | {coverage:.2%} |', '| Mínimo exigido | 80% |',
             f"| Pruebas ejecutadas | {totals['tests']} |", f"| Fallos | {totals['failures']} |",
             f"| Errores | {totals['errors']} |", f"| Omitidas | {totals['skipped']} |", '',
             '| Paquete | Cubiertas | Sin cubrir | Cobertura |', '|---|---:|---:|---:|']
    lines += [f'| {name} | {n} | {m} | {ratio:.2%} |' for name, n, m, ratio in rows]
    lines += ['', '**Alcance:** ViewModels de visitas, login y clima; repositorios de autenticación, visitas, catálogo, clima y dispositivos; reglas de dominio.',
              '', 'La medición excluye Compose, DTO, factorías y adaptadores de plataforma. No representa el porcentaje de todos los archivos de Android ni sustituye las pruebas de integración en el teléfono.',
              '', f"Resultado: {'APROBADO' if passed else 'NO APROBADO'}.", '']
    out = Path(args.out)
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text('\n'.join(lines), encoding='utf-8')
    print(out.read_text(encoding='utf-8'))
    return 0 if passed else 1


if __name__ == '__main__':
    raise SystemExit(main())
