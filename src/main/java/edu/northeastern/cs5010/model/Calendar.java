package edu.northeastern.cs5010.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Calendar {

  // TODO: use Map for events: key would be a combination of subject, and start date
  private String title;
  private final List<Event> eventList = new ArrayList<Event>();
  private final List<RecurrentEvent> recurrentEvents = new ArrayList<RecurrentEvent>();
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
   * Add a non-recurrent event to the calendar. Performs conflict check before adding.
   * @param newEvent an event {@link Event} to be added
   */
  public void addEvent(Event newEvent) {

    for (Event existingEvent : eventList) {
      if (eventList.contains(newEvent)) {
        throw new IllegalArgumentException("Same event exists in this calendar");
      }

      if (allowConflictEvents == false &&
          (isOverlapping(existingEvent, newEvent) || isOverlapping(newEvent, existingEvent))) {
        throw new IllegalArgumentException("There is an overlapping event, you can turn on "
            + "allowConflictEvents in calendar setting");
      }
    }

    // Check for conflicts with recurrent events
    if (!allowConflictEvents) {
      for (RecurrentEvent recurrentEvent : recurrentEvents) {
        for (Event recurrentEventInstance : recurrentEvent.getEvents()) {
          if (isOverlapping(recurrentEventInstance, newEvent)
              || isOverlapping(newEvent, recurrentEventInstance)) {
            throw new IllegalArgumentException("Event conflicts with existing recurrent event. "
                + "You can turn on allowConflictEvents in calendar setting");
          }
        }
      }
    }

    eventList.add(newEvent);
    System.out.println("Add event successful");
  }

  /**
   * Add a recurrent event to the calendar. Performs conflict check against both individual
   * events and existing recurrent events before adding.
   * @param newRecurrentEvent a recurrent event {@link RecurrentEvent} to be added
   * @throws IllegalArgumentException if the recurrent event already exists, or if any of its
   *     generated events would conflict with existing events when allowConflictEvents is false
   */
  public void addRecurrentEvent(RecurrentEvent newRecurrentEvent) {
    // Check if this exact recurrent event already exists
    if (recurrentEvents.contains(newRecurrentEvent)) {
      throw new IllegalArgumentException("Same recurrent event exists in this calendar");
    }

    // If conflicts are not allowed, check for overlaps
    if (!allowConflictEvents) {
      List<Event> newEvents = newRecurrentEvent.getEvents();

      // Check each generated event against individual events in the calendar
      for (Event newEvent : newEvents) {
        for (Event existingEvent : eventList) {
          if (isOverlapping(existingEvent, newEvent) || isOverlapping(newEvent, existingEvent)) {
            throw new IllegalArgumentException("Recurrent event conflicts with existing event. "
                + "You can turn on allowConflictEvents in calendar setting");
          }
        }

        // Check each generated event against events from existing recurrent events
        for (RecurrentEvent existingRecurrentEvent : recurrentEvents) {
          for (Event existingRecurrentEventInstance : existingRecurrentEvent.getEvents()) {
            if (isOverlapping(existingRecurrentEventInstance, newEvent)
                || isOverlapping(newEvent, existingRecurrentEventInstance)) {
              throw new IllegalArgumentException("Recurrent event conflicts with existing "
                  + "recurrent event. You can turn on allowConflictEvents in calendar setting");
            }
          }
        }
      }
    }

    // If no conflicts or conflicts are allowed, add the recurrent event
    recurrentEvents.add(newRecurrentEvent);
    System.out.println("Add recurrent event successful");
  }


  private Boolean isOverlapping(Event e1, Event e2) {
    boolean isAllDayEvent = e1.getStartTime() == null || e2.getStartTime() == null;

    if (isAllDayEvent) {
      LocalDate e1Start = LocalDate.parse(e1.getStartDate());
      LocalDate e1End = LocalDate.parse(e1.getEndDate());
      LocalDate e2Start = LocalDate.parse(e2.getStartDate());
      LocalDate e2End = LocalDate.parse(e2.getEndDate());

      boolean e1StartsWithinE2 = e1Start.isAfter(e2Start) && e1Start.isBefore(e2End);
      boolean e1EndsWithinE2 = e1End.isAfter(e2Start) && e1End.isBefore(e2End);
      boolean sameStartDate = e1Start.isEqual(e2Start);
      boolean e1EndsOnE2Start = e1End.isEqual(e2Start);

      return e1StartsWithinE2 || e1EndsWithinE2 || sameStartDate || e1EndsOnE2Start;
    } else {
      LocalDate e1StartDate = LocalDate.parse(e1.getStartDate());
      LocalDate e1EndDate = LocalDate.parse(e1.getEndDate());
      LocalTime e1StartTime = LocalTime.parse(e1.getStartTime());
      LocalTime e1EndTime = LocalTime.parse(e1.getEndTime());

      LocalDate e2StartDate = LocalDate.parse(e2.getStartDate());
      LocalDate e2EndDate = LocalDate.parse(e2.getEndDate());
      LocalTime e2StartTime = LocalTime.parse(e2.getStartTime());
      LocalTime e2EndTime = LocalTime.parse(e2.getEndTime());

      LocalDateTime thisStartLocalDateTime = e1StartDate.atTime(e1StartTime);
      LocalDateTime thisEndLocalDateTime = e1EndDate.atTime(e1EndTime);
      LocalDateTime eStartLocalDateTime = e2StartDate.atTime(e2StartTime);
      LocalDateTime eEndLocalDateTime = e2EndDate.atTime(e2EndTime);

      boolean e1StartsWithinE2 = thisStartLocalDateTime.isAfter(eStartLocalDateTime)
          && thisStartLocalDateTime.isBefore(eEndLocalDateTime);
      boolean e1EndsWithinE2 = thisEndLocalDateTime.isAfter(eStartLocalDateTime)
          && thisEndLocalDateTime.isBefore(eEndLocalDateTime);
      boolean sameStartTime = thisStartLocalDateTime.isEqual(eStartLocalDateTime);

      return e1StartsWithinE2 || e1EndsWithinE2 || sameStartTime;
    }
  }

  public String getTitle() {
    return title;
  }

  public List<Event> getEventList() {
    return eventList;
  }

  public List<RecurrentEvent> getRecurrentEvents() {
    return recurrentEvents;
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
