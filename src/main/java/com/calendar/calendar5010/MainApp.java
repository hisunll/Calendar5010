package com.calendar.calendar5010;

import com.calendar.calendar5010.controller.AppController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Entry point for launching the Calendar application.
 */
public class MainApp {

  /**
   * Starts the application by creating an {@link AppController}, restoring
   * previously saved calendars, and constructing the initial UI windows.
   *
   * @param args unused command-line arguments
   * @throws Exception if an I/O error occurs while restoring calendars
   */
  public static void main(String[] args) throws Exception {
    AppController controller = new AppController();
    Path dataDir = Paths.get(System.getProperty("user.home"), "CalendarTest");
    Files.createDirectories(dataDir);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        controller.saveCalendars(dataDir);
        System.out.println("Calendars saved to: " + dataDir);
      } catch (Exception e) {
        System.err.println("Failed to save calendars on shutdown: " + e.getMessage());
      }
    }));
    controller.restoreCalendars(dataDir);
    controller.buildViews();
  }
}
