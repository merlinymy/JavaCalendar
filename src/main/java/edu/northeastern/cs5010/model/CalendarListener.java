package edu.northeastern.cs5010.model;

/**
 * Observer that receives notifications about changes performed on a {@link Calendar}.
 * Implementations typically update a view or synchronize external state.
 */
public interface CalendarListener {

  /**
   * Called after a new {@link Event} has been persisted in the calendar.
   *
   * @param event the event that was added.
   */
  void onEventAdded(Event event);

  /**
   * Called after an existing {@link Event} has been modified.
   *
   * @param event the event reflecting the new state.
   */
  void onEventModified(Event event);

  /**
   * Called after a {@link RecurrentEvent} has been added to the calendar.
   *
   * @param event the series that was added.
   */
  void onRecurrentEventAdded(RecurrentEvent event);
}
