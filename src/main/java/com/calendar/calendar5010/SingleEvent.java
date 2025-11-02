package com.calendar.calendar5010;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SingleEvent extends Event {
  private Boolean belongsToRecurringEvent = false;
  private String fatherId;

  @Builder(toBuilder = true)
  public SingleEvent(String subject, LocalDate startDate, LocalTime startTime,
                     LocalDate endDate, LocalTime endTime,
                     Event.Visibility visibility, String description,
                     String location, boolean allowConflict,
                     Boolean belongsToRecurringEvent, String fatherId) {
    super(subject, startDate, startTime, endDate, endTime, visibility, description, location, allowConflict);
    if(belongsToRecurringEvent != null) {
      this.belongsToRecurringEvent = belongsToRecurringEvent;
      this.fatherId = fatherId;
    }
  }

  @Override
  protected void setTimeIntervals() {
    getTimeIntervals().clear();
    LocalDate startDate = getStartDate();
    LocalTime startTime = getStartTime();
    LocalDate endDate = getEndDate();
    LocalTime endTime = getEndTime();
    String id = getId();

    for (LocalDate now = startDate; now.isBefore(endDate.plusDays(1)); now = now.plusDays(1)) {
      LocalTime currentStart = (now.equals(startDate)) ? startTime : LocalTime.MIN;
      LocalTime currentEnd = (now.equals(endDate)) ? endTime : LocalTime.MAX;
      TimeInterval interval = new TimeInterval(id, now, currentStart, currentEnd);
      getTimeIntervals().add(interval);
    }
  }

  @Override
  public ValidationResult checkIsValid(){
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

    if(getEndDateTime().isBefore(getStartDateTime())){
      return ValidationResult.error("End time cannot be before start time");
    }

    return ValidationResult.valid();
  }

  @Override
  public SingleEvent deepCopy() {
    return this.toBuilder().build();
  }

  @Override
  public void prepareForUpdate() {
    setTimeIntervals();
  }

  @Override
  public List<Event> getListEvents() {
    List<Event> events = new ArrayList<>();
    events.add(this);
    return events;
  }

  @Override
  public void copyFrom(Event event, LocalDate startDate) {
    this.setSubject(event.getSubject());
    this.setStartDate(event.getStartDate());
    this.setStartTime(event.getStartTime());
    this.setEndDate(event.getEndDate());
    this.setEndTime(event.getEndTime());
    this.setDescription(event.getDescription());
    this.setLocation(event.getLocation());
    this.setAllowConflict(event.getAllowConflict());
    this.setVisibility(event.getVisibility());
  }
}

