import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/newsjourney")
public class NewsJourneyResource {

    @Inject
    private MidjourneyAPIService midjourneyAPIService;

    @Inject
    private ChatGPTAPIService chatGPTAPIService;

    @Inject
    private MongoDBService mongoDBService;

    @Inject
    private NewsLineFetchService newsLineFetchService;

    private Logger logger = LoggerFactory.getLogger(NewsJourneyResource.class);

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response listToday() {
        try {
            List<NewsArticle> l = newsLineFetchService.fetchNews();

            //List<File> files = mongoDBService.getImages();


            //Files.copy(new FileInputStream(files.get(0)),Paths.get("D:\\GIT\\newsjourney-app\\downladImage.png"));

            l.stream().forEach(na -> {
                String response = chatGPTAPIService.chat(na.getDescription(), 5);
                logger.info("Recived answer from chat GPT:" + response);
                //feed as prompt for Midhourney

                //String midJopurneyResponse = midjourneyAPIService.prompt(response);

                String formattedResponse = response.contains("_X_") ? response.split("_X_")[1] : response;
                na.setChatGPTdescription(formattedResponse);
                ObjectId imageID = mongoDBService.saveImage("https://cdn.discordapp.com/attachments/981697600023560262/1158852460975243324/bluesal_Kurdish_woman_with_blonde_hair_and_blue_eyes_e9c56028-38af-47ce-8b78-4bed07cf7298.png");

                na.setMidjourneyMongoImageID(imageID);
            });
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            List<NewsArticle> persistedList = mongoDBService.getCollection(MongoDBService.NEWS_ARTICLES_COLLECTION, NewsArticle.class);

            //insert new articles
            l.stream().filter(na -> !persistedList.contains(na)).forEach(na -> {
                mongoDBService.insertOne(MongoDBService.NEWS_ARTICLES_COLLECTION, na, NewsArticle.class);
            });

            return Response.ok(mapper.writeValueAsString(l)).build();
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
