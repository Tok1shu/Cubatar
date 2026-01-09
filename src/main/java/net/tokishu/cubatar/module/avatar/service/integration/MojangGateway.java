package net.tokishu.cubatar.module.avatar.service.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.UUID;

import static net.tokishu.cubatar.module.avatar.AvatarConfig.MOJANG_TO_UUID_REGEX;

@Component
@RequiredArgsConstructor
public class MojangGateway {

    private final RestClient restClient;
    private final ObjectMapper mapper;

    public static final String PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/";
    public static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

    // Кешируем преобразование Ник -> UUID
    @Cacheable(value = "uuids", key = "#username", unless = "#result == null")
    public UUID getUUIDFromUsername(String username) {
        try {
            String response = restClient.get()
                    .uri(PROFILE_URL + username)
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isEmpty()) return null;

            JsonNode root = mapper.readTree(response);
            return parseUUID(root.get("id").asString());
        } catch (Exception e) {
            return null;
        }
    }

    @Cacheable(value = "skins", key = "#uuid", unless = "#result == null")
    public String getSkinUrlFromUUID(UUID uuid) {
        try {
            String response = restClient.get()
                    .uri(SKIN_URL + uuid.toString())
                    .retrieve()
                    .body(String.class);

            if (response == null) return null;

            JsonNode root = mapper.readTree(response);
            JsonNode properties = root.get("properties");

            if (properties != null && properties.isArray() && !properties.isEmpty()) {
                String encodedJson = properties.get(0).get("value").asString();
                String decodedJson = new String(Base64.getDecoder().decode(encodedJson));
                return mapper.readTree(decodedJson).get("textures").get("SKIN").get("url").asString();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private UUID parseUUID(String idWithoutDashes) {
        String formatted = idWithoutDashes.replaceFirst(
                MOJANG_TO_UUID_REGEX,
                "$1-$2-$3-$4-$5"
        );
        return UUID.fromString(formatted);
    }
}