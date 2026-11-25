
package com.example.knightinknightout.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.knightinknightout.R;
import com.example.knightinknightout.models.Event;
import com.example.knightinknightout.repositories.EventRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class CreateEventActivity extends AppCompatActivity {

    private CheckBox checkKnightIn;
    private CheckBox checkKnightOut;

    private CheckBox checkAnyone;
    private CheckBox checkLimitedCapacity;
    private CheckBox checkApprovalRequired;

    private EditText editEventName;
    private EditText editCourseActivity;
    private EditText editProfessor;
    private EditText editDescription;
    private EditText editDate;
    private EditText editTime;
    private EditText editEndTime;
    private EditText editLocation;
    private EditText editGroupSize;
    private EditText editCost;

    private Spinner spinnerVibe;
    private Button buttonCreateEvent;

    private EventRepository eventRepository;

    private LocalDate selectedDate;
    private LocalTime selectedStartTime;
    private LocalTime selectedEndTime;

    private String selectedCategory = "";

    private static final ZoneId CAMPUS_ZONE =
            ZoneId.of("America/New_York");

    private static final String[] SOCIAL_CATEGORIES = {
            "Gaming", "Food", "Sports",
            "Chill", "Party", "Other"
    };

    private static final String[] STUDY_VIBES = {
            "Discussion",
            "Quiet Study",
            "Exam Review",
            "Homework Help",
            "Collaborative"
    };

    private static final String[] SOCIAL_VIBES = {
            "Casual",
            "Competitive",
            "Relaxed",
            "Energetic",
            "Social"
    };

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "MM/dd/yyyy", Locale.US);

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern(
                    "h:mm a", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                });

        eventRepository = new EventRepository();

        initializeViews();
        setupEventTypeSelection();
        setupJoiningPermissions();
        setupDateTimePickers();

        checkKnightIn.setChecked(true);
        checkAnyone.setChecked(true);

        editGroupSize.setHint("Capacity (0 = unlimited)");

        findViewById(R.id.btnBack)
                .setOnClickListener(v -> finish());

        buttonCreateEvent.setOnClickListener(
                v -> createEvent());
    }

    // Bind XML views to Java variables
    private void initializeViews() {

        checkKnightIn = findViewById(R.id.checkKnightIn);
        checkKnightOut = findViewById(R.id.checkKnightOut);

        checkAnyone = findViewById(R.id.checkAnyone);

        checkLimitedCapacity =
                findViewById(R.id.checkLimitedCapacity);

        checkApprovalRequired =
                findViewById(R.id.checkApprovalRequired);

        editEventName =
                findViewById(R.id.editEventName);

        editCourseActivity =
                findViewById(R.id.editCourseActivity);

        editProfessor =
                findViewById(R.id.editProfessor);

        editDescription =
                findViewById(R.id.editDescription);

        editDate = findViewById(R.id.editDate);
        editTime = findViewById(R.id.editTime);

        editEndTime =
                findViewById(R.id.editEndTime);

        editLocation =
                findViewById(R.id.editLocation);

        editGroupSize =
                findViewById(R.id.editGroupSize);

        editCost = findViewById(R.id.editCost);

        spinnerVibe = findViewById(R.id.spinnerVibe);

        buttonCreateEvent =
                findViewById(R.id.buttonCreateEvent);
    }

    // Only one event type can be selected
    private void setupEventTypeSelection() {

        checkKnightIn.setOnClickListener(v -> {

            checkKnightIn.setChecked(true);
            checkKnightOut.setChecked(false);

            updateEventTypeUI(true);
        });

        checkKnightOut.setOnClickListener(v -> {

            checkKnightOut.setChecked(true);
            checkKnightIn.setChecked(false);

            updateEventTypeUI(false);
        });

        updateEventTypeUI(true);
    }

    // Change form fields based on event type
    private void updateEventTypeUI(boolean isKnightIn) {

        editCourseActivity.setText("");
        editCourseActivity.setError(null);
        selectedCategory = "";

        if (isKnightIn) {

            editCourseActivity.setHint(
                    "Course Code (e.g. CEN 4360)...");

            editCourseActivity.setFocusableInTouchMode(true);
            editCourseActivity.setFocusable(true);

            editCourseActivity.setOnClickListener(null);

            editProfessor.setVisibility(View.VISIBLE);

            updateVibeOptions(STUDY_VIBES);

        } else {

            editCourseActivity.setHint(
                    "Select Activity Category...");

            editCourseActivity.setFocusable(false);

            editCourseActivity.setOnClickListener(
                    v -> showCategoryPicker());

            editProfessor.setVisibility(View.GONE);

            updateVibeOptions(SOCIAL_VIBES);
        }
    }

    private void showCategoryPicker() {

        new AlertDialog.Builder(this)
                .setTitle("Select Activity Category")
                .setItems(SOCIAL_CATEGORIES,
                        (dialog, which) -> {

                            selectedCategory =
                                    SOCIAL_CATEGORIES[which];

                            editCourseActivity.setText(
                                    selectedCategory);

                            editCourseActivity.setError(null);
                        })
                .show();
    }

    private void updateVibeOptions(String[] options) {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        options
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spinnerVibe.setAdapter(adapter);
    }

    // Only one joining permission can be selected
    private void setupJoiningPermissions() {

        checkAnyone.setOnClickListener(
                v -> selectPermission(checkAnyone));

        checkLimitedCapacity.setOnClickListener(
                v -> selectPermission(checkLimitedCapacity));

        checkApprovalRequired.setOnClickListener(
                v -> selectPermission(checkApprovalRequired));
    }

    private void selectPermission(CheckBox selected) {

        checkAnyone.setChecked(
                selected == checkAnyone);

        checkLimitedCapacity.setChecked(
                selected == checkLimitedCapacity);

        checkApprovalRequired.setChecked(
                selected == checkApprovalRequired);

        if (selected == checkLimitedCapacity) {

            editGroupSize.setHint("Maximum Participants...");

        } else {

            editGroupSize.setHint("Capacity (0 = unlimited)");
        }
    }

    private String getJoinType() {

        if (checkLimitedCapacity.isChecked()) {
            return Event.LIMITED;
        }

        if (checkApprovalRequired.isChecked()) {
            return Event.APPROVAL;
        }

        return Event.OPEN;
    }

    // Prevent manual date/time entry
    private void setupDateTimePickers() {

        editDate.setFocusable(false);
        editTime.setFocusable(false);
        editEndTime.setFocusable(false);

        editDate.setOnClickListener(
                v -> showDatePicker());

        editTime.setOnClickListener(
                v -> showTimePicker(false));

        editEndTime.setOnClickListener(
                v -> showTimePicker(true));
    }

    private void showDatePicker() {

        LocalDate today = LocalDate.now(CAMPUS_ZONE);

        LocalDate initialDate = selectedDate == null
                ? today : selectedDate;

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, day) -> {

                            selectedDate = LocalDate.of(
                                    year, month + 1, day);

                            editDate.setText(
                                    selectedDate.format(DATE_FORMAT));

                            editDate.setError(null);
                        },
                        initialDate.getYear(),
                        initialDate.getMonthValue() - 1,
                        initialDate.getDayOfMonth()
                );

        dialog.getDatePicker().setMinDate(
                System.currentTimeMillis());

        dialog.show();
    }

    private void showTimePicker(boolean isEndTime) {

        LocalTime initialTime = isEndTime
                ? selectedEndTime : selectedStartTime;

        if (initialTime == null) {
            initialTime = LocalTime.now(CAMPUS_ZONE);
        }

        new TimePickerDialog(
                this,
                (view, hour, minute) -> {

                    LocalTime chosenTime =
                            LocalTime.of(hour, minute);

                    if (isEndTime) {

                        selectedEndTime = chosenTime;

                        editEndTime.setText(
                                chosenTime.format(TIME_FORMAT));

                        editEndTime.setError(null);

                    } else {

                        selectedStartTime = chosenTime;

                        editTime.setText(
                                chosenTime.format(TIME_FORMAT));

                        editTime.setError(null);
                    }
                },
                initialTime.getHour(),
                initialTime.getMinute(),
                false
        ).show();
    }

    private String getText(EditText field) {

        return field.getText().toString().trim();
    }

    // Convert campus-local dates into Firestore timestamps
    private Timestamp toTimestamp(
            LocalDate date, LocalTime time) {

        LocalDateTime dateTime =
                LocalDateTime.of(date, time);

        Date convertedDate = Date.from(
                dateTime.atZone(CAMPUS_ZONE).toInstant());

        return new Timestamp(convertedDate);
    }

    // Validate the form and submit to Firestore
    private void createEvent() {

        String eventName = getText(editEventName);
        String courseActivity = getText(editCourseActivity);
        String professor = getText(editProfessor);
        String description = getText(editDescription);
        String location = getText(editLocation);

        boolean isKnightIn = checkKnightIn.isChecked();

        if (eventName.isEmpty()) {

            editEventName.setError("Enter an event name");
            editEventName.requestFocus();
            return;
        }

        if (courseActivity.isEmpty()) {

            editCourseActivity.setError(
                    isKnightIn
                            ? "Enter a course code"
                            : "Select an activity category");

            return;
        }

        if (description.isEmpty()) {

            editDescription.setError("Enter a description");
            editDescription.requestFocus();
            return;
        }

        if (selectedDate == null) {

            editDate.setError("Select a date");
            return;
        }

        if (selectedStartTime == null) {

            editTime.setError("Select a start time");
            return;
        }

        if (selectedEndTime == null) {

            editEndTime.setError("Select an end time");
            return;
        }

        Timestamp startTimestamp = toTimestamp(
                selectedDate, selectedStartTime);

        Timestamp endTimestamp = toTimestamp(
                selectedDate, selectedEndTime);

        if (startTimestamp.compareTo(Timestamp.now()) <= 0) {

            showMessage("The event must start in the future.");
            return;
        }

        if (endTimestamp.compareTo(startTimestamp) <= 0) {

            showMessage("End time must be after start time.");
            return;
        }

        if (location.isEmpty()) {

            editLocation.setError("Enter a location");
            editLocation.requestFocus();
            return;
        }

        int maxParticipants = 0;
        double cost = 0.0;

        try {

            String groupSize = getText(editGroupSize);

            if (!groupSize.isEmpty()) {

                maxParticipants =
                        Integer.parseInt(groupSize);
            }

            if (maxParticipants < 0) {

                editGroupSize.setError("Invalid capacity");
                return;
            }

        } catch (NumberFormatException e) {

            editGroupSize.setError("Enter a valid number");
            return;
        }

        String joinType = getJoinType();

        if (Event.LIMITED.equals(joinType)
                && maxParticipants == 0) {

            editGroupSize.setError(
                    "Limited events require a capacity");

            return;
        }

        try {

            String costText = getText(editCost);

            if (!costText.isEmpty()) {
                cost = Double.parseDouble(costText);
            }

            if (cost < 0 || !Double.isFinite(cost)) {

                editCost.setError("Invalid cost");
                return;
            }

        } catch (NumberFormatException e) {

            editCost.setError("Enter a valid cost");
            return;
        }

        if (FirebaseAuth.getInstance()
                .getCurrentUser() == null) {

            showMessage("You must sign in to create an event.");
            return;
        }

        // Construct the Event model
        Event event = new Event();

        event.setEventName(eventName);

        event.setEventType(
                isKnightIn
                        ? Event.KNIGHT_IN
                        : Event.KNIGHT_OUT);

        event.setCourseCode(
                isKnightIn
                        ? courseActivity.toUpperCase(Locale.US)
                        : "");

        event.setProfessorName(
                isKnightIn ? professor : "");

        event.setCategory(
                isKnightIn ? "Study" : selectedCategory);

        event.setDescription(description);
        event.setLocation(location);

        event.setStartTime(startTimestamp);
        event.setEndTime(endTimestamp);

        event.setMaxParticipants(maxParticipants);
        event.setCost(cost);
        event.setJoinType(joinType);

        event.setVibe(
                spinnerVibe.getSelectedItem().toString());

        // Prevent duplicate submissions
        buttonCreateEvent.setEnabled(false);
        buttonCreateEvent.setText("Creating...");

        eventRepository.createEvent(event)
                .addOnSuccessListener(this, unused -> {

                    showMessage("Event created successfully!");

                    finish();

                })
                .addOnFailureListener(this, error -> {

                    buttonCreateEvent.setEnabled(true);
                    buttonCreateEvent.setText("Create Event");

                    showMessage(
                            "Unable to create event: "
                                    + error.getMessage());
                });
    }

    private void showMessage(String message) {

        Toast.makeText(
                this, message, Toast.LENGTH_LONG
        ).show();
    }
}
