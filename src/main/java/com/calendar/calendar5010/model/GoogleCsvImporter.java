package com.calendar.calendar5010.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * GoogleCsvImporter provides the ability to import a {@link Calendar}
 *     from a Google Calendar-compatible CSV file.
 *
 * @author Liangliang Sun
 */
public final class GoogleCsvImporter {

  private static final int EXPECTED_CSV_COLUMNS = 9;

  private GoogleCsvImporter() {}

  /**
   * Import a Google Calendar-compatible CSV file into the given {@link Calendar}.
   *
   * @param calendar calendar to import events into
   * @param inputPath path to the CSV file
   * @throws RuntimeException if parsing or file reading fails
   */
  public static void importCalendar(Calendar calendar, Path inputPath) {
    DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm a", Locale.US);

    try (BufferedReader reader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8)) {
      String header = reader.readLine(); // skip header
      if (header == null) {
        return;
      }

      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = parseCsvLine(line);
        if (parts.length < EXPECTED_CSV_COLUMNS) {
          continue; // malformed line
        }

        String subject = unquote(parts[0]);
        String startDateStr = unquote(parts[1]);
        String startTimeStr = unquote(parts[2]);
        String endDateStr = unquote(parts[3]);
        String endTimeStr = unquote(parts[4]);
        String allDayStr = unquote(parts[5]);
        String description = unquote(parts[6]);
        String location = unquote(parts[7]);
        String privateStr = unquote(parts[8]);

        boolean allDay = "True".equalsIgnoreCase(allDayStr);

        LocalDate startDate = parseDate(startDateStr, dateFmt);
        LocalDate endDate = parseDate(endDateStr, dateFmt);
        LocalTime startTime = allDay ? LocalTime.MIN : parseTime(startTimeStr, timeFmt);
        LocalTime endTime = allDay ? LocalTime.MAX : parseTime(endTimeStr, timeFmt);

        SingleEvent event = SingleEvent.builder()
            .subject(subject)
            .startDate(startDate)
            .startTime(startTime)
            .endDate(endDate)
            .endTime(endTime)
            .description(description)
            .location(location)
            .visibility("True".equalsIgnoreCase(privateStr)
              ? Event.Visibility.PRIVATE : Event.Visibility.PUBLIC)
            .build();

        calendar.createEvent(event);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read file: " + inputPath + " - " + e.getMessage(), e);
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse CSV line: " + e.getMessage(), e);
    }
  }

  private static LocalDate parseDate(String s, DateTimeFormatter fmt) {
    try {
      return (s == null || s.isEmpty()) ? null : LocalDate.parse(s, fmt);
    } catch (Exception e) {
      return null;
    }
  }

  private static LocalTime parseTime(String s, DateTimeFormatter fmt) {
    try {
      return (s == null || s.isEmpty()) ? null : LocalTime.parse(s, fmt);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Parse a CSV line while handling quoted fields containing commas or quotes.
   */
  private static String[] parseCsvLine(String line) {
    if (line == null) {
      return new String[0];
    }
    var result = new java.util.ArrayList<String>();
    StringBuilder current = new StringBuilder();
    boolean inQuotes = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (c == '\"') {
        if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
          current.append('\"');
          i++;
        } else {
          inQuotes = !inQuotes;
        }
      } else if (c == ',' && !inQuotes) {
        result.add(current.toString());
        current.setLength(0);
      } else {
        current.append(c);
      }
    }
    result.add(current.toString());
    return result.toArray(new String[0]);
  }

  private static String unquote(String s) {
    if (s == null) {
      return "";
    }
    s = s.trim();
    if (s.startsWith("\"") && s.endsWith("\"")) {
      s = s.substring(1, s.length() - 1);
    }
    return s.replace("\"\"", "\"");
  }
}