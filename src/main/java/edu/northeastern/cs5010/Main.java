package edu.northeastern.cs5010;

import edu.northeastern.cs5010.model.Calendar;
import edu.northeastern.cs5010.model.Event;
import edu.northeastern.cs5010.model.RecurrencePattern;
import edu.northeastern.cs5010.model.RecurrentEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Main {

  public static void main(String[] args) {
    System.out.println("=== My Calendar App - CSV Export Demo ===\n");

    // Create a calendar
    Calendar myCalendar = new Calendar("Personal Calendar");
    myCalendar.setAllowConflict(true); // Allow overlapping events for demo

    try {
      // Add a variety of events to showcase CSV export

      // 1. Regular event with all fields
      System.out.println("Adding: Final exam with full details...");
      Event finalExam = new Event.Builder("Final exam", "2025-05-30", "2025-05-30")
          .startTime("10:00:00")
          .endTime("13:00:00")
          .description("50 multiple choice questions and two essay questions")
          .location("Columbia, Schermerhorn 614")
          .isPublic(false)
          .build();
      myCalendar.addEvent(finalExam);

      // 2. All-day event
      System.out.println("Adding: Holiday (all-day event)...");
      Event holiday = new Event.Builder("Christmas", "2025-12-25", "2025-12-25")
          .description("Christmas Day")
          .isPublic(true)
          .build();
      myCalendar.addEvent(holiday);

      // 3. Multi-day event
      System.out.println("Adding: Conference (multi-day)...");
      Event conference = new Event.Builder("Tech Conference", "2025-06-15", "2025-06-17")
          .description("Annual technology conference")
          .location("San Francisco Convention Center")
          .isPublic(true)
          .build();
      myCalendar.addEvent(conference);

      // 4. Morning meeting
      System.out.println("Adding: Morning team meeting...");
      Event morningMeeting = new Event.Builder("Team Standup", "2025-06-01", "2025-06-01")
          .startTime("09:00:00")
          .endTime("09:30:00")
          .description("Daily team standup meeting")
          .location("Office Room 101")
          .isPublic(false)
          .build();
      myCalendar.addEvent(morningMeeting);

      // 5. Afternoon meeting
      System.out.println("Adding: Afternoon meeting...");
      Event afternoonMeeting = new Event.Builder("Client Presentation", "2025-06-01", "2025-06-01")
          .startTime("14:00:00")
          .endTime("16:00:00")
          .description("Quarterly review presentation")
          .location("Board Room")
          .isPublic(true)
          .build();
      myCalendar.addEvent(afternoonMeeting);

      // 6. Add a recurrent event - Weekly team sync
      System.out.println("Adding: Weekly team sync (recurrent event)...");
      List<String> days = List.of("MONDAY", "WEDNESDAY");
      RecurrencePattern pattern = new RecurrencePattern(4, days);
      RecurrentEvent weeklySync = new RecurrentEvent(
          pattern,
          LocalDate.parse("2025-06-02"),
          LocalTime.parse("10:00:00"),
          LocalTime.parse("11:00:00"),
          "Weekly Team Sync",
          true,
          "Recurring team synchronization meeting",
          "Conference Room A"
      );
      myCalendar.addRecurrentEvent(weeklySync);

      // Export to CSV
      String csvFilePath = "my_calendar_export.csv";
      System.out.println("\n=== Exporting calendar to CSV ===");
      myCalendar.exportToCSV(csvFilePath);
      System.out.println("Calendar exported successfully to: " + csvFilePath);

      // Print summary
      System.out.println("\n=== Export Summary ===");
      System.out.println("Total regular events: " + myCalendar.getEventList().size());
      System.out.println("Total recurrent event series: " + myCalendar.getRecurrentEvents().size());
      int totalRecurrentInstances = myCalendar.getRecurrentEvents().stream()
          .mapToInt(re -> re.getEvents().size())
          .sum();
      System.out.println("Total recurrent event instances: " + totalRecurrentInstances);
      System.out.println("Total events in CSV: " + (myCalendar.getEventList().size() + totalRecurrentInstances));
      System.out.println("\nYou can now import '" + csvFilePath + "' into Google Calendar!");

    } catch (IOException e) {
      System.err.println("Error exporting calendar: " + e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
    }
  }
}