package edu.northeastern.cs5010.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Defines a recurrence pattern for creating recurring events.
 * A pattern specifies which days events occur and when the recurrence ends, either after
 * a certain number of occurrences or on a specific date.
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
   * Creates a recurrence pattern that ends after a specified number of occurrences.
   *
   * @param recurrenceNum the number of occurrences before the recurrence ends.
   * @param days the days of the week when events occur (e.g., "MONDAY", "FRIDAY").
   */
  public RecurrencePattern(Integer recurrenceNum, List<String> days) {
    validateDaysAndAssignDays(days);
    validateRecurrenceNumAndAssignNum(recurrenceNum);
  }

  /**
   * Creates a recurrence pattern that ends on a specified date.
   *
   * @param dateTimeToEnd the end date in "yyyy-MM-dd" format.
   * @param days the days of the week when events occur (e.g., "MONDAY", "FRIDAY").
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
   * Gets the number of occurrences before the recurrence ends.
   *
   * @return the occurrence count, or null if an end date is specified instead.
   */
  public Integer getRecurrenceNumToEnd() {
    return recurrenceNumToEnd;
  }

  /**
   * Gets the end date of the recurrence.
   *
   * @return the end date in "yyyy-MM-dd" format, or null if an occurrence count is specified instead.
   */
  public String getDateTimeToEnd() {
    if (dateTimeToEnd == null) {
      return null;
    }
    return dateTimeToEnd.format(formatter);
  }

  /**
   * Gets the days of the week when events occur.
   *
   * @return a list of day names (e.g., "MONDAY", "FRIDAY").
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
