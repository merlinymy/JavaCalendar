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
   * Retrieve an event given its unique ID.
   * @param id the unique ID of the event.
   * @return the {@link Event} with the given ID, or null if not found.
   */
  public Event getEventById(String id) {
    // Check in eventList
    for (Event event : eventList) {
      if (event.getId().equals(id)) {
        return event;
      }
    }

    // Check in recurrentEvents
    for (RecurrentEvent recurrentEvent : recurrentEvents) {
      for (Event event : recurrentEvent.getEvents()) {
        if (event.getId().equals(id)) {
          return event;
        }
      }
    }

    return null;
  }

  /**
   * Retrieve an event given its subject, start date, and start time.
   * @param subject the subject of an event.
   * @param startDate the startDate of an event. Must be in the format of "yyyy-MM-dd".
   * @param startTime the startTime of an event. Must be in the format of "HH:mm:ss".
   * @return the first {@link Event} event object with given params.
   */
  public Event getOneEvent(String subject, String startDate, String startTime) {
    Event res;

    // check if a valid event exists in eventList
    for (Event event : eventList) {
      if (event.getSubject().equals(subject) && event.getStartDate().equals(startDate) &&
          event.getStartTime().equals(startTime)) {
        res = event;
        return res;
      }
    }

    // check if a valid event exists in the recurrentEventList
    for (RecurrentEvent recurrentEvent : recurrentEvents) {
      for (Event event : recurrentEvent.getEvents()) {
        if (event.getSubject().equals(subject) && event.getStartDate().equals(startDate) &&
            event.getStartTime().equals(startTime)) {
          res = event;
          return res;
        }
      }
    }
    return null;
  }

  /**
   * Retrieve all events between a startDate and an endDate.
   * @param startDate the startDate of a date range.
   * @param endDate the endDate of a date range.
   * @return a list of {@link Event} events that are between the startDate and the endDate.
   */
  public List<Event> getAllEventsInRange(String startDate, String endDate) {
    List<Event> res = new ArrayList<>();

    // query non-recurrent event list. Add valid events to res
    for (Event event : eventList) {
      if (checkIfEventIsInRange(event, startDate, endDate)) {
        res.add(event);
      }
    }

    // query recurrent event list. Add valid events to res
    for (RecurrentEvent recurrentEvent : recurrentEvents) {
      for (Event event : recurrentEvent.getEvents()) {
        if (checkIfEventIsInRange(event, startDate, endDate)) {
          res.add(event);
        }
      }
    }

    return res;
  }

  /**
   * Check if an event exists on the provided date and at the provided time.
   * @param date a date used to perform the check.
   * @param time a time used to perform the check
   * @return true if an event exists, false if an event doesn't exist.
   */
  public boolean isUserBusyOnDayAtTime(String date, String time) {
    LocalDate targetDate = LocalDate.parse(date);
    LocalTime targetTime = LocalTime.parse(time);
    LocalDateTime targetDateTime = targetDate.atTime(targetTime);

    // Check for non-recurrent events
    for (Event event : eventList) {
      if (isEventOccupyingDateTime(event, targetDate, targetDateTime)) {
        return true;
      }
    }

    // Check for recurrent events
    for (RecurrentEvent recurrentEvent : recurrentEvents) {
      for (Event event : recurrentEvent.getEvents()) {
        if (isEventOccupyingDateTime(event, targetDate, targetDateTime)) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean isEventOccupyingDateTime(Event event, LocalDate targetDate,
                                            LocalDateTime targetDateTime) {
    LocalDate eventStartDate = LocalDate.parse(event.getStartDate());
    LocalDate eventEndDate = LocalDate.parse(event.getEndDate());

    boolean isDateInRange = (targetDate.isEqual(eventStartDate) || targetDate.isAfter(eventStartDate))
        && (targetDate.isEqual(eventEndDate) || targetDate.isBefore(eventEndDate));

    if (!isDateInRange) {
      return false;
    }

    // If an event has no time, just check if the date matches
    if (event.getStartTime() == null) {
      return true;
    }

    // Check if the target time is between event's time range
    LocalTime eventStartTime = LocalTime.parse(event.getStartTime());
    LocalTime eventEndTime = LocalTime.parse(event.getEndTime());
    LocalDateTime eventStart = eventStartDate.atTime(eventStartTime);
    LocalDateTime eventEnd = eventEndDate.atTime(eventEndTime);

    return (targetDateTime.isEqual(eventStart) || targetDateTime.isAfter(eventStart))
        && targetDateTime.isBefore(eventEnd);
  }

  /**
   * Add a non-recurrent event to the calendar. Performs conflict check before adding.
   * @param newEvent an event {@link Event} to be added.
   * @throws IllegalArgumentException if conflict events exist in the calendar.
   *
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
   * Add a recurrent event to the calendar. Performs conflict check before adding.
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

  private boolean checkIfEventIsInRange (Event event, String startDate, String endDate) {
    LocalDate eventStart = LocalDate.parse(event.getStartDate());
    LocalDate eventEnd = LocalDate.parse(event.getEndDate());
    LocalDate rangeStart = LocalDate.parse(startDate);
    LocalDate rangeEnd = LocalDate.parse(endDate);

    return (eventStart.isBefore(rangeEnd) || eventStart.isEqual(rangeEnd))
        && (eventEnd.isAfter(rangeStart) || eventEnd.isEqual(rangeStart));
  }

  /**
   * Edit an existing event in the calendar. Only non-null parameters will be updated.
   * Pass null for any field you don't want to change.
   *
   * @param eventId the unique ID of the event to edit
   * @param newSubject the new subject, or null to keep current
   * @param newStartDate the new start date (format: "yyyy-MM-dd"), or null to keep current
   * @param newEndDate the new end date (format: "yyyy-MM-dd"), or null to keep current
   * @param newStartTime the new start time (format: "HH:mm:ss"), or null to keep current
   * @param newEndTime the new end time (format: "HH:mm:ss"), or null to keep current
   * @param newIsPublic the new visibility setting, or null to keep current
   * @param newDescription the new description, or null to keep current
   * @param newLocation the new location, or null to keep current
   * @throws IllegalArgumentException if:
   *     - The event with the given ID doesn't exist
   *     - The new end date/time is before or equal to the new start date/time
   *     - The updated event conflicts with another event (when allowConflictEvents is false)
   */
  public void editEvent(String eventId, String newSubject, String newStartDate, String newEndDate,
                        String newStartTime, String newEndTime, Boolean newIsPublic,
                        String newDescription, String newLocation) {
    Event event = getEventById(eventId);

    // Validate that the event exists
    if (event == null) {
      throw new IllegalArgumentException("Event with ID " + eventId + " not found in calendar");
    }

    // Determine the final values (use new values if provided, otherwise keep old values)
    String finalSubject = newSubject != null ? newSubject : event.getSubject();
    String finalStartDate = newStartDate != null ? newStartDate : event.getStartDate();
    String finalEndDate = newEndDate != null ? newEndDate : event.getEndDate();
    String finalStartTime = newStartTime != null ? newStartTime : event.getStartTime();
    String finalEndTime = newEndTime != null ? newEndTime : event.getEndTime();
    Boolean finalIsPublic = newIsPublic != null ? newIsPublic : event.getPublic();
    String finalDescription = newDescription != null ? newDescription : event.getDescription();
    String finalLocation = newLocation != null ? newLocation : event.getLocation();

    // Validate that both times are null or both are non-null (for the final state)
    if ((finalStartTime == null && finalEndTime != null) ||
        (finalStartTime != null && finalEndTime == null)) {
      throw new IllegalArgumentException("Both start time and end time must be null, "
          + "or both must be non-null");
    }

    // Validate that end date/time is after start date/time
    LocalDate startDate = LocalDate.parse(finalStartDate);
    LocalDate endDate = LocalDate.parse(finalEndDate);

    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("End date cannot be before start date");
    }

    // If times are provided, validate them
    if (finalStartTime != null && finalEndTime != null) {
      LocalTime startTime = LocalTime.parse(finalStartTime);
      LocalTime endTime = LocalTime.parse(finalEndTime);
      LocalDateTime startDateTime = startDate.atTime(startTime);
      LocalDateTime endDateTime = endDate.atTime(endTime);

      if (endDateTime.isBefore(startDateTime) || endDateTime.isEqual(startDateTime)) {
        throw new IllegalArgumentException("End date/time must be after start date/time");
      }
    }

    // Create a temporary event with the final values for conflict checking
    Event.Builder tempBuilder = new Event.Builder(finalSubject, finalStartDate, finalEndDate);

    if (finalStartTime != null && finalEndTime != null) {
      tempBuilder.startTime(finalStartTime).endTime(finalEndTime);
    }

    if (finalIsPublic != null) {
      tempBuilder.isPublic(finalIsPublic);
    }
    if (finalDescription != null) {
      tempBuilder.description(finalDescription);
    }
    if (finalLocation != null) {
      tempBuilder.location(finalLocation);
    }

    Event tempEvent = tempBuilder.build();

    // Check for conflicts if allowConflictEvents is false
    if (!allowConflictEvents) {
      // Check against all events in eventList (excluding the original event)
      for (Event existingEvent : eventList) {
        if (existingEvent != event) {
          if (isOverlapping(tempEvent, existingEvent) ||
              isOverlapping(existingEvent, tempEvent)) {
            throw new IllegalArgumentException("Updated event conflicts with existing event");
          }
        }
      }

      // Check against all recurrent events (excluding the original event)
      for (RecurrentEvent recurrentEvent : recurrentEvents) {
        for (Event recurrentEventInstance : recurrentEvent.getEvents()) {
          if (recurrentEventInstance != event) {
            if (isOverlapping(tempEvent, recurrentEventInstance) ||
                isOverlapping(recurrentEventInstance, tempEvent)) {
              throw new IllegalArgumentException("Updated event conflicts with existing recurrent "
                  + "event");
            }
          }
        }
      }
    }

    // All validations passed - update the event
    event.setSubject(finalSubject);
    event.setStartDate(startDate);
    event.setEndDate(endDate);

    if (finalStartTime != null && finalEndTime != null) {
      event.setStartTime(LocalTime.parse(finalStartTime));
      event.setEndTime(LocalTime.parse(finalEndTime));
    } else {
      event.setStartTime(null);
      event.setEndTime(null);
    }

    event.setPublic(finalIsPublic);
    event.setDescription(finalDescription);
    event.setLocation(finalLocation);
  }
}
