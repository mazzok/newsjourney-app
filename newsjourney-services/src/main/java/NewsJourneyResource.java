import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.ChatGPTAPIService;
import services.MidjourneyAPIService;
import services.NewsArticle;
import services.NewsLineFetchService;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Path("/newsjourney")
public class NewsJourneyResource {

    @Inject
    private MidjourneyAPIService midjourneyAPIService;

    @Inject
    private ChatGPTAPIService chatGPTAPIService;

    @Inject
    private NewsLineFetchService newsLineFetchService;

    private Logger logger = LoggerFactory.getLogger(NewsJourneyResource.class);

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response listToday() {
        try {
            List<NewsArticle> l = newsLineFetchService.fetchNews();

            l.subList(0,5).stream().forEach(na -> {
                String response = chatGPTAPIService.chat(na.getDescription(), 5);
                logger.info("Recived answer from chat GPT:" + response);
                //feed as prompt for Midhourney

                //String midJopurneyResponse = midjourneyAPIService.prompt(response);

                String formattedResponse = response.contains("_X_") ? response.split("_X_")[1] : response;
               na.setChatgptdescription(formattedResponse);
            });
            ObjectMapper mapper = new ObjectMapper();

            return Response.ok(mapper.writeValueAsString(l.subList(0,5))).build();
        } catch (Exception e) {
            return RestResponse.ResponseBuilder.serverError().entity(e).build().toResponse();
        }
    }

    private String findPattern(String input, String patternString) {
        Pattern pattern = Pattern.compile(patternString, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);

        if (matcher.matches()) {
            return matcher.group(matcher.groupCount());
        }
        return input;
    }
}
