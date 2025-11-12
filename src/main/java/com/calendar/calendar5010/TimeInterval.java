package com.calendar.calendar5010;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * Immutable interval of time on a specific date associated with an event.
 *
 * @param eventId identifier of the owning event
 * @param date the calendar date on which this interval occurs
 * @param startTime the start time of the interval
 * @param endTime the end time of the interval
 */
public record TimeInterval(String eventId, LocalDate date, LocalTime startTime, LocalTime endTime) {
  /**
   * Canonical constructor that validates the time range.
   *
   * @param eventId identifier of the owning event
   * @param date the calendar date on which this interval occurs
   * @param startTime the inclusive start time of the interval
   * @param endTime the inclusive end time of the interval
   * @throws IllegalArgumentException if illegal
   */
  public TimeInterval {
    if (endTime.isBefore(startTime)) {
      throw new IllegalArgumentException("End time cannot be before start time");
    }
  }

  /**
   * Determines whether this interval overlaps with any of the provided intervals.
   *
   * @param others intervals to check against (nullable)
   * @return {@code true} if an overlap exists; otherwise {@code false}
   */
  public boolean conflictsWith(Set<TimeInterval> others) {
    for (TimeInterval other : others) {
      LocalTime startExistingTime = other.startTime();
      LocalTime endExistingTime = other.endTime();

      if (startExistingTime.isBefore(startTime) && endExistingTime.isAfter(startTime)) {
        return true;
      }
      if (startExistingTime.isBefore(endTime) && endExistingTime.isAfter(endTime)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public String toString() {
    return "TimeInterval[" + startTime + " - " + endTime + "]";
  }
}
