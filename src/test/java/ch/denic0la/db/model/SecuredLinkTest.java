package ch.denic0la.db.model;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class SecuredLinkTest {

    @Test
    @Transactional
    public void testSecuredLinkPersistence() {
        String uniqueKey = "testAccessKey-" + System.currentTimeMillis();
        SecuredLink link = new SecuredLink();
        link.accessKey = uniqueKey;
        link.secondFactorKey = "hashedKey";
        link.targetLink = "https://example.com";
        link.hasBeenAccessed = false;

        link.persist();
        assertNotNull(link.id);

        SecuredLink retrieved = SecuredLink.findById(link.id);
        assertNotNull(retrieved);
        assertEquals(uniqueKey, retrieved.accessKey);
        assertEquals("hashedKey", retrieved.secondFactorKey);
        assertEquals("https://example.com", retrieved.targetLink);
        assertFalse(retrieved.hasBeenAccessed);
    }
}
