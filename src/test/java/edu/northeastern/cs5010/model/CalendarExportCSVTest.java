package edu.northeastern.cs5010.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for CSV export functionality in Calendar.
 */
class CalendarExportCSVTest {

  private Calendar calendar;
  private String testFilePath;

  @BeforeEach
  void setUp() throws IOException {
    calendar = new Calendar("Test Calendar");
    // Create a temporary file for testing
    Path tempFile = Files.createTempFile("calendar_test", ".csv");
    testFilePath = tempFile.toString();
  }

  @AfterEach
  void tearDown() {
    // Clean up the test file
    File file = new File(testFilePath);
    if (file.exists()) {
      file.delete();
    }
  }

  // ==================== Basic Export Tests ====================

  @Test
  void testExportEmptyCalendar() throws IOException {
    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    assertEquals(1, lines.size()); // Only header
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
        lines.get(0));
  }

  @Test
  void testExportSingleEvent() throws IOException {
    Event event = new Event.Builder("Team Meeting", "2025-05-30", "2025-05-30")
        .startTime("10:00:00")
        .endTime("13:00:00")
        .build();
    calendar.addEvent(event);

    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    assertEquals(2, lines.size()); // Header + 1 event
    assertTrue(lines.get(1).startsWith("Team Meeting,05/30/2025,10:00 AM,05/30/2025,1:00 PM,False"));
  }

  @Test
  void testExportMultipleEvents() throws IOException {
    Event event1 = new Event.Builder("Meeting 1", "2025-05-30", "2025-05-30")
        .startTime("10:00:00")
        .endTime("11:00:00")
        .build();

    Event event2 = new Event.Builder("Meeting 2", "2025-05-31", "2025-05-31")
        .startTime("14:00:00")
        .endTime("15:00:00")
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    assertEquals(3, lines.size()); // Header + 2 events
  }

  // ==================== All Day Event Tests ====================

  @Test
  void testExportAllDayEvent() throws IOException {
    Event event = new Event.Builder("Holiday", "2025-12-25", "2025-12-25")
        .build();
    calendar.addEvent(event);

    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    String eventLine = lines.get(1);
    assertTrue(eventLine.contains("True"), "All day event should have 'True' for All Day Event field");
    // Check that start time and end time are empty
    assertTrue(eventLine.contains("Holiday,12/25/2025,,12/25/2025,,True"));
  }

  @Test
  void testExportMultiDayEvent() throws IOException {
    Event event = new Event.Builder("Conference", "2025-06-01", "2025-06-03")
        .build();
    calendar.addEvent(event);

    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    String eventLine = lines.get(1);
    assertTrue(eventLine.contains("Conference,06/01/2025,,06/03/2025,,True"));
  }

  // ==================== Event with Optional Fields Tests ====================

  @Test
  void testExportEventWithDescription() throws IOException {
    Event event = new Event.Builder("Final exam", "2025-05-30", "2025-05-30")
        .startTime("10:00:00")
        .endTime("13:00:00")
        .description("50 multiple choice questions and two essay questions")
        .build();
    calendar.addEvent(event);

    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    String eventLine = lines.get(1);
    assertTrue(eventLine.contains("50 multiple choice questions and two essay questions"));
  }

  @Test
  void testExportEventWithLocation() throws IOException {
    Event event = new Event.Builder("Final exam", "2025-05-30", "2025-05-30")
        .startTime("10:00:00")
        .endTime("13:00:00")
        .location("Columbia, Schermerhorn 614")
        .build();
    calendar.addEvent(event);

    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    String eventLine = lines.get(1);
    // Location has comma, so it should be wrapped in quotes
    assertTrue(eventLine.contains("\"Columbia, Schermerhorn 614\""));
  }

  @Test
  void testExportEventWithAllFields() throws IOException {
    Event event = new Event.Builder("Final exam", "2025-05-30", "2025-05-30")
        .startTime("10:00:00")
        .endTime("13:00:00")
        .description("50 multiple choice questions and two essay questions")
        .location("Columbia, Schermerhorn 614")
        .isPublic(false)
        .build();
    calendar.addEvent(event);

    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    String eventLine = lines.get(1);
    assertTrue(eventLine.contains("Final exam"));
    assertTrue(eventLine.contains("05/30/2025"));
    assertTrue(eventLine.contains("10:00 AM"));
    assertTrue(eventLine.contains("1:00 PM"));
    assertTrue(eventLine.contains("50 multiple choice questions and two essay questions"));
    assertTrue(eventLine.contains("\"Columbia, Schermerhorn 614\""));
    assertTrue(eventLine.endsWith("True")); // Private = True (because isPublic = false)
  }

  // ==================== Privacy Tests ====================

  @Test
  void testExportPrivateEvent() throws IOException {
    Event event = new Event.Builder("Secret Meeting", "2025-05-30", "2025-05-30")
        .startTime("10:00:00")
        .endTime("11:00:00")
        .isPublic(false)
        .build();
    calendar.addEvent(event);

    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    String eventLine = lines.get(1);
    assertTrue(eventLine.endsWith("True")); // Private = True
  }

  @Test
  void testExportPublicEvent() throws IOException {
    Event event = new Event.Builder("Public Meeting", "2025-05-30", "2025-05-30")
        .startTime("10:00:00")
        .endTime("11:00:00")
        .isPublic(true)
        .build();
    calendar.addEvent(event);

    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    String eventLine = lines.get(1);
    assertTrue(eventLine.endsWith("False")); // Private = False
  }

  @Test
  void testExportEventWithNullPrivacy() throws IOException {
    Event event = new Event.Builder("Meeting", "2025-05-30", "2025-05-30")
        .startTime("10:00:00")
        .endTime("11:00:00")
        .build();
    calendar.addEvent(event);

    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    String eventLine = lines.get(1);
    assertTrue(eventLine.endsWith("False")); // Default to Private = False when null
  }

  // ==================== CSV Escaping Tests ====================

  @Test
  void testExportEventWithCommaInSubject() throws IOException {
    Event event = new Event.Builder("Meeting, Team Sync", "2025-05-30", "2025-05-30")
        .startTime("10:00:00")
        .endTime("11:00:00")
        .build();
    calendar.addEvent(event);

    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    String eventLine = lines.get(1);
    assertTrue(eventLine.startsWith("\"Meeting, Team Sync\""));
  }

  @Test
  void testExportEventWithQuoteInDescription() throws IOException {
    Event event = new Event.Builder("Meeting", "2025-05-30", "2025-05-30")
        .startTime("10:00:00")
        .endTime("11:00:00")
        .description("She said \"Hello\"")
        .build();
    calendar.addEvent(event);

    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    String eventLine = lines.get(1);
    // Quotes should be escaped as double quotes
    assertTrue(eventLine.contains("\"She said \"\"Hello\"\"\""));
  }

  @Test
  void testExportEventWithNewlineInDescription() throws IOException {
    Event event = new Event.Builder("Meeting", "2025-05-30", "2025-05-30")
        .startTime("10:00:00")
        .endTime("11:00:00")
        .description("Line 1\nLine 2")
        .build();
    calendar.addEvent(event);

    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    // The event line should contain the description wrapped in quotes
    String fullContent = String.join("\n", lines);
    assertTrue(fullContent.contains("\"Line 1\nLine 2\""));
  }

  // ==================== Recurrent Event Tests ====================

  @Test
  void testExportRecurrentEvent() throws IOException {
    // Create recurrent event: every Wednesday, 3 times
    List<String> days = List.of("WEDNESDAY");
    RecurrencePattern pattern = new RecurrencePattern(3, days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        java.time.LocalDate.parse("2025-01-01"),
        java.time.LocalTime.parse("10:00:00"),
        java.time.LocalTime.parse("11:00:00"),
        "Weekly Meeting",
        true,
        "Team sync meeting",
        "Conference Room"
    );

    calendar.addRecurrentEvent(recurrentEvent);
    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    // Should have header + 3 occurrences (Jan 1, Jan 8, Jan 15 are all Wednesdays)
    assertEquals(4, lines.size());
    assertTrue(lines.get(1).contains("01/01/2025"));
    assertTrue(lines.get(2).contains("01/08/2025"));
    assertTrue(lines.get(3).contains("01/15/2025"));
  }

  @Test
  void testExportMixedEventsAndRecurrentEvents() throws IOException {
    // Add a regular event
    Event regularEvent = new Event.Builder("One-time Meeting", "2025-01-05", "2025-01-05")
        .startTime("14:00:00")
        .endTime("15:00:00")
        .build();
    calendar.addEvent(regularEvent);

    // Add a recurrent event: every Monday, 2 times
    List<String> days = List.of("MONDAY");
    RecurrencePattern pattern = new RecurrencePattern(2, days);

    RecurrentEvent recurrentEvent = new RecurrentEvent(
        pattern,
        java.time.LocalDate.parse("2025-01-06"),
        java.time.LocalTime.parse("10:00:00"),
        java.time.LocalTime.parse("11:00:00"),
        "Weekly Sync",
        false,
        "Weekly team sync",
        "Room A"
    );

    calendar.addRecurrentEvent(recurrentEvent);
    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    // Should have header + 1 regular event + 2 recurrent events
    assertEquals(4, lines.size());
    assertTrue(lines.stream().anyMatch(line -> line.contains("One-time Meeting")));
    assertTrue(lines.stream().anyMatch(line -> line.contains("Weekly Sync")));
  }

  // ==================== Time Format Tests ====================

  @Test
  void testExportEventWithAMTime() throws IOException {
    Event event = new Event.Builder("Morning Meeting", "2025-05-30", "2025-05-30")
        .startTime("09:00:00")
        .endTime("10:30:00")
        .build();
    calendar.addEvent(event);

    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    String eventLine = lines.get(1);
    assertTrue(eventLine.contains("9:00 AM"));
    assertTrue(eventLine.contains("10:30 AM"));
  }

  @Test
  void testExportEventWithPMTime() throws IOException {
    Event event = new Event.Builder("Afternoon Meeting", "2025-05-30", "2025-05-30")
        .startTime("14:00:00")
        .endTime("16:30:00")
        .build();
    calendar.addEvent(event);

    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    String eventLine = lines.get(1);
    assertTrue(eventLine.contains("2:00 PM"));
    assertTrue(eventLine.contains("4:30 PM"));
  }

  @Test
  void testExportEventWithNoonTime() throws IOException {
    Event event = new Event.Builder("Lunch Meeting", "2025-05-30", "2025-05-30")
        .startTime("12:00:00")
        .endTime("13:00:00")
        .build();
    calendar.addEvent(event);

    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    String eventLine = lines.get(1);
    assertTrue(eventLine.contains("12:00 PM"));
    assertTrue(eventLine.contains("1:00 PM"));
  }

  @Test
  void testExportEventWithMidnightTime() throws IOException {
    Event event = new Event.Builder("Late Night Event", "2025-05-30", "2025-05-31")
        .startTime("00:00:00")
        .endTime("01:00:00")
        .build();
    calendar.addEvent(event);

    calendar.exportToCSV(testFilePath);

    List<String> lines = readCSVFile();
    String eventLine = lines.get(1);
    assertTrue(eventLine.contains("12:00 AM"));
    assertTrue(eventLine.contains("1:00 AM"));
  }

  // ==================== Helper Methods ====================

  private List<String> readCSVFile() throws IOException {
    List<String> lines = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(testFilePath))) {
      String line;
      while ((line = reader.readLine()) != null) {
        lines.add(line);
      }
    }
    return lines;
  }
}
