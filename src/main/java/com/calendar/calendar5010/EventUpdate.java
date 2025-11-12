package com.calendar.calendar5010;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EventUpdate {
  private String subject;
  private LocalDate startDate;
  private LocalTime startTime;
  private LocalDate endDate;
  private LocalTime endTime;
  private String description;
  private String location;
  private Boolean allowConflict;
  private Event.Visibility visibility;
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private Set<DayOfWeek> recurrenceDays;
  private Integer repeatCount;
  private LocalDate recurrenceEndDate;

  public Set<DayOfWeek> getRecurrenceDays() {
    if (recurrenceDays == null) {
      return null;
    }
    return Collections.unmodifiableSet(recurrenceDays);
  }

}