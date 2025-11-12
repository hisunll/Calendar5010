package com.calendar.calendar5010;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * RecurringEvent represents events that repeat on specific days of the week.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
public class RecurringEvent extends Event {
  @Getter(AccessLevel.NONE)
  private Set<DayOfWeek> recurrenceDays;
  private Integer repeatCount;
  private LocalDate recurrenceEndDate;
  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private List<Event> events = new ArrayList<>();

  private RecurringEvent(Builder builder) {
    super(builder);
    this.recurrenceDays = builder.recurrenceDays;
    this.repeatCount = builder.repeatCount;
    this.recurrenceEndDate = builder.recurrenceEndDate;
  }


  /**
   * Returns an unmodifiable set of DayOfWeek objects representing days to recur.
   *
   * @return an unmodifiable set of time DayOfWeek
   */
  public Set<DayOfWeek> getRecurrenceDays() {
    return Collections.unmodifiableSet(recurrenceDays);
  }

  @Override
  protected void postBuild() {
    ValidationResult validationResult = checkIsValid();
    if (!validationResult.getValid()) {
      throw new IllegalArgumentException(validationResult.getMessage());
    }
    super.postBuild();
    events = new ArrayList<>();
    generateSingleEvents();
    setTimeIntervals();
  }

  /**
   * Builder for RecurringEvent supporting days of week, repeat count, and end date.
   */
  public static class Builder extends Event.Builder<Builder> {
    private Set<DayOfWeek> recurrenceDays;
    private Integer repeatCount;
    private LocalDate recurrenceEndDate;

    /**
     * Base constructor for subclasses of {@code Builder}.
     */
    protected Builder() {
    }

    /**
     * Sets the days of week on which the event recurs (required).
     *
     * @param recurrenceDays set of days of week
     * @return this builder
     */
    public Builder recurrenceDays(Set<DayOfWeek> recurrenceDays) {
      this.recurrenceDays = recurrenceDays;
      return this;
    }

    /**
     * Sets the maximum number of occurrences (optional). At least one of
     * {@link #repeatCount(Integer)} or {@link #recurrenceEndDate(LocalDate)} must be provided.
     *
     * @param repeatCount upper bound of occurrences
     * @return this builder
     */
    public Builder repeatCount(Integer repeatCount) {
      this.repeatCount = repeatCount;
      return this;
    }

    /**
     * Sets the recurrence end date (optional). At least one of
     * {@link #repeatCount(Integer)} or {@link #recurrenceEndDate(LocalDate)} must be provided.
     *
     * @param recurrenceEndDate end date of recurrence
     * @return this builder
     */
    public Builder recurrenceEndDate(LocalDate recurrenceEndDate) {
      this.recurrenceEndDate = recurrenceEndDate;
      return this;
    }

    @Override
    protected Builder self() {
      return this;
    }

    @Override
    public RecurringEvent build() {
      RecurringEvent event = new RecurringEvent(this);
      event.postBuild();
      return event;
    }
  }

  /**
   * Create a new builder instance.
   *
   * @return new RecurringEvent builder
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Builder toBuilder() {
    return RecurringEvent.builder()
      .id(this.getId())
      .subject(this.getSubject())
      .startDate(this.getStartDate())
      .startTime(this.getStartTime())
      .endDate(this.getEndDate())
      .endTime(this.getEndTime())
      .description(this.getDescription())
      .location(this.getLocation())
      .allowConflict(this.getAllowConflict())
      .visibility(this.getVisibility())
      .recurrenceDays(this.recurrenceDays != null ? new HashSet<>(this.recurrenceDays) : null)
      .repeatCount(this.repeatCount)
      .recurrenceEndDate(this.recurrenceEndDate);
  }

  /**
   * Generate child single events according to recurrence rules.
   */
  protected void generateSingleEvents() {
    this.events.clear();
    LocalDate current = getStartDate();
    int count = 0;

    LocalDate endLimit = recurrenceEndDate != null
        ? recurrenceEndDate
        : getEndDate().plusWeeks(repeatCount);

    while (!current.isAfter(endLimit)) {
      if (recurrenceDays.contains(current.getDayOfWeek())) {
        if (repeatCount != null && count >= repeatCount) {
          break;
        }

        SingleEvent singleEvent = SingleEvent.builder()
            .subject(getSubject())
            .startDate(current)
            .startTime(getStartTime())
            .endDate(current)
            .endTime(getEndTime())
            .visibility(getVisibility())
            .description(getDescription())
            .location(getLocation())
            .allowConflict(getAllowConflict())
            .belongsToRecurringEvent(true)
            .fatherId(getId())
            .build();

        this.events.add(singleEvent);
        count++;
      }

      recurrenceEndDate = current;
      current = current.plusDays(1);
    }

  }

  @Override
  protected void setTimeIntervals() {
    this.timeIntervals.clear();

    for (Event event : events) {
      this.timeIntervals.addAll(event.timeIntervals);
    }
  }

  @Override
  public ValidationResult checkIsValid() {
    super.checkIsValid();

    if (recurrenceDays == null) {
      return ValidationResult.error("Missing required parameters.");
    }

    if (repeatCount == null && recurrenceEndDate == null) {
      return ValidationResult.error("Missing required parameters.");
    }

    LocalDate startDate = getStartDate();
    LocalDate endDate = getEndDate();

    if (startDate.isBefore(endDate)) {
      return ValidationResult.error("Recurring event cannot cross day");
    }

    return ValidationResult.valid();
  }

  @Override
  public RecurringEvent deepCopy() {
    RecurringEvent copyEvent = this.toBuilder().build();
    copyEvent.events.clear();
    for (Event event : this.events) {
      copyEvent.events.add(event.deepCopy());
    }
    return copyEvent;
  }

  @Override
  public void prepareForUpdate() {
    events.removeIf(e ->
        e.getStartDate().isBefore(getStartDate())
    );
    setTimeIntervals();
  }

  @Override
  public List<Event> getListEvents() {
    return Collections.unmodifiableList(events);
  }

  @Override
  public void copyFrom(Event e, LocalDate startDateFilter) {
    RecurringEvent event = (RecurringEvent) e;
    this.setSubject(event.getSubject());
    if (startDateFilter == null) {
      this.setStartDate(event.getStartDate());
      this.setStartTime(event.getStartTime());
      startDateFilter = event.getStartDate();
    }
    this.setEndDate(event.getEndDate());
    this.setEndTime(event.getEndTime());
    this.setDescription(event.getDescription());
    this.setLocation(event.getLocation());
    this.setAllowConflict(event.getAllowConflict());
    this.setVisibility(event.getVisibility());
    this.setRecurrenceEndDate(event.getRecurrenceEndDate());
    this.setRecurrenceDays(event.getRecurrenceDays());
    this.setRepeatCount(event.getRepeatCount());
    LocalDate finalStartDateFilter = startDateFilter;
    events.removeIf(ee ->
        !ee.getStartDate().isBefore(finalStartDateFilter)
    );
    events.addAll(event.getListEvents());
    this.setTimeIntervals();
  }
}