import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.model.DatabaseConnection;

public class TempDbTest {

    private DatabaseConnection db;

    @BeforeEach
    public void setUp() {
        db = new DatabaseConnection();
    }

    @Test
    public void testAddCourse() {
        boolean result = db.addCourse(1, 1234);
        assertTrue(result, "Course should be added successfully");
    }

    @Test
    public void testDropCourse() {
        db.addCourse(1, 1234);
        boolean result = db.dropCourse(1, 1234);
        assertTrue(result, "Course should be dropped successfully");
    }

    @Test
    public void testDropCourseNotAdded() {
        boolean result = db.dropCourse(1, 1234);
        assertFalse(result, "Dropping a course that was not added should fail");
    }
}