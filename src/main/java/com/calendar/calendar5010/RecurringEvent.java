package com.calendar.calendar5010;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class RecurringEvent extends Event{
  //required
  private Set<DayOfWeek> recurrenceDays;

  //optional
  private Integer repeatCount;
  private LocalDate recurrenceEndDate;
  @Setter(AccessLevel.NONE)
  private List<Event> events;

  private RecurringEvent(Builder builder) {
    super(builder);
    this.recurrenceDays = builder.recurrenceDays;
    this.repeatCount = builder.repeatCount;
    this.recurrenceEndDate = builder.recurrenceEndDate;
    postBuild();
  }

  @Override
  protected void postBuild() {
    ValidationResult validationResult = checkIsValid();
    if(!validationResult.getValid()) {
      throw new IllegalArgumentException(validationResult.getMessage());
    }
    super.postBuild();
    events = new ArrayList<>();
    generateSingleEvents();
    setTimeIntervals();
  }

  public static class Builder extends Event.Builder<Builder> {
    private Set<DayOfWeek> recurrenceDays;
    private Integer repeatCount;
    private LocalDate recurrenceEndDate;

    public Builder recurrenceDays(Set<DayOfWeek> recurrenceDays) {
      this.recurrenceDays = recurrenceDays;
      return this;
    }

    public Builder repeatCount(Integer repeatCount) {
      this.repeatCount = repeatCount;
      return this;
    }

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
      return new RecurringEvent(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

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

  private void generateSingleEvents() {
    this.events.clear();
    LocalDate current = getStartDate();
    int count = 0;

    LocalDate endLimit = recurrenceEndDate != null ? recurrenceEndDate : getEndDate().plusWeeks(repeatCount);

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

      current = current.plusDays(1);
    }

  }

  @Override
  protected void setTimeIntervals() {
    getTimeIntervals().clear();

    for(Event event : events) {
      getTimeIntervals().addAll(event.getTimeIntervals());
    }
  }

  @Override
  public ValidationResult checkIsValid(){
    LocalDate startDate = getStartDate();
    LocalDate endDate = getEndDate();
    super.checkIsValid();

    if (recurrenceDays == null) {
      return ValidationResult.error("Missing required parameters.");
    }

    if (repeatCount == null && recurrenceEndDate == null) {
      return ValidationResult.error("Missing required parameters.");
    }

    if (repeatCount != null && recurrenceEndDate != null) {
      return ValidationResult.error("Cannot set both repeatCount and recurrenceEndDate at the same time");
    }

    if(startDate.isBefore(endDate)){
      return ValidationResult.error("Recurring event cannot cross day");
    }

    return ValidationResult.valid();
  }

  @Override
  public RecurringEvent deepCopy() {
    return this.toBuilder().build();
  }

  @Override
  public void prepareForUpdate() {
    generateSingleEvents();
    setTimeIntervals();
  }

  @Override
  public List<Event> getListEvents() {
    return events;
  }

  @Override
  public void copyFrom(Event event, LocalDate startDateFilter) {
    this.setSubject(event.getSubject());
    if(startDateFilter == null) {
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
    LocalDate finalStartDateFilter = startDateFilter;
    this.getEvents().removeIf(e ->
      !e.getStartDate().isBefore(finalStartDateFilter)
    );
    this.getEvents().addAll(event.getListEvents());
    this.setTimeIntervals();
  }
}