package net.tokishu.cubatar.module.resolve;

import java.awt.image.BufferedImage;

/**
 * Результат резолва: картинка скина + модель + URL плаща (если есть).
 * slim == null - модель неизвестна (скин пришёл по прямому URL, а не из
 * профиля Mojang), рендерам следует падать на эвристику по прозрачности.
 * Плащ отдаётся ссылкой, а не картинкой - его качают только те рендеры,
 * которым он реально нужен.
 */
public record ResolvedSkin(BufferedImage image, Boolean slim, String capeUrl) {}
