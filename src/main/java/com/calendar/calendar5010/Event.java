package com.calendar.calendar5010;

import lombok.AccessLevel;
import lombok.Builder;
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
  private final String id;

  // required
  private String subject;
  private LocalDate startDate;

  //optional
  private LocalTime startTime;
  private LocalDate endDate;
  private LocalTime endTime;
  private String description;
  private String location;
  private Boolean allowConflict = false;
  private Visibility visibility = Visibility.PUBLIC;

  @Setter(AccessLevel.NONE)
  private List<TimeInterval> timeIntervals = new ArrayList<>();

  @Builder(toBuilder = true)
  protected Event(String subject, LocalDate startDate,
                  LocalTime startTime, LocalDate endDate, LocalTime endTime,
                  Visibility visibility, String description, String location, boolean allowConflict) {

    this.subject = subject;
    this.startDate = startDate;
    this.startTime = startTime;
    this.endDate = endDate;
    this.endTime = endTime;
    this.description = description;
    this.location = location;
    this.allowConflict = allowConflict;

    if(visibility != null) {
      this.visibility = visibility;
    }

    setTimeIntervals();
    //checkIsValid();
    this.id = UUID.randomUUID().toString();
  }

  protected Event() {
    this.id = UUID.randomUUID().toString();
  }

  public enum Visibility {
    PUBLIC, PRIVATE
  }

  public abstract ValidationResult checkIsValid();

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

  public abstract void setTimeIntervals();

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
