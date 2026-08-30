#!/usr/bin/env python3
"""Compare Android app screenshots (Maestro, живое приложение) with mockup frames and generate diff overlays.

Источники:
  - мокапы:  e2e-cmp/test-results/pixel-report/mockups-dark-phone/frame-*.png (360x800, рендер mockups.html (dark))
  - приложение: docs/qa/design-conformance/android-dark-2026-08-30/*.png (Maestro takeScreenshot, эмулятор Medium_Phone 1080x2400@420, uimode night)

Перед сравнением у Android-скриншота обрезаются системные бары (статус-бар/навбар) — см. CROP_TOP_PX/CROP_BOTTOM_PX.
Результат: diff-overlays + summary.json в .../android-dark-2026-08-30/diffs/.
"""
import json
from pathlib import Path
from PIL import Image, ImageChops, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent
REPO = ROOT.parent
MOCKUPS = ROOT / 'test-results' / 'pixel-report' / 'mockups-dark-phone'
APP = REPO / 'docs' / 'qa' / 'design-conformance' / 'android-dark-2026-08-30'
OUT = APP / 'diffs'
OUT.mkdir(parents=True, exist_ok=True)

# Системные бары эмулятора 1080x2400 @420dpi (3-button nav). Подобрано по скриншоту.
CROP_TOP_PX = 66      # статус-бар
CROP_BOTTOM_PX = 132  # навбар

# (mockup frame, app screenshot, note)
PAIRS = [
    ('frame-onboarding.png', 'onboarding-1.png', 'Onboarding 1'),
    ('frame-onboarding.png', 'onboarding-2.png', 'Onboarding 2'),
    ('frame-onboarding.png', 'onboarding-3.png', 'Onboarding 3'),
    ('frame-login.png', 'login.png', 'Login'),
    ('frame-register.png', 'register.png', 'Register'),
    ('frame-library.png', 'library-guest.png', 'Library guest'),
    ('frame-library.png', 'library-auth.png', 'Library auth'),
    ('frame-profile-guest.png', 'profile-guest.png', 'Profile guest'),
    ('frame-profile.png', 'profile-auth.png', 'Profile auth'),
    ('frame-topics.png', 'topics-guest.png', 'Topics guest'),
    ('frame-topics.png', 'topics-auth.png', 'Topics auth'),
    ('frame-video.png', 'video-auth.png', 'Video'),
    ('frame-locked.png', 'questions-guest-locked.png', 'Questions guest gate'),
    ('frame-questions.png', 'questions-auth.png', 'Questions auth'),
    ('frame-training.png', 'training-idle.png', 'Training idle'),
    ('frame-practice.png', 'practice-ready.png', 'Practice ready'),
    ('frame-submissions.png', 'submissions.png', 'MySubmissions'),
    ('frame-debug.png', 'debug.png', 'Debug menu'),
    # frame-grading — экран admin-web, в Android-аудит не входит.
]


def try_font(size):
    for name in ['arial.ttf', 'DejaVuSans.ttf', '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf']:
        try:
            return ImageFont.truetype(name, size)
        except Exception:
            continue
    return ImageFont.load_default()


font = try_font(12)
results = []

for mock_name, app_name, note in PAIRS:
    mock_path = MOCKUPS / mock_name
    app_path = APP / app_name
    if not mock_path.exists() or not app_path.exists():
        results.append({'screen': note, 'error': 'missing file', 'mockup': str(mock_path), 'app': str(app_path)})
        continue
    mock = Image.open(mock_path).convert('RGB')
    app = Image.open(app_path).convert('RGB')
    # обрезка системных баров
    app = app.crop((0, CROP_TOP_PX, app.width, app.height - CROP_BOTTOM_PX))
    # масштабируем приложение к ширине мокапа
    target_w, target_h = mock.size
    app_scaled = app.resize((target_w, int(target_w * app.height / app.width)), Image.Resampling.LANCZOS)
    if app_scaled.height < target_h:
        pad = Image.new('RGB', (target_w, target_h), (240, 240, 255))
        pad.paste(app_scaled, (0, 0))
        app_scaled = pad
    else:
        app_scaled = app_scaled.crop((0, 0, target_w, target_h))
    diff = ImageChops.difference(mock, app_scaled)
    bbox = diff.getbbox()
    if bbox:
        gray = diff.convert('L')
        pixels = list(gray.getdata())
        changed = sum(1 for p in pixels if p > 16)
        ratio = changed / len(pixels)
    else:
        ratio = 0.0
    total_w = target_w * 3 + 40
    total_h = target_h + 50
    canvas = Image.new('RGB', (total_w, total_h), (255, 255, 255))
    canvas.paste(mock, (10, 40))
    canvas.paste(app_scaled, (target_w + 20, 40))
    canvas.paste(diff, (target_w * 2 + 30, 40))
    draw = ImageDraw.Draw(canvas)
    draw.text((10, 14), f'mockup: {mock_name}', fill=(0, 0, 0), font=font)
    draw.text((target_w + 20, 14), f'app: {app_name}', fill=(0, 0, 0), font=font)
    draw.text((target_w * 2 + 30, 14), f'diff {ratio:.1%}', fill=(0, 0, 0), font=font)
    out_path = OUT / f'diff-{note.replace(" ", "_").replace("/", "_")}.png'
    canvas.save(out_path)
    results.append({'screen': note, 'mockup': mock_name, 'app': app_name, 'diff_ratio': round(ratio, 4), 'diff_image': str(out_path.relative_to(REPO))})

summary_path = OUT / 'summary.json'
summary_path.write_text(json.dumps(results, indent=2, ensure_ascii=False), encoding='utf-8')
print(f'Generated {len(results)} diffs in {OUT}')
for r in results:
    if 'diff_ratio' in r:
        print(f"  {r['screen']}: {r['diff_ratio']:.1%}")
    else:
        print(f"  {r['screen']}: {r['error']}")
