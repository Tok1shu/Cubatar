package net.tokishu.cubatar.module.body.web;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.tokishu.cubatar.common.SkinModel;
import net.tokishu.cubatar.module.body.service.BodyService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/body")
@RequiredArgsConstructor
public class BodyController {

    private final BodyService service;

    @GetMapping("/{input}")
    public void getPlayerBody(
            @PathVariable String input,
            @RequestParam(defaultValue = "128") int size,
            @RequestParam(defaultValue = "false") boolean back,
            @RequestParam(defaultValue = "auto") String model,
            HttpServletResponse response) {

        service.process(input, size, back, SkinModel.parse(model), response);
    }
}
