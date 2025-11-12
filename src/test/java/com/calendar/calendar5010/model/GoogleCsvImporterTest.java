package com.calendar.calendar5010.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GoogleCsvImporterTest {

  private Path tmpDir;

  @BeforeEach
  void setUp() throws IOException {
    tmpDir = Files.createTempDirectory("calendar_test_");
  }

  @AfterEach
  void tearDown() throws IOException {
    Files.walk(tmpDir)
      .map(Path::toFile)
      .forEach(java.io.File::delete);
  }

  /**
   * Generate a calendar, export to CSV, then import and verify fields match.
   */
  @Test
  void shouldExportAndImportCalendarCorrectly() throws IOException {
    // Step 1: create a test calendar
    Calendar calendar = new Calendar("UnitTest");
    SingleEvent e1 = SingleEvent.builder()
      .subject("Team Meeting")
      .startDate(LocalDate.of(2025, 11, 15))
      .startTime(LocalTime.of(10, 0))
      .endDate(LocalDate.of(2025, 11, 15))
      .endTime(LocalTime.of(11, 0))
      .description("Weekly sync-up")
      .location("Zoom")
      .visibility(Event.Visibility.PUBLIC)
      .build();

    SingleEvent e2 = SingleEvent.builder()
      .subject("Doctor Appointment")
      .startDate(LocalDate.of(2025, 11, 16))
      .startTime(LocalTime.of(9, 0))
      .endDate(LocalDate.of(2025, 11, 16))
      .endTime(LocalTime.of(10, 0))
      .description("Health check")
      .location("Boston Clinic")
      .visibility(Event.Visibility.PRIVATE)
      .build();

    calendar.createEvent(e1);
    calendar.createEvent(e2);

    // Step 2: export to CSV
    Path csvPath = tmpDir.resolve("calendar_export.csv");
    GoogleCsvExporter.export(calendar, csvPath);

    assertTrue(Files.exists(csvPath), "CSV should be created");

    // Step 3: import from CSV
    Calendar imported = new Calendar("Imported");
    GoogleCsvImporter.importCalendar(imported, csvPath);

    // Step 4: verify imported data
    assertEquals(2, imported.getEventsId().size(), "Should import 2 events");

    List<Event> importedEvents = imported.getEventsId().values().stream().toList();
    assertEquals("Team Meeting", importedEvents.get(0).getSubject());
    assertEquals("Doctor Appointment", importedEvents.get(1).getSubject());
  }
}