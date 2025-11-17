package com.calendar.calendar5010.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Carries partial update data for an {@link Event}.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
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

  /**
   * Default constructor for {@code EventUpdate}.
   */
  public EventUpdate() {
  }

  /**
   * Returns the set of days on which this event recurs.
   *
   * @return an unmodifiable Set of DayOfWeek values representing
   */
  public Set<DayOfWeek> getRecurrenceDays() {
    if (recurrenceDays == null) {
      return null;
    }
    return Collections.unmodifiableSet(recurrenceDays);
  }

}