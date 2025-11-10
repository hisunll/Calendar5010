package com.calendar.calendar5010;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class GoogleCsvExporter {
  private GoogleCsvExporter() {}

  public static String export(Calendar calendar) {
    String header = "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private";
    DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.US);

    List<Event> allEvents = new ArrayList<>(calendar.getEventsId().values());
    allEvents.sort(Comparator
      .comparing(Event::getStartDate)
      .thenComparing(Event::getStartTime)
      .thenComparing(Event::getSubject, Comparator.nullsLast(String::compareTo))
    );

    StringBuilder sb = new StringBuilder();
    sb.append(header).append("\r\n");

    for (Event e : allEvents) {
      boolean allDay = isAllDay(e);
      String subject = csvEscape(e.getSubject());
      String startDate = e.getStartDate() != null ? e.getStartDate().format(dateFmt) : "";
      String startTime = allDay ? "" : (e.getStartTime() != null ? e.getStartTime().format(timeFmt) : "");
      String endDate = e.getEndDate() != null ? e.getEndDate().format(dateFmt) : "";
      String endTime = allDay ? "" : (e.getEndTime() != null ? e.getEndTime().format(timeFmt) : "");
      String allDayStr = allDay ? "True" : "False";
      String description = csvEscape(e.getDescription());
      String location = csvEscape(e.getLocation());
      String isPrivate = (e.getVisibility() == Event.Visibility.PRIVATE) ? "True" : "False";

      sb.append(subject).append(',')
        .append(startDate).append(',')
        .append(startTime).append(',')
        .append(endDate).append(',')
        .append(endTime).append(',')
        .append(allDayStr).append(',')
        .append(description).append(',')
        .append(location).append(',')
        .append(isPrivate)
        .append("\r\n");
    }

    return sb.toString();
  }

  public static void export(Calendar calendar, Path outputPath) {
    String csv = export(calendar);
    try {
      Files.writeString(outputPath, csv, StandardCharsets.UTF_8);
    } catch (Exception ex) {
      throw new RuntimeException("Failed to write CSV to " + outputPath + ": " + ex.getMessage(), ex);
    }
  }

  private static boolean isAllDay(Event e) {
    return (e.getStartTime() != null && e.getEndTime() != null
      && e.getStartTime().equals(LocalTime.MIN)
      && e.getEndTime().equals(LocalTime.MAX));
  }

  private static String csvEscape(String s) {
    if (s == null) {
      return "";
    }
    String escaped = s.replace("\"", "\"\"");
    boolean mustQuote = escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r") ||
      escaped.startsWith(" ") || escaped.endsWith(" ");
    return mustQuote ? ('\"' + escaped + '\"') : escaped;
  }
}