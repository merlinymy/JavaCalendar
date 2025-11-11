package edu.northeastern.cs5010.controller;

import static org.junit.jupiter.api.Assertions.*;

import edu.northeastern.cs5010.model.Calendar;
import edu.northeastern.cs5010.model.Event;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CalendarsRepositoryTest {

  private CalendarsRepository repo;
  private Path tempDir;

  @BeforeEach
  void setup() throws IOException {
    repo = new CalendarsRepository();
    tempDir = Files.createTempDirectory("cals_repo_test");
  }

  @AfterEach
  void cleanup() throws IOException {
    // delete files in tempDir
    File dir = tempDir.toFile();
    File[] files = dir.listFiles();
    if (files != null) {
      for (File f : files) { f.delete(); }
    }
    dir.delete();
  }

  @Test
  void saveAndRestoreMultipleCalendars() throws IOException {
    Calendar c1 = new Calendar("Work Calendar");
    c1.addEvent(new Event.Builder("Standup", "2025-05-01", "2025-05-01")
        .startTime("09:00:00").endTime("09:15:00").build());

    Calendar c2 = new Calendar("Personal");
    c2.addEvent(new Event.Builder("Gym", "2025-05-02", "2025-05-02").build());

    repo.add(c1);
    repo.add(c2);

    repo.saveAll(tempDir.toString());

    CalendarsRepository restoredRepo = new CalendarsRepository();
    restoredRepo.restoreAll(tempDir.toString());

    assertEquals(2, restoredRepo.list().size());
    assertTrue(restoredRepo.list().stream().anyMatch(cal -> cal.getTitle().contains("Work")));
    assertTrue(restoredRepo.list().stream().anyMatch(cal -> cal.getTitle().contains("Personal")));

    // Verify events imported
    Calendar restoredWork = restoredRepo.list().stream()
        .filter(cal -> cal.getTitle().contains("Work"))
        .findFirst().orElseThrow();
    assertEquals(1, restoredWork.getEventList().size());
  }
}

