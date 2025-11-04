package edu.northeastern.cs5010.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for adding single (non-recurrent) events to a Calendar.
 * Tests basic event additions without conflicts.
 */
class CalendarAddEventTest {

  private Calendar calendar;

  @BeforeEach
  void setUp() {
    calendar = new Calendar("Personal Calendar");
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
    calendar.setAllowConflict(true);
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
}
