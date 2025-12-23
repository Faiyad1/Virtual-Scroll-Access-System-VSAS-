package VirtualScrollAccessSystem;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EncryptorTest {

    
    @Test
    public void fullCycleEncryptionDecryptionTest() {

        Encryptor encryptor = new Encryptor();

        String orig = "password";
        String encrypted = encryptor.encrypt(orig);
        String decrypted = encryptor.decrypt(encrypted);
        assertEquals(decrypted, orig);
    }
}