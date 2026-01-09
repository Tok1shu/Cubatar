package net.tokishu.cubatar.module.avatar;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class AvatarConfig {
    public static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    public static final Pattern UUID_PATTERN = Pattern.compile(UUID_REGEX);

    public static final String MOJANG_TO_UUID_REGEX = "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})";
    public static final Pattern MOJANG_TO_UUID_PATTERN = Pattern.compile(MOJANG_TO_UUID_REGEX);
}
