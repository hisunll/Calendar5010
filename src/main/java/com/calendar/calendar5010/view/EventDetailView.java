package com.calendar.calendar5010.view;

import com.calendar.calendar5010.controller.AppController;
import com.calendar.calendar5010.model.Calendar;
import com.calendar.calendar5010.model.Event;
import com.calendar.calendar5010.model.EventUpdate;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * UI window for viewing and editing an event.
 */
public class EventDetailView extends JFrame {

  private final Calendar calendar;
  private final Event event;
  private final AppController appController;

  private JTextField subjectField;
  private JTextField startDateField;
  private JTextField startTimeField;
  private JTextField endDateField;
  private JTextField endTimeField;
  private JTextField descriptionField;
  private JTextField locationField;

  private JButton saveButton = new JButton("Save Changes");

  /**
   * Constructs a window that displays and allows editing of the details
   * of a specific event.
   *
   * @param calendar the calendar that owns the event
   * @param event the event whose details will be displayed and edited
   * @param appController the controller responsible for coordinating updates
   */
  @SuppressWarnings("EI_EXPOSE_REP2")
  public EventDetailView(Calendar calendar, Event event, AppController appController) {
    super("Event Details");
    this.calendar = calendar;
    this.event = event;
    this.appController = appController;

    setLayout(new GridLayout(0, 2, 5, 5));

    subjectField = new JTextField(event.getSubject(), 15);
    startDateField = new JTextField(event.getStartDate().toString(), 10);
    startTimeField = new JTextField(event.getStartTime().toString(), 10);
    endDateField = new JTextField(event.getEndDate().toString(), 10);
    endTimeField = new JTextField(event.getEndTime().toString(), 10);
    descriptionField = new JTextField(event.getDescription(), 15);
    locationField = new JTextField(event.getLocation(), 15);

    add(new JLabel("Subject:"));
    add(subjectField);

    add(new JLabel("Start Date:"));
    add(startDateField);

    add(new JLabel("Start Time:"));
    add(startTimeField);

    add(new JLabel("End Date:"));
    add(endDateField);

    add(new JLabel("End Time:"));
    add(endTimeField);

    add(new JLabel("Description:"));
    add(descriptionField);

    add(new JLabel("Location:"));
    add(locationField);

    add(saveButton);

    saveButton.addActionListener(e -> handleSave());

    setSize(350, 300);
    setVisible(true);
  }

  private void handleSave() {
    try {
      EventUpdate update = EventUpdate.builder()
          .subject(subjectField.getText())
          .startDate(LocalDate.parse(startDateField.getText()))
          .startTime(LocalTime.parse(startTimeField.getText()))
          .endDate(LocalDate.parse(endDateField.getText()))
          .endTime(LocalTime.parse(endTimeField.getText()))
          .description(descriptionField.getText())
          .location(locationField.getText())
          .build();

      calendar.updateEvent(event, update, event.getStartDate());
      JOptionPane.showMessageDialog(this, "Event updated!");

      appController.getListView().refreshList();
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this,
          "Error saving: " + ex.getMessage(),
          "Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  public void loadEvent() {
    subjectField.setText(event.getSubject());
    startDateField.setText(event.getStartDate().toString());
    startTimeField.setText(event.getStartTime().toString());
    endDateField.setText(event.getEndDate().toString());
    endTimeField.setText(event.getEndTime().toString());
    descriptionField.setText(event.getDescription());
    locationField.setText(event.getLocation());
  }

}
