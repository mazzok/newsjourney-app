package services;

import jakarta.enterprise.context.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@RequestScoped
public class ChatGPTAPIService {

    private Logger logger = LoggerFactory.getLogger(ChatGPTAPIService.class);
    public String chatGPT(String text) throws Exception {

        String template = "Übersetze ins Englische:%s." +
                "Schreib nur Text, ohne quellen. " +
                "Füge am Anfang un Ende deiner Antwort folgenden Text hinzu:_X_";
        String url = new URI("http",
                "host.docker.internal:8081",
                null, "text="+String.format(template,replaceUmlaut(text)),null).toASCIIString();
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("GET");

        logger.info("Making GET request to: "+url);

        con.setDoOutput(true);

        String output = new BufferedReader(new InputStreamReader(con.getInputStream())).lines()
                .reduce((a, b) -> a + b).get();


        return output;
        /*String url = "https://api.openai.com/v1/chat/completions";
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        String openaiKey = System.getenv("OPENAI_KEY");
        openaiKey="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";
        con.setRequestProperty("Authorization", "Bearer "+openaiKey);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode data =mapper.createObjectNode();
        ArrayNode messages = mapper.createArrayNode();
        ObjectNode systemMessage = mapper.createObjectNode();
        systemMessage.put("role","system");
        systemMessage.put("content",role);
        ObjectNode message = mapper.createObjectNode();
        messages.add(systemMessage);
        message.put("role","user");
        message.put("content",replaceUmlaut(text));
        messages.add(message);
        data.put("model", "gpt-3.5-turbo");
        data.put("messages", messages);

        con.setDoOutput(true);
        con.getOutputStream().write(data.toString().getBytes());

        String output = new BufferedReader(new InputStreamReader(con.getInputStream())).lines()
                .reduce((a, b) -> a + b).get();

        JsonNode outNode = mapper.readTree(output);
        return outNode.get("choices").get(0).get("message").get("content").asText();*/
    }

    private String replaceUmlaut(String input) {

        // replace all lower Umlauts
        String output = input.replace("ü", "ue")
                .replace("ö", "oe")
                .replace("ä", "ae")
                .replace("ß", "ss");

        // first replace all capital Umlauts in a non-capitalized context (e.g. Übung)
        output = output.replaceAll("Ü(?=[a-zäöüß ])", "Ue")
                .replaceAll("Ö(?=[a-zäöüß ])", "Oe")
                .replaceAll("Ä(?=[a-zäöüß ])", "Ae");

        // now replace all the other capital Umlauts
        output = output.replace("Ü", "UE")
                .replace("Ö", "OE")
                .replace("Ä", "AE");

        return output;
    }
}
