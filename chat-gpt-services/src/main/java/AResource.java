import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/test")
public class AResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        return "Quarkus  services: Version: 1.0.0";
    }
}
