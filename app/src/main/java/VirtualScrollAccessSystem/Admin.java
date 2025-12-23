package VirtualScrollAccessSystem;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;

import java.util.List;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import org.bson.Document;

public class Admin {
    private static final MongoCollection<Document> collection = Database.getUserProfiles();

    public static String[] ListofUsers(MongoCollection<Document> target_collection){
        //returns list of usernames
        List<String> names = new ArrayList<>();
        for(Document doc : target_collection.find()){
            String name = doc.getString("_id");
            if(name != null && doc.getString("type").equals("Member")){
                names.add(name);
            }
        }
        return names.toArray(new String[0]);

    }

    public static String DeleteUser(MongoCollection<Document> targetCollection, String DeleteName){
        //returns error message   Or, "Successful" 
        try{
            Document DeletedUser = new Document("_id", DeleteName);
            DeleteResult result = targetCollection.deleteOne(DeletedUser);

            // Remove all scrolls associated with the user
            if (targetCollection.getNamespace().getCollectionName().equals("user_profiles")){
                GridFSBucket scrollBucket = Database.getScrolls();
                for (GridFSFile file: scrollBucket.find(Filters.eq("metadata.uploader_id", DeleteName))){
                    scrollBucket.delete(file.getObjectId());
                }
            }

            if (result.getDeletedCount() > 0) {
                if(targetCollection.getNamespace().getCollectionName().equals("user_profiles")){
                    Database.writeToLog("User " + DeleteName + "has been deleted.");;
                }
                return "Successful";
            } else {
                Database.writeToLog("...Unsuccessful, user not found");
                return "Error: User not found";
            }
            
        } catch (Exception e){
            return "Error: " + e.getMessage();
        }

    }

    public static String DeleteUser(String DeleteName){
        return DeleteUser(collection, DeleteName);

    }

    public static String[] logs(){
        ArrayList<String> logContent = new ArrayList<String>();
        try{
            List<String> lines = Files.readAllLines(Paths.get("src\\main\\resources\\log.txt"));
            for (String line: lines) {
                logContent.add(line);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return logContent.toArray(new String[0]);
    }
}

