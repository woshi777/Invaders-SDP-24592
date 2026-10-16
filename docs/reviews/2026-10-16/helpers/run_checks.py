#!/usr/bin/env python3
"""Run the existing records suites without editing the reviewed checkout."""
import argparse
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import time

p = argparse.ArgumentParser()
p.add_argument('--repo', type=Path, required=True)
p.add_argument('--expected-sha', required=True)
p.add_argument('--output', type=Path, required=True)
a = p.parse_args()
repo, output = a.repo.resolve(), a.output.resolve()
sha = subprocess.check_output(['git', '-C', str(repo), 'rev-parse', 'HEAD'], text=True).strip()
if sha != a.expected_sha:
    raise SystemExit('Checkout differs from the reviewed commit; select the intended PR commit first.')
output.mkdir(parents=True, exist_ok=True)
classes = output / 'classes'
classes.mkdir(exist_ok=True)
work = output / 'work'
(work / 'out').mkdir(parents=True, exist_ok=True)
java = shutil.which('java')
if not java:
    raise SystemExit('Java 21 JDK is required.')
classpath = str(classes) + os.pathsep + str(repo / 'res')
compiler = [java, '-m', 'jdk.compiler/com.sun.tools.javac.Main', '-encoding', 'UTF-8']
sources = sorted((repo / 'src').rglob('*.java'))
tests = [repo / ('tests/' + name) for name in (
    'records/RecordsSmokeTest.java', 'records/GameplayIntegrationTest.java',
    'screen/GameScreenWavesTest.java')]
metadata = {'sha': sha, 'source_count': len(sources), 'suites': []}

def run(command, log_name):
    started = time.monotonic()
    result = subprocess.run(command, cwd=work, capture_output=True, text=True, timeout=90)
    (output / log_name).write_text(result.stdout + result.stderr)
    return result, round(time.monotonic() - started, 3)

for files, args, log in ((sources, [], 'build.log'),
                         (tests, ['-cp', classpath], 'test-build.log')):
    result, _ = run(compiler + args + ['-d', str(classes)] + [str(f) for f in files], log)
    if result.returncode:
        print(result.stderr)
        raise SystemExit(result.returncode)

runtime_options = []
if os.name != 'nt' and '"java.exe"' in tests[0].read_text():
    # The reviewed test hard-codes java.exe. Keep its source unchanged and expose
    # a local compatible executable for its real shutdown/restart subprocess.
    properties = subprocess.run([java, '-XshowSettings:properties', '-version'],
                                capture_output=True, text=True, check=True).stderr
    actual_home = Path(re.search(r'^\s*java.home = (.+)$', properties, re.M)[1])
    compatibility_home = output / 'java-home-compat'
    compatibility_home.mkdir(exist_ok=True)
    for child in actual_home.iterdir():
        destination = compatibility_home / child.name
        if child.name != 'bin' and not destination.exists():
            destination.symlink_to(child, target_is_directory=child.is_dir())
    (compatibility_home / 'bin').mkdir(exist_ok=True)
    for name in ('java', 'java.exe'):
        destination = compatibility_home / 'bin' / name
        if not destination.exists():
            destination.symlink_to(Path(java).resolve())
    runtime_options.append('-Djava.home=' + str(compatibility_home))
    metadata['portability_workaround'] = 'Local java.exe alias for the unchanged shutdown probe'

for suite in ('records.RecordsSmokeTest', 'records.GameplayIntegrationTest',
              'screen.GameScreenWavesTest'):
    result, elapsed = run([java] + runtime_options + ['-Djava.awt.headless=true',
                            '-ea', '-cp', classpath, suite], suite + '.log')
    summary = [line for line in result.stdout.splitlines() if 'passed' in line.lower()]
    metadata['suites'].append({'suite': suite, 'exit_code': result.returncode,
                               'seconds': elapsed, 'summary': summary})
    print(suite, result.returncode, '; '.join(summary))

(output / 'results.json').write_text(json.dumps(metadata, indent=2) + '\n')
raise SystemExit(int(any(s['exit_code'] for s in metadata['suites'])))
