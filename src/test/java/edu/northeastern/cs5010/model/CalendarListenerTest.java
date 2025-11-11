package edu.northeastern.cs5010.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CalendarListenerTest {

  private Calendar calendar;

  private static class TestListener implements CalendarListener {
    final List<Event> added = new ArrayList<>();
    final List<Event> modified = new ArrayList<>();
    final List<RecurrentEvent> recurrentAdded = new ArrayList<>();

    @Override
    public void onEventAdded(Event event) {
      added.add(event);
    }

    @Override
    public void onEventModified(Event event) {
      modified.add(event);
    }

    @Override
    public void onRecurrentEventAdded(RecurrentEvent event) {
      recurrentAdded.add(event);
    }
  }

  @BeforeEach
  void setUp() {
    calendar = new Calendar("Listener Tests");
  }

  @Test
  void addSingleEventNotifiesAllListeners() {
    TestListener l1 = new TestListener();
    TestListener l2 = new TestListener();
    calendar.addCalendarListener(l1);
    calendar.addCalendarListener(l2);

    Event e = new Event.Builder("Kickoff", "2025-01-10", "2025-01-10")
        .startTime("09:00:00").endTime("10:00:00").build();

    calendar.addEvent(e);

    assertEquals(1, l1.added.size());
    assertEquals(1, l2.added.size());
  }

  @Test
  void editSingleEventNotifiesAllListeners() {
    TestListener l1 = new TestListener();
    TestListener l2 = new TestListener();
    calendar.addCalendarListener(l1);
    calendar.addCalendarListener(l2);

    Event e = new Event.Builder("Design", "2025-02-01", "2025-02-01")
        .startTime("10:00:00").endTime("11:00:00").build();
    calendar.addEvent(e);

    calendar.editEvent(e.getId(), "Design Review", null, null, null, null, null, null, null);

    assertEquals(1, l1.modified.size());
    assertEquals(1, l2.modified.size());
  }

  @Test
  void removedListenerDoesNotReceiveNotifications() {
    TestListener l1 = new TestListener();
    TestListener l2 = new TestListener();
    calendar.addCalendarListener(l1);
    calendar.addCalendarListener(l2);

    Event e1 = new Event.Builder("Standup", "2025-03-01", "2025-03-01")
        .startTime("09:00:00").endTime("09:15:00").build();
    calendar.addEvent(e1);

    calendar.removeCalendarListener(l2);

    Event e2 = new Event.Builder("Planning", "2025-03-02", "2025-03-02")
        .startTime("13:00:00").endTime("14:00:00").build();
    calendar.addEvent(e2);

    assertEquals(2, l1.added.size());
    assertEquals(1, l2.added.size());
  }

  @Test
  void recurrentEventAddNotifiesRecurrentAdded() {
    TestListener l1 = new TestListener();
    TestListener l2 = new TestListener();
    calendar.addCalendarListener(l1);
    calendar.addCalendarListener(l2);

    RecurrencePattern pattern = new RecurrencePattern(2, List.of("MONDAY", "WEDNESDAY"));
    LocalDate startDate = LocalDate.of(2025, 4, 7); // a Monday
    LocalTime start = LocalTime.of(9, 0);
    LocalTime end = LocalTime.of(10, 0);

    RecurrentEvent re = new RecurrentEvent(pattern, startDate, start, end,
        "Yoga", true, "Morning yoga", "Studio");

    calendar.addRecurrentEvent(re);

    assertEquals(1, l1.recurrentAdded.size());
    assertEquals(1, l2.recurrentAdded.size());
  }

  @Test
  void listenersOnlyReceiveNotificationsForTheirCalendar() {
    Calendar calendarA = new Calendar("A");
    Calendar calendarB = new Calendar("B");

    TestListener l1 = new TestListener();
    TestListener l2 = new TestListener();
    calendarA.addCalendarListener(l1);
    calendarB.addCalendarListener(l2);

    Event eA = new Event.Builder("A-Only", "2025-04-01", "2025-04-01")
        .startTime("08:00:00").endTime("09:00:00").build();
    Event eB = new Event.Builder("B-Only", "2025-04-02", "2025-04-02")
        .startTime("10:00:00").endTime("11:00:00").build();

    calendarA.addEvent(eA);
    calendarB.addEvent(eB);

    assertEquals(1, l1.added.size());
    assertEquals(0, l1.modified.size());

    assertEquals(1, l2.added.size());
    assertEquals(0, l2.modified.size());
  }

  @Test
  void addingNullListenerThrows() {
    assertThrows(NullPointerException.class, () -> calendar.addCalendarListener(null));
  }

  @Test
  void duplicateListenerOnlyRegisteredOnce() {
    TestListener listener = new TestListener();
    calendar.addCalendarListener(listener);
    calendar.addCalendarListener(listener);

    Event e = new Event.Builder("All Hands", "2025-06-01", "2025-06-01")
        .startTime("11:00:00").endTime("12:00:00").build();
    calendar.addEvent(e);

    assertEquals(1, listener.added.size());
  }

  @Test
  void removingNullListenerThrows() {
    assertThrows(NullPointerException.class, () -> calendar.removeCalendarListener(null));
  }
}
