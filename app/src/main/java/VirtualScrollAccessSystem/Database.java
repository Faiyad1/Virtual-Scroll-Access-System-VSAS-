package VirtualScrollAccessSystem;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.*;
import com.mongodb.client.result.DeleteResult;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Database {

    public static MongoDatabase getDatabase(){
        String url = "mongodb+srv://user:group02@cluster0.ww88i.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
        MongoClient mongoClient = MongoClients.create(url);
        MongoDatabase database = mongoClient.getDatabase("VSAM");
        return database;
    }

    public static MongoCollection<Document> getUserProfiles(){
        MongoCollection<Document> collection = getDatabase().getCollection("user_profiles");
        return collection;
    }

    public static MongoCollection<Document> getUserProfilesTesting(){
        MongoCollection<Document> collection = getDatabase().getCollection("user_profiles_testing");
        return collection;
    }

    public static GridFSBucket getScrolls(){

        GridFSBucket scrolls = GridFSBuckets.create(getDatabase(), "scrolls");
        return scrolls;
    }

    public static GridFSBucket getScrollsTesting(){
        GridFSBucket scrolls_testing = GridFSBuckets.create(getDatabase(), "scrolls_testing");
        return scrolls_testing;
    }

    public static GridFSBucket getProfilePictures(){
        GridFSBucket pics = GridFSBuckets.create(getDatabase(), "profile_pictures");
        return pics;
    }

    public static void writeToLog(String text) {

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String logEntry = now.format(formatter) + " -> " + text;

        try {
            File logFile = new File("src/main/resources/log.txt");

            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write(logEntry);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GridFSBucket getProfilePicturesTesting(){
        GridFSBucket pics = GridFSBuckets.create(getDatabase(), "profile_pictures_testing");
        return pics;
    }

    public static MongoCollection<Document> getScrollFiles(){
        MongoCollection<Document> scrollsCollection = getDatabase().getCollection("scrolls.files");
        return scrollsCollection;
    }

    public static MongoCollection<Document> getScrollFilesTesting(){
        MongoCollection<Document> scrollsCollection = getDatabase().getCollection("scrolls_testing.files");
        return scrollsCollection;
    }
}
