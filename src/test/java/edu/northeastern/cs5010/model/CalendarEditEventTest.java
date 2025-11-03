package edu.northeastern.cs5010.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for editing single events in a Calendar.
 * Tests editing event properties and conflict detection during edits.
 */
class CalendarEditEventTest {

  private Calendar calendar;

  @BeforeEach
  void setUp() {
    calendar = new Calendar("Personal Calendar");
  }

  // ==================== Edit Event Tests ====================

  @Test
  void testEditEventSubjectOnly() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();
    calendar.addEvent(event);

    String eventId = event.getId();
    calendar.editEvent(eventId, "Updated Meeting", null, null, null, null, null, null, null);

    assertEquals("Updated Meeting", event.getSubject());
    assertEquals("2025-11-01", event.getStartDate());
    assertEquals("09:00:00", event.getStartTime());
  }

  @Test
  void testEditEventDateOnly() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();
    calendar.addEvent(event);

    String eventId = event.getId();
    calendar.editEvent(eventId, null, "2025-11-05", "2025-11-05", null, null, null, null, null);

    assertEquals("Meeting", event.getSubject());
    assertEquals("2025-11-05", event.getStartDate());
    assertEquals("2025-11-05", event.getEndDate());
    assertEquals("09:00:00", event.getStartTime());
  }

  @Test
  void testEditEventTimeOnly() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();
    calendar.addEvent(event);

    String eventId = event.getId();
    calendar.editEvent(eventId, null, null, null, "14:00:00", "15:00:00", null, null, null);

    assertEquals("Meeting", event.getSubject());
    assertEquals("2025-11-01", event.getStartDate());
    assertEquals("14:00:00", event.getStartTime());
    assertEquals("15:00:00", event.getEndTime());
  }

  @Test
  void testEditEventAllFields() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .isPublic(true)
        .description("Old description")
        .location("Room A")
        .build();
    calendar.addEvent(event);

    String eventId = event.getId();
    calendar.editEvent(eventId, "Conference", "2025-11-10", "2025-11-10",
        "14:00:00", "16:00:00", false, "New description", "Room B");

    assertEquals("Conference", event.getSubject());
    assertEquals("2025-11-10", event.getStartDate());
    assertEquals("2025-11-10", event.getEndDate());
    assertEquals("14:00:00", event.getStartTime());
    assertEquals("16:00:00", event.getEndTime());
    assertEquals(false, event.getPublic());
    assertEquals("New description", event.getDescription());
    assertEquals("Room B", event.getLocation());
  }

  @Test
  void testEditEventNonExistentId() {
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editEvent("non-existent-id", "New Subject", null, null, null, null, null, null, null);
    });
  }

  @Test
  void testEditEventEndDateBeforeStartDate() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();
    calendar.addEvent(event);

    String eventId = event.getId();
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editEvent(eventId, null, "2025-11-10", "2025-11-05", null, null, null, null, null);
    });
  }

  @Test
  void testEditEventEndTimeBeforeStartTime() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();
    calendar.addEvent(event);

    String eventId = event.getId();
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editEvent(eventId, null, null, null, "14:00:00", "13:00:00", null, null, null);
    });
  }

  @Test
  void testEditEventEndTimeEqualsStartTime() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();
    calendar.addEvent(event);

    String eventId = event.getId();
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editEvent(eventId, null, null, null, "14:00:00", "14:00:00", null, null, null);
    });
  }

  @Test
  void testEditEventCreatesConflict() {
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
    calendar.addEvent(event2);

    String event2Id = event2.getId();
    // Try to edit event2 to overlap with event1
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editEvent(event2Id, null, null, null, "09:30:00", "10:30:00", null, null, null);
    });
  }

  @Test
  void testEditEventNoConflictWhenAllowed() {
    calendar.setAllowConflict(true);

    Event event1 = new Event.Builder("Meeting 1", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    Event event2 = new Event.Builder("Meeting 2", "2025-11-01", "2025-11-01")
        .startTime("14:00:00")
        .endTime("15:00:00")
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    String event2Id = event2.getId();
    // Edit event2 to overlap with event1 - should succeed when conflicts are allowed
    calendar.editEvent(event2Id, null, null, null, "09:30:00", "10:30:00", null, null, null);

    assertEquals("09:30:00", event2.getStartTime());
    assertEquals("10:30:00", event2.getEndTime());
  }

  @Test
  void testEditEventConflictWithRecurrentEvent() {
    calendar.setAllowConflict(false);

    // Add a recurrent event
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

    // Add a single event on a different time
    Event singleEvent = new Event.Builder("Single Meeting", "2025-11-03", "2025-11-03")
        .startTime("14:00:00")
        .endTime("15:00:00")
        .build();
    calendar.addEvent(singleEvent);

    String eventId = singleEvent.getId();
    // Try to edit to conflict with recurrent event
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editEvent(eventId, null, null, null, "09:30:00", "10:30:00", null, null, null);
    });
  }

  @Test
  void testEditRecurrentEventInstance() {
    List<String> days = List.of("MONDAY", "WEDNESDAY");
    RecurrencePattern pattern = new RecurrencePattern(4, days);
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

    // Get the first instance and edit it
    Event firstInstance = recurrentEvent.getEvents().get(0);
    String eventId = firstInstance.getId();

    calendar.editEvent(eventId, "Updated Recurring Meeting", null, null,
        "10:00:00", "11:00:00", null, null, null);

    assertEquals("Updated Recurring Meeting", firstInstance.getSubject());
    assertEquals("10:00:00", firstInstance.getStartTime());
    assertEquals("11:00:00", firstInstance.getEndTime());
  }

  @Test
  void testEditEventMultiDayEvent() {
    Event event = new Event.Builder("Conference", "2025-11-01", "2025-11-03")
        .startTime("09:00:00")
        .endTime("17:00:00")
        .build();
    calendar.addEvent(event);

    String eventId = event.getId();
    calendar.editEvent(eventId, null, "2025-11-10", "2025-11-12", null, null, null, null, null);

    assertEquals("2025-11-10", event.getStartDate());
    assertEquals("2025-11-12", event.getEndDate());
  }

  @Test
  void testEditEventAllDayToTimedEvent() {
    Event event = new Event.Builder("Holiday", "2025-11-01", "2025-11-01")
        .build();
    calendar.addEvent(event);

    String eventId = event.getId();
    calendar.editEvent(eventId, null, null, null, "09:00:00", "17:00:00", null, null, null);

    assertEquals("09:00:00", event.getStartTime());
    assertEquals("17:00:00", event.getEndTime());
  }

  @Test
  void testEditEventDescriptionAndLocation() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .description("Initial description")
        .location("Room A")
        .build();
    calendar.addEvent(event);

    String eventId = event.getId();
    calendar.editEvent(eventId, null, null, null, null, null, null,
        "Updated description", "Room B");

    assertEquals("Updated description", event.getDescription());
    assertEquals("Room B", event.getLocation());
    assertEquals("Meeting", event.getSubject());
  }

  @Test
  void testEditEventVisibility() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .isPublic(true)
        .build();
    calendar.addEvent(event);

    String eventId = event.getId();
    calendar.editEvent(eventId, null, null, null, null, null, false, null, null);

    assertEquals(false, event.getPublic());
  }

  @Test
  void testEditEventPartialDateChange() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();
    calendar.addEvent(event);

    String eventId = event.getId();
    // Change only end date to make it a multi-day event
    calendar.editEvent(eventId, null, null, "2025-11-03", null, null, null, null, null);

    assertEquals("2025-11-01", event.getStartDate());
    assertEquals("2025-11-03", event.getEndDate());
  }
}
