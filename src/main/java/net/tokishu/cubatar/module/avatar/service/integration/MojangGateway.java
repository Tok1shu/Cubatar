package net.tokishu.cubatar.module.avatar.service.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.UUID;

import static net.tokishu.cubatar.module.avatar.AvatarConfig.MOJANG_TO_UUID_REGEX;

@Service
@RequiredArgsConstructor
public class MojangGateway {

    private final RestClient restClient;
    private final ObjectMapper mapper;

    public static final String PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/";
    public static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

    @Cacheable(value = "uuids", key = "#username", unless = "#result == null")
    public UUID getUUIDFromUsername(String username) {
        ResponseEntity<String> response = restClient.get()
                .uri(PROFILE_URL + username)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {})
                .toEntity(String.class);

        if (response.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found");

        if (response == null || response.getBody().isEmpty()) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while getting UUID");

        JsonNode root = mapper.readTree(response.getBody());

        return parseUUID(root.get("id").asString());
    }

    @Cacheable(value = "skins", key = "#uuid", unless = "#result == null")
    public String getSkinUrlFromUUID(UUID uuid) {
        String response = restClient.get()
                .uri(SKIN_URL + uuid.toString())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {})
                .body(String.class);

        if (response == null) return null;

        JsonNode root = mapper.readTree(response);
        JsonNode properties = root.get("properties");

        if (properties != null && properties.isArray() && !properties.isEmpty()) {
            String encodedJson = properties.get(0).get("value").asString();
            String decodedJson = new String(Base64.getDecoder().decode(encodedJson));

            String skinUrl = mapper.readTree(decodedJson).get("textures").get("SKIN").get("url").asString();
            if (skinUrl == null) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while getting skin URL");

            return skinUrl;
        }
        return null;
    }

    private UUID parseUUID(String idWithoutDashes) {
        String formatted = idWithoutDashes.replaceFirst(
                MOJANG_TO_UUID_REGEX,
                "$1-$2-$3-$4-$5"
        );
        return UUID.fromString(formatted);
    }
}