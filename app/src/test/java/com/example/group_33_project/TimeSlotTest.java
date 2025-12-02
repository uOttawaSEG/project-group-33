package com.example.group_33_project;
import static org.junit.Assert.*;
import org.junit.Test;
import java.time.ZoneId;
import java.time.ZonedDateTime;

//all tests passed
public class TimeSlotTest {
    private ZonedDateTime start = ZonedDateTime.of(2025, 11, 21, 10, 0, 0, 0, ZoneId.of("UTC"));

    private ZonedDateTime end = ZonedDateTime.of(2025, 11, 21, 12, 0, 0, 0, ZoneId.of("UTC"));
    private Tutor tutor = new Tutor();
    private TimeSlot testTimeslot = new TimeSlot(tutor, false, start, end);

    @Test
    public void testTimeSlotInitialized() {
        assertNotNull(testTimeslot);
    }
    @Test
    public void testStartAndEndDates() {
        assertEquals(start, testTimeslot.getStartDate());
        assertEquals(end, testTimeslot.getEndDate());
    }
    @Test
    public void testStartDayMonth() {
        assertEquals(21, testTimeslot.getStartDay());
        assertEquals(11, testTimeslot.getStartMonth());
    }

    @Test
    public void testRequireApproval() {
        assertFalse(testTimeslot.getRequireApproval());
    }

    @Test
    public void testInitialStatus_isOpen() {
        assertEquals("open", testTimeslot.getStatus());
    }
    @Test
    public void testSetStatus() {
        testTimeslot.setStatus("booked");
        assertEquals("booked", testTimeslot.getStatus());
    }

    @Test
    public void testSetStudent_changesStatusToBooked() {
        Student s = new Student();
        testTimeslot.setStudent(s);

        assertEquals(s, testTimeslot.getStudent());
        assertEquals("booked", testTimeslot.getStatus());
    }



    @Test
    public void overlappingSlots_shouldAssertTrue() {
        //comparing 10am-11am and 10am-11am
        ZonedDateTime start1 = ZonedDateTime.of(2025, 11, 21, 10, 0, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime end1   = ZonedDateTime.of(2025, 11, 21, 11, 0, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime start2 = ZonedDateTime.of(2025, 11, 21, 10, 30, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime end2   = ZonedDateTime.of(2025, 11, 21, 11, 30, 0, 0, ZoneId.of("UTC"));

        TimeSlot slot1 = new TimeSlot(null, false, start1, end1);
        TimeSlot slot2 = new TimeSlot(null, false, start2, end2);

        assertTrue(TimeSlot.isOverlap(slot1, slot2)); //ASSERT TRUE
    }


    @Test
    public void nonoverlapSlots_shouldAssertFalse() {
        //comparing 10am-10:30am and 10:30am and 11am
        ZonedDateTime start1 = ZonedDateTime.of(2025, 11, 21, 10, 0, 0, 0, ZoneId.of("America/New_York"));
        ZonedDateTime end1   = ZonedDateTime.of(2025, 11, 21, 10, 30, 0, 0, ZoneId.of("America/New_York"));
        ZonedDateTime start2 = ZonedDateTime.of(2025, 11, 21, 10, 30, 0, 0, ZoneId.of("America/New_York"));
        ZonedDateTime end2   = ZonedDateTime.of(2025, 11, 21, 11, 0, 0, 0, ZoneId.of("America/New_York"));

        TimeSlot slot1 = new TimeSlot(null, false, start1, end1);
        TimeSlot slot2 = new TimeSlot(null, false, start2, end2);

        assertFalse(TimeSlot.isOverlap(slot1, slot2)); //ASSERT FALSE
    }


}
