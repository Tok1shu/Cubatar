package net.tokishu.cubatar.common;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

@Component
public class PngResponseWriter {

    public void write(BufferedImage image, HttpServletResponse response) {
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "public, max-age=3600");

        try (OutputStream out = response.getOutputStream()) {
            ImageIO.write(image, "PNG", out);
            out.flush();
        } catch (IOException e) {
            if (isClientAbort(e)) {
                return;
            }
            System.err.println("Error while sending image to client: " + e.getMessage());
        }
    }

    private boolean isClientAbort(IOException e) {
        if (e.getClass().getSimpleName().contains("ClientAbortException")) {
            return true;
        }
        String msg = e.getMessage();
        return msg != null && (msg.contains("Broken pipe") || msg.contains("Connection reset"));
    }
}
