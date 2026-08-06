package net.tokishu.cubatar.module.iso.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.tokishu.cubatar.common.PngResponseWriter;
import net.tokishu.cubatar.common.SkinModel;
import net.tokishu.cubatar.module.iso.util.IsometricRenderer;
import net.tokishu.cubatar.module.iso.util.IsometricRenderer.Part;
import net.tokishu.cubatar.module.resolve.ResolvedSkin;
import net.tokishu.cubatar.module.resolve.SkinResolverService;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
@RequiredArgsConstructor
public class IsoRenderService {

    private final SkinResolverService resolver;
    private final PngResponseWriter writer;

    public void process(String input, int size, double yaw, double pitch,
                        Part part, boolean walking, Boolean slimOverride, boolean cape,
                        HttpServletResponse response) {
        ResolvedSkin skin = resolver.resolveWithModel(input);
        Boolean slim = SkinModel.resolve(slimOverride, skin.slim());
        BufferedImage capeImage = cape && part != Part.HEAD && skin.capeUrl() != null
                ? resolver.fetchTexture(skin.capeUrl())
                : null;
        BufferedImage image = IsometricRenderer.render(skin.image(), capeImage, size, yaw, pitch, part, walking, slim);
        writer.write(image, response);
    }
}
