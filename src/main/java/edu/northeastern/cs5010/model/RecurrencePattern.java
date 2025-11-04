package edu.northeastern.cs5010.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Represents a pattern used to create a {@link RecurrentEvent}recurrent event.
 * There are two ways to create a pattern. You can provide a number represents the occurrence
 * times of an event. Or you can provide a date represents the date
 * that this recurrent event will end. You have to provide the days this recurrent event is on.
 */
public class RecurrencePattern {
  private Integer recurrenceNumToEnd;
  private LocalDate dateTimeToEnd;
  private List<String> days;
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private enum RecurrenceDay {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
  }

  /**
   * Create a recurrence pattern from num of occurrence and days when an event happens.
   *
   * @param recurrenceNum the occurrence count of the event before it ends.
   * @param days the days when the event happens.
   */
  public RecurrencePattern(Integer recurrenceNum, List<String> days) {
    validateDaysAndAssignDays(days);
    validateRecurrenceNumAndAssignNum(recurrenceNum);
  }

  /**
   * Create a recurrence pattern from the end date of a recurrence event and days when
   * an event happens.
   *
   * @param dateTimeToEnd the end date of a recurrence event.
   * @param days the days when the event happens.
   */
  public RecurrencePattern(String dateTimeToEnd, List<String> days) {
    validateDaysAndAssignDays(days);
    validateEndDateAndAssignEndDate(dateTimeToEnd);
    System.out.println(this.toString());
  }

  // Validate end-date and assign value
  private void validateEndDateAndAssignEndDate(String dateTimeToEnd) {

    try {
      LocalDate parsed = LocalDate.parse(dateTimeToEnd, formatter);
      this.dateTimeToEnd = parsed;
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("dateTimeToEnd must be in the format of 'yyyy-MM-dd'");
    }
  }

  // Validate recurrenceNum and assign value
  private void validateRecurrenceNumAndAssignNum(int num) {
    if (num <= 0) {
      throw new IllegalArgumentException("Recurrence Num must not be smaller or equals to 0");
    } else {
      this.recurrenceNumToEnd = num;
    }
  }

  // Validate strings inside List days
  private void validateDaysAndAssignDays(List<String> days) {
    for (String day : days) {
      try {
        RecurrenceDay.valueOf(day);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(String.format("Day %s is not a valid day", day));
      } finally {
        this.days = days;
      }
    }
  }

  /**
   * Get the occurrence count of the event before it ends.
   *
   * @return the occurrence count of the event before it ends.
   */
  public Integer getRecurrenceNumToEnd() {
    return recurrenceNumToEnd;
  }

  /**
   * Get the end date of the recurrent event.
   *
   * @return the end date of the recurrent event.
   */
  public String getDateTimeToEnd() {
    if (dateTimeToEnd == null) {
      return null;
    }
    return dateTimeToEnd.format(formatter);
  }

  /**
   * Get the days of when a recurrent event happens.
   *
   * @return a list of the days of when a recurrent event happens.
   */
  public List<String> getDays() {
    return days;
  }

  @Override
  public String toString() {
    return "RecurrencePattern{"
        + "recurrenceNumToEnd=" + recurrenceNumToEnd
        + ", dateTimeToEnd=" + dateTimeToEnd
        + ", days=" + days
        + '}';
  }
}
