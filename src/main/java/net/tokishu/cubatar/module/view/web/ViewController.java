package net.tokishu.cubatar.module.view.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Интерактивный 3D-вьюер скина (skinview3d, WebGL) - страница для вставки
 * через iframe: {@code <iframe src=".../view/ник">}. Страница полностью
 * статическая и одна на всех: input из пути она читает сама на клиенте и
 * грузит текстуру с нашего /v1/skin/{input}, серверу рендерить ничего не
 * нужно. Параметры: walk, rotate, wheelzoom (true/false), zoom, fov, bg (hex).
 */
@RestController
public class ViewController {

    private final String page;

    public ViewController() {
        try (InputStream in = new ClassPathResource("view/viewer.html").getInputStream()) {
            page = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("viewer.html not found in classpath", e);
        }
    }

    @GetMapping(value = "/view/{input}", produces = "text/html;charset=UTF-8")
    public String view(@PathVariable String input, HttpServletResponse response) {
        response.setHeader("Cache-Control", "public, max-age=3600");
        return page;
    }
}
