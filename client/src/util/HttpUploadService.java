package util;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class HttpUploadService {
    private static final String SERVER_URL = "http://localhost:8080/load-program";
    private final OkHttpClient client = new OkHttpClient();

    public String uploadXmlFile(File xmlFile) throws IOException {
        String xmlContent = Files.readString(xmlFile.toPath());

        RequestBody body = RequestBody.create(
                xmlContent,
                MediaType.parse("application/xml")
        );

        Request request = new Request.Builder()
                .url(SERVER_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IOException("Unexpected code: " + response.code());

            return response.body().string();  // JSON string מהשרת
        }
    }
}
