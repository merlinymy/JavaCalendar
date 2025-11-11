package edu.northeastern.cs5010.controller;

import edu.northeastern.cs5010.model.Calendar;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A minimal repository to hold multiple calendars and support saving/restoring them
 * as CSV files using the Calendar export/import format.
 */
public class CalendarsRepository {

  private final List<Calendar> calendars = new ArrayList<>();

  public void add(Calendar calendar) {
    calendars.add(calendar);
  }

  public List<Calendar> list() {
    return Collections.unmodifiableList(calendars);
  }

  /**
   * Saves all calendars into the specified directory as CSV files. One file per calendar.
   * Filenames are derived from sanitized titles (e.g., My Calendar -> my_calendar.csv).
   *
   * @param directory directory path to write CSV files into.
   * @throws IOException on file errors
   */
  public void saveAll(String directory) throws IOException {
    Path dir = Paths.get(directory);
    if (!Files.exists(dir)) {
      Files.createDirectories(dir);
    }

    // Write each calendar to its own CSV
    int idx = 0;
    for (Calendar cal : calendars) {
      String base = sanitizeTitle(cal.getTitle());
      String filename = base + ".csv";
      // Avoid collisions by appending index when necessary
      Path target = dir.resolve(filename);
      while (Files.exists(target)) {
        filename = base + "_" + (++idx) + ".csv";
        target = dir.resolve(filename);
      }
      cal.exportToCsv(target.toString());
    }
  }

  /**
   * Clears current calendars and restores all calendars from CSV files in the provided directory.
   * Each .csv file becomes a calendar, titled from the filename (underscores -> spaces).
   *
   * @param directory directory to read from
   * @throws IOException on IO errors
   */
  public void restoreAll(String directory) throws IOException {
    calendars.clear();
    File dir = new File(directory);
    if (!dir.exists() || !dir.isDirectory()) {
      return; // nothing to restore
    }
    File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".csv"));
    if (files == null) {
      return;
    }
    for (File f : files) {
      String name = f.getName();
      String title = name.substring(0, name.length() - 4); // drop .csv
      title = title.replace('_', ' ');
      Calendar cal = Calendar.importFromCsv(title, f.getAbsolutePath());
      calendars.add(cal);
    }
  }

  private static String sanitizeTitle(String title) {
    String s = title == null ? "calendar" : title.trim();
    s = s.replaceAll("[^A-Za-z0-9]+", "_");
    s = s.replaceAll("_+", "_");
    s = s.replaceAll("^_+|_+$", "");
    if (s.isEmpty()) {
      s = "calendar";
    }
    return s;
  }
}
