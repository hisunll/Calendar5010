package com.calendar.calendar5010.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Event is an abstract base for single and recurring events.
 *
 * <p>Subclasses must implement event expansion ({@link #getListEvents()}),
 * time-interval construction ({@link #setTimeIntervals()}),
 * deep copy ({@link #deepCopy()}), and selective copy for recurring instances
 * ({@link #copyFrom(Event, LocalDate)}).
 *
 * @author Liangliang Sun
 */
@Getter
@Setter
public abstract class Event {
  @Setter(AccessLevel.NONE)
  private String id;

  private String subject;
  private LocalDate startDate;

  private LocalTime startTime;
  private LocalDate endDate;
  private LocalTime endTime;
  private String description;
  private String location;

  @Getter(AccessLevel.NONE)
  private Boolean allowConflict;
  private Visibility visibility;

  /**
   * Internal list of {@link TimeInterval} objects associated with this event.
   */
  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  protected final List<TimeInterval> timeIntervals = new ArrayList<>();

  /**
   * Returns an unmodifiable list of {@link TimeInterval} objects associated with this event.
   *
   * @return an unmodifiable list of time intervals
   */
  public List<TimeInterval> getTimeIntervals() {
    return Collections.unmodifiableList(timeIntervals);
  }

  /**
   * Protected constructor used by {@link Builder} during instantiation.
   * Copies values from the builder into the event instance and applies sensible defaults
   * for boolean and visibility fields.
   *
   * @param builder builder carrying initial values for event fields
   */
  protected Event(Builder<?> builder) {
    this.subject = builder.subject;
    this.startDate = builder.startDate;
    this.startTime = builder.startTime;
    this.endDate = builder.endDate;
    this.endTime = builder.endTime;
    this.description = builder.description;
    this.location = builder.location;
    this.allowConflict = builder.allowConflict;
    this.visibility = builder.visibility != null ? builder.visibility : Visibility.PUBLIC;
    if (this.visibility == null) {
      this.visibility = Visibility.PUBLIC;
    }

    if (builder.id != null) {
      this.id = builder.id;
    }
  }

  /**
   * Post-construction finalization.
   */
  protected void postBuild() {
    if (this.visibility == null) {
      this.visibility = Visibility.PUBLIC;
    }
    if (endTime == null) {
      endTime = LocalTime.MAX;
    }
    if (startTime == null) {
      startTime = LocalTime.MIN;
    }
    if (this.id == null) {
      this.id = UUID.randomUUID().toString();
    }
  }

  /**
   * Returns whether this event allows scheduling conflicts with other events.
   *
   * @return {@code true} if conflicts are allowed; {@code false} otherwise
   */
  public Boolean getAllowConflict() {
    if (this.allowConflict == null) {
      return false;
    }
    return allowConflict;
  }

  /**
   * Checks whether the {@code allowConflict} flag has not been set.
   *
   * @return {@code true} if {@code allowConflict} is {@code null}; {@code false} otherwise
   */
  public boolean checkAllowConflictNull() {
    return this.allowConflict == null;
  }

  /**
   * Event builder providing a fluent API to configure fields and create instances.
   * Subclasses must implement {@link #self()} and {@link #build()}.
   *
   * @param <T> the builder self type to support fluent covariant returns
   */
  public abstract static class Builder<T extends Builder<T>> {
    private String subject;
    private LocalDate startDate;
    private LocalTime startTime;
    private LocalDate endDate;
    private LocalTime endTime;
    private String description;
    private String location;
    private Boolean allowConflict;
    private Visibility visibility;
    private String id;

    /**
     * Base constructor for subclasses of {@code Builder}.
     */
    protected Builder() {
    }

    /**
     * Sets the event identifier.
     *
     * @param id unique identifier for the event
     * @return this builder
     */
    public T id(String id) {
      this.id = id;
      return self();
    }

    /**
     * Sets the event subject (required).
     *
     * @param subject event subject/title
     * @return this builder
     */
    public T subject(String subject) {
      this.subject = subject;
      return self();
    }

    /**
     * Sets the start date (required).
     *
     * @param startDate start date
     * @return this builder
     */
    public T startDate(LocalDate startDate) {
      this.startDate = startDate;
      return self();
    }

    /**
     * Sets the start time (optional).
     *
     * @param startTime start time
     * @return this builder
     */
    public T startTime(LocalTime startTime) {
      this.startTime = startTime;
      return self();
    }

    /**
     * Sets the end date (required).
     *
     * @param endDate end date
     * @return this builder
     */
    public T endDate(LocalDate endDate) {
      this.endDate = endDate;
      return self();
    }

    /**
     * Sets the end time (optional).
     *
     * @param endTime end time
     * @return this builder
     */
    public T endTime(LocalTime endTime) {
      this.endTime = endTime;
      return self();
    }

    /**
     * Sets the event description (optional).
     *
     * @param description description text
     * @return this builder
     */
    public T description(String description) {
      this.description = description;
      return self();
    }

    /**
     * Sets the event location (optional).
     *
     * @param location place name or address
     * @return this builder
     */
    public T location(String location) {
      this.location = location;
      return self();
    }

    /**
     * Sets whether time conflicts with other events are allowed (optional).
     *
     * @param allowConflict whether conflicts are allowed
     * @return this builder
     */
    public T allowConflict(Boolean allowConflict) {
      this.allowConflict = allowConflict;
      return self();
    }

    /**
     * Sets the event visibility (optional).
     *
     * @param visibility visibility enum
     * @return this builder
     */
    public T visibility(Visibility visibility) {
      this.visibility = visibility;
      return self();
    }

    /**
     * Returns the builder itself, supporting fluent generic chaining.
     *
     * @return this builder
     */
    protected abstract T self();

    /**
     * Builds an event instance.
     *
     * @return the constructed event instance
     */
    public abstract Event build();
  }

  /**
   * Event visibility.
   */
  public enum Visibility {
    /** Event is visible to everyone. */
    PUBLIC,
    /** Event is visible only to the owner or selected viewers. */
    PRIVATE
  }

  /**
   * Validate basic event properties.
   *
   * @return validation result
   */
  public ValidationResult checkIsValid() {
    LocalDate startDate = getStartDate();
    LocalTime startTime = getStartTime();
    LocalDate endDate = getEndDate();
    LocalTime endTime = getEndTime();
    String subject = getSubject();

    if (startTime == null && endTime != null) {
      return ValidationResult.error("Cannot set endTime if startTime is null");
    }

    if (subject == null || startDate == null || endDate == null) {
      return ValidationResult.error("Missing required parameters.");
    }

    if (getEndDateTime().isBefore(getStartDateTime())) {
      return ValidationResult.error("End time cannot be before start time");
    }

    return ValidationResult.valid();
  }

  /**
   * Compute the event start date-time.
   *
   * @return the event start date-time
   */
  public LocalDateTime getStartDateTime() {
    if (startTime != null) {
      return startDate.atTime(startTime);
    } else  {
      return startDate.atStartOfDay();
    }
  }

  /**
   * Compute the event end date-time.
   *
   * @return the event end date-time
   */
  public LocalDateTime getEndDateTime() {
    if (endTime != null) {
      return endDate.atTime(endTime);
    } else  {
      return startDate.atTime(LocalTime.MAX);
    }
  }

  /**
   * Create a deep copy of this event.
   *
   * @return deep-copied event instance
   */
  public abstract Event deepCopy();

  /**
   * Prepare the event for update.
   */
  public abstract void prepareForUpdate();

  /**
   * Initializes or updates the list of {@link TimeInterval} objects associated with this event.
   */
  protected abstract void setTimeIntervals();

  /**
   * Convert the current event into a modifiable builder for copying and adjustments.
   *
   * @return builder pre-populated with this event's fields
   */
  public abstract Builder toBuilder();

  /**
   * Returns a list of all {@link Event} instances represented by this event.
   *
   * @return a list of {@link Event} objects associated with this event
   */
  public abstract List<Event> getListEvents();

  /**
   * Copy common fields from another event into this instance.
   *
   * @param event source event
   * @param startDate date to change
   */
  public abstract void copyFrom(Event event, LocalDate startDate);

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Event event)) {
      return false;
    }

    return id.equals(event.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
