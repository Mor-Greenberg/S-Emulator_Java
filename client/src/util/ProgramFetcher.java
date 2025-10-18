package util;

import logic.program.Program;
import logic.xml.XmlLoader;
import okhttp3.*;
import logic.execution.ExecutionContextImpl;

public class ProgramFetcher {

    private static final OkHttpClient client = HttpClientUtil.getClient();

    public static Program fetchProgramFromServer(String uploader, String programName) {
        // הבקשה מבצעת GET לשירות request-program עם שם התוכנית בלבד
        // אין צורך בפרמטר uploader – המידע לא באמת נחוץ לשרת
        String url = "http://localhost:8080/S-Emulator/request-program?name=" + programName;

        try (Response res = client.newCall(new Request.Builder().url(url).get().build()).execute()) {
            if (!res.isSuccessful() || res.body() == null) {
                System.err.println("❌ Failed to fetch XML for program: " + programName +
                        " (status: " + res.code() + ")");
                return null;
            }

            String xml = res.body().string();
            Program program = XmlLoader.fromXmlString(xml);

            // שמירה מקומית בלבד — אין נגיעה בשרת!
            ExecutionContextImpl.loadProgram(program, xml);

            System.out.println("📥 Program '" + programName + "' downloaded and loaded locally.");
            return program;

        } catch (Exception e) {
            System.err.println("⚠️ Exception while fetching program: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
