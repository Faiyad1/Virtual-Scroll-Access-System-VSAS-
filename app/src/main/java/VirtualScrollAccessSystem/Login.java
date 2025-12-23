package VirtualScrollAccessSystem;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Updates;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import static com.mongodb.client.model.Filters.eq;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.bson.conversions.Bson;

import org.bson.Document;

public class Login {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final MongoCollection<Document> collection = Database.getUserProfiles();
    private static final GridFSBucket pics = Database.getProfilePictures();
    private static final String productionCollectionName = "user_profiles";

    public static boolean isValidEmail(String email) {
        // Compile the regex pattern
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        // Match the email against the pattern
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static String addUser(String id, String fullName, String email, String phone, String type, String password, String confirmPassword){
        return addUser(collection, id, fullName, email, phone, type, password, confirmPassword);
    }

    public static String userLogin(String id, String password){
        return userLogin(collection, id, password);
    }

    public static String addUser(MongoCollection<Document> otherCollection, String id, String fullName, String email, String phone, String type, String password, String confirmPassword){
        if (otherCollection.getNamespace().getCollectionName() == productionCollectionName) {
            Database.writeToLog("A User has been added || Name: " + fullName + " | ID: " + id + " | Member Type: " + type);
        }

        // Check if any field is null
        if (id.isEmpty() || fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || type.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            return "At least one field is empty.";
        }

        // Check if ID contains spaces
        if (id.contains(" ")){
            return "ID cannot contain spaces.";
        }

        // Check if email is in the appropriate format
        if (!isValidEmail(email)){
            return "Email is not in a valid format.";
        }

        // Check if phone number is numeric
        try{
            int phoneInt = Integer.parseInt(phone);
        }
        catch(Exception e){
            return "Phone number is not in a valid format";
        }

        // Check if password contains spaces
        if (password.contains(" ")){
            return "Password cannot contain spaces.";
        }

        // Check if ID exists in database to ensure uniqueness
        Document query = new Document("_id", id);
        if (otherCollection.countDocuments(query) > 0){
            return "ID is already used. Please enter another ID.";
        }

        // Check if email is already used
        query = new Document("email", email);
        if (otherCollection.countDocuments(query) > 0){
            return "Email is already used. Please enter another email.";
        }

        // Check if phone number is already used
        query = new Document("phone", phone);
        if (otherCollection.countDocuments(query) > 0){
            return "Phone number is already used. Please enter another phone number.";
        }

        // Check if confirmPassword is the same as password
        if (!password.equals(confirmPassword)){
            return "Password confirmation is not the same as password.";
        }

        // Insert new user to database
        Document newUser = new Document("_id", id)
            .append("full_name", fullName)
            .append("email", email)
            .append("phone", phone)
            .append("uploaded_scrolls", 0)
            .append("downloaded_scrolls", 0)
            .append("encrypted_password", Encryptor.encrypt(password))
            .append("type", type);

        otherCollection.insertOne(newUser);
        return "Success";
    }

    public static String userLogin(MongoCollection<Document> otherCollection, String id, String password){
        Document query = new Document("_id", id);
        Document userDocument = otherCollection.find(query).first();
        if (userDocument != null) {
            // If the user is found, check if the password matches
            if (Encryptor.decrypt(userDocument.getString("encrypted_password")).equals(password)) {
                // Get the user type and return appropriate response
                String userType = userDocument.getString("type");
                if (otherCollection.getNamespace().getCollectionName() == productionCollectionName) {
                    Database.writeToLog("User >" + id + "< has logged in.");
                }
                return userType;
            } else {
                return "Incorrect password.";
            }
        } else {
            return "User not found.";
        }
    }
    
    public static String[] viewDetails(String id){
        return viewDetails(collection, id);
    }

    public static String[] viewDetails(MongoCollection<Document> otherCollection, String id){
        if (otherCollection.getNamespace().getCollectionName() == productionCollectionName) {
            Database.writeToLog("Viewing details:" + id);
        }
        Document query  = new Document("_id", id);
        Document userDocument = otherCollection.find(query).first();
        String[] details = new String[5];
        details[0] = userDocument.getString("full_name");
        details[1] = userDocument.getString("email");
        details[2] = userDocument.getString("phone");
        details[3] = Integer.toString(userDocument.getInteger("uploaded_scrolls"));
        details[4] = Integer.toString(userDocument.getInteger("downloaded_scrolls"));
        return details;
    }

    public static String updateDetails(String old_id, String new_id, String newFullName, String newEmail, String newPhone, String password){
        return updateDetails(collection, old_id, new_id, newFullName, newEmail, newPhone, password);
    }

    public static String updateDetails(MongoCollection<Document> otherCollection, String old_id, String new_id, String newFullName, String newEmail, String newPhone, String password){
        Document query  = new Document("_id", old_id);
        Document userDocument = otherCollection.find(query).first();

        // Check new_id is unique
        if (!old_id.equals(new_id)){
            Document newIDQuery = new Document("_id", new_id);
            if(otherCollection.countDocuments(newIDQuery) > 0){
                return "New ID is already in use. Please enter a different ID.";
            }
        }

        // Check newEmail is not already in use
        if (!userDocument.getString("email").equals(newEmail)){
            Document newEmailQuery = new Document("email", newEmail);
            if(otherCollection.countDocuments(newEmailQuery) > 0){
                return "New email is already in use.";
            }
        }
        
       // Check if email is in the appropriate format
        if (!isValidEmail(newEmail)){
        return "Email is not in a valid format.";
        } 

        // Check newPhone is not already in use
        if (!userDocument.getString("phone").equals(newPhone)){
            Document newPhoneQuery = new Document("phone", newPhone);
            if(otherCollection.countDocuments(newPhoneQuery) > 0){
                return "New phone number is already in use.";
            }
        }

        // Check if password confirmation is correct
        if(!Encryptor.decrypt(userDocument.getString("encrypted_password")).equals(password)){
            return "Password is incorrect. Try again.";
        }

        // Update fields
        Document update = new Document("$set", new Document("full_name", newFullName).append("email", newEmail).append("phone", newPhone));
        otherCollection.updateOne(query, update);

        // Create a new entry with the new ID
        userDocument = otherCollection.find(query).first();
        Document updatedDoc = new Document(userDocument);
        updatedDoc.put("_id", new_id);

        // Delete the old entry
        otherCollection.deleteOne(eq("_id", old_id));

        // Insert the new entry
        otherCollection.insertOne(updatedDoc);

        if (otherCollection.getNamespace().getCollectionName() == productionCollectionName) {
            Database.writeToLog("User " + old_id + " has updated their details" + " | New User ID: " + new_id + " | New Full Name: " + newFullName);
        }

        // Uppdate user ID of associated pfp, if any
        if(otherCollection.getNamespace().getCollectionName().equals("user_profiles")){
            if(Database.getProfilePictures().find(Filters.eq("metadata.user_id", old_id)).first() != null){
                Database.getDatabase().getCollection("profile_pictures.files").updateOne(Filters.eq("metadata.user_id", old_id), new Document("$set", new Document("metadata.user_id", new_id)));
            }

            // Update the uploader_id of the scrolls associated with this user
            Bson idUpdate = Updates.set("metadata.uploader_id", new_id);
            Database.getScrollFiles().updateMany(Filters.eq("metadata.uploader_id", old_id), idUpdate);
        }
        
        return "Success";
    }

    public static String updatePassword(String id, String old_pw, String new_pw, String confirm_pw){
       return updatePassword(collection, id, old_pw, new_pw, confirm_pw);
    }

    public static String updatePassword(MongoCollection<Document> otherCollection, String id, String old_pw, String new_pw, String confirm_pw){
        Document query = new Document("_id", id);
        Document userDocument = otherCollection.find(query).first();

        // Check if password is correct
        if (!Encryptor.decrypt(userDocument.getString("encrypted_password")).equals(old_pw)){
            return "Password is incorrect. Try again.";
        }

        // Check if new_pw does not contain spaces
        if (new_pw.contains(" ")){

            return "New password cannot contain spaces.";
        }

        // Check if new_pw is the same as old_pw
        if (new_pw.equals(old_pw)){
            return "New password must be different from the current password.";
        }

        // Check if confirm_pw is the same as new_pw
        if (!new_pw.equals(confirm_pw)){
            return "New password confirmation is not the same as new password.";
        }

        // Update password
        Document update = new Document("$set", new Document("encrypted_password", Encryptor.encrypt(new_pw)));
        otherCollection.updateOne(query, update);
        if (otherCollection.getNamespace().getCollectionName() == productionCollectionName) {
            Database.writeToLog("User " + id + " has updated their password");
        }
        return "Success";
    }

    public static String saveProfilePic(GridFSBucket bucket, String id, File image){

        if (bucket.getBucketName() == "scrolls") {
            Database.writeToLog("User >" + id + "< is saving profile picture...");
        }

        // Check if the user already has a profile picture and delete if so
        GridFSFile existingPic = bucket.find(Filters.eq("metadata.user_id", id)).first();
        if (existingPic != null){
            bucket.delete(existingPic.getObjectId());
        }
        

        // Upload the image to the database
        try (FileInputStream stream = new FileInputStream(image)){
            Document metadata = new Document("user_id", id);
            GridFSUploadOptions option = new GridFSUploadOptions().metadata(metadata);
            bucket.uploadFromStream(id + "_pfp", stream, option);
            if (bucket.getBucketName() == "scrolls") {
                Database.writeToLog("++ Profile picture saved! ++");
            }
            return "Success";
        }
        catch(IOException e){
            return "Error uploading picture " +e.getMessage();
        }
    }

    public static String saveProfilePic(String id, File image){
        return saveProfilePic(pics, id, image);
    }

    public static File getProfilePic(GridFSBucket bucket, String id){
        // Check if the user already has a profile picture
        GridFSFile existingPic = bucket.find(Filters.eq("metadata.user_id", id)).first();
        if (existingPic == null){
            return null;
        }
        File pfp = new File("pfp_" + id + ".bin");
        try(FileOutputStream fos = new FileOutputStream(pfp)){
            bucket.downloadToStream(existingPic.getObjectId(), fos);
        }
        catch (Exception e){;
            System.err.println("Error getting profile picture " + e.getMessage());
            return null;
        }
        pfp.deleteOnExit();
        return pfp;
    }

    public static File getProfilePic(String id){
        return getProfilePic(pics, id);
    }
}
