package services;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.gridfs.GridFS;
import jakarta.enterprise.context.RequestScoped;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@RequestScoped
public class MongoDBService {

    //cooking collections
    public static final String NEWS_ARTICLES_COLLECTION = "news_collection";

    private Logger logger = LoggerFactory.getLogger(MongoDBService.class);

    public <C> List<C> getCollection(String collectionName, Class<C> clazz) {

        List<C> result = new ArrayList();
        try (MongoClient mongoClient = createClient()) {
            MongoDatabase db = mongoClient.getDatabase("my-db");
            return db.getCollection(collectionName, clazz).find().into(result);
        }
    }

    private MongoClient createClient() {
        ConnectionString connectionString = new ConnectionString("mongodb://host.docker.internal:27017");
        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                pojoCodecRegistry);
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .build();
        return MongoClients.create(clientSettings);
    }

    public <C> void insertOne(String collectionName, C b, Class<C> clazz) {
        try (MongoClient mongoClient = createClient()) {
            MongoDatabase db = mongoClient.getDatabase("my-db");
            MongoCollection col = db.getCollection(collectionName, clazz);
            col.insertOne(b);
        }
    }

    public UpdateResult updateOne(String collectionName, Bson filter, Bson removeElements) {
        try (MongoClient mongoClient = createClient()) {
            MongoDatabase db = mongoClient.getDatabase("my-db");
            MongoCollection col = db.getCollection(collectionName);
            return col.updateOne(filter, removeElements);
        }
    }

    public DeleteResult deleteMany(String collectionName, Bson datum) {
        try (MongoClient mongoClient = createClient()) {
            MongoDatabase db = mongoClient.getDatabase("my-db");
            MongoCollection col = db.getCollection(collectionName);
            return col.deleteMany(datum);
        }
    }

    public ObjectId saveImage(String urlString) {

        try (MongoClient mongoClient = createClient()) {
            MongoDatabase db = mongoClient.getDatabase("my-db");

            // get image file from local drive
            GridFSBucket gridFSBucket = GridFSBuckets.create(db);
            try {
                // Verbindung zur URL herstellen
                URL url = new URL(urlString);
                URLConnection connection = url.openConnection();

                // InputStream zum Lesen des Bilds
                try (InputStream inputStream = connection.getInputStream()) {
                    // FileOutputStream zum Schreiben des Bilds
                    // Bild im GridFS speichern und die Object-ID erhalten
                    ObjectId fileId;
                    logger.info(String.format("Saving image %s...",url.getFile()));
                    try (GridFSUploadStream uploadStream = gridFSBucket.openUploadStream("image.png")) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            uploadStream.write(buffer, 0, bytesRead);
                        }
                        uploadStream.close();
                        fileId = uploadStream.getObjectId();
                        logger.info("Image saved in MOngo DB");
                        return fileId;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public List<File> getImages() {
        // Bild aus GridFS laden
        List<File> fileList = new ArrayList<>();
        logger.info("Returning list of saved Images");
        try (MongoClient mongoClient = createClient()) {
            MongoDatabase db = mongoClient.getDatabase("my-db");
            GridFSBucket gridFSBucket = GridFSBuckets.create(db);
            Document metadataFilter = new Document();

            // GridFSFindIterable erstellen, um alle Dateien zu filtern
            GridFSFindIterable iterable = gridFSBucket.find(metadataFilter);
            for (GridFSFile file : iterable) {
                // Erstelle ein temporäres File-Objekt zum Speichern der geladenen Datei
                File tempFile = File.createTempFile("gridfs_image_", ".png");
                try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                    gridFSBucket.downloadToStream(file.getObjectId(), outputStream);
                    fileList.add(tempFile);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileList;
    }
}
