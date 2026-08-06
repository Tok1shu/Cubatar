package net.tokishu.cubatar.module.body.util;

import lombok.experimental.UtilityClass;

import java.awt.*;
import java.awt.image.BufferedImage;

@UtilityClass
public class FullBodyGenerator {

    private static final int HEAD_FRONT_X = 8, HEAD_FRONT_Y = 8, HEAD_SIZE = 8;
    private static final int HAT_FRONT_X = 40, HAT_FRONT_Y = 8;
    private static final int HAT_BACK_X = 56, HAT_BACK_Y = 8;

    private static final int BODY_FRONT_X = 20, BODY_FRONT_Y = 20;
    private static final int BODY_W = 8, BODY_H = 12;

    // Тело, второй слой (куртка): передняя грань [20,36], форма та же — 8x12
    private static final int JACKET_FRONT_X = 20, JACKET_FRONT_Y = 36;

    private static final int RARM_FRONT_X = 44, RARM_FRONT_Y = 20, ARM_H = 12;
    private static final int STEVE_ARM_W = 4, ALEX_ARM_W = 3;

    // Руки, второй слой (рукава): передние грани, ширина = ширине руки
    private static final int RSLEEVE_FRONT_X = 44, RSLEEVE_FRONT_Y = 36;
    private static final int LSLEEVE_FRONT_X = 52, LSLEEVE_FRONT_Y = 52;

    // Левая рука первого слоя (modern): [36,52]
    private static final int LARM_FRONT_X = 36, LARM_FRONT_Y = 52;

    // Раздутие второго слоя: воксели с каждой стороны (коробка оверлея в игре)
    private static final float OVERLAY_INFLATE = 0.25f;

    // Глубина коробки в вокселях — расстояние в UV-развёртке от передней грани
    // до задней: back_x = front_x + ширина_грани + depth. У головы/шлема
    // (кубическая коробка 8x8x8) depth=8, у тела/рук/оверлеев (плоские
    // коробки глубиной 4 вокселя) depth=4 — стандартная раскладка скина.
    private static final int HEAD_DEPTH = 8;
    private static final int LIMB_DEPTH = 4;

    public static BufferedImage generateFrontView(BufferedImage rawSkin, int headSize, Boolean slim) {
        return generate(rawSkin, headSize, false, slim);
    }

    /**
     * Вид сзади — та же геометрия холста, что и спереди, но: 1) берётся
     * задняя грань каждой части текстуры (по стандартной UV-раскладке она
     * всегда правее передней на width+depth вокселей — см. {@link #HEAD_DEPTH}/
     * {@link #LIMB_DEPTH}); 2) руки меняются местами на холсте — если смотреть
     * на персонажа со спины, его правая рука оказывается у правого края
     * холста, а не у левого, как при виде спереди; 3) шлем: слой-"подложка"
     * (затемнённый, снизу) и видимый верхний слой меняются местами — сзади
     * подложкой становится передняя грань шлема, а видимым слоем — задняя.
     */
    public static BufferedImage generateBackView(BufferedImage rawSkin, int headSize, Boolean slim) {
        return generate(rawSkin, headSize, true, slim);
    }

    private static BufferedImage generate(BufferedImage rawSkin, int headSize, boolean back, Boolean slim) {
        if (rawSkin == null) return null;

        BufferedImage skin = normalizeSkin(rawSkin);
        boolean isLegacy = rawSkin.getHeight() == 32;
        boolean isAlex   = !isLegacy && (slim != null ? slim : detectAlex(skin));

        float scale = skin.getWidth() / 64f;

        int armW  = isAlex ? Math.round(headSize * 3f / 8f) : Math.round(headSize * 4f / 8f);
        int bodyW = headSize;
        int partH = Math.round(headSize * 12f / 8f);
        int hatSize   = (int) (headSize * 1.08);
        int hatOffset = (hatSize - headSize) / 2;

        int armVoxW = isAlex ? ALEX_ARM_W : STEVE_ARM_W;
        // Выступ раздутого второго слоя (рукав/куртка торчит за край базы).
        // Поля холста ровно под него, чтобы выступ не обрезался.
        int marginX = Math.round(OVERLAY_INFLATE * armW  / (float) armVoxW);
        int marginB = Math.round(OVERLAY_INFLATE * partH / (float) ARM_H);

        int totalW = marginX + armW + bodyW + armW + marginX;
        int totalH = hatOffset + headSize + partH + marginB;

        BufferedImage result = new BufferedImage(totalW, totalH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_OFF);

        int headX = marginX + armW;
        int headY = hatOffset;
        int bodyX = marginX + armW;
        int bodyY = hatOffset + headSize; // впритык к голове
        int leftSlotX  = marginX;
        int rightSlotX = marginX + armW + bodyW;
        // Спереди: левый слот холста = правая рука персонажа (зеркало), правый слот = левая.
        // Сзади (смотрим персонажу в спину) — наоборот.
        int rArmX = back ? rightSlotX : leftSlotX;
        int lArmX = back ? leftSlotX  : rightSlotX;
        int armsY = hatOffset + headSize;

        int blockSize = Math.round(HEAD_SIZE * scale);

        // UV передней/задней грани каждой части — сзади всегда +width+depth по X, Y не меняется
        int headUvX    = back ? HEAD_FRONT_X + HEAD_SIZE + HEAD_DEPTH : HEAD_FRONT_X;
        int bodyUvX    = back ? BODY_FRONT_X + BODY_W + LIMB_DEPTH    : BODY_FRONT_X;
        int jacketUvX  = back ? JACKET_FRONT_X + BODY_W + LIMB_DEPTH  : JACKET_FRONT_X;
        int rArmUvX    = back ? RARM_FRONT_X + armVoxW + LIMB_DEPTH   : RARM_FRONT_X;
        int lArmUvX    = back ? LARM_FRONT_X + armVoxW + LIMB_DEPTH   : LARM_FRONT_X;
        int rSleeveUvX = back ? RSLEEVE_FRONT_X + armVoxW + LIMB_DEPTH : RSLEEVE_FRONT_X;
        int lSleeveUvX = back ? LSLEEVE_FRONT_X + armVoxW + LIMB_DEPTH : LSLEEVE_FRONT_X;
        // Шлем спереди: подложка = задняя грань (HAT_BACK), верх = передняя (HAT_FRONT).
        // Сзади — наоборот: подложка = передняя грань, верх = задняя.
        int hatBackdropUvX = back ? HAT_FRONT_X : HAT_BACK_X;
        int hatTopUvX       = back ? HAT_BACK_X : HAT_FRONT_X;

        // ── Слой 1: подложка шлема затемнённая (самый нижний) ─────────────────
        BufferedImage hatTopImg = null;
        if (!isLegacy) {
            int hatBx = Math.round(hatBackdropUvX * scale);
            int hatBy = Math.round(HAT_FRONT_Y * scale);
            int hatFx = Math.round(hatTopUvX * scale);
            int hatFy = Math.round(HAT_FRONT_Y * scale);
            boolean hasHat = shouldRenderOverlay(skin, hatFx, hatFy, blockSize, blockSize, false);
            if (hasHat) {
                // подложка шлема — затемнённая, для объёма
                BufferedImage hatBackdrop = extractAndScale(skin, hatBx, hatBy, blockSize, blockSize, hatSize);
                if (hasVisiblePixels(hatBackdrop)) {
                    g.drawImage(darkenImage(hatBackdrop, 0.6f), headX - hatOffset, 0, null);
                }
                // видимый верхний слой шлема сохраняем для последнего слоя
                hatTopImg = extractAndScale(skin, hatFx, hatFy, blockSize, blockSize, hatSize);
            }
        }

        // ── Слой 2: тело (первый слой) ────────────────────────────────────────
        int steveArmW = Math.round(STEVE_ARM_W * scale);
        int alexArmW  = Math.round(ALEX_ARM_W  * scale);
        int srcArmW   = isAlex ? alexArmW : steveArmW;
        int srcArmH   = Math.round(ARM_H * scale);

        {
            int bx = Math.round(bodyUvX * scale);
            int by = Math.round(BODY_FRONT_Y * scale);
            int bw = Math.round(BODY_W * scale);
            int bh = Math.round(BODY_H * scale);
            BufferedImage body = extractAndScaleRect(skin, bx, by, bw, bh, bodyW, partH);
            g.drawImage(body, bodyX, bodyY, null);
        }

        // ── Слой 3: руки (первый слой) ────────────────────────────────────────
        {
            int rx = Math.round(rArmUvX * scale);
            int ry = Math.round(RARM_FRONT_Y * scale);
            BufferedImage rArm = extractAndScaleRect(skin, rx, ry, srcArmW, srcArmH, armW, partH);
            g.drawImage(rArm, rArmX, armsY, null);
        }

        if (!isLegacy) {
            int lx = Math.round(lArmUvX * scale);
            int ly = Math.round(LARM_FRONT_Y * scale);
            BufferedImage lArm = extractAndScaleRect(skin, lx, ly, srcArmW, srcArmH, armW, partH);
            g.drawImage(lArm, lArmX, armsY, null);
        } else {
            int rx = Math.round(rArmUvX * scale);
            int ry = Math.round(RARM_FRONT_Y * scale);
            BufferedImage rArm = extractAndScaleRect(skin, rx, ry, srcArmW, srcArmH, armW, partH);
            g.drawImage(flipHorizontal(rArm), lArmX, armsY, null);
        }

        // ── Слой 4: второй слой (куртка, рукава) — раздут и центрирован поверх базы ──
        if (!isLegacy) {
            // Куртка поверх тела
            drawInflatedOverlay(g, skin, scale,
                    jacketUvX, JACKET_FRONT_Y, BODY_W, BODY_H,
                    bodyX, bodyY, bodyW, partH);

            // Рукава поверх рук
            drawInflatedOverlay(g, skin, scale,
                    rSleeveUvX, RSLEEVE_FRONT_Y, armVoxW, ARM_H,
                    rArmX, armsY, armW, partH);
            drawInflatedOverlay(g, skin, scale,
                    lSleeveUvX, LSLEEVE_FRONT_Y, armVoxW, ARM_H,
                    lArmX, armsY, armW, partH);
        }

        // ── Слой 5: голова — впереди тела/куртки по глубине ─────────────
        {
            int fx = Math.round(headUvX * scale);
            int fy = Math.round(HEAD_FRONT_Y * scale);
            BufferedImage face = extractAndScale(skin, fx, fy, blockSize, blockSize, headSize);
            g.drawImage(face, headX, headY, null);
        }

        // ── Слой 6: видимый верхний слой шлема — перекрывает всё ───────────
        if (hatTopImg != null) {
            g.drawImage(hatTopImg, headX - hatOffset, 0, null);
        }

        g.dispose();
        return result;
    }

    /**
     * Эвристика на случай, когда модель не пришла из профиля Mojang (скин по
     * прямому URL): у classic-раскладки задняя грань правой руки занимает
     * колонки x 54..56 (y 20..32), левой - x 46..48 (y 52..64); у slim руки
     * на воксель уже и до этих колонок не дотягиваются. Если обе зоны
     * полностью прозрачны - считаем скин slim.
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

    private static BufferedImage extractAndScale(BufferedImage skin, int x, int y, int w, int h, int targetSize) {
        return extractAndScaleRect(skin, x, y, w, h, targetSize, targetSize);
    }

    private static BufferedImage extractAndScaleRect(BufferedImage skin, int x, int y, int srcW, int srcH,
                                                     int targetW, int targetH) {
        srcW = Math.min(srcW, skin.getWidth()  - x);
        srcH = Math.min(srcH, skin.getHeight() - y);
        if (srcW <= 0 || srcH <= 0) return new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        BufferedImage extracted = skin.getSubimage(x, y, srcW, srcH);
        if (srcW == targetW && srcH == targetH) return extracted;
        BufferedImage scaled = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(extracted, 0, 0, targetW, targetH, null);
        g.dispose();
        return scaled;
    }

    /**
     * Рисует второй слой (куртку/рукав), раздутый на {@link #OVERLAY_INFLATE} вокселя
     * с каждой стороны и центрированный поверх базовой части — как коробка оверлея в игре.
     * Раздутие задаётся в вокселях, поэтому масштабируется вместе с размером картинки
     * и для тонкой руки (4 вокселя) и для тела (8 вокселей) пропорции верны.
     * Координаты и размеры источника указываются в вокселях скина 64x64.
     */
    private static void drawInflatedOverlay(Graphics2D g, BufferedImage skin, float scale,
                                            int srcVoxX, int srcVoxY, int srcVoxW, int srcVoxH,
                                            int baseX, int baseY, int baseW, int baseH) {
        int padX = Math.round(OVERLAY_INFLATE * baseW / srcVoxW);
        int padY = Math.round(OVERLAY_INFLATE * baseH / srcVoxH);
        int overlayW = baseW + 2 * padX;
        int overlayH = baseH + 2 * padY;

        BufferedImage overlay = extractAndScaleRect(skin,
                Math.round(srcVoxX * scale), Math.round(srcVoxY * scale),
                Math.round(srcVoxW * scale), Math.round(srcVoxH * scale),
                overlayW, overlayH);
        g.drawImage(overlay, baseX - padX, baseY - padY, null);
    }

    private static BufferedImage flipHorizontal(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage flipped = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = flipped.createGraphics();
        g.drawImage(img, w, 0, -w, h, null);
        g.dispose();
        return flipped;
    }

    private static boolean shouldRenderOverlay(BufferedImage skin, int x, int y, int w, int h, boolean isLegacy) {
        int total = w * h, solid = 0, transparent = 0;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int alpha = (skin.getRGB(x + i, y + j) >> 24) & 0xFF;
                if (alpha == 0) transparent++;
                else if (alpha == 255) solid++;
            }
        }
        if (solid == 0 && transparent == total) return false;
        if (isLegacy && solid == total) return false;
        return true;
    }

    private static BufferedImage darkenImage(BufferedImage img, float factor) {
        BufferedImage res = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int argb = img.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                if (a == 0) continue;
                int r = (int)(((argb >> 16) & 0xFF) * factor);
                int g = (int)(((argb >> 8)  & 0xFF) * factor);
                int b = (int)((argb & 0xFF) * factor);
                res.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return res;
    }

    private static boolean hasVisiblePixels(BufferedImage img) {
        for (int x = 0; x < img.getWidth(); x++)
            for (int y = 0; y < img.getHeight(); y++)
                if (((img.getRGB(x, y) >> 24) & 0xFF) > 0) return true;
        return false;
    }
}
