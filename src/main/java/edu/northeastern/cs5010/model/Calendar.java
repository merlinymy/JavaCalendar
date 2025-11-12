package edu.northeastern.cs5010.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a calendar that manages events and recurrent events, with support for
 * features such as date and time conflict checking, editing, and import/export functionality.
 */
public class Calendar {

  // TODO: use Map for events: key would be a combination of subject, and start date
  private String title;
  private final List<Event> eventList = new ArrayList<>();
  private final List<RecurrentEvent> recurrentEvents = new ArrayList<>();
  // Additional configuration
  private Boolean allowConflictEvents = false;
  private final List<CalendarListener> listeners = new ArrayList<>();


  /**
   * Creates a new instance of the Calendar with the specified title.
   *
   * @param title the title of the calendar
   */
  public Calendar(String title) {
    this.title = title;
  }

  /**
   * Retrieves an event by its unique ID.
   *
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
   * Retrieves an event by its subject, start date, and start time.
   *
   * @param subject the subject of the event.
   * @param startDate the start date of the event in "yyyy-MM-dd" format.
   * @param startTime the start time of the event in "HH:mm:ss" format.
   * @return the first {@link Event} matching the given parameters, or null if not found.
   */
  public Event getOneEvent(String subject, String startDate, String startTime) {
    Event res;

    // check if a valid event exists in eventList
    for (Event event : eventList) {
      if (event.getSubject().equals(subject) && event.getStartDate().equals(startDate)
          && event.getStartTime().equals(startTime)) {
        res = event;
        return res;
      }
    }

    // check if a valid event exists in the recurrentEventList
    for (RecurrentEvent recurrentEvent : recurrentEvents) {
      for (Event event : recurrentEvent.getEvents()) {
        if (event.getSubject().equals(subject) && event.getStartDate().equals(startDate)
            && event.getStartTime().equals(startTime)) {
          res = event;
          return res;
        }
      }
    }
    return null;
  }

  /**
   * Retrieves all events within the specified date range.
   *
   * @param startDate the start date of the range in "yyyy-MM-dd" format.
   * @param endDate the end date of the range in "yyyy-MM-dd" format.
   * @return a list of {@link Event} objects that occur within the date range.
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
   * Checks if an event exists at the specified date and time.
   *
   * @param date the date to check in "yyyy-MM-dd" format.
   * @param time the time to check in "HH:mm:ss" format.
   * @return true if an event exists at that date and time, false otherwise.
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

    boolean isDateInRange = (targetDate.isEqual(eventStartDate)
        || targetDate.isAfter(eventStartDate))
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
   * Adds a non-recurrent event to the calendar.
   *
   * @param newEvent the {@link Event} to be added.
   * @throws IllegalArgumentException if the event already exists or conflicts with an existing
   *     event when conflict checking is enabled.
   */
  public void addEvent(Event newEvent) {

    for (Event existingEvent : eventList) {
      if (eventList.contains(newEvent)) {
        throw new IllegalArgumentException("Same event exists in this calendar");
      }

      if (allowConflictEvents == false
          && (isOverlapping(existingEvent, newEvent) || isOverlapping(newEvent, existingEvent))) {
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

    // notify view
    announceEventAdded(newEvent);

    System.out.println("Add event successful");
  }

  /**
   * Adds a recurrent event to the calendar.
   *
   * @param newRecurrentEvent the {@link RecurrentEvent} to be added.
   * @throws IllegalArgumentException if the recurrent event already exists or if any of its
   *     instances conflict with existing events when conflict checking is enabled.
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

    // notify view
    announceRecurrentEventsAdded(newRecurrentEvent);

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
      LocalDateTime eventStartLocalDateTime = e2StartDate.atTime(e2StartTime);
      LocalDateTime eventEndLocalDateTime = e2EndDate.atTime(e2EndTime);

      boolean e1StartsWithinE2 = thisStartLocalDateTime.isAfter(eventStartLocalDateTime)
          && thisStartLocalDateTime.isBefore(eventEndLocalDateTime);
      boolean e1EndsWithinE2 = thisEndLocalDateTime.isAfter(eventStartLocalDateTime)
          && thisEndLocalDateTime.isBefore(eventEndLocalDateTime);
      boolean sameStartTime = thisStartLocalDateTime.isEqual(eventStartLocalDateTime);

      return e1StartsWithinE2 || e1EndsWithinE2 || sameStartTime;
    }
  }

  /**
   * Gets the title of the calendar.
   *
   * @return the title of the calendar.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the list of non-recurrent events in the calendar.
   *
   * @return the list of non-recurrent events.
   */
  public List<Event> getEventList() {
    return eventList;
  }

  /**
   * Gets the list of recurrent events in the calendar.
   *
   * @return the list of recurrent events.
   */
  public List<RecurrentEvent> getRecurrentEvents() {
    return recurrentEvents;
  }

  /**
   * Sets the title of the calendar.
   *
   * @param title the new title.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Checks if event conflicts are allowed in the calendar.
   *
   * @return true if conflicts are allowed, false otherwise.
   */
  public Boolean getAllowConflict() {
    return allowConflictEvents;
  }

  /**
   * Sets whether event conflicts are allowed in the calendar.
   *
   * @param b true to allow conflicts, false to disallow.
   */
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
    return "Calendar{"
        + "title='" + title + '\''
        + ", eventList=" + eventList
        + '}';
  }

  private boolean checkIfEventIsInRange(Event event, String startDate, String endDate) {
    LocalDate eventStart = LocalDate.parse(event.getStartDate());
    LocalDate eventEnd = LocalDate.parse(event.getEndDate());
    LocalDate rangeStart = LocalDate.parse(startDate);
    LocalDate rangeEnd = LocalDate.parse(endDate);

    return (eventStart.isBefore(rangeEnd) || eventStart.isEqual(rangeEnd))
        && (eventEnd.isAfter(rangeStart) || eventEnd.isEqual(rangeStart));
  }

  /**
   * Edits an existing event in the calendar.
   *
   * @param eventId the unique ID of the event to edit.
   * @param newSubject the new subject, or null to keep current.
   * @param newStartDate the new start date in "yyyy-MM-dd" format, or null to keep current.
   * @param newEndDate the new end date in "yyyy-MM-dd" format, or null to keep current.
   * @param newStartTime the new start time in "HH:mm:ss" format, or null to keep current.
   * @param newEndTime the new end time in "HH:mm:ss" format, or null to keep current.
   * @param newIsPublic the new visibility setting, or null to keep current.
   * @param newDescription the new description, or null to keep current.
   * @param newLocation the new location, or null to keep current.
   * @throws IllegalArgumentException if the event doesn't exist, end date/time is invalid,
   *     or the updated event conflicts with another event when conflict checking is enabled.
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
    final String finalSubject = newSubject != null ? newSubject : event.getSubject();
    String finalStartDate = newStartDate != null ? newStartDate : event.getStartDate();
    String finalEndDate = newEndDate != null ? newEndDate : event.getEndDate();
    String finalStartTime = newStartTime != null ? newStartTime : event.getStartTime();
    String finalEndTime = newEndTime != null ? newEndTime : event.getEndTime();
    final Boolean finalIsPublic = newIsPublic != null ? newIsPublic : event.getPublic();
    final String finalDescription = newDescription != null
        ? newDescription : event.getDescription();
    final String finalLocation = newLocation != null ? newLocation : event.getLocation();

    // Validate that both times are null or both are non-null (for the final state)
    if ((finalStartTime == null && finalEndTime != null)
        || (finalStartTime != null && finalEndTime == null)) {
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
          if (isOverlapping(tempEvent, existingEvent)
              || isOverlapping(existingEvent, tempEvent)) {
            throw new IllegalArgumentException("Updated event conflicts with existing event");
          }
        }
      }

      // Check against all recurrent events (excluding the original event)
      for (RecurrentEvent recurrentEvent : recurrentEvents) {
        for (Event recurrentEventInstance : recurrentEvent.getEvents()) {
          if (recurrentEventInstance != event) {
            if (isOverlapping(tempEvent, recurrentEventInstance)
                || isOverlapping(recurrentEventInstance, tempEvent)) {
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

    // notify view
    announceEventModified(event);
  }

  /**
   * Edits all instances in a recurring event series.
   *
   * @param recurrentEventId the unique ID of the recurrent event series to edit.
   * @param newSubject the new subject for all instances, or null to keep current.
   * @param newStartTime the new start time for all instances in "HH:mm:ss" format,
   *                     or null to keep current.
   * @param newEndTime the new end time for all instances in "HH:mm:ss" format,
   *                   or null to keep current.
   * @param newIsPublic the new visibility setting for all instances, or null to keep current.
   * @param newDescription the new description for all instances, or null to keep current.
   * @param newLocation the new location for all instances, or null to keep current.
   * @throws IllegalArgumentException if the recurrent event doesn't exist, end time is invalid,
   *     or any updated instance conflicts with another event when conflict checking is enabled.
   */
  public void editRecurrentEvent(String recurrentEventId, String newSubject, String newStartTime,
                                  String newEndTime, Boolean newIsPublic, String newDescription,
                                  String newLocation) {
    // Find the recurrent event
    RecurrentEvent targetRecurrentEvent = null;
    for (RecurrentEvent recurrentEvent : recurrentEvents) {
      if (recurrentEvent.getId().equals(recurrentEventId)) {
        targetRecurrentEvent = recurrentEvent;
        break;
      }
    }

    // Validate that the recurrent event exists
    if (targetRecurrentEvent == null) {
      throw new IllegalArgumentException("Recurrent event with ID " + recurrentEventId
          + " not found in calendar");
    }

    List<Event> recurrentEvents = targetRecurrentEvent.getEvents();

    // Validate that we have at least one instance
    if (recurrentEvents.isEmpty()) {
      throw new IllegalArgumentException("Recurrent event has no event instances");
    }

    // Get the first instance to determine current values
    Event firstInstance = recurrentEvents.getFirst();

    // Determine the final values (use new values if provided, otherwise keep old values)
    String finalSubject = newSubject != null ? newSubject : firstInstance.getSubject();
    String finalStartTime = newStartTime != null ? newStartTime : firstInstance.getStartTime();
    String finalEndTime = newEndTime != null ? newEndTime : firstInstance.getEndTime();
    Boolean finalIsPublic = newIsPublic != null ? newIsPublic : firstInstance.getPublic();
    String finalDescription = newDescription != null
        ? newDescription : firstInstance.getDescription();
    String finalLocation = newLocation != null ? newLocation : firstInstance.getLocation();

    // Validate that both times are null or both are non-null (for the final state)
    if ((finalStartTime == null && finalEndTime != null)
        || (finalStartTime != null && finalEndTime == null)) {
      throw new IllegalArgumentException("Both start time and end time must be null, "
          + "or both must be non-null");
    }

    // If times are provided, validate them
    if (finalStartTime != null && finalEndTime != null) {
      LocalTime startTime = LocalTime.parse(finalStartTime);
      LocalTime endTime = LocalTime.parse(finalEndTime);

      // For same-day events, validate times
      if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
        throw new IllegalArgumentException("End time must be after start time");
      }
    }

    // Check for conflicts if allowConflictEvents is false
    if (!allowConflictEvents) {
      // For each instance in the series, check if the updated version would conflict
      for (Event instance : recurrentEvents) {
        // Create a temporary event with the final values for this instance
        Event.Builder tempBuilder = new Event.Builder(finalSubject,
            instance.getStartDate(), instance.getEndDate());

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

        // Check against all events in eventList
        for (Event existingEvent : eventList) {
          if (isOverlapping(tempEvent, existingEvent)
              || isOverlapping(existingEvent, tempEvent)) {
            throw new IllegalArgumentException("Updated recurrent event would conflict with "
                + "existing event on " + instance.getStartDate());
          }
        }

        // Check against all recurrent events (excluding instances from this same series)
        for (RecurrentEvent recurrentEvent : this.recurrentEvents) {
          for (Event recurrentEventInstance : recurrentEvent.getEvents()) {
            // Skip instances from the same series
            if (recurrentEvent == targetRecurrentEvent) {
              continue;
            }

            if (isOverlapping(tempEvent, recurrentEventInstance)
                || isOverlapping(recurrentEventInstance, tempEvent)) {
              throw new IllegalArgumentException("Updated recurrent event would conflict with "
                  + "existing recurrent event on " + instance.getStartDate());
            }
          }
        }
      }
    }

    // All validations passed - update all instances in the series
    for (Event re : recurrentEvents) {
      re.setSubject(finalSubject);

      if (finalStartTime != null && finalEndTime != null) {
        re.setStartTime(LocalTime.parse(finalStartTime));
        re.setEndTime(LocalTime.parse(finalEndTime));
      } else {
        re.setStartTime(null);
        re.setEndTime(null);
      }

      re.setPublic(finalIsPublic);
      re.setDescription(finalDescription);
      re.setLocation(finalLocation);

      // notify view
      announceEventModified(re);
    }

    System.out.println("All instances in recurrent event series edited successfully");
  }

  /**
   * Adds a listener to the calendar to receive notifications about event changes.
   *
   * @param listener the {@link CalendarListener} listener to add.
   * @throws NullPointerException if {@code listener} is null.
   */
  public void addCalendarListener(CalendarListener listener) {
    CalendarListener nonNullListener = Objects.requireNonNull(listener,
        "listener cannot be null");
    if (!listeners.contains(nonNullListener)) {
      listeners.add(nonNullListener);
    }
  }

  /**
   * Removes a previously added {@link CalendarListener} from the calendar.
   *
   * @param listener the {@link CalendarListener} to remove.
   * @throws NullPointerException if {@code listener} is null.
   */
  public void removeCalendarListener(CalendarListener listener) {
    CalendarListener nonNullListener = Objects.requireNonNull(listener,
        "listener cannot be null");
    listeners.remove(nonNullListener);
  }

  private void announceEventAdded(Event event) {
    for (CalendarListener listener :  listeners) {
      listener.onEventAdded(event);
    }
  }

  private void announceRecurrentEventsAdded(RecurrentEvent re) {
    for (CalendarListener listener : listeners) {
      listener.onRecurrentEventAdded(re);
    }
  }

  private void announceEventModified(Event event) {
    for (CalendarListener listener : listeners) {
      listener.onEventModified(event);
    }
  }

  /**
   * Exports the calendar to a CSV file in Google Calendar format.
   *
   * @param filePath the path where the CSV file should be saved.
   * @throws IOException if an I/O error occurs while writing the file.
   */
  public void exportToCsv(String filePath) throws IOException {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

    try (FileWriter writer = new FileWriter(filePath)) {
      // Write CSV header
      writer.append("Subject,Start Date,Start Time,End Date,End Time,"
          + "All Day Event,Description,Location,Private\n");

      // Write all regular events
      for (Event event : eventList) {
        writeEventToCsv(writer, event, dateFormatter, timeFormatter);
      }

      // Write all recurrent events
      for (RecurrentEvent recurrentEvent : recurrentEvents) {
        for (Event event : recurrentEvent.getEvents()) {
          writeEventToCsv(writer, event, dateFormatter, timeFormatter);
        }
      }
    }
  }

  private void writeEventToCsv(FileWriter writer, Event event,
                                DateTimeFormatter dateFormatter,
                                DateTimeFormatter timeFormatter) throws IOException {
    // Subject (required) - escape if contains commas
    String subject = escapeCsvField(event.getSubject());
    writer.append(subject).append(",");

    // Start Date (required)
    LocalDate startDate = LocalDate.parse(event.getStartDate());
    writer.append(startDate.format(dateFormatter)).append(",");

    // Start Time (optional)
    if (event.getStartTime() != null) {
      LocalTime startTime = LocalTime.parse(event.getStartTime());
      writer.append(startTime.format(timeFormatter)).append(",");
    } else {
      writer.append(",");
    }

    // End Date (required)
    LocalDate endDate = LocalDate.parse(event.getEndDate());
    writer.append(endDate.format(dateFormatter)).append(",");

    // End Time (optional)
    if (event.getEndTime() != null) {
      LocalTime endTime = LocalTime.parse(event.getEndTime());
      writer.append(endTime.format(timeFormatter)).append(",");
    } else {
      writer.append(",");
    }

    // All Day Event
    boolean isAllDayEvent = event.getStartTime() == null;
    writer.append(isAllDayEvent ? "True" : "False").append(",");

    // Description (optional) - escape if contains commas
    String description = event.getDescription() != null
        ? escapeCsvField(event.getDescription()) : "";
    writer.append(description).append(",");

    // Location (optional) - escape if contains commas
    String location = event.getLocation() != null
        ? escapeCsvField(event.getLocation()) : "";
    writer.append(location).append(",");

    // Private (opposite of isPublic)
    Boolean isPublic = event.getPublic();
    String privateValue = (isPublic != null && !isPublic) ? "True" : "False";
    writer.append(privateValue);

    // New line
    writer.append("\n");
  }

  private String escapeCsvField(String field) {
    if (field == null) {
      return "";
    }

    // If field contains comma, quote, or newline, wrap in quotes and escape internal quotes
    if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
      return "\"" + field.replace("\"", "\"\"") + "\"";
    }

    return field;
  }

  /**
   * Imports a calendar from a CSV file previously exported by {@link #exportToCsv(String)}.
   * The title is provided by the caller; events are reconstructed as single events.
   *
   * @param title the calendar title to assign.
   * @param filePath path to the CSV file to load.
   * @return a new {@link Calendar} populated with events from the CSV.
   * @throws IOException if reading the file fails or format is invalid.
   */
  public static Calendar importFromCsv(String title, String filePath) throws IOException {
    DateTimeFormatter csvDateFmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter csvTimeFmt = DateTimeFormatter.ofPattern("h:mm a");
    DateTimeFormatter hhmmss = DateTimeFormatter.ofPattern("HH:mm:ss");

    Calendar calendar = new Calendar(title);

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String header = br.readLine();
      if (header == null) {
        return calendar; // empty file, empty calendar
      }

      // Basic header validation (lenient: just ensure first few columns match)
      String expectedPrefix = "Subject,Start Date,Start Time,End Date,End Time,All Day Event";
      if (!header.startsWith(expectedPrefix)) {
        throw new IOException("Unexpected CSV header format");
      }

      String line;
      while ((line = br.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }
        List<String> cols = parseCsvLine(line);
        // Expected 9 columns based on export
        if (cols.size() < 9) {
          throw new IOException("Malformed CSV line: " + line);
        }

        String subject = unescapeCsvField(cols.get(0));
        String startDateStr = cols.get(1);
        String startTimeStr = cols.get(2);
        String endDateStr = cols.get(3);
        String endTimeStr = cols.get(4);
        String allDayStr = cols.get(5);
        String description = unescapeCsvField(cols.get(6));
        String location = unescapeCsvField(cols.get(7));
        final String privateStr = cols.get(8);

        // Convert dates from MM/dd/yyyy to yyyy-MM-dd
        LocalDate startDate = LocalDate.parse(startDateStr, csvDateFmt);
        LocalDate endDate = LocalDate.parse(endDateStr, csvDateFmt);

        Event.Builder builder = new Event.Builder(subject,
            startDate.toString(), endDate.toString());

        boolean isAllDay = "True".equalsIgnoreCase(allDayStr);
        if (!isAllDay) {
          // Convert times from h:mm a to HH:mm:ss
          if (startTimeStr != null && !startTimeStr.isBlank()) {
            LocalTime st = LocalTime.parse(startTimeStr, csvTimeFmt);
            builder.startTime(st.format(hhmmss));
          }
          if (endTimeStr != null && !endTimeStr.isBlank()) {
            LocalTime et = LocalTime.parse(endTimeStr, csvTimeFmt);
            builder.endTime(et.format(hhmmss));
          }
        }

        if (description != null && !description.isEmpty()) {
          builder.description(description);
        }
        if (location != null && !location.isEmpty()) {
          builder.location(location);
        }

        // Private column is opposite of isPublic
        Boolean isPublic = (privateStr != null && privateStr.equalsIgnoreCase("True"))
            ? Boolean.FALSE
            : Boolean.TRUE;
        builder.isPublic(isPublic);

        calendar.addEvent(builder.build());
      }
    }

    return calendar;
  }

  // Minimal CSV parser supporting quotes and commas as produced by export
  private static List<String> parseCsvLine(String line) {
    List<String> out = new ArrayList<>();
    StringBuilder cur = new StringBuilder();
    boolean inQuotes = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (inQuotes) {
        if (c == '\"') {
          // Peek next for escaped quote
          if (i + 1 < line.length() && line.charAt(i + 1) == '\"') {
            cur.append('\"');
            i++; // skip next
          } else {
            inQuotes = false;
          }
        } else {
          cur.append(c);
        }
      } else {
        if (c == ',') {
          out.add(cur.toString());
          cur.setLength(0);
        } else if (c == '\"') {
          inQuotes = true;
        } else {
          cur.append(c);
        }
      }
    }
    out.add(cur.toString());
    return out;
  }

  private static String unescapeCsvField(String field) {
    if (field == null) {
      return null;
    }
    return field;
  }

}
