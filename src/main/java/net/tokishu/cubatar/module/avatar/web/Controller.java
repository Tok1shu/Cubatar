package net.tokishu.cubatar.module.avatar.web;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.tokishu.cubatar.module.avatar.domain.enums.RequestType;
import net.tokishu.cubatar.module.avatar.service.AvatarService;
import net.tokishu.cubatar.module.avatar.util.AvatarGenerator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.io.IOException;

@RestController
@RequestMapping("/v1/avatar")
@RequiredArgsConstructor
public class Controller {

    private final AvatarService service;

    @GetMapping("/{input}")
    public void getPlayerHead(
            @PathVariable String input,
            @RequestParam(defaultValue = "64") int size,
            HttpServletResponse response) {

        service.process(input, size, response);
    }
}
