package VirtualScrollAccessSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;


import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class AdminTest {
    public static final MongoCollection<Document> collection = Database.getUserProfiles();
    public static final MongoCollection<Document> ListofUserCollection = Database.getUserProfilesTesting();

    @Test
    public void ListofUsersTest(){
        
        Document newUser1 = new Document("_id", "userid123")
            .append("full_name", "John")
            .append("email", "john@gmail.com")
            .append("phone", "12345678")
            .append("encrypted_password", Encryptor.encrypt("password"))
            .append("type", "Member");
        ListofUserCollection.insertOne(newUser1);
        Document newUser2 = new Document("_id", "userid124")
            .append("full_name", "Tony")
            .append("email", "Tony@gmail.com")
            .append("phone", "23456789")
            .append("encrypted_password", Encryptor.encrypt("password"))
            .append("type", "Member");
        ListofUserCollection.insertOne(newUser2);
        assertArrayEquals(Admin.ListofUsers(ListofUserCollection), new String[] {"userid123", "userid124"});
        ListofUserCollection.deleteMany(new Document());
    }

    @Test
    public void DeleteUserTest(){
        Login.addUser(ListofUserCollection, "userid123", "John", "john@gmail.com", "12345678", "normal", "password", "password");
        assertEquals(Admin.DeleteUser(ListofUserCollection, "userid123"), "Successful");
        Document query = new Document("_id", "userid123");
        assertTrue(ListofUserCollection.countDocuments(query) == 0);
        assertEquals(Admin.DeleteUser(ListofUserCollection, "userid123"), "Error: User not found");
    }

}