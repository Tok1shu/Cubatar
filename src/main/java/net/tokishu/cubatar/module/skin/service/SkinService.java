package net.tokishu.cubatar.module.skin.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.tokishu.cubatar.common.PngResponseWriter;
import net.tokishu.cubatar.module.resolve.SkinResolverService;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
@RequiredArgsConstructor
public class SkinService {

    private final SkinResolverService resolver;
    private final PngResponseWriter writer;

    public void process(String input, HttpServletResponse response) {
        BufferedImage skin = resolver.resolve(input);
        writer.write(skin, response);
    }
}
