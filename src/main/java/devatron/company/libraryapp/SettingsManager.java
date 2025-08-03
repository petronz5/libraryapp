package devatron.company.libraryapp;

import java.util.prefs.Preferences;

public class SettingsManager {
    private static final Preferences PREFS = Preferences.userNodeForPackage(SettingsManager.class);
    private static final String KEY_LANG = "app.lang";

    public static void saveLang(String langCode) {
        PREFS.put(KEY_LANG, langCode);
    }

    public static String loadLang() {
        return PREFS.get(KEY_LANG, "it");
    }
}
