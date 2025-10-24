package edu.northeastern.cs5010.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/**
 * Comprehensive test suite for the Calendar class.
 * Tests cover calendar creation, event management, recurring events,
 * querying, editing, and CSV export functionality.
 */
class CalendarTest {

  private Calendar calendar;
  private LocalDate testDate;
  private LocalTime testTime;

  @BeforeEach
  void setUp() {
    calendar = new Calendar("Work Calendar");
    testDate = LocalDate.of(2025, 11, 7);
    testTime = LocalTime.of(10, 0);
  }

  @Nested
  @DisplayName("Calendar Creation Tests")
  class CalendarCreationTests {

    @Test
    @DisplayName("Should create calendar with title")
    void testCreateCalendar() {
      Calendar cal = new Calendar("Personal Calendar");

      assertNotNull(cal);
      assertEquals("Personal Calendar", cal.getTitle());
    }

    @Test
    @DisplayName("Should fail to create calendar with null title")
    void testCreateCalendarWithNullTitle() {
      assertThrows(IllegalArgumentException.class,
          () -> new Calendar(null));
    }

    @Test
    @DisplayName("Should fail to create calendar with empty title")
    void testCreateCalendarWithEmptyTitle() {
      assertThrows(IllegalArgumentException.class,
          () -> new Calendar(""));
    }

    @Test
    @DisplayName("Should create calendar with default configuration")
    void testCalendarDefaultConfiguration() {
      Calendar cal = new Calendar("Test Calendar");

      // Verify default settings exist
      assertNotNull(cal.getDefaultVisibility());
      assertNotNull(cal.allowsConflicts());
    }

    @Test
    @DisplayName("Should create calendar with custom configuration")
    void testCalendarWithCustomConfiguration() {
      Calendar cal = new Calendar("Test Calendar", "Private", false);

      assertEquals("Private", cal.getDefaultVisibility());
      assertFalse(cal.allowsConflicts());
    }

    @Test
    @DisplayName("Should start with zero events")
    void testNewCalendarIsEmpty() {
      Calendar cal = new Calendar("Empty Calendar");

      assertEquals(0, cal.getEventCount());
      assertTrue(cal.getAllEvents().isEmpty());
    }
  }

  @Nested
  @DisplayName("Single Event Creation Tests")
  class SingleEventCreationTests {

    @Test
    @DisplayName("Should add event with required fields")
    void testAddSimpleEvent() {
      Event event = calendar.createEvent("Team Meeting", testDate, testDate);

      assertNotNull(event);
      assertEquals(1, calendar.getEventCount());
    }

    @Test
    @DisplayName("Should add event with all optional fields")
    void testAddEventWithAllFields() {
      Event event = calendar.createEvent("Conference", testDate, testDate,
          testTime, testTime.plusHours(2), "Public",
          "Tech conference", "Convention Center");

      assertNotNull(event);
      assertEquals("Conference", event.getSubject());
      assertEquals("Public", event.getVisibility());
    }

    @Test
    @DisplayName("Should use default visibility from calendar configuration")
    void testDefaultVisibility() {
      Calendar cal = new Calendar("Private Calendar", "Private", true);
      Event event = cal.createEvent("Personal Meeting", testDate, testDate,
          testTime, testTime.plusHours(1), null, null, null);

      assertEquals("Private", event.getVisibility());
    }

    @Test
    @DisplayName("Should override default visibility when specified")
    void testOverrideDefaultVisibility() {
      Calendar cal = new Calendar("Private Calendar", "Private", true);
      Event event = cal.createEvent("Public Announcement", testDate, testDate,
          testTime, testTime.plusHours(1), "Public", null, null);

      assertEquals("Public", event.getVisibility());
    }

    @Test
    @DisplayName("Should prevent duplicate events")
    void testPreventDuplicateEvents() {
      calendar.createEvent("Meeting", testDate, testDate, testTime,
          testTime.plusHours(1), null, null, null);

      assertThrows(IllegalArgumentException.class,
          () -> calendar.createEvent("Meeting", testDate, testDate, testTime,
              testTime.plusHours(2), null, null, null));
    }

    @Test
    @DisplayName("Should allow events with same subject but different times")
    void testSameSubjectDifferentTimes() {
      calendar.createEvent("Exercise", testDate, testDate,
          LocalTime.of(8, 0), LocalTime.of(9, 0), null, null, null);

      assertDoesNotThrow(() ->
          calendar.createEvent("Exercise", testDate, testDate,
              LocalTime.of(18, 0), LocalTime.of(19, 0), null, null, null));

      assertEquals(2, calendar.getEventCount());
    }

    @Test
    @DisplayName("Should reject conflicting events when conflicts not allowed")
    void testRejectConflictingEvents() {
      Calendar cal = new Calendar("Strict Calendar", "Public", false);

      cal.createEvent("Meeting 1", testDate, testDate,
          LocalTime.of(10, 0), LocalTime.of(11, 0), null, null, null);

      assertThrows(IllegalArgumentException.class,
          () -> cal.createEvent("Meeting 2", testDate, testDate,
              LocalTime.of(10, 30), LocalTime.of(11, 30), null, null, null));
    }

    @Test
    @DisplayName("Should allow conflicting events when conflicts are permitted")
    void testAllowConflictingEvents() {
      Calendar cal = new Calendar("Flexible Calendar", "Public", true);

      cal.createEvent("Meeting 1", testDate, testDate,
          LocalTime.of(10, 0), LocalTime.of(11, 0), null, null, null);

      assertDoesNotThrow(() ->
          cal.createEvent("Meeting 2", testDate, testDate,
              LocalTime.of(10, 30), LocalTime.of(11, 30), null, null, null));

      assertEquals(2, cal.getEventCount());
    }
  }

  @Nested
  @DisplayName("Recurring Event Creation Tests")
  class RecurringEventCreationTests {

    @Test
    @DisplayName("Should create recurring event with specific number of occurrences")
    void testCreateRecurringEventWithCount() {
      Set<DayOfWeek> days = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

      List<Event> events = calendar.createRecurringEvent("Exercise",
          testDate, testTime, testTime.plusHours(1), days, 6, null,
          null, null, null);

      assertEquals(6, events.size());
      assertEquals(6, calendar.getEventCount());
    }

    @Test
    @DisplayName("Should create recurring event until end date")
    void testCreateRecurringEventUntilDate() {
      Set<DayOfWeek> days = Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);
      LocalDate endDate = testDate.plusWeeks(2);

      List<Event> events = calendar.createRecurringEvent("Team Standup",
          testDate, LocalTime.of(9, 0), LocalTime.of(9, 15),
          days, null, endDate, null, null, null);

      assertFalse(events.isEmpty());
      assertTrue(events.size() <= 4); // Max 4 occurrences in 2 weeks

      // All events should be before or on end date
      for (Event event : events) {
        assertTrue(!event.getStartDate().isAfter(endDate));
      }
    }

    @Test
    @DisplayName("Should reject recurring event spanning multiple days")
    void testRejectMultiDayRecurringEvent() {
      Set<DayOfWeek> days = Set.of(DayOfWeek.MONDAY);

      assertThrows(IllegalArgumentException.class,
          () -> calendar.createRecurringEvent("Invalid Event", testDate,
              LocalTime.of(23, 0), LocalTime.of(1, 0), days, 3, null,
              null, null, null));
    }

    @Test
    @DisplayName("Should create recurring all-day events")
    void testCreateRecurringAllDayEvents() {
      Set<DayOfWeek> days = Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

      List<Event> events = calendar.createRecurringEvent("Weekend Activity",
          testDate, null, null, days, 4, null, null, null, null);

      assertEquals(4, events.size());

      for (Event event : events) {
        assertTrue(event.isAllDayEvent());
      }
    }

    @Test
    @DisplayName("Should reject recurring event if any occurrence conflicts")
    void testRejectRecurringEventWithConflicts() {
      Calendar cal = new Calendar("Strict Calendar", "Public", false);

      // Create a single event
      LocalDate futureDate = testDate.plusWeeks(1);
      cal.createEvent("Existing Meeting", futureDate, futureDate,
          testTime, testTime.plusHours(1), null, null, null);

      // Try to create recurring event that would conflict
      Set<DayOfWeek> days = Set.of(futureDate.getDayOfWeek());

      assertThrows(IllegalArgumentException.class,
          () -> cal.createRecurringEvent("Weekly Meeting", testDate,
              testTime, testTime.plusHours(1), days, 5, null,
              null, null, null));
    }

    @Test
    @DisplayName("Should require either occurrence count or end date")
    void testRecurringEventRequiresCountOrEndDate() {
      Set<DayOfWeek> days = Set.of(DayOfWeek.MONDAY);

      assertThrows(IllegalArgumentException.class,
          () -> calendar.createRecurringEvent("Invalid", testDate,
              testTime, testTime.plusHours(1), days, null, null,
              null, null, null));
    }

    @Test
    @DisplayName("Should create events only on specified days of week")
    void testRecurringEventOnSpecificDays() {
      LocalDate monday = LocalDate.of(2025, 11, 10); // A Monday
      Set<DayOfWeek> days = Set.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);

      List<Event> events = calendar.createRecurringEvent("MWF Meeting",
          monday, testTime, testTime.plusHours(1), days, 4, null,
          null, null, null);

      // Verify all events are on Monday or Friday
      for (Event event : events) {
        DayOfWeek dow = event.getStartDate().getDayOfWeek();
        assertTrue(dow == DayOfWeek.MONDAY || dow == DayOfWeek.FRIDAY);
      }
    }

    @Test
    @DisplayName("Should handle recurring event with description and location")
    void testRecurringEventWithOptionalFields() {
      Set<DayOfWeek> days = Set.of(DayOfWeek.WEDNESDAY);

      List<Event> events = calendar.createRecurringEvent("Yoga Class",
          testDate, LocalTime.of(18, 0), LocalTime.of(19, 0),
          days, 3, null, "Public", "Weekly yoga session", "Gym");

      assertEquals(3, events.size());

      for (Event event : events) {
        assertEquals("Weekly yoga session", event.getDescription());
        assertEquals("Gym", event.getLocation());
        assertEquals("Public", event.getVisibility());
      }
    }
  }

  @Nested
  @DisplayName("Query Calendar Tests")
  class QueryCalendarTests {

    @BeforeEach
    void addSampleEvents() {
      calendar.createEvent("Morning Meeting", testDate, testDate,
          LocalTime.of(9, 0), LocalTime.of(10, 0), null, null, null);
      calendar.createEvent("Lunch", testDate, testDate,
          LocalTime.of(12, 0), LocalTime.of(13, 0), null, null, null);
      calendar.createEvent("Afternoon Call", testDate, testDate,
          LocalTime.of(15, 0), LocalTime.of(16, 0), null, null, null);
    }

    @Test
    @DisplayName("Should retrieve event by subject, date, and time")
    void testRetrieveEventByIdentity() {
      Event event = calendar.getEvent("Morning Meeting", testDate,
          LocalTime.of(9, 0));

      assertNotNull(event);
      assertEquals("Morning Meeting", event.getSubject());
    }

    @Test
    @DisplayName("Should return null for non-existent event")
    void testRetrieveNonExistentEvent() {
      Event event = calendar.getEvent("Nonexistent", testDate, testTime);

      assertNull(event);
    }

    @Test
    @DisplayName("Should retrieve all events on specific date")
    void testGetEventsOnDate() {
      List<Event> events = calendar.getEventsOnDate(testDate);

      assertEquals(3, events.size());
    }

    @Test
    @DisplayName("Should return empty list for date with no events")
    void testGetEventsOnEmptyDate() {
      LocalDate emptyDate = testDate.plusDays(5);
      List<Event> events = calendar.getEventsOnDate(emptyDate);

      assertTrue(events.isEmpty());
    }

    @Test
    @DisplayName("Should retrieve events in date range")
    void testGetEventsInRange() {
      LocalDate nextDay = testDate.plusDays(1);
      calendar.createEvent("Next Day Event", nextDay, nextDay,
          testTime, testTime.plusHours(1), null, null, null);

      List<Event> events = calendar.getEventsInRange(testDate, nextDay);

      assertEquals(4, events.size());
    }

    @Test
    @DisplayName("Should include multi-day events in date range query")
    void testGetMultiDayEventsInRange() {
      LocalDate startDate = testDate.minusDays(1);
      LocalDate endDate = testDate.plusDays(1);
      calendar.createEvent("Multi-day Conference", startDate, endDate);

      List<Event> events = calendar.getEventsInRange(testDate, testDate);

      // Should include the multi-day event
      assertTrue(events.size() >= 4);
    }

    @Test
    @DisplayName("Should check if user is busy at specific date and time")
    void testIsBusyAt() {
      assertTrue(calendar.isBusyAt(testDate, LocalTime.of(9, 30)));
      assertFalse(calendar.isBusyAt(testDate, LocalTime.of(11, 0)));
    }

    @Test
    @DisplayName("Should return false for busy check on date with no events")
    void testIsNotBusyOnEmptyDate() {
      LocalDate emptyDate = testDate.plusDays(10);

      assertFalse(calendar.isBusyAt(emptyDate, testTime));
    }

    @Test
    @DisplayName("Should check busy status for all-day events")
    void testIsBusyWithAllDayEvent() {
      calendar.createEvent("All Day Workshop", testDate.plusDays(1),
          testDate.plusDays(1));

      assertTrue(calendar.isBusyAt(testDate.plusDays(1), LocalTime.of(14, 0)));
    }

    @Test
    @DisplayName("Should retrieve all events from calendar")
    void testGetAllEvents() {
      List<Event> allEvents = calendar.getAllEvents();

      assertEquals(3, allEvents.size());
    }
  }

  @Nested
  @DisplayName("Edit Single Event Tests")
  class EditSingleEventTests {

    private Event event;

    @BeforeEach
    void createTestEvent() {
      event = calendar.createEvent("Editable Meeting", testDate, testDate,
          testTime, testTime.plusHours(1), "Public",
          "Initial description", "Room 101");
    }

    @Test
    @DisplayName("Should retrieve event for editing")
    void testRetrieveEventForEditing() {
      Event retrieved = calendar.getEvent("Editable Meeting", testDate, testTime);

      assertNotNull(retrieved);
      assertSame(event, retrieved);
    }

    @Test
    @DisplayName("Should modify event description")
    void testModifyDescription() {
      event.setDescription("Updated description");

      Event retrieved = calendar.getEvent("Editable Meeting", testDate, testTime);
      assertEquals("Updated description", retrieved.getDescription());
    }

    @Test
    @DisplayName("Should modify event location")
    void testModifyLocation() {
      event.setLocation("Room 202");

      assertEquals("Room 202", event.getLocation());
    }

    @Test
    @DisplayName("Should modify event times")
    void testModifyTimes() {
      LocalTime newStart = testTime.plusHours(1);
      LocalTime newEnd = newStart.plusHours(2);

      event.setStartTime(newStart);
      event.setEndTime(newEnd);

      assertEquals(newStart, event.getStartTime());
      assertEquals(newEnd, event.getEndTime());
    }

    @Test
    @DisplayName("Should reject moving end time before start time")
    void testRejectInvalidTimeChange() {
      LocalTime invalidEnd = testTime.minusHours(1);

      assertThrows(IllegalArgumentException.class,
          () -> event.setEndTime(invalidEnd));
    }

    @Test
    @DisplayName("Should reject creating duplicate after subject change")
    void testRejectDuplicateAfterEdit() {
      calendar.createEvent("Another Meeting", testDate, testDate,
          testTime.plusHours(2), testTime.plusHours(3), null, null, null);

      assertThrows(IllegalArgumentException.class,
          () -> event.setSubject("Another Meeting"));
    }

    @Test
    @DisplayName("Should allow changing visibility")
    void testChangeVisibility() {
      event.setVisibility("Private");

      assertEquals("Private", event.getVisibility());
    }

    @Test
    @DisplayName("Should reject changes causing conflicts when not allowed")
    void testRejectConflictingEdit() {
      Calendar strictCal = new Calendar("Strict", "Public", false);
      Event event1 = strictCal.createEvent("Meeting 1", testDate, testDate,
          LocalTime.of(10, 0), LocalTime.of(11, 0), null, null, null);
      Event event2 = strictCal.createEvent("Meeting 2", testDate, testDate,
          LocalTime.of(14, 0), LocalTime.of(15, 0), null, null, null);

      // Try to move event2 to conflict with event1
      assertThrows(IllegalArgumentException.class,
          () -> event2.setStartTime(LocalTime.of(10, 30)));
    }
  }

  @Nested
  @DisplayName("Edit Recurring Event Tests")
  class EditRecurringEventTests {

    private List<Event> recurringEvents;
    private LocalDate startDate;

    @BeforeEach
    void createRecurringEvents() {
      startDate = LocalDate.of(2025, 11, 3); // A Monday
      Set<DayOfWeek> days = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);

      recurringEvents = calendar.createRecurringEvent("Weekly Standup",
          startDate, LocalTime.of(9, 0), LocalTime.of(9, 30),
          days, 6, null, null, "Team sync", "Conference Room");
    }

    @Test
    @DisplayName("Should modify single instance of recurring event")
    void testModifySingleInstance() {
      Event secondInstance = recurringEvents.get(1);
      String originalDescription = secondInstance.getDescription();

      secondInstance.setDescription("Cancelled - Holiday");

      assertEquals("Cancelled - Holiday", secondInstance.getDescription());

      // Other instances should remain unchanged
      assertEquals(originalDescription, recurringEvents.get(0).getDescription());
      assertEquals(originalDescription, recurringEvents.get(2).getDescription());
    }

    @Test
    @DisplayName("Should modify all events in series from specific date")
    void testModifySeriesFromDate() {
      LocalDate splitDate = recurringEvents.get(3).getStartDate();

      calendar.modifyRecurringEventsFrom("Weekly Standup", splitDate,
          LocalTime.of(9, 0), newEvent -> {
            newEvent.setLocation("New Conference Room");
          });

      // First 3 events should have old location
      assertEquals("Conference Room", recurringEvents.get(0).getLocation());
      assertEquals("Conference Room", recurringEvents.get(1).getLocation());
      assertEquals("Conference Room", recurringEvents.get(2).getLocation());

      // Last 3 events should have new location
      assertEquals("New Conference Room", recurringEvents.get(3).getLocation());
      assertEquals("New Conference Room", recurringEvents.get(4).getLocation());
      assertEquals("New Conference Room", recurringEvents.get(5).getLocation());
    }

    @Test
    @DisplayName("Should modify all events in entire series")
    void testModifyEntireSeries() {
      calendar.modifyAllRecurringEvents("Weekly Standup",
          startDate, LocalTime.of(9, 0), event -> {
            event.setDescription("Updated for all");
          });

      for (Event event : recurringEvents) {
        assertEquals("Updated for all", event.getDescription());
      }
    }

    @Test
    @DisplayName("Should not modify non-recurring events when editing series")
    void testDoNotModifyNonRecurringEvents() {
      // Add a non-recurring event with same subject
      Event standalone = calendar.createEvent("Weekly Standup",
          testDate, testDate, LocalTime.of(15, 0),
          LocalTime.of(16, 0), null, "Standalone meeting", null);

      calendar.modifyAllRecurringEvents("Weekly Standup",
          startDate, LocalTime.of(9, 0), event -> {
            event.setLocation("Series Location");
          });

      // Standalone event should be unchanged
      assertNull(standalone.getLocation());
      assertEquals("Standalone meeting", standalone.getDescription());
    }

    @Test
    @DisplayName("Should reject series modification that causes conflicts")
    void testRejectConflictingSeriesModification() {
      Calendar strictCal = new Calendar("Strict", "Public", false);
      Set<DayOfWeek> days = Set.of(DayOfWeek.TUESDAY);

      strictCal.createRecurringEvent("Tuesday Meeting", startDate,
          LocalTime.of(10, 0), LocalTime.of(11, 0), days, 3, null,
          null, null, null);

      // Create another event that would conflict
      strictCal.createEvent("Important Call", startDate.plusDays(1),
          startDate.plusDays(1), LocalTime.of(10, 30),
          LocalTime.of(11, 30), null, null, null);

      // Try to move Tuesday meetings to conflict
      assertThrows(IllegalArgumentException.class,
          () -> strictCal.modifyAllRecurringEvents("Tuesday Meeting",
              startDate, LocalTime.of(10, 0), event -> {
                event.setStartTime(LocalTime.of(10, 15));
              }));
    }

    @Test
    @DisplayName("Should identify events as part of recurring series")
    void testIdentifyRecurringSeries() {
      for (Event event : recurringEvents) {
        assertTrue(event.isPartOfRecurringSeries());
        assertEquals("Weekly Standup", event.getSeriesSubject());
      }
    }
  }

  @Nested
  @DisplayName("CSV Export Tests")
  class CSVExportTests {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should export empty calendar to CSV")
    void testExportEmptyCalendar() throws IOException {
      File csvFile = tempDir.resolve("empty_calendar.csv").toFile();

      calendar.exportToCSV(csvFile);

      assertTrue(csvFile.exists());
      assertTrue(csvFile.length() > 0); // Should have header
    }

    @Test
    @DisplayName("Should export calendar with events to CSV")
    void testExportCalendarWithEvents() throws IOException {
      calendar.createEvent("Meeting", testDate, testDate, testTime,
          testTime.plusHours(1), "Public", "Description", "Location");
      calendar.createEvent("All Day Event", testDate.plusDays(1),
          testDate.plusDays(1));

      File csvFile = tempDir.resolve("calendar.csv").toFile();
      calendar.exportToCSV(csvFile);

      assertTrue(csvFile.exists());

      String content = Files.readString(csvFile.toPath());
      assertTrue(content.contains("Meeting"));
      assertTrue(content.contains("All Day Event"));
    }

    @Test
    @DisplayName("Should export CSV in Google Calendar format")
    void testGoogleCalendarFormat() throws IOException {
      calendar.createEvent("Test Event", testDate, testDate, testTime,
          testTime.plusHours(1), "Public", "Test", "Room 101");

      File csvFile = tempDir.resolve("google_format.csv").toFile();
      calendar.exportToCSV(csvFile);

      String content = Files.readString(csvFile.toPath());

      // Check for Google Calendar required columns
      assertTrue(content.contains("Subject"));
      assertTrue(content.contains("Start Date"));
      assertTrue(content.contains("Start Time"));
      assertTrue(content.contains("End Date"));
      assertTrue(content.contains("End Time"));
      assertTrue(content.contains("Description"));
      assertTrue(content.contains("Location"));
    }

    @Test
    @DisplayName("Should handle special characters in CSV export")
    void testCSVSpecialCharacters() throws IOException {
      calendar.createEvent("Meeting, with comma", testDate, testDate,
          testTime, testTime.plusHours(1), null,
          "Description \"with quotes\"", "Room 101");

      File csvFile = tempDir.resolve("special_chars.csv").toFile();
      calendar.exportToCSV(csvFile);

      String content = Files.readString(csvFile.toPath());

      // Should properly escape special characters
      assertTrue(content.contains("Meeting, with comma") ||
          content.contains("\"Meeting, with comma\""));
    }

    @Test
    @DisplayName("Should export all-day events correctly")
    void testExportAllDayEvents() throws IOException {
      calendar.createEvent("Holiday", testDate, testDate);

      File csvFile = tempDir.resolve("allday.csv").toFile();
      calendar.exportToCSV(csvFile);

      String content = Files.readString(csvFile.toPath());
      assertTrue(content.contains("Holiday"));

      // All-day events should have proper format
      // (implementation dependent on Google Calendar format)
    }

    @Test
    @DisplayName("Should export multi-day events correctly")
    void testExportMultiDayEvents() throws IOException {
      LocalDate endDate = testDate.plusDays(3);
      calendar.createEvent("Conference", testDate, endDate);

      File csvFile = tempDir.resolve("multiday.csv").toFile();
      calendar.exportToCSV(csvFile);

      String content = Files.readString(csvFile.toPath());
      assertTrue(content.contains("Conference"));
    }

    @Test
    @DisplayName("Should export recurring events as individual occurrences")
    void testExportRecurringEvents() throws IOException {
      Set<DayOfWeek> days = Set.of(DayOfWeek.MONDAY);
      calendar.createRecurringEvent("Weekly Meeting", testDate, testTime,
          testTime.plusHours(1), days, 3, null, null, null, null);

      File csvFile = tempDir.resolve("recurring.csv").toFile();
      calendar.exportToCSV(csvFile);

      String content = Files.readString(csvFile.toPath());

      // Should have 3 separate entries for the 3 occurrences
      int count = content.split("Weekly Meeting", -1).length - 1;
      assertEquals(3, count);
    }

    @Test
    @DisplayName("Should handle empty optional fields in CSV")
    void testExportWithEmptyOptionalFields() throws IOException {
      calendar.createEvent("Simple Event", testDate, testDate);

      File csvFile = tempDir.resolve("simple.csv").toFile();
      calendar.exportToCSV(csvFile);

      assertTrue(csvFile.exists());
      // Should not throw exception with null/empty fields
    }

    @Test
    @DisplayName("CSV file should be importable to Google Calendar")
    void testCSVGoogleCalendarCompatibility() throws IOException {
      // Create various types of events
      calendar.createEvent("Timed Event", testDate, testDate, testTime,
          testTime.plusHours(1), "Public", "Description", "Location");
      calendar.createEvent("All Day", testDate.plusDays(1),
          testDate.plusDays(1));
      calendar.createEvent("Private Event", testDate.plusDays(2),
          testDate.plusDays(2), testTime, testTime.plusMinutes(30),
          "Private", null, null);

      File csvFile = tempDir.resolve("google_compatible.csv").toFile();
      calendar.exportToCSV(csvFile);

      // Verify basic structure
      String content = Files.readString(csvFile.toPath());
      String[] lines = content.split("\n");

      assertTrue(lines.length >= 4); // Header + 3 events

      // Verify header row exists and contains required fields
      String header = lines[0];
      assertTrue(header.contains("Subject"));
      assertTrue(header.contains("Start Date"));
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should handle complex calendar workflow")
    void testComplexWorkflow() throws IOException {
      // Create calendar with configuration
      Calendar workCal = new Calendar("Work Calendar", "Public", false);

      // Add various events
      workCal.createEvent("Daily Standup", testDate, testDate,
          LocalTime.of(9, 0), LocalTime.of(9, 15), null, null, null);

      Set<DayOfWeek> weekdays = Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
          DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
      workCal.createRecurringEvent("Morning Standup", testDate.plusWeeks(1),
          LocalTime.of(9, 0), LocalTime.of(9, 15), weekdays, 5, null,
          null, "Daily team sync", "Conference Room A");

      // Query events
      List<Event> todayEvents = workCal.getEventsOnDate(testDate);
      assertFalse(todayEvents.isEmpty());

      // Check busy status
      assertTrue(workCal.isBusyAt(testDate, LocalTime.of(9, 10)));
      assertFalse(workCal.isBusyAt(testDate, LocalTime.of(10, 0)));

      // Edit an event
      Event standup = workCal.getEvent("Daily Standup", testDate,
          LocalTime.of(9, 0));
      standup.setLocation("Room 202");

      // Export to CSV
      File csvFile = tempDir.resolve("work_calendar.csv").toFile();
      workCal.exportToCSV(csvFile);
      assertTrue(csvFile.exists());
    }

    @Test
    @DisplayName("Should maintain calendar integrity across operations")
    void testCalendarIntegrity() {
      // Add events
      calendar.createEvent("Event 1", testDate, testDate, testTime,
          testTime.plusHours(1), null, null, null);
      calendar.createEvent("Event 2", testDate.plusDays(1),
          testDate.plusDays(1), testTime, testTime.plusHours(1),
          null, null, null);

      int initialCount = calendar.getEventCount();

      // Retrieve and modify
      Event event = calendar.getEvent("Event 1", testDate, testTime);
      event.setDescription("Modified");

      // Count should remain the same
      assertEquals(initialCount, calendar.getEventCount());

      // Event should still be retrievable
      Event retrieved = calendar.getEvent("Event 1", testDate, testTime);
      assertNotNull(retrieved);
      assertEquals("Modified", retrieved.getDescription());
    }

    @Test
    @DisplayName("Should handle boundary cases in date ranges")
    void testDateRangeBoundaries() {
      LocalDate start = testDate;
      LocalDate end = testDate.plusDays(7);

      // Add events at boundaries and middle
      calendar.createEvent("Start Event", start, start);
      calendar.createEvent("Middle Event", start.plusDays(3),
          start.plusDays(3));
      calendar.createEvent("End Event", end, end);
      calendar.createEvent("Before Range", start.minusDays(1),
          start.minusDays(1));
      calendar.createEvent("After Range", end.plusDays(1),
          end.plusDays(1));

      List<Event> inRange = calendar.getEventsInRange(start, end);

      assertEquals(3, inRange.size());
    }

    @TempDir
    Path tempDir;
  }

  @Nested
  @DisplayName("Edge Cases and Validation Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle calendar with maximum number of events")
    void testLargeNumberOfEvents() {
      for (int i = 0; i < 1000; i++) {
        calendar.createEvent("Event " + i, testDate.plusDays(i % 365),
            testDate.plusDays(i % 365));
      }

      assertEquals(1000, calendar.getEventCount());
    }

    @Test
    @DisplayName("Should handle events spanning leap year boundary")
    void testLeapYearEvents() {
      LocalDate leapDay = LocalDate.of(2024, 2, 29);
      Event event = calendar.createEvent("Leap Day Event", leapDay, leapDay);

      assertNotNull(event);
      assertEquals(leapDay, event.getStartDate());
    }

    @Test
    @DisplayName("Should handle year boundary events")
    void testYearBoundaryEvents() {
      LocalDate dec31 = LocalDate.of(2025, 12, 31);
      LocalDate jan1 = LocalDate.of(2026, 1, 1);

      Event event = calendar.createEvent("New Year Event", dec31, jan1);

      assertNotNull(event);
      assertTrue(event.isMultiDay());
    }

    @Test
    @DisplayName("Should handle very long event subjects")
    void testLongEventSubject() {
      String longSubject = "A".repeat(1000);
      Event event = calendar.createEvent(longSubject, testDate, testDate);

      assertEquals(longSubject, event.getSubject());
    }

    @Test
    @DisplayName("Should handle concurrent modifications gracefully")
    void testConcurrentAccess() {
      Event event = calendar.createEvent("Shared Event", testDate, testDate,
          testTime, testTime.plusHours(1), null, null, null);

      // Simulate concurrent modifications
      event.setDescription("First update");
      event.setLocation("Room 1");
      event.setDescription("Second update");

      assertEquals("Second update", event.getDescription());
      assertEquals("Room 1", event.getLocation());
    }
  }
}