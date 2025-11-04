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
     * Creates a new Builder for constructing {@link Event} instances.
     *
     * @param subject the subject of the event.
     * @param startDate the start date in "yyyy-MM-dd" format.
     * @param endDate the end date in "yyyy-MM-dd" format.
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
     * Sets the start time of the event.
     *
     * @param startTime the start time in "HH:mm:ss" format.
     * @return this Builder instance for method chaining.
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
     * Sets the end time of the event.
     *
     * @param endTime the end time in "HH:mm:ss" format.
     * @return this Builder instance for method chaining.
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
     * Sets the visibility of the event.
     *
     * @param isPublic true if the event is public, false if private.
     * @return this Builder instance for method chaining.
     */
    public Builder isPublic(Boolean isPublic) {
      this.isPublic = isPublic;
      return this;
    }

    /**
     * Sets the description of the event.
     *
     * @param description the description text.
     * @return this Builder instance for method chaining.
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the location of the event.
     *
     * @param location the location text.
     * @return this Builder instance for method chaining.
     */
    public Builder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Builds and returns a new Event instance.
     *
     * @return the constructed {@link Event}.
     */
    public Event build() {
      return new Event(this);
    }
  }

  /**
   * Gets the unique ID of the event.
   *
   * @return the event's unique ID.
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the subject of the event.
   *
   * @return the event's subject.
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Sets the subject of the event.
   *
   * @param subject the new subject.
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Gets the start date of the event.
   *
   * @return the start date in "yyyy-MM-dd" format.
   */
  public String getStartDate() {
    return startDate.toString();
  }

  /**
   * Sets the start date of the event.
   *
   * @param startDate the new start date.
   */
  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  /**
   * Gets the end date of the event.
   *
   * @return the end date in "yyyy-MM-dd" format.
   */
  public String getEndDate() {
    return endDate.toString();
  }

  /**
   * Sets the end date of the event.
   *
   * @param endDate the new end date.
   */
  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  /**
   * Gets the start time of the event.
   *
   * @return the start time in "HH:mm:ss" format, or null if not set.
   */
  public String getStartTime() {
    return startTime == null ? null : startTime.format(timeFormatter);
  }

  /**
   * Sets the start time of the event.
   *
   * @param startTime the new start time.
   */
  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  /**
   * Gets the end time of the event.
   *
   * @return the end time in "HH:mm:ss" format, or null if not set.
   */
  public String getEndTime() {
    return endTime == null ? null : endTime.format(timeFormatter);
  }

  /**
   * Sets the end time of the event.
   *
   * @param endTime the new end time.
   */
  public void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
  }

  /**
   * Gets the visibility of the event.
   *
   * @return true if the event is public, false if private, or null if not set.
   */
  public Boolean getPublic() {
    return isPublic;
  }

  /**
   * Sets the visibility of the event.
   *
   * @param isPublic true if the event is public, false if private.
   */
  public void setPublic(Boolean isPublic) {
    this.isPublic = isPublic;
  }

  /**
   * Gets the description of the event.
   *
   * @return the event's description, or null if not set.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description of the event.
   *
   * @param description the new description.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets the location of the event.
   *
   * @return the event's location, or null if not set.
   */
  public String getLocation() {
    return location;
  }

  /**
   * Sets the location of the event.
   *
   * @param location the new location.
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
