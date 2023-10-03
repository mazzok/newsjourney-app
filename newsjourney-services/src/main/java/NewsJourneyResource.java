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
                String response = chatGPTAPIService.chatGPT(content);//services.ChatGPT.chatGPT(content,role != null ? role : "You are a helpful assistant");

                logger.info(response);
                //feed as prompt for Midhourney

                //String midJopurneyResponse = midjourneyAPIService.prompt(response);
                return Response.ok(response).build();
            } catch (Exception e) {
              return RestResponse.ResponseBuilder.serverError().entity(e).build().toResponse();
            }
        }
        return Response.noContent().build();

    }
}
