package edu.northeastern.cs5010.view;

import edu.northeastern.cs5010.model.Calendar;
import edu.northeastern.cs5010.model.Event;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.MaskFormatter;

/**
 * Represents a view for creating an event in the application. This class extends
 * the {@code JDialog} to provide a modal dialog interface for user input and validation.
 * It integrates with a {@link Calendar} object to save the created event data.
 * The dialog includes fields for configuring the event, such as:
 * - Subject
 * - Start date and time
 * - End date and time
 * - All-day event option
 * - Public/private visibility
 * - Event location
 * - Description
 * Includes input validation for date and time ranges, checks for required fields,
 * and provides status messages for errors or success.
 */
public class CreateEventView extends JDialog {

  private final Calendar calendar;

  private final JTextField subjectField = new JTextField(20);
  private final JFormattedTextField startDateField = maskedField("####-##-##");
  private final JFormattedTextField endDateField = maskedField("####-##-##");
  private final JCheckBox allDayCheck = new JCheckBox("All day event");
  private final JFormattedTextField startTimeField = maskedField("##:##:##");
  private final JFormattedTextField endTimeField = maskedField("##:##:##");
  private final JCheckBox publicCheck = new JCheckBox("Public");
  private final JTextArea descriptionArea = new JTextArea(3, 20);
  private final JTextField locationField = new JTextField(20);
  private final JLabel statusLabel = new JLabel(" ");

  /**
   * Constructs a modal dialog for creating a new event.
   * Provides a user interface to input event details such as name,
   * start and end dates, time, location, and description.
   *
   * @param owner the parent JFrame that owns this dialog
   * @param calendar the Calendar instance to which the new event will be added
   */
  public CreateEventView(JFrame owner, Calendar calendar) {
    super(owner, "Create Event", true);
    this.calendar = calendar;
    initUi();
    attachActions();
    pack();
    setLocationRelativeTo(owner);
  }

  private void initUi() {
    final JPanel form = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(4, 4, 4, 4);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 0;

    form.add(new JLabel("Subject*"), gbc);
    gbc.gridx = 1;
    form.add(subjectField, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    form.add(new JLabel("Start date* (yyyy-MM-dd)"), gbc);
    gbc.gridx = 1;
    form.add(startDateField, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    form.add(new JLabel("End date* (yyyy-MM-dd)"), gbc);
    gbc.gridx = 1;
    form.add(endDateField, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    form.add(allDayCheck, gbc);

    gbc.gridy++;
    form.add(new JLabel("Start time (HH:mm:ss)"), gbc);
    gbc.gridx = 1;
    form.add(startTimeField, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    form.add(new JLabel("End time (HH:mm:ss)"), gbc);
    gbc.gridx = 1;
    form.add(endTimeField, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    form.add(publicCheck, gbc);

    gbc.gridy++;
    form.add(new JLabel("Location"), gbc);
    gbc.gridx = 1;
    form.add(locationField, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    form.add(new JLabel("Description"), gbc);
    gbc.gridx = 1;
    form.add(descriptionArea, gbc);

    JButton saveBtn = new JButton("Save");
    JButton cancelBtn = new JButton("Cancel");

    JPanel controls = new JPanel();
    controls.add(statusLabel);
    controls.add(saveBtn);
    controls.add(cancelBtn);

    add(form, BorderLayout.CENTER);
    add(controls, BorderLayout.SOUTH);

    allDayCheck.addActionListener(e -> toggleTimeFields());
    cancelBtn.addActionListener(e -> dispose());
    saveBtn.addActionListener(e -> onSave());
    toggleTimeFields();
  }

  private void attachActions() {
    // additional shortcuts (Enter to save, Esc to cancel) could be registered here later
  }

  private void toggleTimeFields() {
    boolean timed = !allDayCheck.isSelected();
    startTimeField.setEnabled(timed);
    endTimeField.setEnabled(timed);
    if (!timed) {
      startTimeField.setText("");
      endTimeField.setText("");
    }
  }

  private void onSave() {
    try {
      String subject = subjectField.getText().trim();
      if (subject.isEmpty()) {
        fail("Subject is required.");
        return;
      }

      LocalDate startDate = LocalDate.parse(startDateField.getText().trim());
      LocalDate endDate = LocalDate.parse(endDateField.getText().trim());
      if (endDate.isBefore(startDate)) {
        fail("End date cannot precede start date.");
        return;
      }

      String startTime = startTimeField.getText().trim();
      String endTime = endTimeField.getText().trim();
      if (startTime.isEmpty() ^ endTime.isEmpty()) {
        fail("Provide both start and end times or leave both blank.");
        return;
      }

      if (!startTime.isEmpty()) {
        LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        if (!end.isAfter(LocalTime.parse(startTime))) {
          fail("End time must be after start time.");
          return;
        }
      }

      Event.Builder builder = new Event.Builder(subject,
          startDate.toString(), endDate.toString());

      if (!startTime.isEmpty()) {
        builder.startTime(startTime).endTime(endTime);
      }
      builder.isPublic(publicCheck.isSelected());
      if (!descriptionArea.getText().isBlank()) {
        builder.description(descriptionArea.getText());
      }
      if (!locationField.getText().isBlank()) {
        builder.location(locationField.getText().trim());
      }

      Event event = builder.build();
      calendar.addEvent(event);
      statusLabel.setText("Event created.");
      dispose();

    } catch (Exception ex) {
      fail(ex.getMessage());
    }
  }

  private void fail(String message) {
    statusLabel.setText(message);
  }

  private static JFormattedTextField maskedField(String mask) {
    try {
      MaskFormatter formatter = new MaskFormatter(mask);
      formatter.setPlaceholderCharacter('_');
      return new JFormattedTextField(formatter);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to create mask", e);
    }
  }
}
