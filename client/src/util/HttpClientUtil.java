package util;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jaxbV2.jaxb.v2.SProgram;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

public class HttpClientUtil {
    public static String sendXmlFileAndReceiveServerMessage(String filePath) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofFile(Path.of(filePath));
        System.out.println("File to upload: " + filePath);


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/S-Emulator/load-program"))
                .header("Content-Type", "application/xml")
                .POST(body)
                .build();

        System.out.println("Sending POST to http://localhost:8080/S-Emulator//load-program");

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Server returned status: " + response.statusCode() + "\n" + response.body());
        }

        return response.body();
    }

}
