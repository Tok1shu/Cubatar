package net.tokishu.cubatar.module.skin.web;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.tokishu.cubatar.module.skin.service.SkinService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/skin")
@RequiredArgsConstructor
public class SkinController {

    private final SkinService service;

    @GetMapping("/{input}")
    public void getRawSkin(
            @PathVariable String input,
            HttpServletResponse response) {

        service.process(input, response);
    }
}
