package edu.northeastern.cs5010.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Pattern used to create a recurrent event.
 */
public class RecurrencePattern {
  private int recurrenceNumToEnd;
  private LocalDateTime dateTimeToEnd;
  private List<String> days;
  private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


  private enum RecurrenceDay {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
  }

  public RecurrencePattern(int recurrenceNum, List<String> days) {
    validateDaysAndAssignDays(days);
    validateRecurrenceNumAndAssignNum(recurrenceNum);
  }

  public RecurrencePattern(String dateTimeToEnd, List<String> days) {
    validateDaysAndAssignDays(days);
    validateEndDateAndAssignEndDate(dateTimeToEnd);
    System.out.println(this.toString());
  }

  // Validate end-date and assign value
  private void validateEndDateAndAssignEndDate(String dateTimeToEnd) {

    try {
      LocalDateTime parsed = LocalDateTime.parse(dateTimeToEnd, formatter);
      this.dateTimeToEnd = parsed;
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("dateTimeToEnd must be in the format of 'yyyy-MM-DD HH:mm:ss'");
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

  public int getRecurrenceNumToEnd() {
    return recurrenceNumToEnd;
  }

  public void setRecurrenceNumToEnd(int recurrenceNumToEnd) {
    this.recurrenceNumToEnd = recurrenceNumToEnd;
  }

  public String getDateTimeToEnd() {
    return dateTimeToEnd.format(formatter);
  }

  public void setDateTimeToEnd(LocalDateTime dateTimeToEnd) {
    this.dateTimeToEnd = dateTimeToEnd;
  }

  public List<String> getDays() {
    return days;
  }

  public void setDays(List<String> days) {
    this.days = days;
  }

  @Override
  public String toString() {
    return "RecurrencePattern{" +
        "recurrenceNumToEnd=" + recurrenceNumToEnd +
        ", dateTimeToEnd=" + dateTimeToEnd +
        ", days=" + days +
        '}';
  }
}
