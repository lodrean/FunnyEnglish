#!/usr/bin/env python3
"""
Generate side-by-side + diff images for design-conformance audit.
Compares Android app screenshots (1080x2400) with mockup frames (360x800).
Outputs scaled overlays and a per-frame diff summary to REPORT_PIXEL_POLISH.md.
"""
import json
import os
from pathlib import Path
from PIL import Image, ImageChops, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent
MOCKUPS = ROOT / ".." / ".." / ".." / "e2e-cmp" / "test-results" / "pixel-report" / "mockups-light-phone"
APP = ROOT
OUT = ROOT / "diffs"
OUT.mkdir(exist_ok=True)

PAIRS = [
    # (mockup, app_screenshot, notes)
    ("frame-library.png", "android-library-auth.png", "Library (auth)"),
    ("frame-topics.png", "android-topics.png", "Topics list"),
    ("frame-video.png", "android-video.png", "Video player"),
    ("frame-questions.png", "android-questions-guest-locked.png", "Questions + guest CTA"),
    ("frame-training.png", "android-training.png", "Training idle"),
    ("frame-practice.png", "android-practice-ready-auth.png", "Practice idle"),
    ("frame-locked.png", "android-questions-guest-locked.png", "Guest lock gate"),
    ("frame-login.png", "android-login-before.png", "Login screen"),
    ("frame-profile.png", "android-profile-auth.png", "Profile auth"),
    ("frame-profile-guest.png", "android-profile-guest.png", "Profile guest"),
    ("frame-submissions.png", "android-mysubmissions.png", "My submissions"),
]

results = []

def try_font(size):
    for name in ["arial.ttf", "DejaVuSans.ttf", "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"]:
        try:
            return ImageFont.truetype(name, size)
        except Exception:
            continue
    return ImageFont.load_default()

font = try_font(14)
header_font = try_font(18)

for mock_name, app_name, note in PAIRS:
    mock_path = MOCKUPS / mock_name
    app_path = APP / app_name
    if not mock_path.exists() or not app_path.exists():
        results.append({"screen": note, "mockup": str(mock_path), "app": str(app_path), "error": "missing file"})
        continue

    mock = Image.open(mock_path).convert("RGB")
    app = Image.open(app_path).convert("RGB")

    # Scale app to mockup width, keeping aspect ratio
    target_w, target_h = mock.size
    app_scaled = app.resize((target_w, int(target_w * app.height / app.width)), Image.Resampling.LANCZOS)
    # Crop or pad app_scaled to mockup height for diff
    if app_scaled.height < target_h:
        pad = Image.new("RGB", (target_w, target_h), (240, 240, 255))
        pad.paste(app_scaled, (0, 0))
        app_scaled = pad
    else:
        app_scaled = app_scaled.crop((0, 0, target_w, target_h))

    diff = ImageChops.difference(mock, app_scaled)
    # threshold difference
    bbox = diff.getbbox()
    if bbox:
        # compute rough diff pixel count
        diff_gray = diff.convert("L")
        pixels = list(diff_gray.getdata())
        changed = sum(1 for p in pixels if p > 16)
        total = len(pixels)
        ratio = changed / total
    else:
        ratio = 0.0

    # Build triptych
    total_w = target_w * 3 + 40
    total_h = target_h + 60
    canvas = Image.new("RGB", (total_w, total_h), (255, 255, 255))
    canvas.paste(mock, (10, 50))
    canvas.paste(app_scaled, (target_w + 20, 50))
    canvas.paste(diff, (target_w * 2 + 30, 50))
    draw = ImageDraw.Draw(canvas)
    draw.text((10, 18), f"mockup: {mock_name}", fill=(0, 0, 0), font=font)
    draw.text((target_w + 20, 18), f"app: {app_name}", fill=(0, 0, 0), font=font)
    draw.text((target_w * 2 + 30, 18), f"diff ({ratio:.1%})", fill=(0, 0, 0), font=font)
    out_path = OUT / f"diff-{note.replace(' ', '_').replace('+', 'plus')}.png"
    canvas.save(out_path)

    results.append({
        "screen": note,
        "mockup": mock_name,
        "app": app_name,
        "diff_image": str(out_path.relative_to(ROOT)),
        "diff_ratio": round(ratio, 4),
        "mockup_size": mock.size,
        "app_size": app.size,
    })

summary_path = OUT / "summary.json"
summary_path.write_text(json.dumps(results, indent=2, ensure_ascii=False), encoding="utf-8")
print(f"Generated {len(results)} diff images in {OUT}")
print(f"Summary: {summary_path}")
