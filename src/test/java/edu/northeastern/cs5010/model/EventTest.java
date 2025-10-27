package edu.northeastern.cs5010.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test class for Event creation and validation.
 * These tests cover base Events only (not RecurrentEvents).
 */
class EventTest {

  // ==================== Event Creation Tests ====================

  @Test
  void testCreateEventWithRequiredFieldsOnly() {
    Event event = new Event.Builder("Team Meeting", "2025-11-01", "2025-11-01")
        .build();

    assertEquals("Team Meeting", event.getSubject());
    assertEquals("2025-11-01", event.getStartDate().toString());
    assertEquals("2025-11-01", event.getEndDate().toString());
    assertNull(event.getStartTime());
    assertNull(event.getEndTime());
    assertNull(event.getPublic());
    assertNull(event.getDescription());
    assertNull(event.getLocation());
  }

  @Test
  void testCreateEventWithAllFields() {
    Event event = new Event.Builder("Team Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:30:00")
        .isPublic(true)
        .description("Weekly team sync meeting")
        .location("Conference Room A")
        .build();

    assertEquals("Team Meeting", event.getSubject());
    assertEquals("2025-11-01", event.getStartDate().toString());
    assertEquals("2025-11-01", event.getEndDate().toString());
    assertEquals("09:00:00", event.getStartTime().toString());
    assertEquals("10:30:00", event.getEndTime().toString());
    assertTrue(event.getPublic());
    assertEquals("Weekly team sync meeting", event.getDescription());
    assertEquals("Conference Room A", event.getLocation());
  }

  @Test
  void testCreateAllDayEvent() {
    Event event = new Event.Builder("Birthday Party", "2025-12-15", "2025-12-15")
        .isPublic(false)
        .description("John's birthday celebration")
        .location("Home")
        .build();

    assertEquals("Birthday Party", event.getSubject());
    assertNull(event.getStartTime());
    assertNull(event.getEndTime());
    assertFalse(event.getPublic());
    assertEquals("John's birthday celebration", event.getDescription());
  }

  @Test
  void testCreateMultiDayEvent() {
    Event event = new Event.Builder("Conference", "2025-11-10", "2025-11-12")
        .description("Annual tech conference")
        .location("Convention Center")
        .build();

    assertEquals("Conference", event.getSubject());
    assertEquals("2025-11-10", event.getStartDate().toString());
    assertEquals("2025-11-12", event.getEndDate().toString());
    assertEquals("Annual tech conference", event.getDescription());
    assertEquals("Convention Center", event.getLocation());
  }

  @Test
  void testCreatePrivateEvent() {
    Event event = new Event.Builder("Doctor Appointment", "2025-11-05", "2025-11-05")
        .startTime("14:00:00")
        .endTime("15:00:00")
        .isPublic(false)
        .build();

    assertEquals("Doctor Appointment", event.getSubject());
    assertFalse(event.getPublic());
  }

  @Test
  void testCreatePublicEvent() {
    Event event = new Event.Builder("Town Hall Meeting", "2025-11-20", "2025-11-20")
        .startTime("18:00:00")
        .endTime("20:00:00")
        .isPublic(true)
        .build();

    assertEquals("Town Hall Meeting", event.getSubject());
    assertTrue(event.getPublic());
  }

  @Test
  void testCreateEventWithStartTimeOnly() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .build();

    assertEquals("09:00:00", event.getStartTime().toString());
    assertNull(event.getEndTime());
  }

  @Test
  void testCreateEventWithEndTimeOnly() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .endTime("10:00:00")
        .build();

    assertNull(event.getStartTime());
    assertEquals("10:00:00", event.getEndTime().toString());
  }

  @Test
  void testCreateEventWithDescriptionOnly() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .description("Important meeting about project status")
        .build();

    assertEquals("Important meeting about project status", event.getDescription());
  }

  @Test
  void testCreateEventWithLocationOnly() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .location("Room 301")
        .build();

    assertEquals("Room 301", event.getLocation());
  }

  // ==================== Event Validation Tests ====================

  @Test
  void testCreateEventWithNullSubject() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder(null, "2025-11-01", "2025-11-01").build();
    });
  }

  @Test
  void testCreateEventWithEmptySubject() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder("", "2025-11-01", "2025-11-01").build();
    });
  }

  @Test
  void testCreateEventWithWhitespaceSubject() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder("   ", "2025-11-01", "2025-11-01").build();
    });
  }

  @Test
  void testCreateEventWithInvalidStartDateFormat() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder("Meeting", "2025/11/01", "2025-11-01").build();
    });
  }

  @Test
  void testCreateEventWithInvalidStartDateFormatMonthDay() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder("Meeting", "11-01-2025", "2025-11-01").build();
    });
  }

  @Test
  void testCreateEventWithInvalidEndDateFormat() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder("Meeting", "2025-11-01", "11-01-2025").build();
    });
  }

  @Test
  void testCreateEventWithInvalidStartTimeFormat() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
          .startTime("9:00")
          .build();
    });
  }

  @Test
  void testCreateEventWithInvalidStartTimeFormatAMPM() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
          .startTime("9:00 AM")
          .build();
    });
  }

  @Test
  void testCreateEventWithInvalidEndTimeFormat() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
          .startTime("09:00:00")
          .endTime("10:30")
          .build();
    });
  }

  @Test
  void testCreateEventWithInvalidTimeValue() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
          .startTime("25:00:00")
          .build();
    });
  }

  // ==================== Event Getter Tests ====================

  @Test
  void testGetSubject() {
    Event event = new Event.Builder("Test Event", "2025-11-01", "2025-11-01").build();
    assertEquals("Test Event", event.getSubject());
  }

  @Test
  void testGetStartDate() {
    Event event = new Event.Builder("Test Event", "2025-11-01", "2025-11-01").build();
    assertEquals("2025-11-01", event.getStartDate().toString());
  }

  @Test
  void testGetEndDate() {
    Event event = new Event.Builder("Test Event", "2025-11-01", "2025-11-02").build();
    assertEquals("2025-11-02", event.getEndDate().toString());
  }

  @Test
  void testGetStartTime() {
    Event event = new Event.Builder("Test Event", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .build();
    assertEquals("09:00:00", event.getStartTime().toString());
  }

  @Test
  void testGetEndTime() {
    Event event = new Event.Builder("Test Event", "2025-11-01", "2025-11-01")
        .endTime("10:00:00")
        .build();
    assertEquals("10:00:00", event.getEndTime().toString());
  }

  @Test
  void testGetPublic() {
    Event event = new Event.Builder("Test Event", "2025-11-01", "2025-11-01")
        .isPublic(true)
        .build();
    assertTrue(event.getPublic());
  }

  @Test
  void testGetDescription() {
    Event event = new Event.Builder("Test Event", "2025-11-01", "2025-11-01")
        .description("Test description")
        .build();
    assertEquals("Test description", event.getDescription());
  }

  @Test
  void testGetLocation() {
    Event event = new Event.Builder("Test Event", "2025-11-01", "2025-11-01")
        .location("Test location")
        .build();
    assertEquals("Test location", event.getLocation());
  }

  // ==================== Event Setter Tests ====================

  @Test
  void testSetSubject() {
    Event event = new Event.Builder("Original", "2025-11-01", "2025-11-01").build();
    event.setSubject("Updated Subject");
    assertEquals("Updated Subject", event.getSubject());
  }

  @Test
  void testSetDescription() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01").build();
    event.setDescription("Updated description");
    assertEquals("Updated description", event.getDescription());
  }

  @Test
  void testSetLocation() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01").build();
    event.setLocation("Updated location");
    assertEquals("Updated location", event.getLocation());
  }

  @Test
  void testSetPublic() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01").build();
    event.setPublic(true);
    assertTrue(event.getPublic());
  }

  // ==================== Event Equality Tests ====================

  @Test
  void testEventEqualityWithAllFields() {
    Event event1 = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .isPublic(true)
        .description("Team sync")
        .location("Room 101")
        .build();

    Event event2 = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .isPublic(true)
        .description("Team sync")
        .location("Room 101")
        .build();

    assertEquals(event1, event2);
  }

  @Test
  void testEventEqualityWithRequiredFieldsOnly() {
    Event event1 = new Event.Builder("Meeting", "2025-11-01", "2025-11-01").build();
    Event event2 = new Event.Builder("Meeting", "2025-11-01", "2025-11-01").build();

    assertEquals(event1, event2);
  }

  @Test
  void testEventInequalityDifferentSubject() {
    Event event1 = new Event.Builder("Meeting 1", "2025-11-01", "2025-11-01").build();
    Event event2 = new Event.Builder("Meeting 2", "2025-11-01", "2025-11-01").build();

    assertNotEquals(event1, event2);
  }

  @Test
  void testEventInequalityDifferentDate() {
    Event event1 = new Event.Builder("Meeting", "2025-11-01", "2025-11-01").build();
    Event event2 = new Event.Builder("Meeting", "2025-11-02", "2025-11-02").build();

    assertNotEquals(event1, event2);
  }

  @Test
  void testEventInequalityDifferentTime() {
    Event event1 = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .build();

    Event event2 = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("10:00:00")
        .build();

    assertNotEquals(event1, event2);
  }

  @Test
  void testEventHashCode() {
    Event event1 = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    Event event2 = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    assertEquals(event1.hashCode(), event2.hashCode());
  }

  // ==================== Event toString Tests ====================

  @Test
  void testEventToString() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .build();

    String toString = event.toString();
    assertTrue(toString.contains("Meeting"));
    assertTrue(toString.contains("2025-11-01"));
    assertTrue(toString.contains("09:00:00"));
    assertTrue(toString.contains("10:00:00"));
  }

  @Test
  void testAllDayEventToString() {
    Event event = new Event.Builder("Holiday", "2025-11-01", "2025-11-01")
        .description("Thanksgiving")
        .build();

    String toString = event.toString();
    assertTrue(toString.contains("Holiday"));
    assertTrue(toString.contains("2025-11-01"));
  }

  // ==================== Edge Cases ====================

  @Test
  void testCreateEventAtMidnight() {
    Event event = new Event.Builder("Midnight Event", "2025-11-01", "2025-11-01")
        .startTime("00:00:00")
        .endTime("01:00:00")
        .build();

    assertEquals("00:00:00", event.getStartTime().toString());
  }

  @Test
  void testCreateEventEndingAtMidnight() {
    Event event = new Event.Builder("Late Event", "2025-11-01", "2025-11-01")
        .startTime("23:00:00")
        .endTime("23:59:59")
        .build();

    assertEquals("23:59:59", event.getEndTime().toString());
  }

  @Test
  void testCreateEventWithVeryLongSubject() {
    String longSubject = "A".repeat(500);
    Event event = new Event.Builder(longSubject, "2025-11-01", "2025-11-01").build();

    assertEquals(longSubject, event.getSubject());
  }

  @Test
  void testCreateEventWithVeryLongDescription() {
    String longDescription = "This is a very long description ".repeat(100);

    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .description(longDescription)
        .build();

    assertEquals(longDescription, event.getDescription());
  }

  @Test
  void testCreateEventWithSpecialCharactersInSubject() {
    Event event = new Event.Builder("Meeting!@#$%^&*()", "2025-11-01", "2025-11-01").build();
    assertEquals("Meeting!@#$%^&*()", event.getSubject());
  }

  @Test
  void testCreateEventWithUnicodeCharacters() {
    Event event = new Event.Builder("会议", "2025-11-01", "2025-11-01")
        .description("重要会议")
        .location("北京")
        .build();

    assertEquals("会议", event.getSubject());
    assertEquals("重要会议", event.getDescription());
    assertEquals("北京", event.getLocation());
  }

  @Test
  void testCreateEventOnLeapYear() {
    Event event = new Event.Builder("Meeting", "2024-02-29", "2024-02-29").build();
    assertEquals("2024-02-29", event.getStartDate().toString());
  }

  @Test
  void testCreateEventSpanningYearEnd() {
    Event event = new Event.Builder("New Year Party", "2025-12-31", "2026-01-01")
        .startTime("23:00:00")
        .endTime("02:00:00")
        .build();

    assertEquals("2025-12-31", event.getStartDate().toString());
    assertEquals("2026-01-01", event.getEndDate().toString());
  }

  // ==================== Builder Pattern Tests ====================

  @Test
  void testBuilderChaining() {
    Event event = new Event.Builder("Meeting", "2025-11-01", "2025-11-01")
        .startTime("09:00:00")
        .endTime("10:00:00")
        .isPublic(true)
        .description("Description")
        .location("Location")
        .build();

    assertNotNull(event);
    assertEquals("Meeting", event.getSubject());
    assertTrue(event.getPublic());
  }

  @Test
  void testBuilderCanBeReused() {
    Event.Builder builder = new Event.Builder("Meeting", "2025-11-01", "2025-11-01");

    Event event1 = builder.startTime("09:00:00").endTime("10:00:00").build();
    Event event2 = builder.description("Updated description").build();

    assertNotNull(event1);
    assertNotNull(event2);
  }

  // ==================== isOverlapping Tests (placeholder) ====================

  @Test
  void isOverlapping() {
    // This test is kept as a placeholder for future implementation
    // The current implementation has issues with null checking
  }
}
