#!/usr/bin/env python3
"""Generate So to Speak launcher icon PNGs (microphone + colorful waveform).

Outputs:
  - design-assets/app-icon-512.png (1024x1024 master)
  - app/src/main/res/mipmap-*/ic_launcher.png
  - app/src/main/res/mipmap-*/ic_launcher_round.png
"""

from PIL import Image, ImageDraw
import os

MASTER_SIZE = 1024
BG = "#EEF3FF"
MIC = "#FF9F6B"
TEXT_DARK = "#2D3561"
BLUE = "#5B8DEF"
PURPLE = "#9B7EDE"
ORANGE = "#FF9F6B"


def draw_microphone(draw: ImageDraw.ImageDraw, cx: int, cy: int, scale: float):
    """Draw a friendly capsule microphone with grille lines."""
    head_w = int(220 * scale)
    head_h = int(240 * scale)
    corner = head_w // 2
    head_left = cx - head_w // 2
    head_top = cy - int(110 * scale)

    # Capsule body: rounded rect with fully rounded top and flat bottom
    draw.rounded_rectangle(
        [head_left, head_top, head_left + head_w, head_top + head_h],
        radius=corner,
        fill=MIC,
    )
    # Flat bottom cut so it sits on the stand nicely
    flat_y = head_top + head_h - corner
    draw.rectangle(
        [head_left, flat_y, head_left + head_w, head_top + head_h],
        fill=MIC,
    )

    # Grille lines
    line_h = int(20 * scale)
    line_w = int(150 * scale)
    for dy in [-50, -10, 30]:
        y = cy + int(dy * scale) - line_h // 2
        draw.rounded_rectangle(
            [cx - line_w // 2, y, cx + line_w // 2, y + line_h],
            radius=line_h // 2,
            fill=TEXT_DARK,
        )

    # U-shaped stand
    stand_w = int(270 * scale)
    stand_h = int(130 * scale)
    stand_top = cy + int(20 * scale)
    thickness = int(28 * scale)
    outer = [cx - stand_w // 2, stand_top, cx + stand_w // 2, stand_top + stand_h]
    inner = [
        cx - stand_w // 2 + thickness,
        stand_top,
        cx + stand_w // 2 - thickness,
        stand_top + stand_h,
    ]
    draw.pieslice(outer, start=0, end=180, fill=MIC)
    draw.pieslice(inner, start=0, end=180, fill=BG)

    # Stem
    stem_w = int(30 * scale)
    stem_h = int(70 * scale)
    draw.rectangle(
        [cx - stem_w // 2, stand_top + stand_h, cx + stem_w // 2, stand_top + stand_h + stem_h],
        fill=MIC,
    )

    # Base
    base_w = int(130 * scale)
    base_h = int(30 * scale)
    draw.rounded_rectangle(
        [cx - base_w // 2, stand_top + stand_h + stem_h, cx + base_w // 2, stand_top + stand_h + stem_h + base_h],
        radius=base_h // 2,
        fill=MIC,
    )


def draw_wave(draw: ImageDraw.ImageDraw, y_center: int, scale: float):
    """Draw a smooth colorful waveform approximated by a sine curve."""
    start_x = CENTER - int(270 * scale)
    end_x = CENTER + int(270 * scale)
    amplitude = int(36 * scale)
    thickness = int(28 * scale)
    step = 1

    # Split into three colored segments
    total = end_x - start_x
    seg_len = total // 3
    colors = [BLUE, PURPLE, ORANGE]

    freq = 0.028 / scale if scale > 0 else 0.028

    for seg, color in enumerate(colors):
        seg_start = start_x + seg * seg_len
        seg_end = seg_start + seg_len
        points = []
        for x in range(seg_start, seg_end + step, step):
            y = y_center + int(amplitude * __import__("math").sin((x - start_x) * freq))
            points.append((x, y))
        if len(points) > 1:
            draw.line(points, fill=color, width=thickness)


def render_icon(size: int) -> Image.Image:
    global CENTER
    CENTER = size // 2
    img = Image.new("RGBA", (size, size), BG)
    draw = ImageDraw.Draw(img)
    scale = size / MASTER_SIZE
    draw_microphone(draw, size // 2, int(size * 0.43), scale)
    draw_wave(draw, int(size * 0.80), scale)
    return img


def main():
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

    master = render_icon(MASTER_SIZE)
    master_path = os.path.join(root, "design-assets", "app-icon-512.png")
    master.save(master_path)
    print(f"Saved {master_path}")

    mipmap_sizes = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }
    for folder, size in mipmap_sizes.items():
        for suffix in ["", "_round"]:
            path = os.path.join(root, "app", "src", "main", "res", folder, f"ic_launcher{suffix}.png")
            img = render_icon(size)
            img.save(path)
            print(f"Saved {path}")


if __name__ == "__main__":
    main()
