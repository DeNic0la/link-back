package ch.denic0la;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class SecuredLinkTest {

    @Test
    @Transactional
    public void testSecuredLinkPersistence() {
        SecuredLink link = new SecuredLink();
        link.accessKey = "testAccessKey";
        link.secondFactorKey = "hashedKey";
        link.targetLink = "https://example.com";
        link.hasBeenAccessed = false;

        link.persist();
        assertNotNull(link.id);

        SecuredLink retrieved = SecuredLink.findById(link.id);
        assertNotNull(retrieved);
        assertEquals("testAccessKey", retrieved.accessKey);
        assertEquals("hashedKey", retrieved.secondFactorKey);
        assertEquals("https://example.com", retrieved.targetLink);
        assertFalse(retrieved.hasBeenAccessed);
    }
}
