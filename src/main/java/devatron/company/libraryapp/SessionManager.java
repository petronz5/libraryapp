package devatron.company.libraryapp;

import java.io.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SessionManager {
    private static final String SESSION_FILE = "session.json";
    private static final long SESSION_DURATION_MS = 20 * 60 * 1000; // 20 minuti

    public static void saveSession(String email) {
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("email", email);
        sessionData.put("timestamp", Instant.now().toEpochMilli());

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File(SESSION_FILE), sessionData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getValidSessionEmail() {
        File file = new File(SESSION_FILE);
        if (!file.exists()) return null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<?, ?> sessionData = mapper.readValue(file, Map.class);

            String email = (String) sessionData.get("email");
            long timestamp = ((Number) sessionData.get("timestamp")).longValue();

            long now = Instant.now().toEpochMilli();
            if (now - timestamp <= SESSION_DURATION_MS) {
                return email;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void clearSession() {
        File file = new File(SESSION_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}
