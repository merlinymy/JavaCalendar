package edu.northeastern.cs5010;

import edu.northeastern.cs5010.model.Calendar;
import edu.northeastern.cs5010.model.Event;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

  public static void main(String[] args) {
    //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
    // to see how IntelliJ IDEA suggests fixing it.
    System.out.printf("My Calendar App");

    Calendar studyCalendar = new Calendar("Study");
    System.out.println(studyCalendar.getAllowConflict());

    Event event1 = new Event.Builder("event1", "2025-10-26", "2025-10-26").startTime("09:00:00").endTime("10:00:00").build();
    Event event2 = new Event.Builder("event1", "2025-10-26", "2025-10-26").startTime("08:00:00").endTime("09:15:00").build();

    studyCalendar.addEvent(event1);
    try {
      studyCalendar.addEvent(event2);

    } catch (Exception e) {
      System.out.println(e);
    }
    System.out.println(studyCalendar);



  }
}