package edu.northeastern.cs5010.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for adding and managing recurrent events in a Calendar.
 */
class CalendarRecurrentEventTest {

  private Calendar calendar;

  @BeforeEach
  void setUp() {
    calendar = new Calendar("Personal Calendar");
  }

  // ==================== Calendar Add Recurrent Events Tests ====================

  @Test
  void testAddRecurrentEventsWithOccurrenceCountToEmptyCalendar() {
    List<String> days = List.of("MONDAY", "SATURDAY");
    RecurrencePattern pattern = new RecurrencePattern(5, days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-10-31"),
        LocalTime.parse("10:30:00"),
        LocalTime.parse("22:00:00"),
        "Weekly Team Meeting",
        true,
        "Recurring team sync",
        "Conference Room B"
    );

    calendar.addRecurrentEvent(recurrentEvent);

    assertEquals(1, calendar.getRecurrentEvents().size());
    assertEquals(5, recurrentEvent.getEvents().size());
  }

  @Test
  void testAddRecurrentEventsWithEndDateToEmptyCalendar() {
    List<String> days = List.of("TUESDAY", "THURSDAY");
    RecurrencePattern pattern = new RecurrencePattern("2025-11-20", days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-04"),
        LocalTime.parse("08:00:00"),
        LocalTime.parse("09:00:00"),
        "Morning Yoga",
        true,
        "Daily yoga practice",
        "Studio B"
    );

    calendar.addRecurrentEvent(recurrentEvent);

    // From Nov 4 to Nov 20: Tuesdays (4, 11, 18) and Thursdays (6, 13, 20) = 6 events
    assertEquals(1, calendar.getRecurrentEvents().size());
    assertEquals(6, recurrentEvent.getEvents().size());
  }

  @Test
  void testAddMultipleRecurrentEventsWithoutConflicts() {
    calendar.setAllowConflict(false);

    // First recurrent event: Monday/Wednesday mornings
    List<String> days1 = List.of("MONDAY", "WEDNESDAY");
    RecurrencePattern pattern1 = new RecurrencePattern(4, days1);
    RecurrentEvent recurrentEvent1 = new RecurrentEvent(
        pattern1,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Morning Standup",
        false,
        "Daily standup meeting",
        "Office"
    );

    // Second recurrent event: Tuesday/Thursday afternoons
    List<String> days2 = List.of("TUESDAY", "THURSDAY");
    RecurrencePattern pattern2 = new RecurrencePattern(4, days2);
    RecurrentEvent recurrentEvent2 = new RecurrentEvent(
        pattern2,
        LocalDate.parse("2025-11-04"),
        LocalTime.parse("14:00:00"),
        LocalTime.parse("15:00:00"),
        "Code Review",
        false,
        "Weekly code review session",
        "Office"
    );

    calendar.addRecurrentEvent(recurrentEvent1);
    calendar.addRecurrentEvent(recurrentEvent2);

    assertEquals(2, calendar.getRecurrentEvents().size());
  }

  @Test
  void testAddRecurrentEventsConflictWithExistingEvent() {
    calendar.setAllowConflict(false);

    // Add a single event on Nov 12
    Event singleEvent = new Event.Builder("Special Meeting", "2025-11-12", "2025-11-12")
        .startTime("10:00:00")
        .endTime("11:00:00")
        .build();
    calendar.addEvent(singleEvent);

    // Try to add recurrent events - first occurrence will be on Nov 12 which conflicts
    List<String> days = List.of("WEDNESDAY");
    RecurrencePattern pattern = new RecurrencePattern(3, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-05"), // Start on Nov 5, first event will be Nov 12
        LocalTime.parse("10:30:00"), // Conflicts with singleEvent
        LocalTime.parse("11:30:00"),
        "Weekly Team Sync",
        true,
        "Recurring sync",
        "Room A"
    );

    // Should throw exception because recurrent event conflicts with existing single event
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addRecurrentEvent(recurrentEvent);
    });

    // Verify calendar still only has the original event
    assertEquals(1, calendar.getEventList().size());
    assertEquals(0, calendar.getRecurrentEvents().size());
  }

  @Test
  void testAddRecurrentEventsWithConflictsAllowed() {
    calendar.setAllowConflict(true);

    // Add a single event
    Event singleEvent = new Event.Builder("Important Meeting", "2025-11-03", "2025-11-03")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();
    calendar.addEvent(singleEvent);

    // Add recurrent events that would normally conflict
    List<String> days = List.of("MONDAY");
    RecurrencePattern pattern = new RecurrencePattern(3, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("09:30:00"), // Overlaps with singleEvent
        LocalTime.parse("10:30:00"),
        "Recurring Workshop",
        false,
        "Weekly workshop",
        "Lab"
    );

    calendar.addRecurrentEvent(recurrentEvent);

    assertEquals(1, calendar.getEventList().size()); // 1 single event
    assertEquals(1, calendar.getRecurrentEvents().size()); // 1 recurrent event series
  }

  @Test
  void testAddRecurrentEventsWithDifferentTimesSameDay() {
    calendar.setAllowConflict(false);

    // Morning recurrent events
    List<String> days = List.of("FRIDAY");
    RecurrencePattern pattern1 = new RecurrencePattern(3, days);
    RecurrentEvent morningEvents = new RecurrentEvent(
        pattern1,
        LocalDate.parse("2025-10-31"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Morning Class",
        true,
        "Morning session",
        "Room 101"
    );

    // Afternoon recurrent events on same days
    RecurrencePattern pattern2 = new RecurrencePattern(3, days);
    RecurrentEvent afternoonEvents = new RecurrentEvent(
        pattern2,
        LocalDate.parse("2025-10-31"),
        LocalTime.parse("14:00:00"),
        LocalTime.parse("15:00:00"),
        "Afternoon Class",
        true,
        "Afternoon session",
        "Room 102"
    );

    calendar.addRecurrentEvent(morningEvents);
    calendar.addRecurrentEvent(afternoonEvents);

    assertEquals(2, calendar.getRecurrentEvents().size());
  }

  @Test
  void testAddRecurrentEventsWithAllWeekdays() {
    List<String> days = List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY");
    RecurrencePattern pattern = new RecurrencePattern(10, days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("17:00:00"),
        "Work Day",
        false,
        "Office hours",
        "Office"
    );

    calendar.addRecurrentEvent(recurrentEvent);
    assertEquals(1, calendar.getRecurrentEvents().size());
    assertEquals(10, recurrentEvent.getEvents().size());
  }

  @Test
  void testAddRecurrentEventsStartingOnNonRecurrenceDay() {
    List<String> days = List.of("MONDAY", "WEDNESDAY");
    RecurrencePattern pattern = new RecurrencePattern(4, days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-10-31"), // Friday, not in recurrence days
        LocalTime.parse("10:00:00"),
        LocalTime.parse("11:00:00"),
        "Biweekly Review",
        true,
        "Should start on next Monday",
        "Office"
    );

    calendar.addRecurrentEvent(recurrentEvent);

    assertEquals(1, calendar.getRecurrentEvents().size());
    assertEquals(4, recurrentEvent.getEvents().size());
    // First event should be on Monday, Nov 3
    assertEquals("2025-11-03", recurrentEvent.getEvents().get(0).getStartDate());
  }

  @Test
  void testAddSingleOccurrenceRecurrentEvent() {
    List<String> days = List.of("FRIDAY");
    RecurrencePattern pattern = new RecurrencePattern(1, days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-10-31"),
        LocalTime.parse("18:00:00"),
        LocalTime.parse("19:00:00"),
        "One-time Recurring",
        true,
        "Just one occurrence",
        "Location X"
    );

    calendar.addRecurrentEvent(recurrentEvent);
    assertEquals(1, calendar.getRecurrentEvents().size());
    assertEquals(1, recurrentEvent.getEvents().size());
  }

  @Test
  void testAddRecurrentEventsWithWeekendDays() {
    List<String> days = List.of("SATURDAY", "SUNDAY");
    RecurrencePattern pattern = new RecurrencePattern(4, days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-01"),
        LocalTime.parse("10:00:00"),
        LocalTime.parse("12:00:00"),
        "Weekend Brunch",
        true,
        "Family time",
        "Home"
    );

    calendar.addRecurrentEvent(recurrentEvent);
    assertEquals(1, calendar.getRecurrentEvents().size());
    assertEquals(4, recurrentEvent.getEvents().size());
  }

  @Test
  void testAddRecurrentEventsVerifyPropertiesPreserved() {
    List<String> days = List.of("MONDAY");
    RecurrencePattern pattern = new RecurrencePattern(2, days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("15:00:00"),
        LocalTime.parse("16:30:00"),
        "Property Check Event",
        false,
        "Verify all properties",
        "Test Location"
    );

    calendar.addRecurrentEvent(recurrentEvent);

    // Verify all events have correct properties
    for (Event event : recurrentEvent.getEvents()) {
      assertEquals("Property Check Event", event.getSubject());
      assertEquals("15:00:00", event.getStartTime());
      assertEquals("16:30:00", event.getEndTime());
      assertEquals("Verify all properties", event.getDescription());
      assertEquals(false, event.getPublic());
      assertEquals("Test Location", event.getLocation());
    }
  }

  @Test
  void testAddRecurrentEventsThenAddSingleEvent() {
    calendar.setAllowConflict(false);

    // Add recurrent events first
    List<String> days = List.of("TUESDAY");
    RecurrencePattern pattern = new RecurrencePattern(3, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-04"),
        LocalTime.parse("10:00:00"),
        LocalTime.parse("11:00:00"),
        "Recurring Meeting",
        true,
        "Weekly meeting",
        "Room A"
    );

    calendar.addRecurrentEvent(recurrentEvent);
    assertEquals(1, calendar.getRecurrentEvents().size());

    // Add a single event on a different time - should succeed since no conflict
    Event singleEvent = new Event.Builder("One-off Meeting", "2025-11-04", "2025-11-04")
        .startTime("14:00:00")
        .endTime("15:00:00")
        .build();
    calendar.addEvent(singleEvent);

    assertEquals(1, calendar.getEventList().size());
    assertEquals(1, calendar.getRecurrentEvents().size());
  }

  @Test
  void testAddRecurrentEventsWithExactEndDate() {
    List<String> days = List.of("MONDAY");
    RecurrencePattern pattern = new RecurrencePattern("2025-11-17", days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("13:00:00"),
        LocalTime.parse("14:00:00"),
        "Exact End Date Event",
        false,
        "Ends exactly on Nov 17 (Monday)",
        "Room 101"
    );

    calendar.addRecurrentEvent(recurrentEvent);

    // Nov 3, 10, 17 = 3 Mondays
    assertEquals(1, calendar.getRecurrentEvents().size());
    assertEquals(3, recurrentEvent.getEvents().size());

    // Verify last event is on the end date
    Event lastEvent = recurrentEvent.getEvents().get(recurrentEvent.getEvents().size() - 1);
    assertEquals("2025-11-17", lastEvent.getStartDate());
  }

  @Test
  void testAddMultipleRecurrentSeriesWithDifferentPatterns() {
    calendar.setAllowConflict(false);

    // Weekly Monday meetings
    List<String> days1 = List.of("MONDAY");
    RecurrencePattern pattern1 = new RecurrencePattern(3, days1);
    RecurrentEvent series1 = new RecurrentEvent(
        pattern1,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Monday Standup",
        true,
        "Weekly standup",
        "Office"
    );

    // Multiple days per week
    List<String> days2 = List.of("TUESDAY", "THURSDAY");
    RecurrencePattern pattern2 = new RecurrencePattern(6, days2);
    RecurrentEvent series2 = new RecurrentEvent(
        pattern2,
        LocalDate.parse("2025-11-04"),
        LocalTime.parse("14:00:00"),
        LocalTime.parse("15:00:00"),
        "Training Session",
        false,
        "Bi-weekly training",
        "Training Room"
    );

    // Weekend events
    List<String> days3 = List.of("SATURDAY");
    RecurrencePattern pattern3 = new RecurrencePattern(2, days3);
    RecurrentEvent series3 = new RecurrentEvent(
        pattern3,
        LocalDate.parse("2025-11-01"),
        LocalTime.parse("10:00:00"),
        LocalTime.parse("11:00:00"),
        "Weekend Workshop",
        true,
        "Weekend learning",
        "Workshop"
    );

    calendar.addRecurrentEvent(series1);
    calendar.addRecurrentEvent(series2);
    calendar.addRecurrentEvent(series3);

    assertEquals(3, calendar.getRecurrentEvents().size()); // 3 series
  }

  @Test
  void testAddRecurrentEventsWithEndDateSpanningMultipleWeeks() {
    List<String> days = List.of("WEDNESDAY", "FRIDAY");
    RecurrencePattern pattern = new RecurrencePattern("2025-11-28", days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-05"),
        LocalTime.parse("11:00:00"),
        LocalTime.parse("12:00:00"),
        "Multi-week Event",
        true,
        "Spanning multiple weeks",
        "Various"
    );

    calendar.addRecurrentEvent(recurrentEvent);

    // From Nov 5 to Nov 28:
    // Wednesdays: 5, 12, 19, 26 = 4
    // Fridays: 7, 14, 21, 28 = 4
    // Total = 8
    assertEquals(1, calendar.getRecurrentEvents().size());
    assertEquals(8, recurrentEvent.getEvents().size());
  }

  @Test
  void testCannotAddDuplicateRecurrentEventToCalendar() {
    calendar.setAllowConflict(false);

    List<String> days = List.of("MONDAY");
    RecurrencePattern pattern = new RecurrencePattern(2, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Duplicate Test",
        true,
        "Testing duplicates",
        "Office"
    );

    // Add the recurrent event once
    calendar.addRecurrentEvent(recurrentEvent);

    // Try to add the same recurrent event again
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addRecurrentEvent(recurrentEvent);
    });
  }

  @Test
  void testAddRecurrentEventConflictsWithAnotherRecurrentEvent() {
    calendar.setAllowConflict(false);

    // Add first recurrent event: Mondays 9-10am
    List<String> days1 = List.of("MONDAY");
    RecurrencePattern pattern1 = new RecurrencePattern(3, days1);
    RecurrentEvent recurrentEvent1 = new RecurrentEvent(
        pattern1,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Morning Meeting",
        true,
        "Weekly meeting",
        "Room A"
    );
    calendar.addRecurrentEvent(recurrentEvent1);

    // Try to add second recurrent event: Mondays 9:30-10:30am (conflicts)
    List<String> days2 = List.of("MONDAY");
    RecurrencePattern pattern2 = new RecurrencePattern(2, days2);
    RecurrentEvent recurrentEvent2 = new RecurrentEvent(
        pattern2,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("09:30:00"),
        LocalTime.parse("10:30:00"),
        "Another Meeting",
        true,
        "Conflicting meeting",
        "Room B"
    );

    // Should throw exception because recurrent events conflict
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addRecurrentEvent(recurrentEvent2);
    });

    // Verify only first recurrent event was added
    assertEquals(1, calendar.getRecurrentEvents().size());
  }

  @Test
  void testAddSingleEventConflictsWithRecurrentEvent() {
    calendar.setAllowConflict(false);

    // Add recurrent event: Wednesdays 2-3pm
    List<String> days = List.of("WEDNESDAY");
    RecurrencePattern pattern = new RecurrencePattern(3, days);
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

    // Try to add single event on Nov 12 (Wednesday) at 2:30-3:30pm (conflicts)
    Event singleEvent = new Event.Builder("Special Event", "2025-11-12", "2025-11-12")
        .startTime("14:30:00")
        .endTime("15:30:00")
        .build();

    // Should throw exception because single event conflicts with recurrent event
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addEvent(singleEvent);
    });

    assertEquals(0, calendar.getEventList().size());
    assertEquals(1, calendar.getRecurrentEvents().size());
  }

  @Test
  void testAddRecurrentEventsStartingOnRecurrenceDay() {
    // Start on Monday when MONDAY is in the recurrence pattern
    List<String> days = List.of("MONDAY");
    RecurrencePattern pattern = new RecurrencePattern(3, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-03"), // Monday, IS in recurrence days
        LocalTime.parse("10:00:00"),
        LocalTime.parse("11:00:00"),
        "Monday Meeting",
        true,
        "Should include start date",
        "Office"
    );

    calendar.addRecurrentEvent(recurrentEvent);

    assertEquals(1, calendar.getRecurrentEvents().size());
    assertEquals(3, recurrentEvent.getEvents().size());
    // First event should be on the start date itself (Monday, Nov 3)
    assertEquals("2025-11-03", recurrentEvent.getEvents().get(0).getStartDate());
    // Second event should be next Monday (Nov 10)
    assertEquals("2025-11-10", recurrentEvent.getEvents().get(1).getStartDate());
    // Third event should be the Monday after (Nov 17)
    assertEquals("2025-11-17", recurrentEvent.getEvents().get(2).getStartDate());
  }

  @Test
  void testAddRecurrentEventsStartingOnRecurrenceDayWithMultipleDays() {
    // Start on Wednesday when both WEDNESDAY and FRIDAY are in pattern
    List<String> days = List.of("WEDNESDAY", "FRIDAY");
    RecurrencePattern pattern = new RecurrencePattern(4, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-05"), // Wednesday, IS in recurrence days
        LocalTime.parse("14:00:00"),
        LocalTime.parse("15:00:00"),
        "Biweekly Sync",
        false,
        "Should start on Wednesday",
        "Room A"
    );

    calendar.addRecurrentEvent(recurrentEvent);

    assertEquals(1, calendar.getRecurrentEvents().size());
    assertEquals(4, recurrentEvent.getEvents().size());
    // First event should be on start date (Wednesday, Nov 5)
    assertEquals("2025-11-05", recurrentEvent.getEvents().get(0).getStartDate());
    // Second event should be Friday, Nov 7
    assertEquals("2025-11-07", recurrentEvent.getEvents().get(1).getStartDate());
    // Third event should be Wednesday, Nov 12
    assertEquals("2025-11-12", recurrentEvent.getEvents().get(2).getStartDate());
    // Fourth event should be Friday, Nov 14
    assertEquals("2025-11-14", recurrentEvent.getEvents().get(3).getStartDate());
  }

  @Test
  void testAddRecurrentEventsWithEndDateStartingOnRecurrenceDay() {
    // Start on Tuesday when TUESDAY is in pattern, with end date
    List<String> days = List.of("TUESDAY", "THURSDAY");
    RecurrencePattern pattern = new RecurrencePattern("2025-11-18", days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-04"), // Tuesday, IS in recurrence days
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Morning Standup",
        true,
        "Daily standup",
        "Office"
    );

    calendar.addRecurrentEvent(recurrentEvent);

    assertEquals(1, calendar.getRecurrentEvents().size());
    // From Nov 4 to Nov 18:
    // Tuesdays: Nov 4, 11, 18 = 3
    // Thursdays: Nov 6, 13 = 2
    // Total = 5 events
    assertEquals(5, recurrentEvent.getEvents().size());
    // First event should be on start date (Tuesday, Nov 4)
    assertEquals("2025-11-04", recurrentEvent.getEvents().get(0).getStartDate());
  }

  @Test
  void testAddRecurrentEventsSingleOccurrenceOnStartDate() {
    // Start on Friday when FRIDAY is in pattern, only 1 occurrence
    List<String> days = List.of("FRIDAY");
    RecurrencePattern pattern = new RecurrencePattern(1, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-07"), // Friday, IS in recurrence days
        LocalTime.parse("16:00:00"),
        LocalTime.parse("17:00:00"),
        "One-time Friday Event",
        false,
        "Should only create one event",
        "Conference Room"
    );

    calendar.addRecurrentEvent(recurrentEvent);

    assertEquals(1, calendar.getRecurrentEvents().size());
    assertEquals(1, recurrentEvent.getEvents().size());
    // The only event should be on the start date itself (Friday, Nov 7)
    assertEquals("2025-11-07", recurrentEvent.getEvents().get(0).getStartDate());
  }

  @Test
  void testAddRecurrentEventsVerifyDatesWhenStartingOnRecurrenceDay() {
    // Verify all generated dates are correct when starting on a recurrence day
    List<String> days = List.of("MONDAY", "WEDNESDAY");
    RecurrencePattern pattern = new RecurrencePattern(6, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-03"), // Monday, IS in recurrence days
        LocalTime.parse("11:00:00"),
        LocalTime.parse("12:00:00"),
        "Test Event",
        true,
        "Testing dates",
        "Office"
    );

    calendar.addRecurrentEvent(recurrentEvent);

    List<Event> events = recurrentEvent.getEvents();
    assertEquals(6, events.size());

    // Verify each date
    assertEquals("2025-11-03", events.get(0).getStartDate()); // Monday
    assertEquals("2025-11-05", events.get(1).getStartDate()); // Wednesday
    assertEquals("2025-11-10", events.get(2).getStartDate()); // Monday
    assertEquals("2025-11-12", events.get(3).getStartDate()); // Wednesday
    assertEquals("2025-11-17", events.get(4).getStartDate()); // Monday
    assertEquals("2025-11-19", events.get(5).getStartDate()); // Wednesday
  }

  @Test
  void testGetEventByIdFromRecurrentEvents() {
    List<String> days = List.of("MONDAY");
    RecurrencePattern pattern = new RecurrencePattern(2, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Recurring Meeting",
        true,
        "Weekly meeting",
        "Room A"
    );
    calendar.addRecurrentEvent(recurrentEvent);

    Event firstInstance = recurrentEvent.getEvents().get(0);
    String eventId = firstInstance.getId();

    Event retrievedEvent = calendar.getEventById(eventId);

    assertNotNull(retrievedEvent);
    assertEquals(firstInstance, retrievedEvent);
  }

  @Test
  void testRecurrentEventIdIsUnique() {
    List<String> days1 = List.of("MONDAY");
    RecurrencePattern pattern1 = new RecurrencePattern(2, days1);
    RecurrentEvent recurrentEvent1 = new RecurrentEvent(
        pattern1,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Meeting 1",
        true,
        "Description",
        "Location"
    );

    List<String> days2 = List.of("TUESDAY");
    RecurrencePattern pattern2 = new RecurrencePattern(2, days2);
    RecurrentEvent recurrentEvent2 = new RecurrentEvent(
        pattern2,
        LocalDate.parse("2025-11-04"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Meeting 2",
        true,
        "Description",
        "Location"
    );

    assertNotEquals(recurrentEvent1.getId(), recurrentEvent2.getId());
  }
}
