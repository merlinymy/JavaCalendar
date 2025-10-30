package edu.northeastern.cs5010.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Non AI generated tests

class RecurrencePatternTest {
  private List<String> days;

  @BeforeEach
  void setup(){
     days = new ArrayList<>();

  }

  @Test
  void testRecurrentOnMondayEndsAfter3Times() {
    days.add("MONDAY");
    RecurrencePattern pattern = new RecurrencePattern(3, days);
    assertEquals(3, pattern.getRecurrenceNumToEnd());
    assertEquals("MONDAY", pattern.getDays().toString().replace("[", "").replace("]", ""));
  }

  @Test
  void testRecurrentWithAValidDateFormatEndDate() {
    days.add("MONDAY");
    String end_datetime = "2025-04-23 11:11:11";
    RecurrencePattern pattern = new RecurrencePattern(end_datetime, days);
    assertEquals("2025-04-23 11:11:11", pattern.getDateTimeToEnd().toString());
  }

  // Conflicts tests


  @Test
  void testRecurrentOnMultiplyDaysEndsAfter10Times() {
    days.addAll(Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY","SATURDAY","SUNDAY"));
    RecurrencePattern pattern = new RecurrencePattern(10, days);
    assertEquals(10, pattern.getRecurrenceNumToEnd());
    assertEquals("MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY", pattern.getDays().toString().replace("[", "").replace("]", ""));
  }

  @Test
  void testRecurrentWithInvalidDays() {
    days.add("not a day");
    assertThrows(IllegalArgumentException.class, ()-> {
      RecurrencePattern pattern = new RecurrencePattern(10, days);
    });
  }

  @Test
  void testRecurrentWithInvalidOccurrence() {
    days.add("MONDAY");
    assertThrows(IllegalArgumentException.class, ()-> {
      RecurrencePattern pattern = new RecurrencePattern(0, days);
    });
  }

  @Test
  void testRecurrentWithInvalidEndDate() {
    String end_date = "2025-10-40 34:22:11";

    days.add("MONDAY");
    assertThrows(IllegalArgumentException.class, ()-> {
      RecurrencePattern pattern = new RecurrencePattern(end_date, days);
    });
  }

}

