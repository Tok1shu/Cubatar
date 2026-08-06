package net.tokishu.cubatar.module.iso.web;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.tokishu.cubatar.module.iso.service.IsoRenderService;
import net.tokishu.cubatar.module.iso.util.IsometricRenderer.Part;
import org.springframework.web.bind.annotation.*;

/** Легаси-алиас: {@code /v1/iso/{input}} = {@code /v1/iso/head/{input}}. */
@RestController
@RequestMapping("/v1/iso")
@RequiredArgsConstructor
public class IsoHeadController {

    private final IsoRenderService service;

    @GetMapping("/{input}")
    public void getIsoHead(
            @PathVariable String input,
            @RequestParam(defaultValue = "128") int size,
            @RequestParam(defaultValue = "-45") double yaw,
            @RequestParam(defaultValue = "30") double pitch,
            HttpServletResponse response) {

        service.process(input, size, yaw, pitch, Part.HEAD, false, null, response);
    }
}
