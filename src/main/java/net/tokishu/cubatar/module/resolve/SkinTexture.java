package net.tokishu.cubatar.module.resolve;

/**
 * Текстура скина из профиля Mojang: URL + модель. slim (Alex, руки 3 вокселя)
 * приходит явным полем {@code textures.SKIN.metadata.model = "slim"};
 * отсутствие metadata означает classic (Steve, руки 4 вокселя).
 */
public record SkinTexture(String url, boolean slim) {}
