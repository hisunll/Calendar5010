package com.calendar.calendar5010.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

class EventValidationTest {

  private final LocalDate DATE = LocalDate.of(2025, 11, 10);

  @Test
  void singleEventShouldBeValidWithAllRequiredFields() {
    Event event = SingleEvent.builder().subject("Valid Event").startDate(DATE).endDate(DATE).build();
    assertTrue(event.checkIsValid().getValid());
  }

  @Test
  void allDayEventShouldBeInvalidIfItHasEndTime() {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> SingleEvent.builder()
        .subject("All Day Error")
        .startDate(DATE)
        .endDate(DATE)
        .startTime(null)
        .endTime(LocalTime.of(17, 0))
        .build()
    );

    assertEquals("Cannot set endTime if startTime is null", exception.getMessage());
  }

  @Test
  void allDayEventShouldBeValidWithoutTimes() {
    Event event = SingleEvent.builder()
      .subject("All Day Valid")
      .startDate(DATE)
      .endDate(DATE)
      .startTime(null).endTime(null)
      .build();
    assertTrue(event.checkIsValid().getValid());
  }

  @Test
  void eventShouldBeInvalidIfEndTimeBeforeStartTime() {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> SingleEvent.builder()
        .subject("Time Order Error")
        .startDate(DATE)
        .endDate(DATE)
        .startTime(LocalTime.of(11, 0))
        .endTime(LocalTime.of(10, 0))
        .build()
    );
    assertEquals("End time cannot be before start time", exception.getMessage());
  }

  @Test
  void singleEventShouldBeValidIfItSpansMultipleDays() {
    Event event = SingleEvent.builder()
      .subject("Multi Day")
      .startDate(DATE)
      .endDate(DATE.plusDays(2))
      .build();
    assertTrue(event.checkIsValid().getValid());
  }
}