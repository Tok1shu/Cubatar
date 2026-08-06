package net.tokishu.cubatar.module.iso.web;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.tokishu.cubatar.common.SkinModel;
import net.tokishu.cubatar.module.iso.service.IsoRenderService;
import net.tokishu.cubatar.module.iso.util.IsometricRenderer.Part;
import org.springframework.web.bind.annotation.*;

/**
 * v2 тела: тот же API, что /v1/body (size + back), но рендер идёт через
 * воксельный движок iso - раздутие вторых слоёв и изнанка их дальних граней
 * получаются из честной геометрии коробок, а не из плоских наложений.
 */
@RestController
@RequestMapping("/v2/body")
@RequiredArgsConstructor
public class IsoBodyV2Controller {

    private final IsoRenderService service;

    @GetMapping("/{input}")
    public void getPlayerBody(
            @PathVariable String input,
            @RequestParam(defaultValue = "128") int size,
            @RequestParam(defaultValue = "false") boolean back,
            @RequestParam(defaultValue = "auto") String model,
            @RequestParam(defaultValue = "true") boolean cape,
            HttpServletResponse response) {

        service.process(input, size, back ? 180 : 0, 0, Part.BODY, false, SkinModel.parse(model), cape, response);
    }
}
