package edu.northeastern.cs5010.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Calendar {

  private String title;
  private final List<Event> eventList = new ArrayList<Event>();

  // Additional configuration
  private Boolean allowConflictEvents = false;

  /**
   * Creates a new instance of the Calendar with the specified title.
   *
   * @param title the title of the calendar
   */
  public Calendar(String title) {
    this.title = title;
  }


  /**
   *
   */
  public void addEvent(Event newEvent) {

    for (Event existingEvent : eventList) {
      if (eventList.contains(newEvent)) {
        throw new IllegalArgumentException("Same event exists in this calendar");
      }

      if (allowConflictEvents == false &&
          (existingEvent.isOverlapping(newEvent) || newEvent.isOverlapping(existingEvent))) {
        throw new IllegalArgumentException("There is an overlapping event, you can turn on "
            + "allowConflictEvents in calendar setting");
      }
    }
    eventList.add(newEvent);
    System.out.println("Add event successful");
  }

  public String getTitle() {
    return title;
  }

  public List<Event> getEventList() {
    return eventList;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Boolean getAllowConflict() {
    return allowConflictEvents;
  }

  public void setAllowConflict(Boolean b) {
    this.allowConflictEvents = b;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Calendar calendar = (Calendar) o;
    return Objects.equals(title, calendar.title) && Objects.equals(eventList,
        calendar.eventList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, eventList);
  }

  @Override
  public String toString() {
    return "Calendar{" +
        "title='" + title + '\'' +
        ", eventList=" + eventList +
        '}';
  }
}
