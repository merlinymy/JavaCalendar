package edu.northeastern.cs5010.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for conflict detection when adding events to a Calendar.
 * Covers both timed and all-day events, as well as allowConflict settings.
 */
class CalendarConflictTest {

  private Calendar calendar;

  @BeforeEach
  void setUp() {
    calendar = new Calendar("Personal Calendar");
  }

  // ==================== Conflict Detection Tests - Timed Events ====================

  @Test
  void testConflictNotAllowed_Event2StartsInsideEvent1() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("Meeting 1", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("11:00:00")
        .build();

    Event event2 = new Event.Builder("Meeting 2", "2025-11-01", "2025-11-01")
        .startTime("10:00:00")  // starts after 09:00:00 and before 11:00:00
        .endTime("12:00:00")
        .build();

    calendar.addEvent(event1);

    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addEvent(event2);
    });
  }

  @Test
  void testConflictNotAllowed_Event1StartsInsideEvent2() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("Meeting 1", "2025-11-01", "2025-11-01")
        .startTime("10:00:00")
        .endTime("12:00:00")
        .build();

    Event event2 = new Event.Builder("Meeting 2", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("11:00:00")  // ends after 10:00:00
        .build();

    calendar.addEvent(event2);

    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addEvent(event1);
    });
  }

  @Test
  void testConflictNotAllowed_Event2ContainsEvent1() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("Meeting 1", "2025-11-01", "2025-11-01")
        .startTime("10:00:00")
        .endTime("11:00:00")
        .build();

    Event event2 = new Event.Builder("Meeting 2", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("12:00:00")
        .build();

    calendar.addEvent(event1);

    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addEvent(event2);
    });
  }

  @Test
  void testConflictNotAllowed_Event1ContainsEvent2() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("Meeting 1", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("12:00:00")
        .build();

    Event event2 = new Event.Builder("Meeting 2", "2025-11-01", "2025-11-01")
        .startTime("10:00:00")
        .endTime("11:00:00")
        .build();

    calendar.addEvent(event1);

    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addEvent(event2);
    });
  }

  @Test
  void testConflictNotAllowed_ExactSameTime() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("Meeting 1", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    Event event2 = new Event.Builder("Meeting 2", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    calendar.addEvent(event1);

    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addEvent(event2);
    });
  }

  @Test
  void testNoConflict_AdjacentEvents_Event1EndsWhenEvent2Starts() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("Meeting 1", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    Event event2 = new Event.Builder("Meeting 2", "2025-11-01", "2025-11-01")
        .startTime("10:00:00")  // starts exactly when event1 ends
        .endTime("11:00:00")
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);  // Should succeed - no overlap

    assertEquals(2, calendar.getEventList().size());
  }

  @Test
  void testNoConflict_AdjacentEvents_Event2EndsWhenEvent1Starts() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("Meeting 1", "2025-11-01", "2025-11-01")
        .startTime("10:00:00")
        .endTime("11:00:00")
        .build();

    Event event2 = new Event.Builder("Meeting 2", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")  // ends exactly when event1 starts
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);  // Should succeed - no overlap

    assertEquals(2, calendar.getEventList().size());
  }

  @Test
  void testNoConflict_DifferentTimes() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("Meeting 1", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    Event event2 = new Event.Builder("Meeting 2", "2025-11-01", "2025-11-01")
        .startTime("14:00:00")
        .endTime("15:00:00")
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);  // Should succeed - no overlap

    assertEquals(2, calendar.getEventList().size());
  }

  @Test
  void testConflictNotAllowed_MultiDayEventsOverlap() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("Conference 1", "2025-11-01", "2025-11-03")
        .startTime("09:00:00")
        .endTime("17:00:00")
        .build();

    Event event2 = new Event.Builder("Conference 2", "2025-11-02", "2025-11-04")
        .startTime("09:00:00")
        .endTime("17:00:00")
        .build();

    calendar.addEvent(event1);

    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addEvent(event2);
    });
  }

  @Test
  void testConflictNotAllowed_EventSpansMidnight() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("Night Shift", "2025-11-01", "2025-11-02")
        .startTime("22:00:00")
        .endTime("02:00:00")
        .build();

    Event event2 = new Event.Builder("Late Meeting", "2025-11-01", "2025-11-01")
        .startTime("23:00:00")
        .endTime("23:59:59")
        .build();

    calendar.addEvent(event1);

    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addEvent(event2);
    });
  }

  @Test
  void testConflictNotAllowed_OneMinuteOverlap() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("Meeting 1", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    Event event2 = new Event.Builder("Meeting 2", "2025-11-01", "2025-11-01")
        .startTime("09:59:00")
        .endTime("11:00:00")
        .build();

    calendar.addEvent(event1);

    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addEvent(event2);
    });
  }

  // ==================== Conflict Detection Tests - All-Day Events ====================

  @Test
  void testConflictNotAllowed_AllDayEventsSameDate() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("Holiday 1", "2025-11-01", "2025-11-01")
        .build();

    Event event2 = new Event.Builder("Holiday 2", "2025-11-01", "2025-11-01")
        .build();

    calendar.addEvent(event1);

    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addEvent(event2);
    });
  }

  @Test
  void testNoConflict_AllDayEventsDifferentDates() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("Holiday 1", "2025-11-01", "2025-11-01")
        .build();

    Event event2 = new Event.Builder("Holiday 2", "2025-11-02", "2025-11-02")
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);  // Should succeed - different dates

    assertEquals(2, calendar.getEventList().size());
  }

  @Test
  void testConflictNotAllowed_AllDayMultiDayEventsOverlap() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("Vacation 1", "2025-11-01", "2025-11-05")
        .build();

    Event event2 = new Event.Builder("Vacation 2", "2025-11-03", "2025-11-07")
        .build();

    calendar.addEvent(event1);

    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addEvent(event2);
    });
  }

  @Test
  void testConflictNotAllowed_AllDayEventsEndDateEqualsStartDate() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("Event 1", "2025-11-01", "2025-11-03")
        .build();

    Event event2 = new Event.Builder("Event 2", "2025-11-03", "2025-11-05")
        .build();

    calendar.addEvent(event1);

    // Special case: if end date of event1 equals start date of event2, they conflict
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addEvent(event2);
    });
  }

  @Test
  void testConflictNotAllowed_AllDayEventAndTimedEventSameDate() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("All Day Meeting", "2025-11-01", "2025-11-01")
        .build();

    Event event2 = new Event.Builder("Timed Meeting", "2025-11-01", "2025-11-01")
        .startTime("14:00:00")
        .endTime("15:00:00")
        .build();

    calendar.addEvent(event1);

    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addEvent(event2);
    });
  }

  @Test
  void testNoConflict_AllDayEventAndTimedEventDifferentDates() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("All Day Meeting", "2025-11-01", "2025-11-01")
        .build();

    Event event2 = new Event.Builder("Timed Meeting", "2025-11-02", "2025-11-02")
        .startTime("14:00:00")
        .endTime("15:00:00")
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);  // Should succeed - different dates

    assertEquals(2, calendar.getEventList().size());
  }

  @Test
  void testConflictNotAllowed_MultiDayAllDayEventAndTimedEvent() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("Conference", "2025-11-01", "2025-11-03")
        .build();

    Event event2 = new Event.Builder("Timed Meeting", "2025-11-02", "2025-11-02")
        .startTime("14:00:00")
        .endTime("15:00:00")
        .build();

    calendar.addEvent(event1);

    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addEvent(event2);
    });
  }

  @Test
  void testConflictNotAllowed_AllDayEventOneContainsOther() {
    calendar.setAllowConflict(false);

    Event event1 = new Event.Builder("Long Vacation", "2025-11-01", "2025-11-10")
        .build();

    Event event2 = new Event.Builder("Short Trip", "2025-11-03", "2025-11-05")
        .build();

    calendar.addEvent(event1);

    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addEvent(event2);
    });
  }

  // ==================== Conflict Tests with AllowConflict = true ====================

  @Test
  void testConflictAllowed_OverlappingTimedEvents() {
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
    calendar.addEvent(event2);  // Should succeed - conflicts are allowed

    assertEquals(2, calendar.getEventList().size());
  }

  @Test
  void testConflictAllowed_AllDayEventsSameDate() {
    calendar.setAllowConflict(true);

    Event event1 = new Event.Builder("Holiday 1", "2025-11-01", "2025-11-01")
        .build();

    Event event2 = new Event.Builder("Holiday 2", "2025-11-01", "2025-11-01")
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);  // Should succeed - conflicts are allowed

    assertEquals(2, calendar.getEventList().size());
  }

  @Test
  void testConflictAllowed_MultipleOverlappingEvents() {
    calendar.setAllowConflict(true);

    Event event1 = new Event.Builder("Event 1", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("12:00:00")
        .build();

    Event event2 = new Event.Builder("Event 2", "2025-11-01", "2025-11-01")
        .startTime("10:00:00")
        .endTime("11:00:00")
        .build();

    Event event3 = new Event.Builder("Event 3", "2025-11-01", "2025-11-01")
        .startTime("11:30:00")
        .endTime("13:00:00")
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);
    calendar.addEvent(event3);  // All should succeed - conflicts are allowed

    assertEquals(3, calendar.getEventList().size());
  }
}
