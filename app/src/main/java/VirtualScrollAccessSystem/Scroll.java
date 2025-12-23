package VirtualScrollAccessSystem;

import com.mongodb.client.gridfs.*;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import static com.mongodb.client.model.Updates.inc;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.bson.conversions.Bson;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.*;

public class Scroll {
    public static GridFSBucket scrollBucket = Database.getScrolls();

    public static String[][] myScrolls(GridFSBucket bucket, String userID) { 
        if (userID == null || userID.isEmpty()) {
            System.err.println("User ID cannot be null.");
            return new String[0][0];
        }

        System.err.println("MYSCROLLS: Given userID -> " + userID);
    
        // Bson filter = Filters.eq("metadata.uploader_id", userID);
        List<String[]> scrollList = new ArrayList<>();
    
        try (MongoCursor<GridFSFile> cursor = bucket.find().iterator()) { // Originally .find(filter).iterator()
            while (cursor.hasNext()) {
                GridFSFile file = cursor.next();
                Document metadata = file.getMetadata();
    
                if (metadata != null) {
                    // Extract scroll information
                    String scrollID = metadata.getString("scroll_id"); // This is giving me its name
                    String name = metadata.getString("name");
                    String uploader_id = metadata.getString("uploader_id");
                    Date lastUpdated = metadata.getDate("last_updated");
                    String lastUploadedStr = edit_date(metadata.getDate("uploaded").toString());
    
                    String lastUpdatedStr = lastUpdated != null ? lastUpdated.toString() : "";
    
                    System.err.println(">>> Looking at scroll with name" + scrollID);
                    System.err.println(">>> Scroll Uploader ID: " + uploader_id);
                    if (uploader_id.equals(userID)) {
                        System.err.println("MYSCROLLS: UserID matches with this scroll's uploader ID");
                        scrollList.add(new String[]{scrollID, name, lastUploadedStr});
                    } else {
                        System.err.println("Current uploader id '" + uploader_id + "' does not match with '" + userID + "'");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error retrieving scrolls: " + e.getMessage());
            return new String[0][0]; // Return empty array in case of error
        }

        String[][] result = new String[scrollList.size()][];
        System.err.println("Scrolls list size is: " + scrollList.size());
        for (int i = 0; i < scrollList.size(); i++) {
            result[i] = scrollList.get(i);
        }
    
        return result;
    }

    public static String[][] myScrolls(String userID) { // Faiyad specified to have parameter username, but scrolls store userID not username so I just used that instead
        return myScrolls(scrollBucket, userID);
    }

    public static String[][] searchScroll(String searchUserID, String searchScrollName, String searchScrollID, String searchUploadDate) {
        return searchScroll(scrollBucket, searchUserID, searchScrollName, searchScrollID, searchUploadDate);
    }

    public static String[][] searchScroll(GridFSBucket bucket, String searchUserID, String searchScrollID, String searchScrollName, String searchUploadDate) {

        String[] params = {searchUserID, searchScrollID, searchScrollName, searchUploadDate};
        boolean isAllNull = true;
    
        System.err.println("Checking if given parameters are null values");
        
        for (String param : params) {
            if (param != null && !param.isEmpty()) {
                isAllNull = false;
                System.err.println(" --- Given parameter >" + param + "< is not a null or empty value.");
            } else {
                System.err.println(" +++ Given parameter is a null or empty value");
            }
        }
    
        if (isAllNull) {
             // System.err.println("All given parameters are null. Returning empty grid.");
            // return new String[0][0];

            System.err.println("All given parameters are null. Returning a view of all scrolls");
            String[][] viewAll = viewAllScroll(bucket);
            return viewAll;
        }
    
        List<String[]> scrollList = new ArrayList<>();
    
        try (MongoCursor<GridFSFile> cursor = bucket.find().iterator()) {
            while (cursor.hasNext()) {
                GridFSFile file = cursor.next();
                Document metadata = file.getMetadata();
    
                if (metadata != null) {
                    // Extract scroll information
                    String scrollID = metadata.getString("scroll_id");
                    String name = metadata.getString("name");
                    String uploader_id = metadata.getString("uploader_id");
                    Date lastUpdated = metadata.getDate("last_updated");
                    String uploadDateStr = edit_date(metadata.getDate("uploaded").toString());
                    String downloadNum = metadata.getInteger("downloads").toString();
    
                    String lastUpdatedStr = lastUpdated != null ? lastUpdated.toString() : "";
    
                    System.err.print("UPLOAD DATE IS " + uploadDateStr);
    
                    System.err.println(">>> Looking at scroll with ID " + scrollID);
    
                    String[] scrollParams = {uploader_id, scrollID, name, uploadDateStr};

                    System.err.println("> SCROLL UPLOADER ID: " + uploader_id);
                    System.err.println("> SCROLL ID: " + scrollID);
                    System.err.println("> SCROLL NAME: " + name);
                    System.err.println("> SCROLL UPLOAD DATE: " + uploadDateStr);

    
                    boolean matches = true;
    
                    for (int i = 0; i < 4; i++) {
                        if (params[i] == null || params[i].isEmpty()) {
                            // Parameter not specified, skip
                            continue;
                        } else {
                            String searchParam = params[i].toLowerCase();
                            String targetParam = scrollParams[i].toLowerCase();
    
                            System.err.println("Comparing given param >" + searchParam + "< to target param >" + targetParam + "<");
    
                            if (searchParam.length() > 0 && targetParam.contains(searchParam)) {
                                // Parameter matches, continue checking other parameters
                                System.err.println("MATCH: Search parameter is in target parameter.");
                            } else {
                                System.err.println("Parameters do not match.");
                                matches = false;
                                break; // No need to check further parameters
                            }
                        }
                    }
    
                    if (matches) {
                        // All specified parameters matched
                        scrollList.add(new String[]{uploader_id, scrollID, name, uploadDateStr, downloadNum});
                        System.err.println("+++ All specified parameters matched. Adding scroll to list.");
                    } else {
                        System.err.println("--- Not all specified parameters matched scroll details. Disregarding this scroll.");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error retrieving scrolls: " + e.getMessage());
            return new String[0][0]; // Return empty array in case of error
        }
    
        String[][] result = new String[scrollList.size()][];
        System.err.println("= = = = = = = = = = = Scrolls list size is: " + scrollList.size() + " = = = = = = = = = = =");
        for (int i = 0; i < scrollList.size(); i++) {
            result[i] = scrollList.get(i);
        }
    
        return result;
    }
    

    public static String addScroll(String userID, String scrollName, File fileContents){
        return addScroll(scrollBucket, userID, scrollName, fileContents);
    }

    public static String addScroll(GridFSBucket bucket, String userID, String scrollName, File fileContents){

        if (bucket.getBucketName() == "scrolls") {
            Database.writeToLog("Adding scroll | User ID: " + userID + " | Scroll Name: " + scrollName);
        }
        if (userID.isEmpty()){
            return "User ID cannot be empty.";
        }
        else if(scrollName.isEmpty()){
            return "Scroll name cannot be empty.";
        }
        else if(fileContents == null){
            return "Please upload a file.";
        }
        GridFSFile existingScroll = scrollBucket.find(Filters.eq("metadata.name", scrollName)).first();
        if (existingScroll != null) {
            return "A scroll with this name already exists. Please choose a different name.";
        }
        try (FileInputStream uploadStream = new FileInputStream(fileContents)){
            Document metadata = new Document("name", scrollName)
                                    .append("uploader_id", userID)
                                    .append("downloads", 0)
                                    .append("last_updated", new Date())
                                    .append("uploaded", new Date());

            GridFSUploadOptions options = new GridFSUploadOptions().metadata(metadata);
            ObjectId scrollId = bucket.uploadFromStream(scrollName, uploadStream, options);

             // Update the metadata with scroll_id
            Document updatedMetadata = new Document("$set", new Document("metadata.scroll_id", scrollId.toHexString()));

            MongoCollection<Document> filesCollection = Database.getDatabase().getCollection(bucket.getBucketName() + ".files");

            // Perform update on the GridFS file metadata
            filesCollection.updateOne(Filters.eq("_id", scrollId), updatedMetadata);
            incrementUserUploadCount(bucket, userID);
            return "Success";
        }
        catch(IOException e){
            return "Error uploading scroll: " + e.getMessage();
        }
    }

    public static String deleteScroll(String scroll_id){

        return deleteScroll(scrollBucket, scroll_id);
    }

    public static String deleteScroll(GridFSBucket scrollBucket, String scroll_id){
        if (scrollBucket.getBucketName() == "scrolls") {
            Database.writeToLog("Deleting scroll | Scroll ID: " + scroll_id);
        }
        if (scroll_id.isEmpty()) {
            return "Error: Scroll ID cannot be empty.";
        }
        try{
            GridFSFile scrollToDelete = scrollBucket.find(Filters.eq("metadata.scroll_id", scroll_id)).first();
            if (scrollToDelete == null) {
                return "Error: Scroll not found.";
            }
            scrollBucket.delete(scrollToDelete.getObjectId());
            return "Success";
        }
        catch (IllegalArgumentException e) {
            return "Error: Invalid Scroll ID format."; 
        }catch (Exception e) {
            return "Error deleting scroll: " + e.getMessage();
        }
    }

    public static String edit_date(String date){

        String[] date_part = date.split(" ");
        return date_part[1] + " " + date_part[2] + ", 2024";

    }

    public static String[][] viewAllScroll(){
        return viewAllScroll(scrollBucket);
    }

    public static String[][] viewAllScroll(GridFSBucket scrollBucket){

        MongoCursor<GridFSFile> cursor = scrollBucket.find().iterator();
        List<String[]> scrollDetails = new ArrayList<>();
        //GridFSFile gridFSFile = null;
        //ObjectId uploadedFileId = null;
        while (cursor.hasNext()) {
            GridFSFile scroll = cursor.next();
            Document metadata = scroll.getMetadata();
            if (metadata != null) {
                String name = metadata.getString("name");
                //gridFSFile = scrollBucket.find(new org.bson.Document("metadata.name", name)).first();
                //uploadedFileId = gridFSFile.getObjectId();
                //String id = uploadedFileId.toHexString(); 
                String id = metadata.getString("scroll_id"); 
                String uploaderId = metadata.getString("uploader_id"); 
                String Updated = edit_date(metadata.getDate("uploaded").toString());
                String downloads = metadata.getInteger("downloads").toString();
                scrollDetails.add(new String[]{uploaderId, id, name, Updated, downloads});
            }
        }
        String[][] result = new String[scrollDetails.size()][4];
        return scrollDetails.toArray(result);
        
    }


    public static File viewScroll(String scrollId) {
        return viewScroll(scrollBucket, scrollId);
    }
    

    public static File viewScroll(GridFSBucket bucket, String scrollId) {
        if (bucket.getBucketName() == "scrolls") {
            Database.writeToLog("Viewing scroll with Scroll ID: " + scrollId);
        }
        GridFSFile file = bucket.find(Filters.eq("metadata.scroll_id", scrollId)).first();
        File downloadedFile = new File(file.getMetadata().getString("name"));

        // Create the output stream to save the file's binary content
        try (FileOutputStream outputStream = new FileOutputStream(downloadedFile)) {
            // Download the file to the output stream
            bucket.downloadToStream(file.getObjectId(), outputStream);
        } catch (Exception e) {
            System.err.println("Error downloading scroll: " + e.getMessage());
            return null;
        }
        downloadedFile.deleteOnExit();
    
        return downloadedFile; // Return the downloaded file
    }

    public static String getFileName(String scrollId){
        GridFSFile file = scrollBucket.find(Filters.eq("metadata.scroll_id", scrollId)).first();
        return file.getMetadata().getString("name").split(".txt")[0];
    }
    

    public static String editScroll(String scrollId, String newName, File updatedScroll){
        return editScroll(scrollBucket, scrollId, newName, updatedScroll);

    }

    public static String editScroll(GridFSBucket bucket, String scrollId, String newName, File updatedScroll){
        if (bucket.getBucketName() == "scrolls") {
            Database.writeToLog("Editing scroll | Scroll ID: " + scrollId + " | New Scroll Name: " + newName);
        }
        if (scrollId == null){
            return "Scroll ID cannot be null.";
        }
        if (scrollId.isEmpty()){
            return "Scroll ID cannot be empty.";
        }
        if (newName.isEmpty()){
            return "New scroll name cannot be empty.";
        }
        if (updatedScroll == null){
            return "Please upload the updated scroll.";
        }
        try {
            // Convert scrollId to ObjectId
            ObjectId objectId = new ObjectId(scrollId);
            
            // Check if the scroll exists
            GridFSFile existingFile = bucket.find(Filters.eq("metadata.scroll_id", scrollId)).first();
    
            // Check if a scroll with the new name already exists (if the name is changed)
            if (!existingFile.getMetadata().getString("name").equals(newName)) {
                GridFSFile newNameCheck = bucket.find(Filters.eq("metadata.name", newName)).first();
                if (newNameCheck != null) {
                    return "A scroll with this name already exists. Please choose a different name.";
                }
            }
    
            // Delete the old scroll
            bucket.delete(existingFile.getObjectId());

            // Upload the updated scroll
            try (FileInputStream uploadStream = new FileInputStream(updatedScroll)) {
                Document metadata = new Document("name", newName + ".txt")
                        .append("uploader_id", existingFile.getMetadata().getString("uploader_id"))
                        .append("downloads", existingFile.getMetadata().getInteger("downloads"))
                        .append("last_updated", new Date())
                        .append("uploaded", existingFile.getMetadata().getDate("uploaded"))
                        .append("scroll_id", existingFile.getMetadata().getString("scroll_id"));
    
                GridFSUploadOptions options = new GridFSUploadOptions().metadata(metadata);
    
                // Overwrite the existing file
                bucket.uploadFromStream(newName, uploadStream, options);
                return "Scroll updated successfully.";
            }
        } catch (IOException e) {
            return "Error uploading updated scroll: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "Invalid Scroll ID: " + scrollId;
        }

    }

    public static String userScrollStat(String scrollId){
        return userScrollStat(scrollBucket, scrollId);
    }

    public static String userScrollStat(GridFSBucket bucket, String scrollId){
        if (bucket.getBucketName() == "scrolls") {
            Database.writeToLog("Generating User Scroll Statistics for Scroll ID: " + scrollId);
        }
        GridFSFile scroll = bucket.find(Filters.eq("_id", new ObjectId(scrollId))).first();
        if (scroll == null){
            return "Scroll not found.";
        }
        int downloads = scroll.getMetadata().getInteger("downloads");
        return String.valueOf(downloads);
    }

    public static String updateDownloadCount(String scrollId){
        return updateDownloadCount(Database.getScrollFiles(), scrollId);
    }

    public static String updateDownloadCount(MongoCollection<Document> scrollFiles, String scrollId){
        if (scrollFiles.find(Filters.eq("metadata.scroll_id", scrollId)).first() == null){
            return "Scroll not found.";
        }
        scrollFiles.updateOne(Filters.eq("metadata.scroll_id", scrollId), inc("metadata.downloads", 1));
        return "Success";
    }

    public static void incrementUserUploadCount(GridFSBucket bucket, String id){
        MongoCollection<Document> db = null;
        if(bucket.getBucketName().equals("scrolls")){
            db = Database.getUserProfiles();
        }
        else if (bucket.getBucketName().equals("scrolls_testing")){
            db = Database.getUserProfilesTesting();
        }
        Document userDoc = db.find(Filters.eq("_id", id)).first();
        db.updateOne(Filters.eq("_id", id), inc("uploaded_scrolls", 1));
        return;
    }

    public static String incrementUserDownloadCount(String id){
        Document userDoc = Database.getUserProfiles().find(Filters.eq("_id", id)).first();
        Database.getUserProfiles().updateOne(Filters.eq("_id", id), inc("downloaded_scrolls", 1));
        return "Success";
    }
}
