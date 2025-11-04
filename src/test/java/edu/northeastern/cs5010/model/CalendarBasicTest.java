package edu.northeastern.cs5010.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for basic Calendar operations including creation, configuration,
 * equality, and toString functionality.
 */
class CalendarBasicTest {

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

  @Test
  void testGetEventById() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();
    calendar.addEvent(event);

    String eventId = event.getId();
    Event retrievedEvent = calendar.getEventById(eventId);

    assertNotNull(retrievedEvent);
    assertEquals(event, retrievedEvent);
    assertEquals("Meeting", retrievedEvent.getSubject());
  }

  @Test
  void testGetEventByIdNonExistent() {
    Event retrievedEvent = calendar.getEventById("non-existent-id");
    assertNull(retrievedEvent);
  }

  @Test
  void testEventIdIsUnique() {
    Event event1 = new Event.Builder("Meeting 1", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    Event event2 = new Event.Builder("Meeting 2", "2025-11-02", "2025-11-02")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    assertNotEquals(event1.getId(), event2.getId());
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
