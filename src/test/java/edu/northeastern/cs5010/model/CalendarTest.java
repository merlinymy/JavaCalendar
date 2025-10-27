package edu.northeastern.cs5010.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for Calendar creation and adding Events to a Calendar.
 * These tests cover base Events only (not RecurrentEvents).
 */
class CalendarTest {

  private Calendar calendar;

  @BeforeEach
  void setUp() {
    calendar = new Calendar("Personal Calendar");
  }

  // ==================== Calendar Creation Tests ====================

  @Test
  void testCreateCalendarWithTitle() {
    Calendar cal = new Calendar("Work Calendar");
    assertEquals("Work Calendar", cal.getTitle());
    assertNotNull(cal.getEventList());
    assertTrue(cal.getEventList().isEmpty());
  }

  @Test
  void testCreateCalendarDefaultConfiguration() {
    Calendar cal = new Calendar("My Calendar");
    assertFalse(cal.getAllowConflict());
  }

  @Test
  void testSetCalendarTitle() {
    calendar.setTitle("Updated Title");
    assertEquals("Updated Title", calendar.getTitle());
  }

  @Test
  void testSetAllowConflict() {
    calendar.setAllowConflict(true);
    assertTrue(calendar.getAllowConflict());

    calendar.setAllowConflict(false);
    assertFalse(calendar.getAllowConflict());
  }

  // ==================== Adding Events to Calendar Tests ====================

  @Test
  void testAddEventToEmptyCalendar() {
    Event event = new Event.Builder("Lunch", "2025-11-01", "2025-11-01")
        .startTime("12:00:00")
        .endTime("13:00:00")
        .build();

    calendar.addEvent(event);

    assertEquals(1, calendar.getEventList().size());
    assertTrue(calendar.getEventList().contains(event));
  }

  @Test
  void testAddMultipleNonConflictingEvents() {
    Event event1 = new Event.Builder("Morning Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    Event event2 = new Event.Builder("Lunch", "2025-11-01", "2025-11-01")
        .startTime("12:00:00")
        .endTime("13:00:00")
        .build();

    Event event3 = new Event.Builder("Afternoon Meeting", "2025-11-01", "2025-11-01")
        .startTime("15:00:00")
        .endTime("16:00:00")
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);
    calendar.addEvent(event3);

    assertEquals(3, calendar.getEventList().size());
  }

  @Test
  void testAddEventsOnDifferentDates() {
    Event event1 = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    Event event2 = new Event.Builder("Meeting", "2025-11-02", "2025-11-02")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    assertEquals(2, calendar.getEventList().size());
  }

  @Test
  void testAddTwoEventsWithSameSubjectButDifferentTimes() {
    Event event1 = new Event.Builder("Exercise", "2025-11-01", "2025-11-01")
        .startTime("06:00:00")
        .endTime("07:00:00")
        .build();

    Event event2 = new Event.Builder("Exercise", "2025-11-01", "2025-11-01")
        .startTime("18:00:00")
        .endTime("19:00:00")
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    assertEquals(2, calendar.getEventList().size());
  }

  @Test
  void testAddAllDayEventsWithDifferentSubjects() {
    Event event1 = new Event.Builder("Holiday", "2025-11-01", "2025-11-01")
        .build();

    Event event2 = new Event.Builder("Birthday", "2025-11-01", "2025-11-01")
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    assertEquals(2, calendar.getEventList().size());
  }

  @Test
  void testAddMultiDayEvents() {
    Event event1 = new Event.Builder("Conference", "2025-11-10", "2025-11-12")
        .description("Tech conference")
        .build();

    Event event2 = new Event.Builder("Vacation", "2025-12-20", "2025-12-27")
        .description("Winter vacation")
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    assertEquals(2, calendar.getEventList().size());
  }

  // ==================== Duplicate Event Tests ====================

  @Test
  void testAddDuplicateEvent() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    calendar.addEvent(event);

    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addEvent(event);
    });
  }

  // ==================== Conflict Detection Tests ====================

  @Test
  void testAddConflictingEventWhenConflictsNotAllowed() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("Meeting 1", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    Event event2 = new Event.Builder("Meeting 2", "2025-11-01", "2025-11-01")
        .startTime("09:30:00")
        .endTime("10:30:00")
        .build();

    calendar.addEvent(event1);

    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addEvent(event2);
    });
  }

  @Test
  void testAddConflictingEventWhenConflictsAllowed() {
    calendar.setAllowConflict(true);

    Event event1 = new Event.Builder("Meeting 1", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    Event event2 = new Event.Builder("Meeting 2", "2025-11-01", "2025-11-01")
        .startTime("09:30:00")
        .endTime("10:30:00")
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    assertEquals(2, calendar.getEventList().size());
  }

  // ==================== Event Retrieval Tests ====================

  @Test
  void testGetEventListFromNewCalendar() {
    assertNotNull(calendar.getEventList());
    assertTrue(calendar.getEventList().isEmpty());
  }

  @Test
  void testGetEventListAfterAddingEvents() {
    Event event1 = new Event.Builder("Event 1", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    Event event2 = new Event.Builder("Event 2", "2025-11-02", "2025-11-02")
        .startTime("14:00:00")
        .endTime("15:00:00")
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    assertEquals(2, calendar.getEventList().size());
    assertTrue(calendar.getEventList().contains(event1));
    assertTrue(calendar.getEventList().contains(event2));
  }

  // ==================== Calendar Equality Tests ====================

  @Test
  void testCalendarEquality() {
    Calendar cal1 = new Calendar("Test Calendar");
    Calendar cal2 = new Calendar("Test Calendar");

    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    cal1.addEvent(event);
    cal2.addEvent(event);

    assertEquals(cal1, cal2);
  }

  @Test
  void testCalendarInequality() {
    Calendar cal1 = new Calendar("Calendar 1");
    Calendar cal2 = new Calendar("Calendar 2");

    assertNotEquals(cal1, cal2);
  }

  @Test
  void testCalendarHashCode() {
    Calendar cal1 = new Calendar("Test Calendar");
    Calendar cal2 = new Calendar("Test Calendar");

    assertEquals(cal1.hashCode(), cal2.hashCode());
  }

  // ==================== Edge Cases ====================

  @Test
  void testAddEventAtMidnight() {
    Event event = new Event.Builder("Midnight Event", "2025-11-01", "2025-11-01")
        .startTime("00:00:00")
        .endTime("01:00:00")
        .build();

    calendar.addEvent(event);
    assertEquals(1, calendar.getEventList().size());
  }

  @Test
  void testAddEventSpanningMidnight() {
    Event event = new Event.Builder("Night Shift", "2025-11-01", "2025-11-02")
        .startTime("22:00:00")
        .endTime("06:00:00")
        .build();

    calendar.addEvent(event);
    assertEquals(1, calendar.getEventList().size());
  }

  @Test
  void testAddEventWithVeryLongDescription() {
    String longDescription = "This is a very long description ".repeat(50);

    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .description(longDescription)
        .build();

    calendar.addEvent(event);
    assertEquals(longDescription, event.getDescription());
  }

  @Test
  void testCalendarToString() {
    calendar.addEvent(new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build());

    String toString = calendar.toString();
    assertTrue(toString.contains("Personal Calendar"));
  }

  // ==================== Multiple Calendars Tests ====================

  @Test
  void testMultipleCalendarsCanHaveSameEventSubjectAndDate() {
    Calendar cal1 = new Calendar("Calendar 1");
    Calendar cal2 = new Calendar("Calendar 2");

    Event event1 = new Event.Builder("Thanksgiving", "2025-11-07", "2025-11-07")
        .build();

    Event event2 = new Event.Builder("Thanksgiving", "2025-11-07", "2025-11-07")
        .build();

    cal1.addEvent(event1);
    cal2.addEvent(event2);

    assertEquals(1, cal1.getEventList().size());
    assertEquals(1, cal2.getEventList().size());
  }
}
