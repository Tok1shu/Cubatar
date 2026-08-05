package net.tokishu.cubatar.module.body.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.tokishu.cubatar.common.PngResponseWriter;
import net.tokishu.cubatar.module.body.util.FullBodyGenerator;
import net.tokishu.cubatar.module.resolve.SkinResolverService;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
@RequiredArgsConstructor
public class BodyService {

    private final SkinResolverService resolver;
    private final PngResponseWriter writer;

    public void process(String input, int size, boolean back, HttpServletResponse response) {
        BufferedImage skin = resolver.resolve(input);
        BufferedImage image = back
                ? FullBodyGenerator.generateBackView(skin, size)
                : FullBodyGenerator.generateFrontView(skin, size);
        writer.write(image, response);
    }
}
