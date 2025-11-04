package edu.northeastern.cs5010.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * RecurrentEvent class creates a recurrentEvent object. This object has a List of Events.
 *
 */

public class RecurrentEvent {

  // unique identifier for the recurrent event series
  private final String id;

  private RecurrencePattern pattern;
  private LocalDate startDate;
  private LocalTime startTime;
  private LocalTime endTime;
  private List<Event> events = new ArrayList<>();

  /**
   * Constructor of RecurrentEvent class.

   * @param pattern the recurrence {@link RecurrencePattern} used for this event
   * @param startDate the start date for this recurrentEvent
   * @param startTime the start time for this recurrentEvent
   * @param endTime the end time for this recurrentEvent
   * @param subject the subject of the event.
   * @param isPublic the visibility of the event
   * @param description the description of the event
   * @param location the location of the event
   */

  public RecurrentEvent(RecurrencePattern pattern, LocalDate startDate, LocalTime startTime,
      LocalTime endTime, String subject, Boolean isPublic, String description, String location) {
    this.id = UUID.randomUUID().toString();
    this.pattern = pattern;

    this.events = generateFutureEvents(pattern, startDate, startTime, endTime, subject,
        isPublic, description, location);
    System.out.println(this.events);
  }

  private List<Event> generateFutureEvents(RecurrencePattern pattern, LocalDate startDate,
      LocalTime startTime, LocalTime endTime, String subject, Boolean isPublic, String description,
      String location) {

    List<Event> events = new ArrayList<>();
    List<String> days = pattern.getDays();
    Integer recurrenceToEnd = pattern.getRecurrenceNumToEnd();
    LocalDate dateTimeToEnd = null;
    if (pattern.getDateTimeToEnd() != null) {
      dateTimeToEnd = LocalDate.parse(pattern.getDateTimeToEnd());
    }

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    Map<String, Integer> dayRankingHelper = Map.of("MONDAY", 1,
        "TUESDAY", 2,
        "WEDNESDAY", 3,
        "THURSDAY", 4,
        "FRIDAY", 5,
        "SATURDAY", 6,
        "SUNDAY", 7);

    // invalid cases
    if ((recurrenceToEnd != null && days.size() > recurrenceToEnd)
        || (dateTimeToEnd != null && dateTimeToEnd.isBefore(startDate))) {
      System.out.println(recurrenceToEnd != null);
      throw new IllegalArgumentException("Invalid cases");
    }

    LocalDate currentStartDate = startDate;

    // End condition is after number of recurrence
    if (recurrenceToEnd != null) {
      int daysPointer = 0;
      String nextDay = "";

      while (recurrenceToEnd > 0) {

        String currentDay = currentStartDate.getDayOfWeek().toString();

        int indexOfCurrentDay = days.indexOf(currentDay);

        // If the current day IS a recurrence day, create an event for it first
        if (indexOfCurrentDay != -1) {
          Event event = new Event.Builder(subject,
              currentStartDate.format(dateFormatter),
              currentStartDate.format(dateFormatter))
              .startTime(startTime.format(timeFormatter))
              .endTime(endTime.format(timeFormatter))
              .description(description)
              .isPublic(isPublic)
              .location(location).build();

          events.add(event);
          recurrenceToEnd--;

          if (recurrenceToEnd == 0) {
            break;
          }

          // Move to the next recurrence day
          if (indexOfCurrentDay == days.size() - 1) {
            daysPointer = 0;
          } else {
            daysPointer = indexOfCurrentDay + 1;
          }
        } else {
          // Current day is NOT a recurrence day, find the next one
          int currentDayRank = dayRankingHelper.get(currentDay);
          for (int i = 0; i <= days.size() - 1; i++) {
            int dayRank = dayRankingHelper.get(days.get(i));
            if (dayRank > currentDayRank) {
              daysPointer = i;
              break;
            }
          }
        }

        nextDay = days.get(daysPointer);

        int daysDiff = ((dayRankingHelper.get(nextDay) - dayRankingHelper.get(currentDay)
            - 1 + 7) % 7) + 1;

        currentStartDate = currentStartDate.plusDays(daysDiff);
      }
    } else { // End condition is on a certain day
      while (!startDate.isAfter(dateTimeToEnd)) {
        if (days.contains(startDate.getDayOfWeek().toString())) {
          Event newEvent = new Event.Builder(subject, startDate.format(dateFormatter),
              startDate.format(dateFormatter)).description(description)
              .isPublic(isPublic)
              .location(location)
              .startTime(startTime.format(timeFormatter))
              .endTime(endTime.format(timeFormatter))
              .build();
          events.add(newEvent);
        }
        startDate = startDate.plusDays(1);
      }
    }
    return events;
  }

  /**
   * Get the id of the recurrent event.
   *
   * @return the id of the recurrent event.
   */
  public String getId() {
    return id;
  }

  /**
   * Get the recurrent event list.
   *
   * @return the recurrent event list.
   */
  public List<Event> getEvents() {
    return events;
  }
}
