package com.example.knightinknightout.repositories;

import com.example.knightinknightout.models.Event;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class EventRepository {

    private final FirebaseAuth auth;
    private final CollectionReference events;

    public EventRepository() {
        auth = FirebaseAuth.getInstance();

        FirebaseFirestore db =
                FirebaseFirestore.getInstance();

        events = db.collection("events");
    }

    // CREATE EVENT
    public Task<Void> createEvent(Event event) {

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            return Tasks.forException(
                    new IllegalStateException(
                            "You must be signed in."
                    )
            );
        }

        if (event == null
                || event.getEventName() == null
                || event.getEventName().trim().isEmpty()
                || event.getLocation() == null
                || event.getLocation().trim().isEmpty()
                || event.getStartTime() == null
                || event.getEndTime() == null
                || event.getStartTime().compareTo(
                Timestamp.now()) <= 0
                || event.getEndTime().compareTo(
                event.getStartTime()) <= 0
                || event.getMaxParticipants() < 0
                || event.getCost() < 0
                || !Double.isFinite(event.getCost())) {

            return Tasks.forException(
                    new IllegalArgumentException(
                            "Invalid event information."
                    )
            );
        }

        if (!Event.KNIGHT_IN.equals(event.getEventType())
                && !Event.KNIGHT_OUT.equals(
                event.getEventType())) {

            return Tasks.forException(
                    new IllegalArgumentException(
                            "Invalid event type."
                    )
            );
        }

        if (!Event.OPEN.equals(event.getJoinType())
                && !Event.LIMITED.equals(event.getJoinType())
                && !Event.APPROVAL.equals(event.getJoinType())) {

            return Tasks.forException(
                    new IllegalArgumentException(
                            "Invalid joining permissions."
                    )
            );
        }

        if (Event.LIMITED.equals(event.getJoinType())
                && event.getMaxParticipants() == 0) {

            return Tasks.forException(
                    new IllegalArgumentException(
                            "Limited events require a capacity."
                    )
            );
        }

        DocumentReference document = events.document();

        event.setEventId(document.getId());
        event.setHostUserId(currentUser.getUid());
        event.setParticipantCount(0);
        event.setStatus(Event.ACTIVE);
        event.setCreatedAt(Timestamp.now());

        return document.set(event);
    }

    // GET ALL ACTIVE EVENTS
    public Task<QuerySnapshot> getAllActiveEvents() {

        return events
                .whereEqualTo("status", Event.ACTIVE)
                .get();
    }

    // GET EVENTS BY TYPE
    public Task<QuerySnapshot> getEventsByType(
            String eventType) {

        return events
                .whereEqualTo("eventType", eventType)
                .whereEqualTo("status", Event.ACTIVE)
                .get();
    }

    // GET A SINGLE EVENT
    public Task<DocumentSnapshot> getEventById(
            String eventId) {

        return events.document(eventId).get();
    }

    // GET EVENTS CREATED BY A SPECIFIC USER
    public Task<QuerySnapshot> getEventsByHost(
            String userId) {

        return events
                .whereEqualTo("hostUserId", userId)
                .get();
    }

    // CANCEL AN EVENT
    public Task<Void> cancelEvent(String eventId) {

        if (auth.getCurrentUser() == null) {

            return Tasks.forException(
                    new IllegalStateException(
                            "You must be signed in."
                    )
            );
        }

        return events.document(eventId)
                .update("status", Event.CANCELLED);
    }
}
