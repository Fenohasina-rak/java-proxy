package proxy.src.Helpers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Decoder {
    public static String decodeBase64(String encodedString) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
