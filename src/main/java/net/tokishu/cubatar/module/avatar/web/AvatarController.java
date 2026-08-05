package net.tokishu.cubatar.module.avatar.web;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.tokishu.cubatar.module.avatar.service.AvatarService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/avatar")
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService service;

    @GetMapping("/{input}")
    public void getPlayerHead(
            @PathVariable String input,
            @RequestParam(defaultValue = "64") int size,
            HttpServletResponse response) {

        service.process(input, size, response);
    }
}
