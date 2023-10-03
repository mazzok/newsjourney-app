import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.ChatGPTAPIService;
import services.MidjourneyAPIService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/newsjourney")
public class NewsJourneyResource {

    @Inject
    private MidjourneyAPIService midjourneyAPIService;

    @Inject
    private ChatGPTAPIService chatGPTAPIService;

    private Logger logger = LoggerFactory.getLogger(NewsJourneyResource.class);

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response get(@QueryParam("content") String content, @QueryParam("role") String role) {
        if (content != null) {
            try {
                int retryCounter = 5;
                String response = "No content!";
                boolean validAnswerReceived = false;
                while (!validAnswerReceived && retryCounter > 0) {

                    try {

                        response = chatGPTAPIService.chatGPT(content);
                        validAnswerReceived = true;
                    } catch (Exception e) {
                        retryCounter--;
                        logger.info(String.format("Received %s from Chatgpt API service, trying again %s more times", e.getMessage(), retryCounter));
                    }
                }

                logger.info("Recived answer:" + response);
                //feed as prompt for Midhourney

                //String midJopurneyResponse = midjourneyAPIService.prompt(response);

                String formattedResponse = response.contains("_X_")? response.split("_X_")[1] : response;

                return Response.ok(formattedResponse).build();
            } catch (Exception e) {
                return RestResponse.ResponseBuilder.serverError().entity(e).build().toResponse();
            }
        }
        return Response.noContent().build();

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
