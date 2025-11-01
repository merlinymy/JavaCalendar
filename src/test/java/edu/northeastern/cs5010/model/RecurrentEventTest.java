package edu.northeastern.cs5010.model;

import static org.junit.jupiter.api.Assertions.*;

import edu.northeastern.cs5010.model.Event.Builder;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class RecurrentEventTest {
  @Test
  void testCreationWithValidBuilderAndOccurrence(){

    List<String> days = List.of("MONDAY", "SATURDAY");
    RecurrencePattern pattern = new RecurrencePattern(5, days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-10-31"),
        LocalTime.parse("10:30:00"),
        LocalTime.parse("22:00:00"),
        "Test Event",
        true,
        "Test Description",
        "Test Location"
    );

    assertEquals(5, recurrentEvent.getEvents().size());

  }

  @Test
  void testCreationWithValidBuilderAndEndDate(){

    List<String> days = List.of("MONDAY", "SATURDAY");
    RecurrencePattern pattern = new RecurrencePattern("2025-11-14", days);
    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-10-31"),
        LocalTime.parse("10:30:00"),
        LocalTime.parse("22:00:00"),
        "Test Event",
        true,
        "Test Description",
        "Test Location"
    );

    assertEquals(4, recurrentEvent.getEvents().size());

  }

  @Test
  void testCreationWithSingleDayRecurrence(){
    List<String> days = List.of("FRIDAY");
    RecurrencePattern pattern = new RecurrencePattern(3, days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-10-31"), // Friday
        LocalTime.parse("09:00:00"),
        LocalTime.parse("10:00:00"),
        "Weekly Meeting",
        true,
        "Team standup",
        "Conference Room A"
    );

    assertEquals(3, recurrentEvent.getEvents().size());
    assertEquals("Weekly Meeting", recurrentEvent.getEvents().get(0).getSubject());
  }

  @Test
  void testCreationWithMultipleDaysInWeek(){
    List<String> days = List.of("MONDAY", "WEDNESDAY", "FRIDAY");
    RecurrencePattern pattern = new RecurrencePattern(6, days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-03"), // Monday
        LocalTime.parse("14:00:00"),
        LocalTime.parse("15:30:00"),
        "Workout Session",
        false,
        "Gym time",
        "Fitness Center"
    );

    assertEquals(6, recurrentEvent.getEvents().size());
  }

  @Test
  void testCreationWithEndDateSpanningMultipleWeeks(){
    List<String> days = List.of("TUESDAY", "THURSDAY");
    RecurrencePattern pattern = new RecurrencePattern("2025-11-20", days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-04"), // Tuesday
        LocalTime.parse("08:00:00"),
        LocalTime.parse("09:00:00"),
        "Morning Yoga",
        true,
        "Daily yoga practice",
        "Studio B"
    );

    // From Nov 4 to Nov 20: Tuesdays (4, 11, 18) and Thursdays (6, 13, 20) = 6 events
    assertEquals(6, recurrentEvent.getEvents().size());
  }

  @Test
  void testCreationWithAllWeekdays(){
    List<String> days = List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY");
    RecurrencePattern pattern = new RecurrencePattern(10, days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-03"), // Monday
        LocalTime.parse("09:00:00"),
        LocalTime.parse("17:00:00"),
        "Work Day",
        false,
        "Office hours",
        "Office"
    );

    assertEquals(10, recurrentEvent.getEvents().size());
  }

  @Test
  void testCreationWithWeekendDays(){
    List<String> days = List.of("SATURDAY", "SUNDAY");
    RecurrencePattern pattern = new RecurrencePattern(4, days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-01"), // Saturday
        LocalTime.parse("10:00:00"),
        LocalTime.parse("12:00:00"),
        "Weekend Brunch",
        true,
        "Family time",
        "Home"
    );

    assertEquals(4, recurrentEvent.getEvents().size());
  }

  @Test
  void testInvalidConstructionWithMoreDaysThanOccurrences(){
    List<String> days = List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY");
    RecurrencePattern pattern = new RecurrencePattern(3, days);

    assertThrows(IllegalArgumentException.class, () -> {
      new RecurrentEvent(
          pattern,
          LocalDate.parse("2025-10-31"),
          LocalTime.parse("10:00:00"),
          LocalTime.parse("11:00:00"),
          "Invalid Event",
          true,
          "Should fail",
          "Nowhere"
      );
    });
  }

  @Test
  void testInvalidConstructionWithEndDateBeforeStartDate(){
    List<String> days = List.of("MONDAY");
    RecurrencePattern pattern = new RecurrencePattern("2025-10-01", days);

    assertThrows(IllegalArgumentException.class, () -> {
      new RecurrentEvent(
          pattern,
          LocalDate.parse("2025-10-31"),
          LocalTime.parse("10:00:00"),
          LocalTime.parse("11:00:00"),
          "Invalid Event",
          true,
          "End date before start",
          "Nowhere"
      );
    });
  }

  @Test
  void testCreationStartingOnNonRecurrenceDay(){
    List<String> days = List.of("MONDAY", "WEDNESDAY");
    RecurrencePattern pattern = new RecurrencePattern(4, days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-10-31"), // Friday (not in recurrence days)
        LocalTime.parse("10:00:00"),
        LocalTime.parse("11:00:00"),
        "Event Starting on Non-Recurrence Day",
        true,
        "Should start on next Monday",
        "Office"
    );

    assertEquals(4, recurrentEvent.getEvents().size());
    // First event should be on Monday, Nov 3
    assertEquals("2025-11-03", recurrentEvent.getEvents().get(0).getStartDate());
  }

  @Test
  void testCreationWithEndDateOnExactRecurrenceDay(){
    List<String> days = List.of("MONDAY");
    RecurrencePattern pattern = new RecurrencePattern("2025-11-17", days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-03"), // Monday
        LocalTime.parse("13:00:00"),
        LocalTime.parse("14:00:00"),
        "Exact End Date Event",
        false,
        "Ends exactly on Nov 17 (Monday)",
        "Room 101"
    );

    // Nov 3, 10, 17 = 3 Mondays
    assertEquals(3, recurrentEvent.getEvents().size());
  }

  @Test
  void testCreationWithSingleOccurrence(){
    List<String> days = List.of("FRIDAY");
    RecurrencePattern pattern = new RecurrencePattern(1, days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-10-31"), // Friday
        LocalTime.parse("18:00:00"),
        LocalTime.parse("19:00:00"),
        "One-time Recurring Event",
        true,
        "Just one occurrence",
        "Location X"
    );

    assertEquals(1, recurrentEvent.getEvents().size());
  }

  @Test
  void testEventPropertiesAreCorrectlySet(){
    List<String> days = List.of("WEDNESDAY");
    RecurrencePattern pattern = new RecurrencePattern(2, days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        LocalDate.parse("2025-11-05"), // Wednesday
        LocalTime.parse("15:00:00"),
        LocalTime.parse("16:30:00"),
        "Property Check Event",
        false,
        "Checking all properties",
        "Test Location"
    );

    Event firstEvent = recurrentEvent.getEvents().get(0);
    assertEquals("Property Check Event", firstEvent.getSubject());
    assertEquals("15:00:00", firstEvent.getStartTime());
    assertEquals("16:30:00", firstEvent.getEndTime());
    assertEquals("Checking all properties", firstEvent.getDescription());
    assertEquals(false, firstEvent.getPublic());
    assertEquals("Test Location", firstEvent.getLocation());
  }

}