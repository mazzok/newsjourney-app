import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Path("/chat")
public class ChatGPTResource {


    private Logger logger = LoggerFactory.getLogger(ChatGPTResource.class);
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response get(@QueryParam("content") String content, @QueryParam("role") String role) {
        if (content != null) {
            try {
                String response = ChatGPT.chatGPT(content,role != null ? role : "You are a helpful assistant");

                logger.info(response);
                //feed as prompt for Midhourney

                String midJopurneyResponse = MidjourneyAPI.prompt(response);
                return Response.ok(midJopurneyResponse).build();
            } catch (Exception e) {
              return RestResponse.ResponseBuilder.serverError().entity(e).build().toResponse();
            }
        }
        return Response.noContent().build();

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
