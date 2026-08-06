package net.tokishu.cubatar.module.iso.web;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.tokishu.cubatar.module.iso.service.IsoRenderService;
import net.tokishu.cubatar.module.iso.util.IsometricRenderer.Part;
import org.springframework.web.bind.annotation.*;

/**
 * v2 головы: тот же движок, что и /v1/iso, но по умолчанию yaw=0/pitch=0 -
 * визуально эквивалентно плоскому v1 /v1/avatar анфас, с опцией включить
 * псевдо-3D через query-параметры без похода на отдельный эндпоинт.
 */
@RestController
@RequestMapping("/v2/avatar")
@RequiredArgsConstructor
public class IsoHeadV2Controller {

    private final IsoRenderService service;

    @GetMapping("/{input}")
    public void getPlayerHead(
            @PathVariable String input,
            @RequestParam(defaultValue = "64") int size,
            @RequestParam(defaultValue = "0") double yaw,
            @RequestParam(defaultValue = "0") double pitch,
            HttpServletResponse response) {

        service.process(input, size, yaw, pitch, Part.HEAD, false, null, false, response);
    }
}
