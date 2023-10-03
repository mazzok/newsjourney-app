package services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.enterprise.context.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RequestScoped
public class NewsLineFetchService {

    private Logger logger = LoggerFactory.getLogger(NewsLineFetchService.class);

    public List<NewsArticle> fetchNews() throws URISyntaxException, IOException {

        String key = System.getenv("MEDIASTACK_API_KEY");
        Map<String, String> params = new HashMap<>();
        params.put("access_key", key);
        params.put("languages", "de");
        params.put("sort", "published_desc");
        params.put("countries", "at");

        String url = new URI("http",
                "api.mediastack.com",
                "/v1/news",
                params.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&")), null).toString();

        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("GET");

        logger.info("Making GET request to: " + url);

        con.setDoOutput(true);

        String output = new BufferedReader(new InputStreamReader(con.getInputStream())).lines()
                .reduce((a, b) -> a + b).get();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNode tree = mapper.readTree(output);
        JsonNode arrayNode = tree.get("data");
        List<NewsArticle> result = new ArrayList<>();
        if (arrayNode != null) {
            result = mapper.readValue(arrayNode.toString(), new TypeReference<List<NewsArticle>>() {
            });
        } else {
            result = Arrays.asList(mapper.readValue(tree.toString(), NewsArticle.class));
        }
        return result;
    }
}
