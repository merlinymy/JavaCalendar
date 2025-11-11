package edu.northeastern.cs5010.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CalendarImportCSVTest {

  private Calendar source;
  private String csvPath;

  @BeforeEach
  void setup() throws IOException {
    source = new Calendar("Source");
    Path tmp = Files.createTempFile("calendar_import_test", ".csv");
    csvPath = tmp.toString();
  }

  @AfterEach
  void cleanup() {
    File f = new File(csvPath);
    if (f.exists()) f.delete();
  }

  @Test
  void importFromPreviouslyExportedCsv_preservesFields() throws IOException {
    Event e1 = new Event.Builder("Final exam", "2025-05-30", "2025-05-30")
        .startTime("10:00:00").endTime("13:00:00")
        .description("50 MCQs and essays")
        .location("Columbia, Schermerhorn 614")
        .isPublic(false)
        .build();
    Event e2 = new Event.Builder("Holiday", "2025-12-25", "2025-12-25").build();
    source.addEvent(e1);
    source.addEvent(e2);

    source.exportToCsv(csvPath);

    Calendar restored = Calendar.importFromCsv("Restored", csvPath);
    assertEquals("Restored", restored.getTitle());
    assertEquals(2, restored.getEventList().size());

    // Find Final exam by subject
    Event restoredExam = restored.getEventList().stream()
        .filter(ev -> "Final exam".equals(ev.getSubject()))
        .findFirst().orElse(null);
    assertNotNull(restoredExam);
    assertEquals("2025-05-30", restoredExam.getStartDate());
    assertEquals("10:00:00", restoredExam.getStartTime());
    assertEquals("13:00:00", restoredExam.getEndTime());
    assertEquals("50 MCQs and essays", restoredExam.getDescription());
    assertEquals("Columbia, Schermerhorn 614", restoredExam.getLocation());
    assertEquals(false, restoredExam.getPublic());

    // All day event
    Event restoredHoliday = restored.getEventList().stream()
        .filter(ev -> "Holiday".equals(ev.getSubject()))
        .findFirst().orElse(null);
    assertNotNull(restoredHoliday);
    assertNull(restoredHoliday.getStartTime());
    assertNull(restoredHoliday.getEndTime());
  }
}

