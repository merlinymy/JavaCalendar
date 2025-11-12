package edu.northeastern.cs5010.controller;

import edu.northeastern.cs5010.model.Calendar;
import edu.northeastern.cs5010.view.CreateEventView;
import java.io.IOException;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * The CalendarController class serves as the main entry point for a simple calendar
 * management application.
 *
 * {@link CalendarsRepository}, which can save and restore calendar data to and from files.
 */
public class CalendarController {

  private static final String STORAGE_DIR = System.getProperty("user.home") + "/.mycalendar";

  /**
   * The main entry point for the calendar management application.
   *
   * @param args command-line arguments; not used by this application
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      try {
        // restore calendars from previous runs
        CalendarsRepository repository = new CalendarsRepository();
        repository.restoreAll(STORAGE_DIR);

        // select a calendar
        Calendar selectedCalendar = selectCalendar(repository);
        if (selectedCalendar == null) {
          System.exit(0);
          return;
        }

        // create and display
        JFrame frame = new JFrame("My Calendar - " + selectedCalendar.getTitle());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null);

        // Show CreateEventView when frame is displayed
        CreateEventView eventView = new CreateEventView(frame, selectedCalendar);
        eventView.setVisible(true);

        frame.setVisible(true);

        // Save calendars on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
          try {
            repository.saveAll(STORAGE_DIR);
          } catch (IOException e) {
            System.err.println("Error saving calendars: " + e.getMessage());
          }
        }));

      } catch (IOException e) {
        JOptionPane.showMessageDialog(null,
            "Error loading calendars: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
        System.exit(1);
      }
    });
  }

  private static Calendar selectCalendar(CalendarsRepository repository) {
    List<Calendar> calendars = repository.list();

    if (calendars.isEmpty()) {
      String title = JOptionPane.showInputDialog(null,
          "No calendars found. Create a new calendar:",
          "Create Calendar",
          JOptionPane.PLAIN_MESSAGE);

      if (title == null || title.trim().isEmpty()) {
        return null;
      }

      Calendar newCalendar = new Calendar(title.trim());
      repository.add(newCalendar);
      return newCalendar;
    }

    String[] calendarTitles = calendars.stream()
        .map(Calendar::getTitle)
        .toArray(String[]::new);

    String selected = (String) JOptionPane.showInputDialog(null,
        "Select a calendar:",
        "Calendar Selection",
        JOptionPane.PLAIN_MESSAGE,
        null,
        calendarTitles,
        calendarTitles[0]);

    if (selected == null) {
      return null;
    }

    return calendars.stream()
        .filter(cal -> cal.getTitle().equals(selected))
        .findFirst()
        .orElse(null);
  }
}
