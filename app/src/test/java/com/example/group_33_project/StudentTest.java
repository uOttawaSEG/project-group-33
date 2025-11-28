package com.example.group_33_project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

//all tests passed
public class StudentTest {

    private Student student;
    @Before
    public void setUp() {
        // 3. Initialize a new Student object before each test runs.
        student = new Student("test", "test", "test@example.com", "password123",
                "12345", "B.Sc. cs", "pending",
                new ArrayList<>(), new ArrayList<>());
    }
    @Test
    public void testStudentInitialized() {
        assertNotNull(student);
    }
    @Test
    public void testBasicInfo() {
        assertEquals("test", student.getFirstName());
        assertEquals("test", student.getLastName());
        assertEquals("test@example.com", student.getEmail());
        assertEquals("12345", student.getPhone());
        assertEquals("B.Sc. cs", student.getProgram());
        assertEquals("pending", student.getStatus());

    }
    @Test
    public void testStatus() {
        student.setStatus("approved");
        assertEquals("approved", student.getStatus());
    }
    @Test
    public void testProgram(){
        student.setProgram("communications");
        assertEquals("communications", student.getProgram());
    }

    @Test
    public void testSessionTokens() {
        // This test now starts with a student that has an empty session token list.
        ArrayList<String> tokensTest = new ArrayList<>();
        student.addSessionToken("token1");
        student.addSessionToken("token2");
        tokensTest.add("token1");
        tokensTest.add("token2");

        assertEquals(tokensTest, student.getSessionTokens());

        student.removeSessionToken("token1");
        tokensTest.remove("token1");

        assertEquals(tokensTest, student.getSessionTokens());
    }

    @Test
    public void testRejectedSessionTokens() {
        // This test also starts with a student that has an empty rejected token list.
        ArrayList<String> rejectedTokensTest = new ArrayList<>();
        student.addRejectedSessionToken("reject1");
        student.addRejectedSessionToken("reject2");
        rejectedTokensTest.add("reject1");
        rejectedTokensTest.add("reject2");

        assertEquals(rejectedTokensTest, student.getRejectedSessionTokens());
    }


}
