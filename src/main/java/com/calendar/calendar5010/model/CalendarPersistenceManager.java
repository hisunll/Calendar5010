package com.calendar.calendar5010.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * CalendarPersistenceManager provides methods to save and restore
 * multiple {@link Calendar} instances to and from CSV files.
 *
 * @author Liangliang Sun
 */
public final class CalendarPersistenceManager {

  private CalendarPersistenceManager() {}

  /**
   * Save all given calendars as individual CSV files.
   *
   * @param calendars list of calendars to save
   * @param outputDir directory to store exported CSVs
   * @throws RuntimeException if export fails
   */
  public static void saveAllCalendars(List<Calendar> calendars, Path outputDir) {
    if (calendars == null || calendars.isEmpty()) {
      return;
    }
    try {
      File dir = outputDir.toFile();
      if (!dir.exists()) {
        if (!dir.mkdirs() && !dir.exists()) {
          throw new RuntimeException("Failed to create directory: " + outputDir);
        }
      }


      for (Calendar cal : calendars) {
        if (cal.getTitle() == null || cal.getTitle().isEmpty()) {
          continue;
        }
        String safeName = cal.getTitle()
            .replaceAll("[^a-zA-Z0-9-_]", "_")
            .replaceAll("_+", "_");
        Path csvPath = outputDir.resolve(safeName + ".csv");
        GoogleCsvExporter.export(cal, csvPath);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to save all calendars: " + e.getMessage(), e);
    }
  }

  /**
   * Restore all calendars from CSV files located in a directory.
   *
   * @param inputDir directory containing CSV files
   * @return list of restored {@link Calendar} objects
   * @throws RuntimeException if reading fails
   */
  public static List<Calendar> restoreAllCalendars(Path inputDir) {
    List<Calendar> restored = new ArrayList<>();
    File dir = inputDir.toFile();

    if (!dir.exists() || !dir.isDirectory()) {
      throw new RuntimeException("Invalid input directory: " + inputDir);
    }

    File[] csvFiles = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".csv"));

    if (csvFiles == null) {
      return restored;
    }

    for (File file : csvFiles) {
      String fileName = file.getName();
      String title = fileName.replaceFirst("\\.csv$", "");
      Calendar cal = new Calendar(title);
      try {
        GoogleCsvImporter.importCalendar(cal, file.toPath());
        restored.add(cal);
      } catch (Exception e) {
        System.err.println("Failed to import calendar file: " + fileName + " - " + e.getMessage());
      }
    }
    return restored;
  }

}
