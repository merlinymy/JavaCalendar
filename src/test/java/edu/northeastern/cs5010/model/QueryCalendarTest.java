package edu.northeastern.cs5010.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueryCalendarTest {

  private Calendar calendar;

  @BeforeEach
  void setUp() {
    calendar = new Calendar("Test Calendar");
  }

  // ==================== getOneEvent Tests ====================

  @Test
  void testGetOneEventFromEmptyCalendar() {
    Event result = calendar.getOneEvent("Meeting", "2025-11-01", "09:00:00");
    assertNull(result);
  }

  @Test
  void testGetOneEventFromNonRecurrentEventList() {
    Event event = new Event.Builder("Team Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    calendar.addEvent(event);

    Event result = calendar.getOneEvent("Team Meeting", "2025-11-01", "09:00:00");
    assertNotNull(result);
    assertEquals("Team Meeting", result.getSubject());
    assertEquals("2025-11-01", result.getStartDate());
    assertEquals("09:00:00", result.getStartTime());
  }

  @Test
  void testGetOneEventFromRecurrentEventList() {
    List<String> days = List.of("MONDAY", "WEDNESDAY");
    RecurrencePattern pattern = new RecurrencePattern(3, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("14:00:00"),
        LocalTime.parse("15:00:00"),
        "Weekly Standup",
        false,
        "Team sync",
        "Office"
    );

    calendar.addRecurrentEvent(recurrentEvent);

    // First occurrence should be Monday, Nov 3
    Event result = calendar.getOneEvent("Weekly Standup", "2025-11-03", "14:00:00");
    assertNotNull(result);
    assertEquals("Weekly Standup", result.getSubject());
    assertEquals("2025-11-03", result.getStartDate());
    assertEquals("14:00:00", result.getStartTime());
  }

  @Test
  void testGetOneEventWithWrongSubject() {
    Event event = new Event.Builder("Team Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    calendar.addEvent(event);

    Event result = calendar.getOneEvent("Wrong Subject", "2025-11-01", "09:00:00");
    assertNull(result);
  }

  @Test
  void testGetOneEventWithWrongDate() {
    Event event = new Event.Builder("Team Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    calendar.addEvent(event);

    Event result = calendar.getOneEvent("Team Meeting", "2025-11-02", "09:00:00");
    assertNull(result);
  }

  @Test
  void testGetOneEventWithWrongTime() {
    Event event = new Event.Builder("Team Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    calendar.addEvent(event);

    Event result = calendar.getOneEvent("Team Meeting", "2025-11-01", "10:00:00");
    assertNull(result);
  }

  @Test
  void testGetOneEventReturnsFirstMatchingEvent() {
    calendar.setAllowConflict(true);

    Event event1 = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .description("First meeting")
        .build();

    Event event2 = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .description("Second meeting")
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    Event result = calendar.getOneEvent("Meeting", "2025-11-01", "09:00:00");
    assertNotNull(result);
    assertEquals("First meeting", result.getDescription());
  }

  @Test
  void testGetOneEventWithMultipleEventsInCalendar() {
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

    Event result = calendar.getOneEvent("Lunch", "2025-11-01", "12:00:00");
    assertNotNull(result);
    assertEquals("Lunch", result.getSubject());
  }

  @Test
  void testGetOneEventWithBothNonRecurrentAndRecurrentEvents() {
    // Add non-recurrent event
    Event singleEvent = new Event.Builder("Single Meeting", "2025-11-03", "2025-11-03")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();
    calendar.addEvent(singleEvent);

    // Add recurrent event
    List<String> days = List.of("WEDNESDAY");
    RecurrencePattern pattern = new RecurrencePattern(2, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-05"),
        LocalTime.parse("14:00:00"),
        LocalTime.parse("15:00:00"),
        "Weekly Review",
        true,
        "Team review",
        "Conference Room"
    );
    calendar.addRecurrentEvent(recurrentEvent);

    // Should find the single event
    Event result1 = calendar.getOneEvent("Single Meeting", "2025-11-03", "09:00:00");
    assertNotNull(result1);
    assertEquals("Single Meeting", result1.getSubject());

    // Should find the recurrent event
    Event result2 = calendar.getOneEvent("Weekly Review", "2025-11-05", "14:00:00");
    assertNotNull(result2);
    assertEquals("Weekly Review", result2.getSubject());
  }

  // ==================== getAllEventsInRange Tests ====================

  @Test
  void testGetAllEventsInRangeFromEmptyCalendar() {
    List<Event> result = calendar.getAllEventsInRange("2025-11-01", "2025-11-30");
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testGetAllEventsInRangeWithSingleDayRange() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    calendar.addEvent(event);

    List<Event> result = calendar.getAllEventsInRange("2025-11-01", "2025-11-01");
    assertEquals(1, result.size());
    assertEquals("Meeting", result.get(0).getSubject());
  }

  @Test
  void testGetAllEventsInRangeWithMultipleEvents() {
    Event event1 = new Event.Builder("Event 1", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    Event event2 = new Event.Builder("Event 2", "2025-11-05", "2025-11-05")
        .startTime("14:00:00")
        .endTime("15:00:00")
        .build();

    Event event3 = new Event.Builder("Event 3", "2025-11-10", "2025-11-10")
        .startTime("11:00:00")
        .endTime("12:00:00")
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);
    calendar.addEvent(event3);

    List<Event> result = calendar.getAllEventsInRange("2025-11-01", "2025-11-10");
    assertEquals(3, result.size());
  }

  @Test
  void testGetAllEventsInRangeExcludesEventsOutsideRange() {
    Event event1 = new Event.Builder("Before Range", "2025-10-30", "2025-10-30")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    Event event2 = new Event.Builder("In Range", "2025-11-05", "2025-11-05")
        .startTime("14:00:00")
        .endTime("15:00:00")
        .build();

    Event event3 = new Event.Builder("After Range", "2025-11-20", "2025-11-20")
        .startTime("11:00:00")
        .endTime("12:00:00")
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);
    calendar.addEvent(event3);

    List<Event> result = calendar.getAllEventsInRange("2025-11-01", "2025-11-10");
    assertEquals(1, result.size());
    assertEquals("In Range", result.get(0).getSubject());
  }

  @Test
  void testGetAllEventsInRangeIncludesRecurrentEvents() {
    List<String> days = List.of("MONDAY", "WEDNESDAY", "FRIDAY");
    RecurrencePattern pattern = new RecurrencePattern(6, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("10:00:00"),
        LocalTime.parse("11:00:00"),
        "Recurring Meeting",
        false,
        "Daily standup",
        "Office"
    );

    calendar.addRecurrentEvent(recurrentEvent);

    List<Event> result = calendar.getAllEventsInRange("2025-11-01", "2025-11-30");
    assertEquals(6, result.size());

    for (Event event : result) {
      assertEquals("Recurring Meeting", event.getSubject());
    }
  }

  @Test
  void testGetAllEventsInRangeIncludesBothNonRecurrentAndRecurrentEvents() {
    // Add non-recurrent events
    Event event1 = new Event.Builder("Single Event 1", "2025-11-02", "2025-11-02")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    Event event2 = new Event.Builder("Single Event 2", "2025-11-08", "2025-11-08")
        .startTime("14:00:00")
        .endTime("15:00:00")
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);
    System.out.println(calendar.getEventList());

    // Add recurrent event
    List<String> days = List.of("MONDAY");
    RecurrencePattern pattern = new RecurrencePattern(2, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("11:00:00"),
        LocalTime.parse("12:00:00"),
        "Weekly Meeting",
        true,
        "Team sync",
        "Room A"
    );
    calendar.addRecurrentEvent(recurrentEvent);

    System.out.println(calendar.getRecurrentEvents());
    List<Event> result = calendar.getAllEventsInRange("2025-11-01", "2025-11-15");
    System.out.println(result);
    assertEquals(4, result.size()); // 2 single events + 2 recurrent events
  }

  @Test
  void testGetAllEventsInRangeWithNoEventsInRange() {
    Event event = new Event.Builder("Event", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    calendar.addEvent(event);

    List<Event> result = calendar.getAllEventsInRange("2025-12-01", "2025-12-31");
    assertTrue(result.isEmpty());
  }

  @Test
  void testGetAllEventsInRangeWithMultiDayEvent() {
    Event event = new Event.Builder("Conference", "2025-11-05", "2025-11-07")
        .description("Tech conference")
        .build();

    calendar.addEvent(event);

    List<Event> result = calendar.getAllEventsInRange("2025-11-01", "2025-11-30");
    assertEquals(1, result.size());
    assertEquals("Conference", result.get(0).getSubject());
  }

  @Test
  void testGetAllEventsInRangeWithExactDateMatch() {
    Event event = new Event.Builder("Birthday", "2025-11-15", "2025-11-15")
        .build();

    calendar.addEvent(event);

    List<Event> result = calendar.getAllEventsInRange("2025-11-15", "2025-11-15");
    assertEquals(1, result.size());
    assertEquals("Birthday", result.get(0).getSubject());
  }

  // ==================== isUserBusyOnDayAtTime Tests ====================

  @Test
  void testIsUserBusyOnDayAtTimeWithEmptyCalendar() {
    boolean result = calendar.isUserBusyOnDayAtTime("2025-11-01", "09:00:00");
    assertFalse(result);
  }

  @Test
  void testIsUserBusyOnDayAtTimeReturnsTrueWhenBusy() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    calendar.addEvent(event);

    boolean result = calendar.isUserBusyOnDayAtTime("2025-11-01", "09:30:00");
    assertTrue(result);
  }

  @Test
  void testIsUserBusyOnDayAtTimeReturnsFalseWhenNotBusy() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    calendar.addEvent(event);

    boolean result = calendar.isUserBusyOnDayAtTime("2025-11-01", "14:00:00");
    assertFalse(result);
  }

  @Test
  void testIsUserBusyOnDayAtTimeAtExactStartTime() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    calendar.addEvent(event);

    boolean result = calendar.isUserBusyOnDayAtTime("2025-11-01", "09:00:00");
    assertTrue(result);
  }

  @Test
  void testIsUserBusyOnDayAtTimeAtExactEndTime() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    calendar.addEvent(event);

    boolean result = calendar.isUserBusyOnDayAtTime("2025-11-01", "10:00:00");
    assertFalse(result); // End time is exclusive
  }

  @Test
  void testIsUserBusyOnDayAtTimeOnDifferentDate() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    calendar.addEvent(event);

    boolean result = calendar.isUserBusyOnDayAtTime("2025-11-02", "09:30:00");
    assertFalse(result);
  }

  @Test
  void testIsUserBusyOnDayAtTimeWithMultiDayEvent() {
    Event event = new Event.Builder("Conference", "2025-11-01", "2025-11-03")
        .startTime("09:00:00")
        .endTime("17:00:00")
        .build();

    calendar.addEvent(event);

    // Should be busy on the first day
    assertTrue(calendar.isUserBusyOnDayAtTime("2025-11-01", "10:00:00"));

    // Should be busy on the middle day
    assertTrue(calendar.isUserBusyOnDayAtTime("2025-11-02", "12:00:00"));

    // Should be busy on the last day
    assertTrue(calendar.isUserBusyOnDayAtTime("2025-11-03", "16:00:00"));

    // Should not be busy after the event
    assertFalse(calendar.isUserBusyOnDayAtTime("2025-11-04", "10:00:00"));
  }

  @Test
  void testIsUserBusyOnDayAtTimeWithAllDayEvent() {
    Event event = new Event.Builder("Holiday", "2025-11-01", "2025-11-01")
        .build();

    calendar.addEvent(event);

    // Should be busy at any time on that day for all-day event
    assertTrue(calendar.isUserBusyOnDayAtTime("2025-11-01", "00:00:00"));
    assertTrue(calendar.isUserBusyOnDayAtTime("2025-11-01", "12:00:00"));
    assertTrue(calendar.isUserBusyOnDayAtTime("2025-11-01", "23:59:59"));
  }

  @Test
  void testIsUserBusyOnDayAtTimeWithRecurrentEvent() {
    List<String> days = List.of("MONDAY", "WEDNESDAY", "FRIDAY");
    RecurrencePattern pattern = new RecurrencePattern(3, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("14:00:00"),
        LocalTime.parse("15:00:00"),
        "Recurring Meeting",
        false,
        "Team standup",
        "Office"
    );

    calendar.addRecurrentEvent(recurrentEvent);

    // Monday, Nov 3 at 14:30 should be busy
    assertTrue(calendar.isUserBusyOnDayAtTime("2025-11-03", "14:30:00"));

    // Wednesday, Nov 5 at 14:30 should be busy
    assertTrue(calendar.isUserBusyOnDayAtTime("2025-11-05", "14:30:00"));

    // Tuesday, Nov 4 at 14:30 should not be busy
    assertFalse(calendar.isUserBusyOnDayAtTime("2025-11-04", "14:30:00"));
  }

  @Test
  void testIsUserBusyOnDayAtTimeBeforeEventStartTime() {
    Event event = new Event.Builder("Lunch", "2025-11-01", "2025-11-01")
        .startTime("12:00:00")
        .endTime("13:00:00")
        .build();

    calendar.addEvent(event);

    boolean result = calendar.isUserBusyOnDayAtTime("2025-11-01", "11:59:59");
    assertFalse(result);
  }

  @Test
  void testIsUserBusyOnDayAtTimeAfterEventEndTime() {
    Event event = new Event.Builder("Lunch", "2025-11-01", "2025-11-01")
        .startTime("12:00:00")
        .endTime("13:00:00")
        .build();

    calendar.addEvent(event);

    boolean result = calendar.isUserBusyOnDayAtTime("2025-11-01", "13:00:01");
    assertFalse(result);
  }

  @Test
  void testIsUserBusyOnDayAtTimeWithMultipleEventsOnSameDay() {
    calendar.setAllowConflict(false);

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

    // Should be busy during events
    assertTrue(calendar.isUserBusyOnDayAtTime("2025-11-01", "09:30:00"));
    assertTrue(calendar.isUserBusyOnDayAtTime("2025-11-01", "12:30:00"));
    assertTrue(calendar.isUserBusyOnDayAtTime("2025-11-01", "15:30:00"));

    // Should not be busy between events
    assertFalse(calendar.isUserBusyOnDayAtTime("2025-11-01", "11:00:00"));
    assertFalse(calendar.isUserBusyOnDayAtTime("2025-11-01", "14:00:00"));
  }

  @Test
  void testIsUserBusyOnDayAtTimeWithEventSpanningMidnight() {
    Event event = new Event.Builder("Night Shift", "2025-11-01", "2025-11-02")
        .startTime("22:00:00")
        .endTime("06:00:00")
        .build();

    calendar.addEvent(event);

    // Should be busy late on first day
    assertTrue(calendar.isUserBusyOnDayAtTime("2025-11-01", "23:00:00"));

    // Should be busy early on second day
    assertTrue(calendar.isUserBusyOnDayAtTime("2025-11-02", "05:00:00"));

    // Should not be busy after end time on second day
    assertFalse(calendar.isUserBusyOnDayAtTime("2025-11-02", "07:00:00"));
  }

  @Test
  void testIsUserBusyOnDayAtTimeWithBothNonRecurrentAndRecurrentEvents() {
    // Add non-recurrent event
    Event singleEvent = new Event.Builder("Single Meeting", "2025-11-03", "2025-11-03")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();
    calendar.addEvent(singleEvent);

    // Add recurrent event
    List<String> days = List.of("WEDNESDAY");
    RecurrencePattern pattern = new RecurrencePattern(2, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-05"),
        LocalTime.parse("14:00:00"),
        LocalTime.parse("15:00:00"),
        "Weekly Review",
        true,
        "Team review",
        "Conference Room"
    );
    calendar.addRecurrentEvent(recurrentEvent);

    // Should be busy during single event
    assertTrue(calendar.isUserBusyOnDayAtTime("2025-11-03", "09:30:00"));

    // Should be busy during recurrent event
    assertTrue(calendar.isUserBusyOnDayAtTime("2025-11-05", "14:30:00"));

    // Should not be busy at other times
    assertFalse(calendar.isUserBusyOnDayAtTime("2025-11-04", "09:30:00"));
  }
}
