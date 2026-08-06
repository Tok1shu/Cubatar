package net.tokishu.cubatar.module.skin.web;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.tokishu.cubatar.module.skin.service.CapeService;
import org.springframework.web.bind.annotation.*;

/** Сырая текстура плаща игрока (64x32); 404, если плаща нет. */
@RestController
@RequestMapping("/v1/cape")
@RequiredArgsConstructor
public class CapeController {

    private final CapeService service;

    @GetMapping("/{input}")
    public void getPlayerCape(@PathVariable String input, HttpServletResponse response) {
        service.process(input, response);
    }
}
