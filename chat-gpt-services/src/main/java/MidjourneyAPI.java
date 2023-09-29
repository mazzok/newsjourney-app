import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MidjourneyAPI {

    private static Logger logger = LoggerFactory.getLogger(MidjourneyAPI.class);

    public static String prompt(String prompt) throws Exception {
        String url = "http://host.docker.internal:3000/api/imagine/";
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode body = mapper.createObjectNode();
        body.put("prompt", prompt);
        logger.info(body.asText());

        con.setDoOutput(true);
        con.getOutputStream().write(body.toString().getBytes());

        String output = new BufferedReader(new InputStreamReader(con.getInputStream())).lines()
                .reduce((a, b) -> a + b).get();

        List<String> progressResponse = Arrays.stream(output.split("}\\{"))
                .map(s -> "{" + s + "}").filter(s -> s.contains("progress")).collect(Collectors.toList());

        JsonNode tree = mapper.readTree(progressResponse.get(progressResponse.size() - 1));

        return tree.get("uri").asText();
    }

    private String getInfoBodyResponse(URI uri) {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = null;
        request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
        return response.body();
    }
}
