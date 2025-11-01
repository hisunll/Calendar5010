package com.calendar.calendar5010;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record TimeInterval(String eventId, LocalDate date, LocalTime startTime, LocalTime endTime) {
  public TimeInterval {
    if (endTime.isBefore(startTime)) {
      throw new IllegalArgumentException("End time cannot be before start time");
    }
  }

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
