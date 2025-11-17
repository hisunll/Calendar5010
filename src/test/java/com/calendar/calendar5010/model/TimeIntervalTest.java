package com.calendar.calendar5010.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TimeIntervalTest {
  private final LocalDate DATE = LocalDate.of(2025, 11, 10);
  private final String ID_1 = "event1";
  private final TimeInterval INTERVAL_10_11 = new TimeInterval(ID_1, DATE, LocalTime.of(10, 0), LocalTime.of(11, 0));

  @Test
  void shouldConflictWhenNewIntervalStartsInsideExisting() {
    TimeInterval existing = new TimeInterval("E_ID", DATE, LocalTime.of(9, 0), LocalTime.of(11, 0));
    TimeInterval newInterval = new TimeInterval(ID_1, DATE, LocalTime.of(10, 30), LocalTime.of(11, 30));
    assertTrue(newInterval.conflictsWith(Set.of(existing)));
  }

  @Test
  void shouldConflictWhenNewIntervalEndsInsideExisting() {
    TimeInterval existing = new TimeInterval("E_ID", DATE, LocalTime.of(10, 0), LocalTime.of(12, 0));
    TimeInterval newInterval = new TimeInterval(ID_1, DATE, LocalTime.of(9, 0), LocalTime.of(11, 0));
    assertTrue(newInterval.conflictsWith(Set.of(existing)));
  }

  @Test
  void shouldNotConflictWithNonOverlappingBefore() {
    TimeInterval other = new TimeInterval("E_ID", DATE, LocalTime.of(9, 0), LocalTime.of(10, 0));
    assertFalse(INTERVAL_10_11.conflictsWith(Set.of(other)));
  }

  @Test
  void shouldNotConflictWithNonOverlappingAfter() {
    TimeInterval other = new TimeInterval("E_ID", DATE, LocalTime.of(11, 0), LocalTime.of(12, 0));
    assertFalse(INTERVAL_10_11.conflictsWith(Set.of(other)));
  }

  @Test
  void shouldRejectInvalidTimeInterval() {
    assertThrows(IllegalArgumentException.class,
      () -> new TimeInterval(ID_1, DATE, LocalTime.of(11, 0), LocalTime.of(10, 0)),
      "End time cannot be before start time" //
    );
  }
}

