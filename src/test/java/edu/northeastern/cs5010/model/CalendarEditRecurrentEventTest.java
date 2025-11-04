package edu.northeastern.cs5010.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for editing recurrent event series in a Calendar.
 * Tests editing all instances in a series and conflict detection.
 */
class CalendarEditRecurrentEventTest {

  private Calendar calendar;

  @BeforeEach
  void setUp() {
    calendar = new Calendar("Personal Calendar");
  }

  // ==================== Edit Recurring Event Series Tests ====================

  @Test
  void testEditRecurrentEventSeriesSubject() {
    List<String> days = List.of("MONDAY", "WEDNESDAY");
    RecurrencePattern pattern = new RecurrencePattern(4, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Weekly Meeting",
        true,
        "Team sync",
        "Room A"
    );
    calendar.addRecurrentEvent(recurrentEvent);

    String recurrentEventId = recurrentEvent.getId();
    calendar.editRecurrentEvent(recurrentEventId, "Updated Weekly Meeting", null, null, null, null, null);

    // Verify all instances have been updated
    for (Event instance : recurrentEvent.getEvents()) {
      assertEquals("Updated Weekly Meeting", instance.getSubject());
      assertEquals("09:00:00", instance.getStartTime());
      assertEquals("Team sync", instance.getDescription());
    }
  }

  @Test
  void testEditRecurrentEventSeriesTimes() {
    List<String> days = List.of("TUESDAY", "THURSDAY");
    RecurrencePattern pattern = new RecurrencePattern(3, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-04"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Morning Standup",
        false,
        "Daily standup",
        "Office"
    );
    calendar.addRecurrentEvent(recurrentEvent);

    String recurrentEventId = recurrentEvent.getId();
    calendar.editRecurrentEvent(recurrentEventId, null, "14:00:00", "15:00:00", null, null, null);

    // Verify all instances have updated times
    for (Event instance : recurrentEvent.getEvents()) {
      assertEquals("Morning Standup", instance.getSubject());
      assertEquals("14:00:00", instance.getStartTime());
      assertEquals("15:00:00", instance.getEndTime());
    }
  }

  @Test
  void testEditRecurrentEventSeriesAllFields() {
    List<String> days = List.of("FRIDAY");
    RecurrencePattern pattern = new RecurrencePattern(2, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-07"),
        LocalTime.parse("10:00:00"),
        LocalTime.parse("11:00:00"),
        "Weekly Review",
        true,
        "Old description",
        "Room A"
    );
    calendar.addRecurrentEvent(recurrentEvent);

    String recurrentEventId = recurrentEvent.getId();
    calendar.editRecurrentEvent(recurrentEventId, "Monthly Review", "15:00:00", "16:00:00",
        false, "New description", "Room B");

    // Verify all instances have been fully updated
    for (Event instance : recurrentEvent.getEvents()) {
      assertEquals("Monthly Review", instance.getSubject());
      assertEquals("15:00:00", instance.getStartTime());
      assertEquals("16:00:00", instance.getEndTime());
      assertEquals(false, instance.getPublic());
      assertEquals("New description", instance.getDescription());
      assertEquals("Room B", instance.getLocation());
    }
  }

  @Test
  void testEditRecurrentEventSeriesNonExistentId() {
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editRecurrentEvent("non-existent-id", "New Subject", null, null, null, null, null);
    });
  }

  @Test
  void testEditRecurrentEventSeriesInvalidTimes() {
    List<String> days = List.of("MONDAY");
    RecurrencePattern pattern = new RecurrencePattern(2, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Meeting",
        true,
        "Description",
        "Location"
    );
    calendar.addRecurrentEvent(recurrentEvent);

    String recurrentEventId = recurrentEvent.getId();
    // Try to set end time before start time
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editRecurrentEvent(recurrentEventId, null, "14:00:00", "13:00:00", null, null, null);
    });
  }

  @Test
  void testEditRecurrentEventSeriesEndTimeEqualsStartTime() {
    List<String> days = List.of("WEDNESDAY");
    RecurrencePattern pattern = new RecurrencePattern(2, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-05"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Meeting",
        true,
        "Description",
        "Location"
    );
    calendar.addRecurrentEvent(recurrentEvent);

    String recurrentEventId = recurrentEvent.getId();
    // Try to set end time equal to start time
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editRecurrentEvent(recurrentEventId, null, "14:00:00", "14:00:00", null, null, null);
    });
  }

  @Test
  void testEditRecurrentEventSeriesCreatesConflict() {
    calendar.setAllowConflict(false);

    // Add a single event
    Event singleEvent = new Event.Builder("Important Meeting", "2025-11-03", "2025-11-03")
        .startTime("14:00:00")
        .endTime("15:00:00")
        .build();
    calendar.addEvent(singleEvent);

    // Add a recurrent event at different times
    List<String> days = List.of("MONDAY", "WEDNESDAY");
    RecurrencePattern pattern = new RecurrencePattern(3, days);
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

    String recurrentEventId = recurrentEvent.getId();
    // Try to edit recurrent event to conflict with single event
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editRecurrentEvent(recurrentEventId, null, "14:30:00", "15:30:00", null, null, null);
    });
  }

  @Test
  void testEditRecurrentEventSeriesNoConflictWhenAllowed() {
    calendar.setAllowConflict(true);

    // Add a single event
    Event singleEvent = new Event.Builder("Important Meeting", "2025-11-03", "2025-11-03")
        .startTime("14:00:00")
        .endTime("15:00:00")
        .build();
    calendar.addEvent(singleEvent);

    // Add a recurrent event at different times
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

    String recurrentEventId = recurrentEvent.getId();
    // Edit to overlap - should succeed when conflicts are allowed
    calendar.editRecurrentEvent(recurrentEventId, null, "14:30:00", "15:30:00", null, null, null);

    for (Event instance : recurrentEvent.getEvents()) {
      assertEquals("14:30:00", instance.getStartTime());
      assertEquals("15:30:00", instance.getEndTime());
    }
  }

  @Test
  void testEditRecurrentEventSeriesConflictWithAnotherRecurrentSeries() {
    calendar.setAllowConflict(false);

    // Add first recurrent event series
    List<String> days1 = List.of("MONDAY", "WEDNESDAY");
    RecurrencePattern pattern1 = new RecurrencePattern(3, days1);
    RecurrentEvent recurrentEvent1 = new RecurrentEvent(
        pattern1,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Morning Meeting",
        true,
        "First series",
        "Room A"
    );
    calendar.addRecurrentEvent(recurrentEvent1);

    // Add second recurrent event series at different times
    List<String> days2 = List.of("MONDAY");
    RecurrencePattern pattern2 = new RecurrencePattern(2, days2);
    RecurrentEvent recurrentEvent2 = new RecurrentEvent(
        pattern2,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("14:00:00"),
        LocalTime.parse("15:00:00"),
        "Afternoon Meeting",
        false,
        "Second series",
        "Room B"
    );
    calendar.addRecurrentEvent(recurrentEvent2);

    String recurrentEvent2Id = recurrentEvent2.getId();
    // Try to edit second series to conflict with first series
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editRecurrentEvent(recurrentEvent2Id, null, "09:30:00", "10:30:00", null, null, null);
    });
  }

  @Test
  void testEditSingleInstanceDoesNotAffectOtherInstances() {
    List<String> days = List.of("TUESDAY", "THURSDAY");
    RecurrencePattern pattern = new RecurrencePattern(4, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-04"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Recurring Meeting",
        true,
        "Team sync",
        "Room A"
    );
    calendar.addRecurrentEvent(recurrentEvent);

    // Get the first instance and edit it
    Event firstInstance = recurrentEvent.getEvents().get(0);
    String firstInstanceId = firstInstance.getId();

    calendar.editEvent(firstInstanceId, "Modified Single Instance", null, null,
        "11:00:00", "12:00:00", null, null, null);

    // Verify only the first instance was modified
    assertEquals("Modified Single Instance", firstInstance.getSubject());
    assertEquals("11:00:00", firstInstance.getStartTime());

    // Verify other instances remain unchanged
    for (int i = 1; i < recurrentEvent.getEvents().size(); i++) {
      Event instance = recurrentEvent.getEvents().get(i);
      assertEquals("Recurring Meeting", instance.getSubject());
      assertEquals("09:00:00", instance.getStartTime());
      assertEquals("10:00:00", instance.getEndTime());
    }
  }

  @Test
  void testEditRecurrentEventSeriesDescriptionAndLocation() {
    List<String> days = List.of("WEDNESDAY");
    RecurrencePattern pattern = new RecurrencePattern(2, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-05"),
        LocalTime.parse("10:00:00"),
        LocalTime.parse("11:00:00"),
        "Team Sync",
        true,
        "Old description",
        "Old location"
    );
    calendar.addRecurrentEvent(recurrentEvent);

    String recurrentEventId = recurrentEvent.getId();
    calendar.editRecurrentEvent(recurrentEventId, null, null, null, null,
        "Updated description", "Updated location");

    // Verify all instances have updated description and location
    for (Event instance : recurrentEvent.getEvents()) {
      assertEquals("Team Sync", instance.getSubject());
      assertEquals("Updated description", instance.getDescription());
      assertEquals("Updated location", instance.getLocation());
    }
  }

  @Test
  void testEditRecurrentEventSeriesWithNoInstances() {
    // Create a recurrent event
    List<String> days = List.of("MONDAY");
    RecurrencePattern pattern = new RecurrencePattern(2, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Meeting",
        true,
        "Description",
        "Location"
    );
    calendar.addRecurrentEvent(recurrentEvent);

    String recurrentEventId = recurrentEvent.getId();

    // Manually clear all instances to simulate an empty recurrent event
    // This tests the validation at Calendar.java:555-557
    recurrentEvent.getEvents().clear();

    // Try to edit the recurrent event with no instances - should throw exception
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editRecurrentEvent(recurrentEventId, "Updated Meeting", null, null, null, null, null);
    });
  }

  @Test
  void testEditRecurrentEventSeriesMismatchedTimeNullability_OnlyEndTimeProvided() {
    // Create timed recurrent events, convert to all-day, then try to add only end time
    List<String> days = List.of("MONDAY");
    RecurrencePattern pattern = new RecurrencePattern(2, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Meeting",
        true,
        "Description",
        "Location"
    );
    calendar.addRecurrentEvent(recurrentEvent);

    String recurrentEventId = recurrentEvent.getId();

    // First, manually set all instances to have null times (simulating all-day events)
    for (Event instance : recurrentEvent.getEvents()) {
      instance.setStartTime(null);
      instance.setEndTime(null);
    }

    // Now try to add only end time (start time remains null) - should throw exception
    // This tests the validation at Calendar.java:572-576
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editRecurrentEvent(recurrentEventId, null, null, "10:00:00", null, null, null);
    });
  }

  @Test
  void testEditRecurrentEventSeriesMismatchedTimeNullability_OnlyStartTimeProvided() {
    // Create timed recurrent events, convert to all-day, then try to add only start time
    List<String> days = List.of("TUESDAY");
    RecurrencePattern pattern = new RecurrencePattern(2, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-04"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Meeting",
        true,
        "Description",
        "Location"
    );
    calendar.addRecurrentEvent(recurrentEvent);

    String recurrentEventId = recurrentEvent.getId();

    // First, manually set all instances to have null times (simulating all-day events)
    for (Event instance : recurrentEvent.getEvents()) {
      instance.setStartTime(null);
      instance.setEndTime(null);
    }

    // Now try to add only start time (end time remains null) - should throw exception
    // This tests the validation at Calendar.java:572-576
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editRecurrentEvent(recurrentEventId, null, "09:00:00", null, null, null, null);
    });
  }

  @Test
  void testEditRecurrentEventSeriesKeepAllDayTimes() {
    // First, create a timed recurrent event and convert it to all-day
    List<String> days = List.of("FRIDAY");
    RecurrencePattern pattern = new RecurrencePattern(2, days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-07"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Meeting",
        true,
        "Description",
        "Location"
    );
    calendar.addRecurrentEvent(recurrentEvent);

    String recurrentEventId = recurrentEvent.getId();

    // First, manually set all instances to have null times (simulating conversion to all-day)
    for (Event instance : recurrentEvent.getEvents()) {
      instance.setStartTime(null);
      instance.setEndTime(null);
    }

    // Now edit other properties without providing times
    // This tests the else branch at Calendar.java:648-650
    calendar.editRecurrentEvent(recurrentEventId, "Updated Meeting", null, null, null, null, null);

    // Verify all instances still have null times and updated subject
    for (Event instance : recurrentEvent.getEvents()) {
      assertNull(instance.getStartTime());
      assertNull(instance.getEndTime());
      assertEquals("Updated Meeting", instance.getSubject());
    }
  }

  @Test
  void testEditRecurrentEventSeriesConflictWithSingleEvent() {
    calendar.setAllowConflict(false);

    // Add a single event
    Event singleEvent = new Event.Builder("Important Meeting", "2025-11-03", "2025-11-03")
        .startTime("14:00:00")
        .endTime("15:00:00")
        .build();
    calendar.addEvent(singleEvent);

    // Add a recurrent event at different times
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

    String recurrentEventId = recurrentEvent.getId();
    // Try to edit recurrent event to conflict with single event - should be caught by conflict check
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editRecurrentEvent(recurrentEventId, null, "14:00:00", "15:00:00", null, null, null);
    });
  }

  @Test
  void testEditRecurrentEventSeriesConflictWithSingleEventReverseOverlap() {
    calendar.setAllowConflict(false);

    // Add a single event
    Event singleEvent = new Event.Builder("Important Meeting", "2025-11-03", "2025-11-03")
        .startTime("14:00:00")
        .endTime("15:00:00")
        .build();
    calendar.addEvent(singleEvent);

    // Add a recurrent event at different times
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

    String recurrentEventId = recurrentEvent.getId();
    // Edit recurrent event to completely contain the single event's time range
    // This tests the second condition: isOverlapping(existingEvent, tempEvent)
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editRecurrentEvent(recurrentEventId, null, "13:00:00", "16:00:00", null, null, null);
    });
  }

  @Test
  void testEditRecurrentEventSeriesConflictWithOtherRecurrentSeries() {
    calendar.setAllowConflict(false);

    // Add first recurrent event series
    List<String> days1 = List.of("MONDAY", "WEDNESDAY");
    RecurrencePattern pattern1 = new RecurrencePattern(2, days1);
    RecurrentEvent recurrentEvent1 = new RecurrentEvent(
        pattern1,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Morning Meeting",
        true,
        "First series",
        "Room A"
    );
    calendar.addRecurrentEvent(recurrentEvent1);

    // Add second recurrent event series at different times
    List<String> days2 = List.of("MONDAY");
    RecurrencePattern pattern2 = new RecurrencePattern(2, days2);
    RecurrentEvent recurrentEvent2 = new RecurrentEvent(
        pattern2,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("14:00:00"),
        LocalTime.parse("15:00:00"),
        "Afternoon Meeting",
        false,
        "Second series",
        "Room B"
    );
    calendar.addRecurrentEvent(recurrentEvent2);

    String recurrentEvent2Id = recurrentEvent2.getId();
    // Try to edit second series to conflict with first series - should be caught
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editRecurrentEvent(recurrentEvent2Id, null, "09:00:00", "10:00:00", null, null, null);
    });
  }

  @Test
  void testEditRecurrentEventSeriesConflictWithOtherRecurrentSeriesReverseOverlap() {
    calendar.setAllowConflict(false);

    // Add first recurrent event series
    List<String> days1 = List.of("MONDAY");
    RecurrencePattern pattern1 = new RecurrencePattern(2, days1);
    RecurrentEvent recurrentEvent1 = new RecurrentEvent(
        pattern1,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Morning Meeting",
        true,
        "First series",
        "Room A"
    );
    calendar.addRecurrentEvent(recurrentEvent1);

    // Add second recurrent event series at different times
    List<String> days2 = List.of("MONDAY");
    RecurrencePattern pattern2 = new RecurrencePattern(2, days2);
    RecurrentEvent recurrentEvent2 = new RecurrentEvent(
        pattern2,
        LocalDate.parse("2025-11-03"),
        LocalTime.parse("14:00:00"),
        LocalTime.parse("15:00:00"),
        "Afternoon Meeting",
        false,
        "Second series",
        "Room B"
    );
    calendar.addRecurrentEvent(recurrentEvent2);

    String recurrentEvent2Id = recurrentEvent2.getId();
    // Edit second series to completely contain first series's time range
    // This tests the second condition: isOverlapping(recurrentEventInstance, tempEvent)
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editRecurrentEvent(recurrentEvent2Id, null, "08:00:00", "11:00:00", null, null, null);
    });
  }
}
