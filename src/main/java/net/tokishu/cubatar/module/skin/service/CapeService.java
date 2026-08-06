package net.tokishu.cubatar.module.skin.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.tokishu.cubatar.common.PngResponseWriter;
import net.tokishu.cubatar.module.resolve.SkinResolverService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CapeService {

    private final SkinResolverService resolver;
    private final PngResponseWriter writer;

    public void process(String input, HttpServletResponse response) {
        String capeUrl = resolver.resolveCapeUrl(input);
        if (capeUrl == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player has no cape");
        writer.write(resolver.fetchTexture(capeUrl), response);
    }
}
