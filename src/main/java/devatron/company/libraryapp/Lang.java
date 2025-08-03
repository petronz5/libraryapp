
package devatron.company.libraryapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Lang {
    private static Map<String, String> strings = new HashMap<>();
    private static String current = "it";

    public static void load(String langCode) {
        try {
            // aggiorno il codice lingua _prima_ di caricare il JSON
            current = langCode;

            ObjectMapper mapper = new ObjectMapper();
            InputStream is = Lang.class.getResourceAsStream(
                "/devatron/company/libraryapp/lang/" + langCode + ".json"
            );
            strings = mapper.readValue(is, HashMap.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String current() {
        return current;
    }

    public static String get(String key) {
        return strings.getOrDefault(key, key);
    }
}