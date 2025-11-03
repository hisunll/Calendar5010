package com.calendar.calendar5010;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public abstract class Event {
  @Setter(AccessLevel.NONE)
  private String id;

  // required
  private String subject;
  private LocalDate startDate;

  //optional
  private LocalTime startTime;
  private LocalDate endDate;
  private LocalTime endTime;
  private String description;
  private String location;
  private Boolean allowConflict;
  private Visibility visibility;
  @Setter(AccessLevel.NONE)
  private List<TimeInterval> timeIntervals;

  protected Event(Builder<?> builder) {
    this.subject = builder.subject;
    this.startDate = builder.startDate;
    this.startTime = builder.startTime;
    this.endDate = builder.endDate;
    this.endTime = builder.endTime;
    this.description = builder.description;
    this.location = builder.location;
    this.allowConflict = builder.allowConflict != null ? builder.allowConflict : false;
    this.visibility = builder.visibility != null ? builder.visibility : Visibility.PUBLIC;
    this.timeIntervals = new ArrayList<>();
    if (this.visibility == null) {
      this.visibility = Visibility.PUBLIC;
    }

    if (builder.id != null) {
      this.id = builder.id;
    }
  }

  protected void postBuild() {
    if (this.visibility == null) {
      this.visibility = Visibility.PUBLIC;
    }
    if(endTime == null){
      endTime = LocalTime.MAX;
    }
    if(startTime == null){
      startTime = LocalTime.MIN;
    }
    if(this.id == null) {
      this.id = UUID.randomUUID().toString();
    }
  }

  public static abstract class Builder<T extends Builder<T>> {
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

    public T id(String id) {
      this.id = id;
      return self();
    }

    public T subject(String subject) {
      this.subject = subject;
      return self();
    }

    public T startDate(LocalDate startDate) {
      this.startDate = startDate;
      return self();
    }

    public T startTime(LocalTime startTime) {
      this.startTime = startTime;
      return self();
    }

    public T endDate(LocalDate endDate) {
      this.endDate = endDate;
      return self();
    }

    public T endTime(LocalTime endTime) {
      this.endTime = endTime;
      return self();
    }

    public T description(String description) {
      this.description = description;
      return self();
    }

    public T location(String location) {
      this.location = location;
      return self();
    }

    public T allowConflict(Boolean allowConflict) {
      this.allowConflict = allowConflict;
      return self();
    }

    public T visibility(Visibility visibility) {
      this.visibility = visibility;
      return self();
    }

    protected abstract T self();
    public abstract Event build();
  }

  public enum Visibility {
    PUBLIC, PRIVATE
  }

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

  public LocalDateTime getStartDateTime() {
    if(startTime != null) {
      return startDate.atTime(startTime);
    } else  {
      return startDate.atStartOfDay();
    }
  }

  public LocalDateTime getEndDateTime() {
    if(endTime != null) {
      return endDate.atTime(endTime);
    } else  {
      return startDate.atTime(LocalTime.MAX);
    }
  }

  public abstract Event deepCopy();

  public abstract void prepareForUpdate();

  public void copyFrom(Event event) {
    this.subject = event.subject;
    this.startDate = event.startDate;
    this.startTime = event.startTime;
    this.endDate = event.endDate;
    this.endTime = event.endTime;
    this.description = event.description;
    this.location = event.location;
    this.allowConflict = event.allowConflict;
    this.visibility = event.visibility;
  }

  protected abstract void setTimeIntervals();
  public abstract List<Event> getListEvents();
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
