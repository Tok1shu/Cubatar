package net.tokishu.cubatar.module.iso.web;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.tokishu.cubatar.common.SkinModel;
import net.tokishu.cubatar.module.iso.service.IsoRenderService;
import net.tokishu.cubatar.module.iso.util.IsometricRenderer.Part;
import org.springframework.web.bind.annotation.*;

/**
 * Iso-неймспейс по частям модели: head (куб головы), body (по пояс),
 * full (в полный рост) - каждая под произвольным углом yaw/pitch.
 * У body/full дополнительно pose=walk (поза ходьбы) и model=slim|classic
 * (явная модель поверх авто-определения). {@code /v1/iso/{input}} без саба
 * остаётся алиасом head (см. {@link IsoHeadController}).
 */
@RestController
@RequestMapping("/v1/iso")
@RequiredArgsConstructor
public class IsoRenderController {

    private final IsoRenderService service;

    @GetMapping("/head/{input}")
    public void head(
            @PathVariable String input,
            @RequestParam(defaultValue = "128") int size,
            @RequestParam(defaultValue = "-45") double yaw,
            @RequestParam(defaultValue = "30") double pitch,
            HttpServletResponse response) {

        service.process(input, size, yaw, pitch, Part.HEAD, false, null, response);
    }

    @GetMapping("/body/{input}")
    public void body(
            @PathVariable String input,
            @RequestParam(defaultValue = "128") int size,
            @RequestParam(defaultValue = "-45") double yaw,
            @RequestParam(defaultValue = "30") double pitch,
            @RequestParam(defaultValue = "stand") String pose,
            @RequestParam(defaultValue = "auto") String model,
            HttpServletResponse response) {

        service.process(input, size, yaw, pitch, Part.BODY, isWalking(pose), SkinModel.parse(model), response);
    }

    @GetMapping("/full/{input}")
    public void full(
            @PathVariable String input,
            @RequestParam(defaultValue = "128") int size,
            @RequestParam(defaultValue = "-45") double yaw,
            @RequestParam(defaultValue = "30") double pitch,
            @RequestParam(defaultValue = "stand") String pose,
            @RequestParam(defaultValue = "auto") String model,
            HttpServletResponse response) {

        service.process(input, size, yaw, pitch, Part.FULL, isWalking(pose), SkinModel.parse(model), response);
    }

    private static boolean isWalking(String pose) {
        return "walk".equalsIgnoreCase(pose) || "walking".equalsIgnoreCase(pose);
    }
}
