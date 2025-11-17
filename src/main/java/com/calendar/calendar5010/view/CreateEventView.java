package com.calendar.calendar5010.view;

import com.calendar.calendar5010.controller.AppController;
import com.calendar.calendar5010.model.Calendar;
import com.calendar.calendar5010.model.Event;
import com.calendar.calendar5010.model.SingleEvent;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * A very simple Swing UI that allows the user to enter event fields
 * and click a button to create the event in the given Calendar.
 */
public class CreateEventView extends JFrame {
  private final Calendar calendar;

  private JTextField subjectField = new JTextField(15);
  private JTextField startDateField = new JTextField("2025-11-20", 10);
  private JTextField startTimeField = new JTextField("09:00", 10);
  private JTextField endDateField = new JTextField("2025-11-20", 10);
  private JTextField endTimeField = new JTextField("10:00", 10);
  private JTextField descriptionField = new JTextField(15);
  private JTextField locationField = new JTextField(15);

  private JButton createButton = new JButton("Create Event");

  /**
   * Constructs a small UI window for creating an event.
   */
  public CreateEventView(Calendar calendar, AppController appController) {
    super("Create Event");
    this.calendar = calendar;

    setLayout(new GridLayout(0, 2, 5, 5));

    add(new JLabel("Subject:"));
    add(subjectField);

    add(new JLabel("Start Date (YYYY-MM-DD):"));
    add(startDateField);

    add(new JLabel("Start Time (HH:mm):"));
    add(startTimeField);

    add(new JLabel("End Date (YYYY-MM-DD):"));
    add(endDateField);

    add(new JLabel("End Time (HH:mm):"));
    add(endTimeField);

    add(new JLabel("Description:"));
    add(descriptionField);

    add(new JLabel("Location:"));
    add(locationField);

    add(createButton);

    createButton.addActionListener(e -> handleCreateEvent(appController));

    setSize(350, 300);
    setVisible(true);
  }

  /**
   * Reads input from the fields, constructs an event, and calls calendar.createEvent.
   */
  private void handleCreateEvent(AppController controller) {
    try {
      String subject = subjectField.getText().trim();
      LocalDate startDate = LocalDate.parse(startDateField.getText().trim());
      LocalTime startTime = LocalTime.parse(startTimeField.getText().trim());
      LocalDate endDate = LocalDate.parse(endDateField.getText().trim());
      LocalTime endTime = LocalTime.parse(endTimeField.getText().trim());
      String desc = descriptionField.getText().trim();
      String loc = locationField.getText().trim();

      SingleEvent event = SingleEvent.builder()
          .subject(subject)
          .startDate(startDate)
          .startTime(startTime)
          .endDate(endDate)
          .endTime(endTime)
          .description(desc)
          .location(loc)
          .visibility(Event.Visibility.PUBLIC)
          .build();

      calendar.createEvent(event);

      JOptionPane.showMessageDialog(this, "Event created successfully!");

      subjectField.setText("");
      startDateField.setText("");
      startTimeField.setText("");
      endDateField.setText("");
      endTimeField.setText("");
      descriptionField.setText("");
      locationField.setText("");

      controller.getListView().refreshList();

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(
          this,
          ex.getMessage(),
          "Error",
          JOptionPane.ERROR_MESSAGE
      );
    }
  }

}
