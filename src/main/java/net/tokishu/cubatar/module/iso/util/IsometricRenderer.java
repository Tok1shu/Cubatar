package net.tokishu.cubatar.module.iso.util;

import lombok.experimental.UtilityClass;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Псевдо-3D рендер модели игрока: набор коробок в воксельных координатах
 * (Y вверх, персонаж смотрит в +Z, камера со стороны +Z) поворачивается
 * yaw/pitch и проецируется ортографией. Каждая видимая грань - параллелограмм,
 * на который текстура из стандартной UV-раскладки скина натягивается одним
 * {@link AffineTransform} (плоский квадрат при любом повороте + ортографии
 * остаётся параллелограммом); порядок отрисовки - painter's algorithm по
 * глубине центра грани.
 * <p>
 * Вторые слои (шлем/куртка/рукава/штаны) - те же коробки, равномерно
 * раздутые от центра как в игре (шлем на 0.5 вокселя, остальные на 0.25):
 * у раздутой коробки все рёбра сходятся сами, без швов. Рисуются они
 * двусторонними - сквозь прозрачные пиксели и за силуэтом базы видна
 * изнанка дальних граней.
 * <p>
 * Поза: у конечности есть шарнир (pivot) - горизонтальная ось, вокруг
 * которой коробка качается до глобальных поворотов, как rotationPoint у
 * моделей в игре.
 */
@UtilityClass
public class IsometricRenderer {

    /** Какую часть модели рендерить. */
    public enum Part {HEAD, BODY, FULL}

    /** Раздутие коробки второго слоя в вокселях с каждой стороны - как в игре. */
    private static final double HAT_INFLATE = 0.5;
    private static final double LAYER_INFLATE = 0.25;

    /** Шарниры конечностей - те же, что rotationPoint у моделей в игре. */
    private static final double ARM_PIVOT_Y = -2;
    private static final double LEG_PIVOT_Y = -12;

    /** Амплитуда маха рук/ног в позе ходьбы. */
    private static final double WALK_ARM_SWING = Math.toRadians(27);
    private static final double WALK_LEG_SWING = Math.toRadians(30);

    // ── Публичное API ────────────────────────────────────────────────────

    /**
     * @param size    размер головы (8 вокселей) в пикселях
     * @param walking поза ходьбы: правая рука и левая нога вперёд, как в игре
     * @param slim    модель из профиля Mojang; null - неизвестна, определяем
     *                эвристикой по прозрачности
     */
    public static BufferedImage render(BufferedImage rawSkin, int size, double yawDeg, double pitchDeg,
                                       Part part, boolean walking, Boolean slim) {
        if (rawSkin == null) return null;

        BufferedImage skin = normalizeSkin(rawSkin);
        boolean isLegacy = rawSkin.getHeight() == 32;
        boolean isAlex = !isLegacy && (slim != null ? slim : detectAlex(skin));
        float scale = skin.getWidth() / 64f;
        double ppv = size / 8.0; // пикселей на воксель

        double yaw = Math.toRadians(yawDeg);
        double pitch = Math.toRadians(pitchDeg);

        List<Box> boxes = buildBoxes(part, walking, isAlex, isLegacy, skin, scale);
        Canvas canvas = boundingCanvas(boxes, yaw, pitch, ppv);

        List<Quad> quads = new ArrayList<>();
        for (Box b : boxes) {
            for (FaceSpec f : FACE_SPECS) {
                double nz = f.normal().rotateX(b.swing()).rotateY(yaw).rotateX(pitch).z();
                if (Math.abs(nz) < 1e-6) continue;        // ребром к камере - вырожденная матрица
                if (!b.doubleSided() && nz < 0) continue; // непрозрачная база: изнанку не рисуем
                addFaceQuad(quads, skin, scale, b, f, yaw, pitch, ppv, canvas.ox(), canvas.oy());
            }
        }
        quads.sort(Comparator.comparingDouble(Quad::depth));

        BufferedImage result = new BufferedImage(canvas.w(), canvas.h(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        for (Quad q : quads) {
            g.drawImage(q.image(), q.transform(), null);
        }
        g.dispose();
        return result;
    }

    // ── Модель: коробки и поза ───────────────────────────────────────────

    /**
     * Собирает коробки запрошенной части модели. Голова сидит на торсе
     * (y 0..8), торс y -12..0, ноги y -24..-12; руки по бокам торса, у
     * slim-модели они на воксель уже. Для legacy-скинов (64x32) левые
     * конечности - зеркальные копии правых, вторых слоёв кроме шлема нет
     * (и его тоже: у legacy другая раскладка хвоста текстуры).
     */
    private static List<Box> buildBoxes(Part part, boolean walking, boolean isAlex, boolean isLegacy,
                                        BufferedImage skin, float scale) {
        int armW = isAlex ? 3 : 4;
        double armCx = 4 + armW / 2.0;
        // Мах конечностей: правая рука и левая нога вперёд (+Z), их пары - назад
        double armSwing = walking ? -WALK_ARM_SWING : 0;
        double legSwing = walking ? WALK_LEG_SWING : 0;

        List<Box> boxes = new ArrayList<>();
        boxes.add(new Box(new Vec3(0, 4, 0), 8, 8, 8, 0, 0, 0, false, false));                 // голова
        if (part != Part.HEAD) {
            boxes.add(new Box(new Vec3(0, -6, 0), 8, 12, 4, 16, 16, 0, false, false));         // торс
            boxes.add(new Box(new Vec3(-armCx, -6, 0), armW, 12, 4, 40, 16, 0, false, false, ARM_PIVOT_Y, armSwing)); // правая рука
            boxes.add(isLegacy
                    ? new Box(new Vec3(armCx, -6, 0), armW, 12, 4, 40, 16, 0, true, false, ARM_PIVOT_Y, -armSwing)    // левая = зеркало правой
                    : new Box(new Vec3(armCx, -6, 0), armW, 12, 4, 32, 48, 0, false, false, ARM_PIVOT_Y, -armSwing));
        }
        if (part == Part.FULL) {
            boxes.add(new Box(new Vec3(-2, -18, 0), 4, 12, 4, 0, 16, 0, false, false, LEG_PIVOT_Y, legSwing));        // правая нога
            boxes.add(isLegacy
                    ? new Box(new Vec3(2, -18, 0), 4, 12, 4, 0, 16, 0, true, false, LEG_PIVOT_Y, -legSwing)
                    : new Box(new Vec3(2, -18, 0), 4, 12, 4, 16, 48, 0, false, false, LEG_PIVOT_Y, -legSwing));
        }

        if (!isLegacy) {
            addOverlayIfPresent(boxes, skin, scale, new Box(new Vec3(0, 4, 0), 8, 8, 8, 32, 0, HAT_INFLATE, false, true));
            if (part != Part.HEAD) {
                addOverlayIfPresent(boxes, skin, scale, new Box(new Vec3(0, -6, 0), 8, 12, 4, 16, 32, LAYER_INFLATE, false, true));
                addOverlayIfPresent(boxes, skin, scale, new Box(new Vec3(-armCx, -6, 0), armW, 12, 4, 40, 32, LAYER_INFLATE, false, true, ARM_PIVOT_Y, armSwing));
                addOverlayIfPresent(boxes, skin, scale, new Box(new Vec3(armCx, -6, 0), armW, 12, 4, 48, 48, LAYER_INFLATE, false, true, ARM_PIVOT_Y, -armSwing));
            }
            if (part == Part.FULL) {
                addOverlayIfPresent(boxes, skin, scale, new Box(new Vec3(-2, -18, 0), 4, 12, 4, 0, 32, LAYER_INFLATE, false, true, LEG_PIVOT_Y, legSwing));
                addOverlayIfPresent(boxes, skin, scale, new Box(new Vec3(2, -18, 0), 4, 12, 4, 0, 48, LAYER_INFLATE, false, true, LEG_PIVOT_Y, -legSwing));
            }
        }
        return boxes;
    }

    /** Добавляет коробку второго слоя, только если в её UV-области есть хоть один непрозрачный пиксель. */
    private static void addOverlayIfPresent(List<Box> boxes, BufferedImage skin, float scale, Box overlay) {
        int x0 = Math.round(overlay.uvX() * scale);
        int y0 = Math.round(overlay.uvY() * scale);
        int w = Math.round(2 * (overlay.w() + overlay.d()) * scale);
        int h = Math.round((overlay.d() + overlay.h()) * scale);
        int x1 = Math.min(x0 + w, skin.getWidth());
        int y1 = Math.min(y0 + h, skin.getHeight());
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                if (((skin.getRGB(x, y) >> 24) & 0xFF) > 0) {
                    boxes.add(overlay);
                    return;
                }
            }
        }
    }

    // ── Геометрия ────────────────────────────────────────────────────────

    private record Vec3(double x, double y, double z) {
        Vec3 add(Vec3 o) {
            return new Vec3(x + o.x, y + o.y, z + o.z);
        }

        Vec3 scale(double s) {
            return new Vec3(x * s, y * s, z * s);
        }

        Vec3 rotateY(double rad) {
            double c = Math.cos(rad), s = Math.sin(rad);
            return new Vec3(x * c + z * s, y, -x * s + z * c);
        }

        Vec3 rotateX(double rad) {
            double c = Math.cos(rad), s = Math.sin(rad);
            return new Vec3(x, y * c - z * s, y * s + z * c);
        }
    }

    private enum Side {FRONT, BACK, LEFT, RIGHT, TOP, BOTTOM}

    private record FaceSpec(Side side, Vec3 normal, Vec3 uAxis, Vec3 vAxis, float shade) {}

    private static final FaceSpec[] FACE_SPECS = {
            new FaceSpec(Side.FRONT,  new Vec3(0, 0, 1),  new Vec3(1, 0, 0),  new Vec3(0, -1, 0), 0.85f),
            new FaceSpec(Side.BACK,   new Vec3(0, 0, -1), new Vec3(-1, 0, 0), new Vec3(0, -1, 0), 0.85f),
            new FaceSpec(Side.RIGHT,  new Vec3(1, 0, 0),  new Vec3(0, 0, -1), new Vec3(0, -1, 0), 0.7f),
            new FaceSpec(Side.LEFT,   new Vec3(-1, 0, 0), new Vec3(0, 0, 1),  new Vec3(0, -1, 0), 0.7f),
            new FaceSpec(Side.TOP,    new Vec3(0, 1, 0),  new Vec3(1, 0, 0),  new Vec3(0, 0, 1),  1f),
            new FaceSpec(Side.BOTTOM, new Vec3(0, -1, 0), new Vec3(1, 0, 0),  new Vec3(0, 0, 1),  0.55f),
    };

    /**
     * Коробка модели. w/h/d - размеры БАЗОВОЙ коробки в вокселях (UV-раскладка
     * всегда считается от них), inflate добавляется к геометрии с каждой
     * стороны при рендере. mirrored - для legacy-скинов: конечность рисуется
     * зеркальной копией правой. doubleSided - рисовать и отвёрнутые от камеры
     * грани (вторые слои). swing/pivotY - поза, см. javadoc класса.
     */
    private record Box(Vec3 center, int w, int h, int d, int uvX, int uvY,
                       double inflate, boolean mirrored, boolean doubleSided,
                       double pivotY, double swing) {

        Box(Vec3 center, int w, int h, int d, int uvX, int uvY,
            double inflate, boolean mirrored, boolean doubleSided) {
            this(center, w, h, d, uvX, uvY, inflate, mirrored, doubleSided, 0, 0);
        }

        /** Применяет позу: качает точку вокруг шарнира {y=pivotY, z=0}. */
        Vec3 posed(Vec3 local) {
            if (swing == 0) return local;
            Vec3 rel = new Vec3(local.x(), local.y() - pivotY, local.z()).rotateX(swing);
            return new Vec3(rel.x(), rel.y() + pivotY, rel.z());
        }
    }

    /** Тангенциальный полуразмер грани вдоль её uAxis, с учётом раздутия. */
    private static double halfU(Box b, Side s) {
        return (switch (s) {
            case FRONT, BACK, TOP, BOTTOM -> b.w();
            case LEFT, RIGHT -> b.d();
        }) / 2.0 + b.inflate();
    }

    /** Тангенциальный полуразмер грани вдоль её vAxis, с учётом раздутия. */
    private static double halfV(Box b, Side s) {
        return (switch (s) {
            case FRONT, BACK, LEFT, RIGHT -> b.h();
            case TOP, BOTTOM -> b.d();
        }) / 2.0 + b.inflate();
    }

    /** Полуразмер коробки вдоль нормали грани, с учётом раздутия. */
    private static double halfN(Box b, Side s) {
        return (switch (s) {
            case FRONT, BACK -> b.d();
            case LEFT, RIGHT -> b.w();
            case TOP, BOTTOM -> b.h();
        }) / 2.0 + b.inflate();
    }

    // ── Проекция и отрисовка ─────────────────────────────────────────────

    private record Canvas(int w, int h, double ox, double oy) {}

    /**
     * Границы холста по всем углам всех коробок (с учётом позы); модель
     * несимметрична по Y, поэтому считаются честные min/max, а не max|.|
     */
    private static Canvas boundingCanvas(List<Box> boxes, double yaw, double pitch, double ppv) {
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (Box b : boxes) {
            double hw = b.w() / 2.0 + b.inflate();
            double hh = b.h() / 2.0 + b.inflate();
            double hd = b.d() / 2.0 + b.inflate();
            for (double sx = -1; sx <= 1; sx += 2) {
                for (double sy = -1; sy <= 1; sy += 2) {
                    for (double sz = -1; sz <= 1; sz += 2) {
                        Vec3 c = b.posed(b.center().add(new Vec3(sx * hw, sy * hh, sz * hd))).rotateY(yaw).rotateX(pitch);
                        minX = Math.min(minX, c.x() * ppv);
                        maxX = Math.max(maxX, c.x() * ppv);
                        minY = Math.min(minY, -c.y() * ppv);
                        maxY = Math.max(maxY, -c.y() * ppv);
                    }
                }
            }
        }
        return new Canvas((int) Math.ceil(maxX - minX) + 2, (int) Math.ceil(maxY - minY) + 2,
                1 - minX, 1 - minY);
    }

    /** Текстурированная грань, готовая к отрисовке; depth - для painter's-сортировки. */
    private record Quad(BufferedImage image, AffineTransform transform, double depth) {}

    private static void addFaceQuad(List<Quad> out, BufferedImage skin, float scale, Box b, FaceSpec f,
                                    double yaw, double pitch, double ppv, double ox, double oy) {
        BufferedImage tex = faceTexture(skin, scale, b, f);
        if (tex == null) return;

        Vec3 p00 = projectCorner(b, f, -1, -1, yaw, pitch, ppv, ox, oy);
        Vec3 p10 = projectCorner(b, f, +1, -1, yaw, pitch, ppv, ox, oy);
        Vec3 p01 = projectCorner(b, f, -1, +1, yaw, pitch, ppv, ox, oy);
        Vec3 p11 = projectCorner(b, f, +1, +1, yaw, pitch, ppv, ox, oy);

        AffineTransform t = toTransform(p00, p10, p01, tex.getWidth(), tex.getHeight());
        double depth = (p00.z() + p10.z() + p01.z() + p11.z()) / 4;
        out.add(new Quad(tex, t, depth));
    }

    private static Vec3 projectCorner(Box b, FaceSpec f, double au, double bv,
                                      double yaw, double pitch, double ppv, double ox, double oy) {
        Vec3 local = b.center()
                .add(f.normal().scale(halfN(b, f.side())))
                .add(f.uAxis().scale(au * halfU(b, f.side())))
                .add(f.vAxis().scale(bv * halfV(b, f.side())));
        Vec3 r = b.posed(local).rotateY(yaw).rotateX(pitch);
        return new Vec3(r.x() * ppv + ox, -r.y() * ppv + oy, r.z());
    }

    private static AffineTransform toTransform(Vec3 d00, Vec3 d10, Vec3 d01, int srcW, int srcH) {
        double m00 = (d10.x() - d00.x()) / srcW;
        double m10 = (d10.y() - d00.y()) / srcW;
        double m01 = (d01.x() - d00.x()) / srcH;
        double m11 = (d01.y() - d00.y()) / srcH;
        return new AffineTransform(m00, m10, m01, m11, d00.x(), d00.y());
    }

    // ── Текстуры ─────────────────────────────────────────────────────────

    private record UvRect(int x, int y, int w, int h) {}

    /**
     * UV-прямоугольник грани по стандартной раскладке коробки скина: слева
     * направо LEFT(d), FRONT(w), RIGHT(d), BACK(w); над FRONT - TOP, над
     * RIGHT - BOTTOM. Для зеркальной коробки LEFT и RIGHT меняются местами
     * (сама картинка дополнительно отражается в {@link #faceTexture}).
     */
    private static UvRect faceUv(Box b, Side side) {
        Side src = b.mirrored() ? switch (side) {
            case LEFT -> Side.RIGHT;
            case RIGHT -> Side.LEFT;
            default -> side;
        } : side;
        int u = b.uvX(), v = b.uvY(), w = b.w(), h = b.h(), d = b.d();
        return switch (src) {
            case FRONT  -> new UvRect(u + d, v + d, w, h);
            case BACK   -> new UvRect(u + d + w + d, v + d, w, h);
            case RIGHT  -> new UvRect(u + d + w, v + d, d, h);
            case LEFT   -> new UvRect(u, v + d, d, h);
            case TOP    -> new UvRect(u + d, v, w, d);
            case BOTTOM -> new UvRect(u + d + w, v, w, d);
        };
    }

    private static BufferedImage faceTexture(BufferedImage skin, float scale, Box b, FaceSpec f) {
        UvRect uv = faceUv(b, f.side());
        int sx = Math.round(uv.x() * scale);
        int sy = Math.round(uv.y() * scale);
        int sw = Math.round(uv.w() * scale);
        int sh = Math.round(uv.h() * scale);
        if (sx + sw > skin.getWidth() || sy + sh > skin.getHeight()) return null;

        BufferedImage crop = skin.getSubimage(sx, sy, sw, sh);
        if (b.mirrored()) crop = flipHorizontal(crop);
        return f.shade() >= 1f ? crop : darken(crop, f.shade());
    }

    // ── Утилиты скина ────────────────────────────────────────────────────

    /**
     * Эвристика на случай, когда модель не пришла из профиля Mojang (скин по
     * прямому URL): у classic-раскладки задняя грань правой руки занимает
     * колонки x 54..56 (y 20..32), левой - x 46..48 (y 52..64); у slim руки
     * на воксель уже и до этих колонок не дотягиваются. Если обе зоны
     * полностью прозрачны - считаем скин slim. Смотрим обе зоны и весь их
     * столбец, а не один пиксель: редакторы порой оставляют мусор в
     * неиспользуемых областях.
     */
    private static boolean detectAlex(BufferedImage skin) {
        float scale = skin.getWidth() / 64f;
        return regionTransparent(skin, scale, 54, 20, 2, 12)
                && regionTransparent(skin, scale, 46, 52, 2, 12);
    }

    private static boolean regionTransparent(BufferedImage skin, float scale, int vx, int vy, int vw, int vh) {
        int x0 = Math.round(vx * scale), y0 = Math.round(vy * scale);
        int x1 = Math.min(Math.round((vx + vw) * scale), skin.getWidth());
        int y1 = Math.min(Math.round((vy + vh) * scale), skin.getHeight());
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                if (((skin.getRGB(x, y) >> 24) & 0xFF) != 0) return false;
            }
        }
        return true;
    }

    private static BufferedImage normalizeSkin(BufferedImage original) {
        int w = original.getWidth();
        int h = original.getHeight();
        if (w == h && original.getType() == BufferedImage.TYPE_INT_ARGB) return original;
        BufferedImage normalized = new BufferedImage(w, w, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = normalized.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return normalized;
    }

    private static BufferedImage flipHorizontal(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage flipped = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = flipped.createGraphics();
        g.drawImage(img, w, 0, -w, h, null);
        g.dispose();
        return flipped;
    }

    private static BufferedImage darken(BufferedImage img, float factor) {
        BufferedImage res = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int argb = img.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                if (a == 0) continue;
                int r = (int) (((argb >> 16) & 0xFF) * factor);
                int gr = (int) (((argb >> 8) & 0xFF) * factor);
                int b = (int) ((argb & 0xFF) * factor);
                res.setRGB(x, y, (a << 24) | (r << 16) | (gr << 8) | b);
            }
        }
        return res;
    }
}
