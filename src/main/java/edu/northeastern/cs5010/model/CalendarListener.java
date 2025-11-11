package edu.northeastern.cs5010.model;

public interface CalendarListener {
  void onEventAdded(Event event);
  void onEventModified(Event event);
  void onRecurrentEventAdded(RecurrentEvent event);
}
