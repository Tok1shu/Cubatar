package net.tokishu.cubatar.module.resolve;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static net.tokishu.cubatar.module.resolve.ResolverConfig.UUID_PATTERN;

@Service
@RequiredArgsConstructor
public class SkinResolverService {

    private final MojangGateway gateway;
    private final RestClient restClient;

    public BufferedImage resolve(String input) {
        RequestType type = getRequestType(input);
        return switch (type) {
            case UUID     -> skinFromUUID(input);
            case URL      -> skinFromUrl(input);
            case NICKNAME -> skinFromUsername(input);
        };
    }

    private BufferedImage skinFromUUID(String uuid) {
        String url = gateway.getSkinUrlFromUUID(UUID.fromString(uuid));
        return skinFromUrl(url);
    }

    private BufferedImage skinFromUsername(String username) {
        UUID uuid = gateway.getUUIDFromUsername(username);
        String url = gateway.getSkinUrlFromUUID(uuid);
        return skinFromUrl(url);
    }

    private BufferedImage skinFromUrl(String input) {

        String realUrl = input;

        if (isBase64Url(input)) {
            byte[] decodedBytesUrl = Base64.getUrlDecoder().decode(input);
            realUrl = new String(decodedBytesUrl, StandardCharsets.UTF_8);
        }

        byte[] imageBytes = restClient.get()
                .uri(realUrl)
                .retrieve()
                .body(byte[].class);

        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(bis);
            if (image == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not image or unsupported format");
            }
            return image;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while reading image");
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

        if (len <= 16) {
            return RequestType.NICKNAME;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
    }

    private boolean isBase64Url(String input) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(input);
            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);

            return decodedString.startsWith("http://") || decodedString.startsWith("https://");
        } catch (IllegalArgumentException e) {
            return false; // Это не валидный Base64
        }
    }
}
