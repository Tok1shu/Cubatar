package net.tokishu.cubatar.module.avatar.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.tokishu.cubatar.module.avatar.AvatarConfig;
import net.tokishu.cubatar.module.avatar.domain.enums.RequestType;
import net.tokishu.cubatar.module.avatar.service.integration.MojangGateway;
import net.tokishu.cubatar.module.avatar.util.AvatarGenerator;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static net.tokishu.cubatar.module.avatar.AvatarConfig.UUID_PATTERN;

@Service
@RequiredArgsConstructor
public class AvatarService {

    private final AvatarConfig config;
    private final MojangGateway gateway;

    /***
     *
     * @param input User input
     * @param size  Image Size
     */
    public void process(String input, int size, HttpServletResponse response) {
        RequestType type = getRequestType(input);
        BufferedImage image = switch (type) {
            case UUID     -> headFromUUID(input, size);
            case URL      -> headFromUrl(input, size);
            case NICKNAME -> headFromUsername(input, size);
        };
        writePngToResponse(image, response);
    }

    private BufferedImage headFromUUID(String uuid, int size){
        String url = gateway.getSkinUrlFromUUID(UUID.fromString(uuid));
        return headFromUrl(url, size);
    }

    private BufferedImage headFromUsername(String username, int size){
        UUID uuid = gateway.getUUIDFromUsername(username);
        String url = gateway.getSkinUrlFromUUID(uuid);
        return headFromUrl(url, size);
    }

    private BufferedImage headFromUrl(String input, int size) {

        String realUrl = input;

        if (isBase64Url(input)) {
            byte[] decodedBytesUrl = Base64.getUrlDecoder().decode(input);
            realUrl = new String(decodedBytesUrl, StandardCharsets.UTF_8);
        }

        RestClient restClient = RestClient.create();

        byte[] imageBytes = restClient.get()
                .uri(realUrl)
                .retrieve()
                .body(byte[].class);

        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(bis);
            if (image == null) {
                throw new IllegalArgumentException("По ссылке не картинка или формат не поддерживается");
            }
            return AvatarGenerator.extractHeadIcon(image, size);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении изображения", e);
        }
    }

    public RequestType getRequestType(String input) {
        int len = input.length();
        if ((input.length() == 32 || input.length() == 36) && UUID_PATTERN.matcher(input).matches()) {
            return RequestType.UUID;
        }

        if (isBase64Url(input)) {
            return RequestType.URL;
        }

        if (len <=16){
            return RequestType.NICKNAME;
        }

        throw new IllegalArgumentException("Недопустимый аргумент"); // TODO: Как появится exception handler поменять на ошибку.
    }

    public void writePngToResponse(BufferedImage image, HttpServletResponse response) {
        response.setContentType("image/png");
        try (OutputStream out = response.getOutputStream()) {
            ImageIO.write(image, "PNG", out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isBase64Url(String input) {
        try {
            byte[] decodedBytes = java.util.Base64.getUrlDecoder().decode(input);
            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);

            return decodedString.startsWith("http://") || decodedString.startsWith("https://");
        } catch (IllegalArgumentException e) {
            return false; // Это не валидный Base64
        }
    }
}
