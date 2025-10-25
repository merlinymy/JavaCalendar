package edu.northeastern.cs5010.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Calendar {

  private String title;
  private final List<Event> eventList = new ArrayList<Event>();

  public Calendar(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public List<Event> getEventList() {
    return eventList;
  }

  public void setTitle(String title) {
    this.title = title;
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
    return "Calendar{" +
        "title='" + title + '\'' +
        ", eventList=" + eventList +
        '}';
  }
}
