package edu.northeastern.cs5010.view;

import edu.northeastern.cs5010.model.Calendar;
import edu.northeastern.cs5010.model.Event;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
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
 * Modal Swing dialog that displays every editable field on an {@link Event} and lets the user
 * persist changes back through {@link Calendar#editEvent(String, String, String, String, String,
 * String, Boolean, String, String)}. No conflict checking happens here—the model handles it.
 */
public class EventDetailView extends JDialog {

  private final Calendar calendar;
  private final Event event;

  private final JTextField subjectField = new JTextField(20);
  private final JFormattedTextField startDateField = masked("####-##-##");
  private final JFormattedTextField endDateField = masked("####-##-##");
  private final JCheckBox allDayCheck = new JCheckBox("All day event");
  private final JFormattedTextField startTimeField = masked("##:##:##");
  private final JFormattedTextField endTimeField = masked("##:##:##");
  private final JCheckBox publicCheck = new JCheckBox("Public visibility");
  private final JTextField locationField = new JTextField(20);
  private final JTextArea descriptionArea = new JTextArea(3, 20);
  private final JLabel statusLabel = new JLabel(" ");

  private final String originalSubject;
  private final String originalStartDate;
  private final String originalEndDate;
  private final String originalStartTime;
  private final String originalEndTime;
  private final Boolean originalPublic;
  private final String originalDescription;
  private final String originalLocation;

  /**
   * Constructs an EventDetailView dialog to display and edit details of an event.
   *
   * @param owner the parent frame that owns this dialog
   * @param calendar the calendar associated with the event
   * @param event the event whose details are to be viewed or edited
   */
  public EventDetailView(JFrame owner, Calendar calendar, Event event) {
    super(owner, "Event Details", true);
    this.calendar = calendar;
    this.event = event;

    originalSubject = event.getSubject();
    originalStartDate = event.getStartDate();
    originalEndDate = event.getEndDate();
    originalStartTime = event.getStartTime();
    originalEndTime = event.getEndTime();
    originalPublic = event.getPublic();
    originalDescription = event.getDescription();
    originalLocation = event.getLocation();

    initUi();
    loadEventValues();
    pack();
    setLocationRelativeTo(owner);
  }

  private void initUi() {
    final JPanel form = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(4, 4, 4, 4);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    int row = 0;
    addRow(form, gbc, row++, "Event ID", new JLabel(event.getId()));
    addRow(form, gbc, row++, "Subject*", subjectField);
    addRow(form, gbc, row++, "Start date*", startDateField);
    addRow(form, gbc, row++, "End date*", endDateField);
    gbc.gridx = 0;
    gbc.gridy = row++;
    form.add(allDayCheck, gbc);

    addRow(form, gbc, row++, "Start time", startTimeField);
    addRow(form, gbc, row++, "End time", endTimeField);
    gbc.gridx = 0;
    gbc.gridy = row++;
    form.add(publicCheck, gbc);

    addRow(form, gbc, row++, "Location", locationField);
    gbc.gridx = 0;
    gbc.gridy = row++;
    form.add(new JLabel("Description"), gbc);
    gbc.gridx = 1;
    form.add(descriptionArea, gbc);

    JButton saveButton = new JButton("Save");
    JButton closeButton = new JButton("Close");
    saveButton.addActionListener(e -> onSave());
    closeButton.addActionListener(e -> dispose());
    allDayCheck.addActionListener(e -> toggleTimeFields());

    JPanel footer = new JPanel();
    footer.add(statusLabel);
    footer.add(saveButton);
    footer.add(closeButton);

    add(form, BorderLayout.CENTER);
    add(footer, BorderLayout.SOUTH);
  }

  private void loadEventValues() {
    subjectField.setText(originalSubject);
    startDateField.setText(originalStartDate);
    endDateField.setText(originalEndDate);

    boolean hasTime = originalStartTime != null && originalEndTime != null;
    allDayCheck.setSelected(!hasTime);
    startTimeField.setText(hasTime ? originalStartTime : "");
    endTimeField.setText(hasTime ? originalEndTime : "");
    publicCheck.setSelected(Boolean.TRUE.equals(originalPublic));
    locationField.setText(originalLocation == null ? "" : originalLocation);
    descriptionArea.setText(originalDescription == null ? "" : originalDescription);

    toggleTimeFields();
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
        fail("Subject cannot be empty.");
        return;
      }

      String startDate = startDateField.getText().trim();
      String endDate = endDateField.getText().trim();
      LocalDate start = LocalDate.parse(startDate);
      LocalDate end = LocalDate.parse(endDate);
      if (end.isBefore(start)) {
        fail("End date must be on/after start date.");
        return;
      }

      String startTimeText = startTimeField.getText().trim();
      String endTimeText = endTimeField.getText().trim();
      if (startTimeText.isEmpty() ^ endTimeText.isEmpty()) {
        fail("Provide both start and end time or leave both blank.");
        return;
      }
      if (!startTimeText.isEmpty()) {
        LocalTime startTime = LocalTime.parse(startTimeText);
        LocalTime endTime = LocalTime.parse(endTimeText);
        if (!endTime.isAfter(startTime)) {
          fail("End time must be after start time.");
          return;
        }
      } else {
        startTimeText = null;
        endTimeText = null;
      }

      Boolean isPublic = publicCheck.isSelected();
      String location = locationField.getText().trim();
      if (location.isEmpty()) {
        location = null;
      }
      String description = descriptionArea.getText().isBlank()
          ? null : descriptionArea.getText();

      // send only changed values so Calendar keeps untouched fields as-is
      calendar.editEvent(
          event.getId(),
          diff(originalSubject, subject),
          diff(originalStartDate, startDate),
          diff(originalEndDate, endDate),
          diff(originalStartTime, startTimeText),
          diff(originalEndTime, endTimeText),
          diff(originalPublic, isPublic),
          diff(originalDescription, description),
          diff(originalLocation, location)
      );

      statusLabel.setText("Saved");
    } catch (Exception ex) {
      fail(ex.getMessage());
    }
  }

  private String diff(String original, String current) {
    return Objects.equals(original, current) ? null : current;
  }

  private Boolean diff(Boolean original, Boolean current) {
    return Objects.equals(original, current) ? null : current;
  }

  private void fail(String message) {
    statusLabel.setText(message);
  }

  private static JFormattedTextField masked(String mask) {
    try {
      MaskFormatter formatter = new MaskFormatter(mask);
      formatter.setPlaceholderCharacter('_');
      return new JFormattedTextField(formatter);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to create mask field", e);
    }
  }

  private static void addRow(JPanel panel, GridBagConstraints gbc, int row,
      String labelText, java.awt.Component component) {
    gbc.gridx = 0;
    gbc.gridy = row;
    panel.add(new JLabel(labelText), gbc);
    gbc.gridx = 1;
    panel.add(component, gbc);
  }
}
