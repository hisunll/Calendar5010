package com.calendar.calendar5010;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

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
  private Set<DayOfWeek> recurrenceDays;
  private Integer repeatCount;
  private LocalDate recurrenceEndDate;
}