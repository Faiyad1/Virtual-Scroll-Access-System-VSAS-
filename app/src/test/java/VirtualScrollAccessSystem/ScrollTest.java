package VirtualScrollAccessSystem;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class ScrollTest {

    private GridFSBucket testBucket = Database.getScrollsTesting();  // Test-specific bucket
    private GridFSBucket productionBucket = Database.getScrolls();
    private ObjectId uploadedFileId = null;  // To track the uploaded file for cleanup

    // Utility method to create a temporary file
    public static File getTempFile() {
        try {
            File tempFile = File.createTempFile("testScroll", ".bin"); // Create a temp file
            byte[] binaryContent = { 0x01, 0x02, 0x03, 0x04, 0x05 }; // Example binary content
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(binaryContent);  // Write content to the temp file
            }
            tempFile.deleteOnExit();  // Ensure it gets deleted when the JVM exits
            return tempFile;
        } catch (IOException e) {
            System.out.println("Failed to create temporary file for testing: " + e.getMessage());
        }
        return null;
    }

    @BeforeEach
    public void clearUploadedFileId(){
        uploadedFileId = null;
    }

    // Test for myScrolls method

    @Test
    public void myScrollsTest() { // Problem: I require the user's ID in order to test if it is able to find the user's scrolls, not the scroll name
        File originalScroll = getTempFile();
        String addResult = Scroll.addScroll(testBucket, "unique_user_id", "Unique Scroll Name", originalScroll);
        assertEquals("Success", addResult);

        String[][] userScrolls = Scroll.myScrolls(testBucket, "unique_user_id");
        String expectedUserID = "Unique Scroll Name";

        assertArrayEquals(Scroll.myScrolls(testBucket, ""), new String[0][0]);

        assertEquals(true, userScrolls.length > 0);
        assertEquals(true, userScrolls[0].length == 3);
        // assertEquals(expectedScrollName, userScrolls[0][1]);
        System.out.println(userScrolls[0][1]);

    }


    @Test
    public void myScrollTest_BranchTest_NoUser() {
        File originalScroll = getTempFile();
        String addResult1 = Scroll.addScroll(testBucket, "testscroll", "testscroll", originalScroll);
        assertEquals("Success", addResult1);
        String addResult2 = Scroll.addScroll(testBucket, "testscroll2", "testscroll2", originalScroll);
        assertEquals("Success", addResult2);

        System.err.println("MYSCROLLTEST BRANCH_TEST NO_USER");
        String[][] userScrolls = Scroll.myScrolls(testBucket, "thereisnouserwiththisid");
        assertEquals(0, userScrolls.length);
    }

    @Test
    public void searchFilterTest_ScrollName() {
        File originalScroll = getTempFile();
        String addResult = Scroll.addScroll(testBucket, "search_filter_test_user", "Search Filter Test Scroll Name gre4", originalScroll);
        assertEquals("Success", addResult);

        String[][] potentialScrolls = Scroll.searchScroll(testBucket, null, null, "gre4", null);
        assertEquals(1, potentialScrolls.length);

        GridFSFile gridFSFile = testBucket.find(new org.bson.Document("metadata.name", "Search Filter Test Scroll Name gre4")).first();
        assertNotNull(gridFSFile, "File should have been uploaded to the bucket.");
        
        String scroll_id = gridFSFile.getMetadata().getString("scroll_id");
        Scroll.deleteScroll(testBucket, scroll_id);

    }

    @Test
    public void searchFilterTest_UploaderName() {
        File originalScroll = getTempFile();
        String addResult = Scroll.addScroll(testBucket, "search_filter_test_user 34g1", "Search Filter Test Scroll Name 34g1", originalScroll);
        assertEquals("Success", addResult);

        String[][] potentialScrolls = Scroll.searchScroll(testBucket, "34g1", null, null, null);
        assertEquals(1, potentialScrolls.length);

        GridFSFile gridFSFile = testBucket.find(new org.bson.Document("metadata.name", "Search Filter Test Scroll Name 34g1")).first();
        assertNotNull(gridFSFile, "File should have been uploaded to the bucket.");
        
        String scroll_id = gridFSFile.getMetadata().getString("scroll_id");
        Scroll.deleteScroll(testBucket, scroll_id);

    }

    @Test
    public void searchFilterTest_BranchTest_SearchParamLengthZero() {
        // This shouldn't return anything
        MongoCursor<GridFSFile> cursor = testBucket.find().iterator();
        while (cursor.hasNext()) {
            GridFSFile scroll = cursor.next();
            Document metadata = scroll.getMetadata();
            String id = metadata.getString("scroll_id"); 
            Scroll.deleteScroll(testBucket, id);
        }
        Scroll.addScroll(testBucket, "userid123", "View Scroll Test 1", getTempFile());
        Scroll.addScroll(testBucket, "userid234", "View Scroll Test 2", getTempFile());
        GridFSFile gridFSFile = testBucket.find(new org.bson.Document("metadata.name", "View Scroll Test 1")).first();
        String scroll_id1 = gridFSFile.getMetadata().getString("scroll_id");
        String Updated1 = Scroll.edit_date(gridFSFile.getMetadata().getDate("uploaded").toString());
        String downloads1 = gridFSFile.getMetadata().getInteger("downloads").toString();
        GridFSFile gridFSFile_2 = testBucket.find(new org.bson.Document("metadata.name", "View Scroll Test 2")).first();
        String scroll_id2 = gridFSFile_2.getMetadata().getString("scroll_id");
        String Updated2 = Scroll.edit_date(gridFSFile_2.getMetadata().getDate("uploaded").toString());
        String downloads2 = gridFSFile.getMetadata().getInteger("downloads").toString();

        String[][] expect = {{"userid123", scroll_id1,"View Scroll Test 1", Updated1, downloads1}, {"userid234", scroll_id2,"View Scroll Test 2", Updated2, downloads2}};
        assertArrayEquals(Scroll.searchScroll(testBucket, null, null, null, null), expect);
        Scroll.deleteScroll(testBucket, scroll_id1);
        Scroll.deleteScroll(testBucket, scroll_id2);
    }

    @Test
    public void searchFilterTest_BranchTest_ParametersDoNotMatch() {
        String[][] potentialScrolls = Scroll.searchScroll(testBucket, "thereisnowaythisstringisusedinanyscroll", null, null, null);
        assertEquals(0, potentialScrolls.length);
    }

    @Test
    public void addScrollTest() {

        // Create dummy user
        Login.addUser(Database.getUserProfilesTesting(), "userid123", "John", "john@gmail.com", "12345678", "Member", "password", "password");
        // Add a scroll using the addScroll method
        String result = Scroll.addScroll(testBucket, "userid123", "Test Scroll", getTempFile());
        assertEquals("Success", result);

        // Verify if the file was actually uploaded
        GridFSFile gridFSFile = testBucket.find(new org.bson.Document("metadata.name", "Test Scroll")).first();
        assertNotNull(gridFSFile, "File should have been uploaded to the bucket.");
        assertEquals("Test Scroll", gridFSFile.getMetadata().getString("name"), "Scroll name should match.");
        assertEquals("userid123", gridFSFile.getMetadata().getString("uploader_id"), "Uploader ID should match.");

        // Store the uploaded file ID for cleanup
        uploadedFileId = gridFSFile.getObjectId();

        // Check download count
        assertEquals(Scroll.userScrollStat(testBucket, gridFSFile.getObjectId().toHexString()), "0");

        // Update download count then check
        Scroll.updateDownloadCount(Database.getScrollFilesTesting(), gridFSFile.getObjectId().toHexString());
        assertEquals(Scroll.userScrollStat(testBucket, gridFSFile.getObjectId().toHexString()), "1");

        // Check user upload count
        assertEquals(Database.getUserProfilesTesting().find(Filters.eq("_id", "userid123")).first().getInteger("uploaded_scrolls"), 1);
        Database.getUserProfilesTesting().deleteMany(new Document());

    }


    // Test for empty user ID edge case
    @Test
    public void addScrollEmptyUserIdTest() {
        String result = Scroll.addScroll(testBucket, "", "Test Scroll", getTempFile());
        assertEquals("User ID cannot be empty.", result);
    }

    // Test for empty scroll name edge case
    @Test
    public void addScrollEmptyScrollNameTest() {
        String result = Scroll.addScroll(testBucket, "userid123", "", getTempFile());
        assertEquals("Scroll name cannot be empty.", result);
    }

    // Test for null file input edge case
    @Test
    public void addScrollNullFileTest() {
        String result = Scroll.addScroll(testBucket, "userid123", "Test Scroll", null);
        assertEquals("Please upload a file.", result);
    }

    @Test
    public void deleteScrollTest(){
        Scroll.addScroll(testBucket, "userid123", "View Scroll Test", getTempFile());

        GridFSFile gridFSFile = testBucket.find(new org.bson.Document("metadata.name", "View Scroll Test")).first();
        assertNotNull(gridFSFile, "File should have been uploaded to the bucket.");
        
        String scroll_id = gridFSFile.getMetadata().getString("scroll_id");
        
        String deleteResult = Scroll.deleteScroll(testBucket, scroll_id);
        assertEquals(deleteResult, "Success", "The scroll should be deleted successfully");

        GridFSFile afterDeleteFile = testBucket.find(Filters.eq("scroll_id", scroll_id)).first();
        assertNull(afterDeleteFile, "File should no longer exist in the bucket after deletion.");

        String deleteResult_2 = Scroll.deleteScroll(testBucket, scroll_id);
        assertEquals(deleteResult_2, "Error: Scroll not found.", "The scroll should not be found after deletion");

        uploadedFileId = null;

    }

    @Test
    public void deleteScrollTest_BranchTest_EmptyID() {
        String deleteResult = Scroll.deleteScroll(testBucket, "");
        assertEquals(deleteResult, "Error: Scroll ID cannot be empty.", "The scroll should be deleted successfully");
    }

    
    @Test
    public void viewAllScrollTest(){
        
        MongoCursor<GridFSFile> cursor = testBucket.find().iterator();
        while (cursor.hasNext()) {
            GridFSFile scroll = cursor.next();
            Document metadata = scroll.getMetadata();
            String id = metadata.getString("scroll_id"); 
            Scroll.deleteScroll(testBucket, id);
        }
        Scroll.addScroll(testBucket, "userid123", "View Scroll Test 1", getTempFile());
        Scroll.addScroll(testBucket, "userid234", "View Scroll Test 2", getTempFile());
        GridFSFile gridFSFile = testBucket.find(new org.bson.Document("metadata.name", "View Scroll Test 1")).first();
        String scroll_id1 = gridFSFile.getMetadata().getString("scroll_id");
        String Updated1 = Scroll.edit_date(gridFSFile.getMetadata().getDate("uploaded").toString());
        String downloads1 = gridFSFile.getMetadata().getInteger("downloads").toString();
        GridFSFile gridFSFile_2 = testBucket.find(new org.bson.Document("metadata.name", "View Scroll Test 2")).first();
        String scroll_id2 = gridFSFile_2.getMetadata().getString("scroll_id");
        String Updated2 = Scroll.edit_date(gridFSFile_2.getMetadata().getDate("uploaded").toString());
        String downloads2 = gridFSFile.getMetadata().getInteger("downloads").toString();
    
        String[][] expect = {{"userid123", scroll_id1,"View Scroll Test 1", Updated1, downloads1}, {"userid234", scroll_id2,"View Scroll Test 2", Updated2, downloads2}};
        assertArrayEquals(Scroll.viewAllScroll(testBucket), expect);
        Scroll.deleteScroll(testBucket, scroll_id1);
        Scroll.deleteScroll(testBucket, scroll_id2);
    }

    @Test
    public void viewScrollTest() {
        // First, add a scroll to the test bucket to retrieve
        String addResult = Scroll.addScroll(testBucket, "userid123", "View Scroll Test", getTempFile());
        assertEquals("Success", addResult);

        // Retrieve the uploaded file ID for the viewScroll test
        GridFSFile gridFSFile = testBucket.find(new org.bson.Document("metadata.name", "View Scroll Test")).first();
        assertNotNull(gridFSFile, "File should have been uploaded to the bucket.");
        uploadedFileId = gridFSFile.getObjectId(); // Store for cleanup

        // Call the viewScroll method
        File retrievedFile = Scroll.viewScroll(testBucket, uploadedFileId.toHexString());
        assertNotNull(retrievedFile, "Retrieved file should not be null.");

        // Compare file contents
        try {
            byte[] expectedData = Files.readAllBytes(getTempFile().toPath()); // Read the original file's bytes
            byte[] retrievedData = Files.readAllBytes(retrievedFile.toPath()); // Read the downloaded file's bytes

            assertArrayEquals(expectedData, retrievedData, "The contents of the retrieved file should match the original.");
            retrievedFile.delete();
        } catch (IOException e) {
            fail("IOException while reading file contents: " + e.getMessage());
        }
    }

    @Test
    public void editScrollTest() {
        // Add a dummy scroll
        File originalScroll = getTempFile();
        String addResult = Scroll.addScroll(testBucket, "userid123", "Original Scroll", originalScroll);
        assertEquals("Success", addResult);

        // Retrieve the ObjectId of the uploaded scroll
        GridFSFile gridFSFile = testBucket.find(new org.bson.Document("metadata.name", "Original Scroll")).first();
        assertNotNull(gridFSFile, "Original scroll should exist.");
        uploadedFileId = gridFSFile.getObjectId(); // Store for cleanup

        // Prepare an updated file
        File updatedScroll = getTempFile();
        try (FileOutputStream fos = new FileOutputStream(updatedScroll)) {
            byte[] updatedContent = { 0x06, 0x07, 0x08, 0x09, 0x0A }; // Example updated binary content
            fos.write(updatedContent);
        }
        catch(Exception e){
            fail("Exception while writing to file " + e.getMessage());
        }

        // Test edge cases
        assertEquals(Scroll.editScroll(testBucket, null, "Updated Scroll", updatedScroll), "Scroll ID cannot be null.");
        assertEquals(Scroll.editScroll(testBucket, "", "Updated Scroll", updatedScroll), "Scroll ID cannot be empty.");
        assertEquals(Scroll.editScroll(testBucket, uploadedFileId.toHexString(), "Updated Scroll", null), "Please upload the updated scroll.");
        assertEquals(Scroll.editScroll(testBucket, uploadedFileId.toHexString(), "", updatedScroll), "New scroll name cannot be empty.");
        assertTrue(Scroll.editScroll(testBucket, "nonexistendID", "Updated Scroll", updatedScroll).startsWith("Invalid Scroll ID: "));

        // Call the editScroll method
        String editResult = Scroll.editScroll(testBucket, uploadedFileId.toHexString(), "Updated Scroll", updatedScroll);
        assertEquals("Scroll updated successfully.", editResult);

        // Verify the scroll is updated
        GridFSFile updatedFile = testBucket.find(new org.bson.Document("metadata.name", "Updated Scroll.txt")).first();
        assertNotNull(updatedFile, "Updated scroll should exist.");
        
        // Check if the contents match
        File retrievedUpdatedFile = Scroll.viewScroll(testBucket, updatedFile.getMetadata().getString("scroll_id"));
        assertNotNull(retrievedUpdatedFile, "Retrieved updated file should not be null.");
        
        try {
            byte[] expectedData = Files.readAllBytes(updatedScroll.toPath()); // Read the updated file's bytes
            byte[] retrievedUpdatedData = Files.readAllBytes(retrievedUpdatedFile.toPath()); // Read the retrieved updated file's bytes

            assertArrayEquals(expectedData, retrievedUpdatedData, "The contents of the retrieved updated file should match.");
            retrievedUpdatedFile.delete();
        } catch (IOException e) {
            fail("IOException while reading updated file contents: " + e.getMessage());
        }
        uploadedFileId = updatedFile.getObjectId();
    }

    // Cleanup method to delete the uploaded file after the test
    @AfterEach
    public void cleanup() {
        if (uploadedFileId != null) {
            testBucket.delete(uploadedFileId);  // Delete the file from the test bucket

        }
    }
}