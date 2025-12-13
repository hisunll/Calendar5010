package com.calendar.calendar5010.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.*;

/**
 * Unit tests for {@link CalendarPersistenceManager}.
 *
 * Ensures branch and line coverage > 90%.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CalendarPersistenceManagerTest {

  private Path tmpDir;

  @BeforeEach
  void setUp() throws IOException {
    tmpDir = Files.createTempDirectory("cal_persist_test_");
  }

  @AfterEach
  void tearDown() throws IOException {
    // delete recursively
    Files.walk(tmpDir)
      .map(Path::toFile)
      .sorted((a, b) -> -a.compareTo(b)) // delete children first
      .forEach(File::delete);
  }

  // ---------- Utility ----------
  private Calendar makeCalendar(String title, int eventCount) {
    Calendar cal = new Calendar(title);
    for (int i = 0; i < eventCount; i++) {
      SingleEvent event = SingleEvent.builder()
        .subject("Event_" + i)
        .startDate(LocalDate.of(2025, 11, 10 + i))
        .startTime(LocalTime.of(9, 0))
        .endDate(LocalDate.of(2025, 11, 10 + i))
        .endTime(LocalTime.of(10, 0))
        .description("desc_" + i)
        .location("loc_" + i)
        .visibility(i % 2 == 0 ? Event.Visibility.PUBLIC : Event.Visibility.PRIVATE)
        .build();
      cal.createEvent(event);
    }
    return cal;
  }

  // ---------- Tests ----------

  @Test
  @Order(1)
  void testSaveAndRestoreMultipleCalendars() throws IOException {
    Calendar work = makeCalendar("Work", 2);
    Calendar personal = makeCalendar("Personal", 3);

    // Save all
    CalendarPersistenceManager.saveAllCalendars(List.of(work, personal), tmpDir);
    assertTrue(Files.list(tmpDir).count() >= 2, "Expected CSV files in directory");

    // Restore all
    List<Calendar> restored = CalendarPersistenceManager.restoreAllCalendars(tmpDir);
    assertEquals(2, restored.size(), "Should restore two calendars");

    Calendar restoredWork = restored.stream()
      .filter(c -> c.getTitle().equals("Work"))
      .findFirst()
      .orElseThrow();

    assertEquals(2, restoredWork.getEventsId().size());
  }

  @Test
  @Order(2)
  void testSaveAllCalendars_emptyList_shouldNotThrow() {
    // should silently pass
    assertDoesNotThrow(() ->
      CalendarPersistenceManager.saveAllCalendars(new ArrayList<>(), tmpDir));
  }

  @Test
  @Order(3)
  void testRestoreAllCalendars_invalidDir_shouldThrow() {
    Path fake = tmpDir.resolve("not_a_dir.csv");
    // create file to make path invalid (exists but not a dir)
    try {
      Files.writeString(fake, "dummy");
    } catch (IOException ignored) {}

    assertThrows(RuntimeException.class, () ->
      CalendarPersistenceManager.restoreAllCalendars(fake));
  }

  @Test
  @Order(4)
  void testSaveAllCalendars_handlesInvalidTitleAndSpecialChars() throws IOException {
    Calendar special = makeCalendar("My@Invalid Title!!", 1);
    CalendarPersistenceManager.saveAllCalendars(List.of(special), tmpDir);

    boolean found = Files.list(tmpDir)
      .anyMatch(p -> p.getFileName().toString().startsWith("My_Invalid_Title"));
    assertTrue(found, "File name should be sanitized");
  }

  @Test
  @Order(5)
  void testRestoreAllCalendars_handlesCorruptedCsvGracefully() throws IOException {
    Path badCsv = tmpDir.resolve("Broken.csv");
    Files.writeString(badCsv, "Subject,Start Date,...\nBadLineWithoutEnoughFields");

    List<Calendar> restored = CalendarPersistenceManager.restoreAllCalendars(tmpDir);
    // It should skip broken CSV and not crash
    assertNotNull(restored);
  }

  @Test
  @Order(6)
  void testSaveAllCalendars_createsDirectoryIfMissing() throws IOException {
    Path newDir = tmpDir.resolve("nested");
    Calendar cal = makeCalendar("NewCal", 1);
    assertDoesNotThrow(() ->
      CalendarPersistenceManager.saveAllCalendars(List.of(cal), newDir));
    assertTrue(Files.exists(newDir), "Directory should be created automatically");
  }

  @Test
  @Order(7)
  void testSaveAllCalendars_nullCalendarTitle_shouldSkip() throws IOException {
    Calendar cal = makeCalendar("", 1);
    CalendarPersistenceManager.saveAllCalendars(List.of(cal), tmpDir);
    long count = Files.list(tmpDir).count();
    assertEquals(0, count, "Calendar with empty title should not be saved");
  }

  @Test
  @Order(8)
  void testSaveAllCalendars_exceptionPath() {
    List<Calendar> calendars = List.of(makeCalendar("Work", 1));

    Path invalidPath;
    String os = System.getProperty("os.name").toLowerCase();
    if (os.contains("win")) {
      invalidPath = Path.of("C:\\Windows\\System32\\ProtectedTest");
    } else {
      invalidPath = Path.of("/root/protected_path_test");
    }

    assertThrows(RuntimeException.class, () ->
      CalendarPersistenceManager.saveAllCalendars(calendars, invalidPath));
  }

  @Test
  @Order(9)
  void testRestoreAllCalendars_nonexistentDirectory_shouldThrow() {
    Path nonexistent = tmpDir.resolve("does_not_exist_dir");
    assertThrows(RuntimeException.class, () ->
        CalendarPersistenceManager.restoreAllCalendars(nonexistent),
      "Nonexistent directory should trigger exception");
  }

  @Test
  @Order(10)
  void testRestoreAllCalendars_emptyDirectory_shouldReturnEmptyList() {
    List<Calendar> restored = CalendarPersistenceManager.restoreAllCalendars(tmpDir);
    assertTrue(restored.isEmpty(), "Empty directory should yield empty list");
  }

  @Test
  @Order(11)
  void testRestoreAllCalendars_handlesImporterException() throws IOException {
    Path badCsv = tmpDir.resolve("Corrupted.csv");
    Files.writeString(badCsv, "InvalidHeaderLine,NoDataHere");
    List<Calendar> restored = CalendarPersistenceManager.restoreAllCalendars(tmpDir);
    assertNotNull(restored, "Result list should not be null even if import fails");
  }

  @Test
  @Order(12)
  void testRestoreAllCalendars_catchesImporterException_andLogsToStderr() throws IOException {
    Path badCsv = tmpDir.resolve("Bad.csv");
    String header = "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\r\n";
    String badRow = "Bad Event,11/15/2025,,11/15/2025,10:00 AM,False,desc,loc,False\r\n";
    Files.writeString(badCsv, header + badRow, java.nio.charset.StandardCharsets.UTF_8);

    java.io.PrintStream oldErr = System.err;
    java.io.ByteArrayOutputStream errBuf = new java.io.ByteArrayOutputStream();
    System.setErr(new java.io.PrintStream(errBuf));

    try {
      List<Calendar> restored = CalendarPersistenceManager.restoreAllCalendars(tmpDir);
      assertNotNull(restored, "Result list should not be null when importer throws");
      assertTrue(restored.isEmpty(), "Importer failure should be caught; no calendars should be added");
      String errOut = errBuf.toString();
      assertTrue(
        errOut.contains("Failed to import calendar file: Bad.csv"),
        "stderr should contain failure log from catch clause");
    } finally {
      System.setErr(oldErr);
    }
  }

  @Test
  @Order(13)
  void testSaveAllCalendars_nullAndEmptyList_shouldReturnImmediately() {
    // null list
    assertDoesNotThrow(() ->
      CalendarPersistenceManager.saveAllCalendars(null, tmpDir));

    // empty list
    assertDoesNotThrow(() ->
      CalendarPersistenceManager.saveAllCalendars(new ArrayList<>(), tmpDir));
  }

  @Test
  @Order(14)
  void testSaveAllCalendars_skipsCalendarsWithEmptyOrNullTitle() throws IOException {
    Calendar emptyTitle = new Calendar("");
    Calendar nullTitle = new Calendar((String) null);

    Path out = tmpDir.resolve("out");
    CalendarPersistenceManager.saveAllCalendars(List.of(emptyTitle, nullTitle), out);

    long csvCount = Files.list(out)
      .filter(p -> p.toString().endsWith(".csv"))
      .count();
    assertEquals(0, csvCount, "Calendars with null or empty titles should be skipped");
  }
  
}
