package edu.northeastern.cs5010.model;

import edu.northeastern.cs5010.util.CheckDateFormat;
import edu.northeastern.cs5010.util.CheckTimeFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents an event with details such as subject, dates, times, visibility, description,
 * and location. This class uses the Builder Pattern for construction.
 *
 * An Event must contain a subject, a start date, and an end date as required fields.
 * Additional optional fields include start time, end time, visibility, description,
 * and location.
 */

public class Event {

  // required information
  private String subject;
  private LocalDate startDate;
  private LocalDate endDate;

  // optional info
  private LocalTime startTime;
  private LocalTime endTime;
  private Boolean isPublic;
  private String description;
  private Boolean isRecurrent;
  private String location;   // TODO: use proper geolocation type

  // helper set to label an event instance (ex: allDay, )


  private Event(Builder builder) {
    this.subject = builder.subject;
    this.startDate = builder.startDate;
    this.endDate = builder.endDate;
    this.startTime = builder.startTime;
    this.endTime = builder.endTime;
    this.isPublic = builder.isPublic;
    this.description = builder.description;
    this.location = builder.location;
    this.isRecurrent = builder.isRecurrent;
  }

  public Boolean isOverlapping(Event e) {
    return this.startTime.isAfter(e.startTime) && this.startTime.isBefore(e.endTime) ||
        this.endTime.isAfter(e.startTime) && this.endTime.isBefore(e.endTime);
  }

  /**
   * Builder class for constructing Event objects using the Builder Pattern.
   */

  public static class Builder {
    // required information
    private String subject;
    private LocalDate startDate;
    private LocalDate endDate;

    // optional info
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isPublic;
    private String description;
    private String location;
    private Boolean isRecurrent;


    public Builder(String subject, String startDate, String endDate) {
      if (subject != null && !subject.isEmpty()) {
        this.subject = subject;
      } else {
        throw new IllegalArgumentException("Subject must not be null or empty");
      }

      if (CheckDateFormat.useRegex(startDate)) {
        this.startDate = LocalDate.parse(startDate);
      } else {
        throw new IllegalArgumentException("Start date must be in the format of 'yyyy-mm-dd'");
      }
      if (CheckDateFormat.useRegex(endDate)) {
        this.endDate = LocalDate.parse(endDate);
      } else {
        throw new IllegalArgumentException("End date must be in the format of 'yyyy-mm-dd'");
      }
    }
    public Builder startTime(String startTime) {
      if (CheckTimeFormat.useRegex(startTime)){
        this.startTime = LocalTime.parse(startTime);
      } else {
        throw new IllegalArgumentException("Start time must be in the format of 'hh:mm:ss'");
      }
      return this;
    }
    public Builder endTime(String endTime) {
      if (CheckTimeFormat.useRegex(endTime)){
        this.endTime = LocalTime.parse(endTime);
      } else {
        throw new IllegalArgumentException("End time must be in the format of 'hh:mm:ss'");
      }
      return this;
    }
    public Builder isPublic(Boolean isPublic) {
        this.isPublic = isPublic;
      return this;
    }
    public Builder description(String description) {
      this.description = description;
      return this;
    }
    public Builder location (String location) {
      this.location = location;
      return this;
    }
    public Builder isRecurrent(Boolean b) {
      this.isRecurrent = b;
      return this;
    }

    public Event build() {
      return new Event(this);
    }
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
  }

  public Boolean getPublic() {
    return isPublic;
  }

  public void setPublic(Boolean aPublic) {
    isPublic = aPublic;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public Boolean getRecurrent() {
    return isRecurrent;
  }

  public void setRecurrent(Boolean recurrent) {
    isRecurrent = recurrent;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Event event = (Event) o;
    return Objects.equals(subject, event.subject) && Objects.equals(startDate,
        event.startDate) && Objects.equals(endDate, event.endDate)
        && Objects.equals(startTime, event.startTime) && Objects.equals(endTime,
        event.endTime) && Objects.equals(isPublic, event.isPublic)
        && Objects.equals(description, event.description) && Objects.equals(
        location, event.location);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDate, endDate, startTime, endTime, isPublic, description,
        location);
  }

  @Override
  public String toString() {
    return "Event{" +
        "subject='" + subject + '\'' +
        ", startDate=" + startDate +
        ", endDate=" + endDate +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", isPublic=" + isPublic +
        ", description='" + description + '\'' +
        ", location='" + location + '\'' +
        '}';
  }
}
