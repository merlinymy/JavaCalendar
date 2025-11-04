package edu.northeastern.cs5010.model;

import edu.northeastern.cs5010.util.CheckDateFormat;
import edu.northeastern.cs5010.util.CheckTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an event with details such as subject, dates, times, visibility, description,
 * and location. This class uses the Builder Pattern for construction.
 * An Event must contain a subject, a start date, and an end date as required fields.
 * Additional optional fields include start time, end time, visibility, description,
 * and location.
 */

public class Event {

  // unique identifier
  private final String id;

  // required information
  private String subject;
  private LocalDate startDate;
  private LocalDate endDate;

  // optional info
  private LocalTime startTime;
  private LocalTime endTime;
  private Boolean isPublic;
  private String description;

  private String location;   // TODO: use proper geolocation type

  private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

  private Event(Builder builder) {

    if (builder.startTime == null && builder.endTime != null
        || builder.endTime == null && builder.startTime != null) {
      throw new IllegalArgumentException("You must not have only startTime or only endTime");
    }

    this.id = UUID.randomUUID().toString();
    this.subject = builder.subject;
    this.startDate = builder.startDate;
    this.endDate = builder.endDate;
    this.startTime = builder.startTime;
    this.endTime = builder.endTime;
    this.isPublic = builder.isPublic;
    this.description = builder.description;
    this.location = builder.location;
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

    /**
     * A builder for creating {@link Event} instances.

     * @param subject subject of the event
     * @param startDate start date of the event
     * @param endDate end date of the event
     */
    public Builder(String subject, String startDate, String endDate) {
      if (subject != null && !subject.trim().isEmpty()) {
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

    /**
     * Set the start time of the event.

     * @param startTime the start time of the event.
     * @return A builder of Event class
     */
    public Builder startTime(String startTime) {
      if (CheckTimeFormat.useRegex(startTime)) {
        this.startTime = LocalTime.parse(startTime);
      } else {
        throw new IllegalArgumentException("Start time must be in the format of 'hh:mm:ss'");
      }
      return this;
    }

    /**
     * Set the end time of the event.

     * @param endTime the end time of the event.
     * @return A builder of Event class
     */
    public Builder endTime(String endTime) {
      if (CheckTimeFormat.useRegex(endTime)) {
        this.endTime = LocalTime.parse(endTime);
      } else {
        throw new IllegalArgumentException("End time must be in the format of 'hh:mm:ss'");
      }
      return this;
    }

    /**
     * Set the visibility of the event.

     * @param isPublic the visibility of the event.
     * @return A builder of Event class.
     */
    public Builder isPublic(Boolean isPublic) {
      this.isPublic = isPublic;
      return this;
    }

    /**
     * Set the description of the event.

     * @param description the description of the event.
     * @return A builder of Event class.
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Set the location of the event.

     * @param location the location of the event.
     * @return A builder of Event class.
     */
    public Builder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Create an event instance.

     * @return an {@link Event} event instance.
     */
    public Event build() {
      return new Event(this);
    }
  }

  /**
   * Get the id of an event.

   * @return the id of an event.
   */
  public String getId() {
    return id;
  }

  /**
   * Get the subject of an event.
   *
   * @return the subject of an event.
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Set the subject of an event.

   * @param subject the subject of an event/
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Get the start date of an event.
   *
   * @return the start date of an event.
   */
  public String getStartDate() {
    return startDate.toString();
  }

  /**
   * Set the start date of an event.

   * @param startDate the start date of an event.
   */
  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  /**
   * Get the end date of an event.

   * @return the end date of an event.
   */
  public String getEndDate() {
    return endDate.toString();
  }

  /**
   * Set the end date of an event.

   * @param endDate the end date of an event.
   */
  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  /**
   * Get the start time of an event.
   *
   * @return the start time of an event.
   */
  public String getStartTime() {
    return startTime == null ? null : startTime.format(timeFormatter);
  }

  /**
   * Set the start time of an event.
   *
   * @param startTime the start time of an event.
   */
  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  /**
   * Get the end time of an event.
   *
   * @return the end time of an event.
   */
  public String getEndTime() {
    return endTime == null ? null : endTime.format(timeFormatter);
  }

  /**
   * Set the end time of an event.
   *
   * @param endTime the end time of an event.
   */
  public void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
  }

  /**
   * Get the visibility of an event.

   * @return the visibility of an event.
   */
  public Boolean getPublic() {
    return isPublic;
  }

  /**
   * Set the visibility of an event.

   * @param isPublic visibility of an event.
   */
  public void setPublic(Boolean isPublic) {
    this.isPublic = isPublic;
  }

  /**
   * Get the description of an event.
   *
   * @return the description of an event.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the description of an event.

   * @param description the description of an event.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Get the location of an event.

   * @return the location of an event.
   */
  public String getLocation() {
    return location;
  }

  /**
   * Set the location of an event.

   * @param location the location of an event.
   */
  public void setLocation(String location) {
    this.location = location;
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
    return "Event{"
        + "subject='" + subject + '\''
        + ", startDate=" + startDate
        + ", endDate=" + endDate
        + ", startTime=" + startTime
        + ", endTime=" + endTime
        + ", isPublic=" + isPublic
        + ", description='" + description + '\''
        + ", location='" + location + '\''
        + '}';
  }
}
