package com.calendar.calendar5010.view;

import com.calendar.calendar5010.controller.AppController;
import com.calendar.calendar5010.model.Calendar;
import com.calendar.calendar5010.model.Event;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

/**
 * A Swing-based view that displays all events contained in a specific {@link Calendar}.
 */
public class EventListView extends JFrame {
  private final Calendar calendar;
  private final AppController controller;
  private final DefaultListModel<Event> listModel = new DefaultListModel<>();
  private final JList<Event> eventList = new JList<>(listModel);

  /**
   * Constructs a window that displays and allows editing of the details
   * of a specific event.
   *
   * @param controller the controller responsible for coordinating updates
   *     between views and the model;
   * @param calendar the calendar that owns the event; must not be {@code null}
   */
  public EventListView(AppController controller, Calendar calendar) {
    this.controller = controller;
    this.calendar = calendar;

    setTitle("All Events");
    setSize(400, 500);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    eventList.setCellRenderer(new EventCellRenderer());
    eventList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    refreshList();

    eventList.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        Event selected = eventList.getSelectedValue();
        if (selected != null) {
          controller.showEventDetail(selected);
        }
      }
    });

    JButton saveButton = new JButton("Save Calendars");
    saveButton.addActionListener(e -> {
      Path dataDir = Paths.get(System.getProperty("user.home"), "CalendarTest");
      controller.saveCalendars(dataDir);
      System.out.println("Manual save done.");
    });

    JPanel topPanel = new JPanel();
    topPanel.add(saveButton);

    add(topPanel, BorderLayout.NORTH);
    add(new JScrollPane(eventList), BorderLayout.CENTER);
  }

  /**
   * Refresh event list from calendar.
   */
  public void refreshList() {
    listModel.clear();
    List<Event> events = calendar.getEventsId().values().stream().toList();
    for (Event e : events) {
      listModel.addElement(e);
    }
  }

  private static class EventCellRenderer extends JLabel implements ListCellRenderer<Event> {

    @Override
    public Component getListCellRendererComponent(
        JList<? extends Event> list,
        Event event,
        int index,
        boolean isSelected,
        boolean cellHasFocus) {

      // Text shown in the list (simple version)
      String text = event.getSubject()
          + "  |  "
          + event.getStartDate()
          + "  "
          + event.getStartTime();

      setText(text);

      // Basic styling
      if (isSelected) {
        setBackground(new Color(0xCCE5FF));
        setOpaque(true);
      } else {
        setBackground(Color.WHITE);
        setOpaque(true);
      }

      return this;
    }
  }
}
