package com.calendar.calendar5010.model;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GoogleCsvExportTest {

  private static final LocalDate D = LocalDate.of(2025, 11, 3); // Monday
  private static final LocalTime T9 = LocalTime.of(9, 0);
  private static final LocalTime T10 = LocalTime.of(10, 0);

  @Test
  void shouldExportHeaderAndBasicRows() {
    Calendar calendar = new Calendar("Export Test");

    // All-day single event (same day)
    Event allDay = SingleEvent.builder().subject("AllDay").startDate(D).endDate(D).allowConflict(true).build();
    calendar.createEvent(allDay);

    // Timed single event
    Event meeting = SingleEvent.builder().subject("Meeting").startDate(D).endDate(D).startTime(T9).endTime(T10).allowConflict(true).build();
    calendar.createEvent(meeting);

    // Recurring by count: Mon/Wed, 3 instances
    RecurringEvent recurring = RecurringEvent.builder()
      .subject("Fitness")
      .startDate(D).endDate(D)
      .startTime(T9).endTime(T10)
      .recurrenceDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
      .repeatCount(3)
      .allowConflict(true)
      .build();
    calendar.createEvent(recurring);

    // Multi-day all-day event
    Event trip = SingleEvent.builder().subject("Trip").startDate(D).endDate(D.plusDays(2)).allowConflict(true).build();
    calendar.createEvent(trip);

    String csv = GoogleCsvExporter.export(calendar);

    // Write CSV to a temp file for inspection when assertions fail
    try {
      java.nio.file.Path tmp = java.nio.file.Path.of("build/tmp/GoogleCsvExportTest_shouldExportHeaderAndBasicRows.csv");
      java.nio.file.Files.createDirectories(tmp.getParent());
      java.nio.file.Files.writeString(tmp, csv);
    } catch (Exception ignore) {}

    // Header
    String header = "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private";
    assertTrue(csv.startsWith(header + "\r\n"), "CSV header is incorrect");

    // All-day row: times blank, All Day True, Description/Location blank, Private False
    assertTrue(csv.contains("AllDay,11/03/2025,,11/03/2025,,True,,,False"), "All-day event row missing or incorrect");

    // Timed meeting row: times present, All Day False, Description/Location blank
    assertTrue(csv.contains("Meeting,11/03/2025,09:00 AM,11/03/2025,10:00 AM,False,,,False"), "Timed event row missing or incorrect");

    // Recurring rows include expected dates
    assertTrue(csv.contains("Fitness,11/03/2025,09:00 AM,11/03/2025,10:00 AM,False,,,False"));
    assertTrue(csv.contains("Fitness,11/05/2025,09:00 AM,11/05/2025,10:00 AM,False,,,False"));
    assertTrue(csv.contains("Fitness,11/10/2025,09:00 AM,11/10/2025,10:00 AM,False,,,False"));

    // Multi-day all-day row (Trip): All Day True, times blank
    assertTrue(csv.contains("Trip,11/03/2025,,11/05/2025,,True,,,False"), "Multi-day all-day event row missing or incorrect");
  }
}