package net.tokishu.cubatar.module.avatar.service.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.UUID;

import static net.tokishu.cubatar.module.avatar.AvatarConfig.MOJANG_TO_UUID_REGEX;

@Component
@RequiredArgsConstructor
public class MojangGateway {

    // https://api.mojang.com/users/profiles/minecraft/jeb_
    public static final String PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/";
    public static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

    public UUID getUUIDFromUsername(String username) {
        ObjectMapper mapper = new ObjectMapper();
        RestClient restClient = RestClient.create();
        String response = restClient.get()
                .uri(PROFILE_URL+username)
                .retrieve()
                .body(String.class);

        return parseUUID(mapper.readTree(response).get("id").asString());
    }

    public String getSkinUrlFromUUID(UUID uuid) {
        ObjectMapper mapper = new ObjectMapper();
        RestClient restClient = RestClient.create();
        String response = restClient.get()
                .uri(SKIN_URL+uuid.toString())
                .retrieve()
                .body(String.class);

        String encodedJson = mapper.readTree(response).get("properties").get(0).get("value").asString();
        return mapper.readTree(Base64.getDecoder().decode(encodedJson)).get("textures").get("SKIN").get("url").asString();
    }

    private UUID parseUUID(String idWithoutDashes) {
        String formatted = idWithoutDashes.replaceFirst(
                MOJANG_TO_UUID_REGEX,
                "$1-$2-$3-$4-$5"
        );
        return UUID.fromString(formatted);
    }


}
