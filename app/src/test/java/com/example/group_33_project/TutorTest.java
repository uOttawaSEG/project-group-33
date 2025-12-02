package com.example.group_33_project;
import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;

//all test passed
public class TutorTest {
    private List<String> courses = Arrays.asList("cs", "math");
    private Tutor tutor = new Tutor("test", "test", "test@example.com", "password123",
            "12345", "B.Sc. cs", courses);

    @Test
    public void testTutorInitialized() {
        assertNotNull(tutor);
    }
    @Test
    public void testName() {
        assertEquals("test", tutor.getFirstName());
        assertEquals("test", tutor.getLastName());
    }
    @Test
    public void testPhone() {
        assertEquals("12345", tutor.getPhone());
    }

    @Test
    public void testCourses() {
        assertEquals(courses, tutor.getCourses());
    }
    @Test
    public void testEducation() {
        assertEquals("B.Sc. cs", tutor.getEducation());
    }

    @Test
    public void testSetCourses() {
        List<String> newCourses = Arrays.asList("physics", "biology");
        tutor.setCourses(newCourses);
        assertEquals(newCourses, tutor.getCourses());
    }

    @Test
    public void testRateTutor() {
        tutor.rate(5); // first rating
        assertEquals(1, tutor.getNumRatings());
        assertEquals(5.0, tutor.getRating(), 0.01);

        tutor.rate(3); // second rating
        assertEquals(2, tutor.getNumRatings());
        assertEquals(4.0, tutor.getRating(), 0.01); // average of 5 and 3
    }
    @Test
    public void testStatus() {
        assertEquals("pending", tutor.getStatus());
    }


}
