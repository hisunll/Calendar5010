package com.calendar.calendar5010;

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
  private Boolean belongsToRecurringEvent;
  private String fatherId;

  private SingleEvent(Builder builder) {
    super(builder);
    this.belongsToRecurringEvent = builder.belongsToRecurringEvent != null ? builder.belongsToRecurringEvent : false;
    this.fatherId = builder.fatherId;

    postBuild();
  }

  public static class Builder extends Event.Builder<Builder> {
    private Boolean belongsToRecurringEvent;
    private String fatherId;

    public Builder belongsToRecurringEvent(Boolean belongsToRecurringEvent) {
      this.belongsToRecurringEvent = belongsToRecurringEvent;
      return this;
    }

    public Builder fatherId(String fatherId) {
      this.fatherId = fatherId;
      return this;
    }

    @Override
    protected Builder self() {
      return this;
    }

    @Override
    public SingleEvent build() {
      return new SingleEvent(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  protected void postBuild() {
    ValidationResult validationResult = checkIsValid();
    if(!validationResult.getValid()) {
      throw new IllegalArgumentException(validationResult.getMessage());
    }
    super.postBuild();
    if(this.belongsToRecurringEvent == null) {
      this.belongsToRecurringEvent = false;
    }
    setTimeIntervals();
  }

  public Builder toBuilder() {
    return SingleEvent.builder()
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
      .belongsToRecurringEvent(this.getBelongsToRecurringEvent())
      .fatherId(this.getFatherId());
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

