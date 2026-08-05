import glob
import sys

files = glob.glob('composeApp/build/wasm-dist/*.wasm')
print('files:', files, file=sys.stderr)
for f in files:
    data = open(f, 'rb').read().decode('latin-1', errors='ignore')
    print(f, 'Base URL' in data, '8080' in data, '8081' in data, 'localhost' in data, file=sys.stderr)
