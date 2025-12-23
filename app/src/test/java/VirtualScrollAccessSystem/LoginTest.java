package VirtualScrollAccessSystem;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;

import org.bson.Document;

public class LoginTest {
    
    public static final MongoCollection<Document> testCollection = Database.getUserProfilesTesting();

    @Test
    public void addUserTest(){

        // Create dummy existing user
        Login.addUser(testCollection, "43110", "Varrent", "varrentnw@gmail.com", "0426400656", "Member", "password", "password");

        //Test empty fields
        assertEquals(Login.addUser(testCollection, "", "", "", "", "", "", ""), "At least one field is empty.");

        // Test ID contains spaces
        assertEquals(Login.addUser(testCollection, "user id", "John", "john@gmail.com", "123455", "Member", "password", "password"), "ID cannot contain spaces.");

        // Check email format
        assertEquals(Login.addUser(testCollection, "userid123", "John", "thisisnotemail", "123456", "Member", "password", "password"), "Email is not in a valid format.");

        // Check phone number format
        assertEquals(Login.addUser(testCollection, "userid123", "John", "john@gmail.com", "phonenumber", "Member", "password", "password"), "Phone number is not in a valid format");

        // Check password contains spaces
        assertEquals(Login.addUser(testCollection, "userid123", "John", "john@gmail.com", "12345678", "Member", "pass word", "pass word"), "Password cannot contain spaces.");

        // New user, password retype incorrect
        assertEquals(Login.addUser(testCollection, "user123", "John", "john@gmail.com", "123456", "Member", "password", "pwd"), "Password confirmation is not the same as password.");

        // Email is already in use
        assertEquals(Login.addUser(testCollection, "userid123", "John", "varrentnw@gmail.com", "1234555", "Member", "password", "password"), "Email is already used. Please enter another email.");

        // Phone number is already in use
        assertEquals(Login.addUser(testCollection, "userid123", "John", "john@gmail.com", "0426400656", "Member", "password", "password"), "Phone number is already used. Please enter another phone number.");

        // Insert new user
        assertEquals(Login.addUser(testCollection, "userid123", "John", "john@gmail.com", "12345678", "Member", "password", "password"), "Success");

        // Check if new user is inserted successfully
        Document query = new Document("_id", "userid123");
        assertTrue(testCollection.countDocuments(query) > 0);

        // Check if database fields are correctly inserted
        Document userDocument = testCollection.find(query).first();
        assertEquals(userDocument.getString("_id"), "userid123");
        assertEquals(userDocument.getString("full_name"), "John");
        assertEquals(userDocument.getString("email"), "john@gmail.com");
        assertEquals(userDocument.getString("phone"), "12345678");
        assertEquals(userDocument.getString("type"), "Member");
        assertEquals(userDocument.getInteger("uploaded_scrolls"), 0);
        assertEquals(userDocument.getInteger("downloaded_scrolls"), 0);
        assertEquals(Encryptor.decrypt(userDocument.getString("encrypted_password")), "password");

        // Check if database fields are correctly inserted via viewDetails method
        String[] details = Login.viewDetails(testCollection, "userid123");
        assertEquals(details[0], "John");
        assertEquals(details[1], "john@gmail.com");
        assertEquals(details[2], "12345678");
        assertEquals(details[3], "0");
        assertEquals(details[4], "0");

        // Test inserting a user with the same ID and different email and phone
        assertEquals(Login.addUser(testCollection, "userid123", "Jane", "jane@gmail.com", "543210", "Member", "password", "password"), "ID is already used. Please enter another ID.");

         // Delete new user
         testCollection.deleteOne(query);
     }

    @Test
    public void loginTest(){

        // User not found
        assertEquals(Login.userLogin(testCollection, "anotherID", "somepassword"), "User not found.");

        // Create dummy user
        Login.addUser(testCollection, "userid123", "John", "john@gmail.com", "12345678", "Member", "password", "password");
    
        // Incorrect password
        assertEquals(Login.userLogin(testCollection,"userid123", "wrongpassword"), "Incorrect password.");

        // Successful login
        assertEquals(Login.userLogin(testCollection, "userid123", "password"), "Member");

        // Delete dummy user
        Document query = new Document("_id", "userid123");
        testCollection.deleteOne(query);
    }

    @Test
    public void testUpdateDetails(){

        // Create dummy user
        Login.addUser(testCollection, "userid123", "John", "john@gmail.com", "12345678", "Member", "password", "password");
        Login.addUser(testCollection, "43110", "Varrent", "varrentnw@gmail.com", "0426400656", "Member", "password", "password");

        // ID uniqueness
        assertEquals(Login.updateDetails(testCollection, "userid123", "43110", "John", "john@gmail.com", "12345678", "password"), "New ID is already in use. Please enter a different ID.");

        // Email already in use
        assertEquals(Login.updateDetails(testCollection, "userid123", "userid123", "John", "varrentnw@gmail.com", "12345678", "password"), "New email is already in use.");

        // Phone already in use
        assertEquals(Login.updateDetails(testCollection, "userid123", "userid123", "John", "john@gmail.com", "0426400656", "password"), "New phone number is already in use.");

        // Test changing some fields
        assertEquals(Login.updateDetails(testCollection, "userid123", "userid123", "John Doe", "john@gmail.com", "543210", "password"), "Success");

        // Incorrect password
        assertEquals(Login.updateDetails(testCollection, "userid123", "anotherid123", "John", "anotheremail@gmail.com", "543210", "wrongpassword"), "Password is incorrect. Try again.");

        // Update details
        assertEquals(Login.updateDetails(testCollection, "userid123", "anotherid123", "John", "anotheremail@gmail.com", "543210", "password"), "Success");

        // Check if details are updated
        Document query = new Document("_id", "anotherid123");
        Document userDocument = testCollection.find(query).first();
        assertEquals(userDocument.getString("email"), "anotheremail@gmail.com");
        assertEquals(userDocument.getString("phone"), "543210");

        // Delete dummy user
        testCollection.deleteOne(query);
        testCollection.deleteOne(new Document("_id", "43110"));
    }

    @Test
    public void testUpdatePassword(){

        // Create dummy user
        Login.addUser(testCollection, "userid123", "John", "john@gmail.com", "12345678", "Member", "password", "password");

        // Password incorrect
        assertEquals(Login.updatePassword(testCollection, "userid123", "wrongpw", "newpw", "newpw"), "Password is incorrect. Try again.");

        // New password contains spaces
        assertEquals(Login.updatePassword(testCollection, "userid123", "password", "new pw", "new pw"), "New password cannot contain spaces.");

        // New password is the same as old password
        assertEquals(Login.updatePassword(testCollection, "userid123", "password", "password", "password"), "New password must be different from the current password.");

        // Confirm password is not the same as new password
        assertEquals(Login.updatePassword(testCollection, "userid123", "password", "newpw", "wrongpw"), "New password confirmation is not the same as new password.");

        // Password successfully updated
        assertEquals(Login.updatePassword(testCollection, "userid123", "password", "newpw", "newpw"), "Success");

        // Check new password
        Document query = new Document("_id", "userid123");
        Document userDocument = testCollection.find(query).first();
        assertEquals(Encryptor.decrypt(userDocument.getString("encrypted_password")), "newpw");

        // Delete dummy user
        testCollection.deleteOne(query);
    }

    @Test
    public void testSaveProfilePic() throws IOException {
        String userId = "testUser123";
        File image = new File("test_image.jpg");  // Create a dummy image for testing

        // Create dummy image file (simulate uploading a real image)
        try (FileOutputStream fos = new FileOutputStream(image)) {
            fos.write(new byte[1024]);  // Write dummy data to the image file
        }

        // Call the saveProfilePic method and check result
        String result = Login.saveProfilePic(Database.getProfilePicturesTesting(), userId, image);
        assertEquals("Success", result);

        // Verify that the profile picture was uploaded to GridFS
        GridFSFile savedPic = Database.getProfilePicturesTesting().find(Filters.eq("metadata.user_id", userId)).first();
        assertNotNull(savedPic, "Profile picture should exist in GridFS.");

        // Clean up: delete the uploaded file from GridFS
        if (savedPic != null) {
            Database.getProfilePicturesTesting().delete(savedPic.getObjectId());
        }

        // Delete the dummy image file
        assertTrue(image.delete(), "Failed to delete dummy image file after test.");
    }

    @Test
    public void testGetProfilePic() throws IOException {
        String userId = "testUser123";
        File image = new File("test_image.jpg");

        // Create and save a dummy profile picture
        try (FileOutputStream fos = new FileOutputStream(image)) {
            fos.write(new byte[1024]);  // Write dummy data
        }
        Login.saveProfilePic(Database.getProfilePicturesTesting(), userId, image);

        // Call getProfilePic and check if the file is returned
        File retrievedImage = Login.getProfilePic(Database.getProfilePicturesTesting(), userId);
        assertNotNull(retrievedImage, "Profile picture should be retrieved.");
        assertTrue(retrievedImage.exists(), "The retrieved profile picture file should exist.");

        // Clean up: delete the profile picture from GridFS and the local file
        GridFSFile savedPic = Database.getProfilePicturesTesting().find(Filters.eq("metadata.user_id", userId)).first();
        if (savedPic != null) {
            Database.getProfilePicturesTesting().delete(savedPic.getObjectId());
        }
        assertTrue(retrievedImage.delete(), "Failed to delete retrieved profile picture after test.");

        // Delete the dummy image file
        assertTrue(image.delete(), "Failed to delete dummy image file after test.");
    }


}
