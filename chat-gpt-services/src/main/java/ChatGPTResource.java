import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/chat")
public class ChatGPTResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response get(@QueryParam("content") String content, @QueryParam("role") String role) {
        if (content != null) {
            try {
                String response = ChatGPT.chatGPT(content,role != null ? role : "You are a helpful assistant");
                return Response.ok(response).build();
            } catch (Exception e) {
              return RestResponse.ResponseBuilder.serverError().entity(e).build().toResponse();
            }
        }
        return Response.noContent().build();

    }
}
