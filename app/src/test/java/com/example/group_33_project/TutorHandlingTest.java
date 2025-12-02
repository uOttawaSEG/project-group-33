package com.example.group_33_project;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;


@RunWith(MockitoJUnitRunner.class) // mock initialization

//all tests passed
public class TutorHandlingTest {
    @Mock
    private FirebaseFirestore mockDb;
    @Mock
    private TutorRatingCallback mockRatingCallback;
    @Mock
    private CollectionReference mockAccountsCollection;
    @Mock
    private DocumentReference mockTutorDocument;
    @Mock
    private CollectionReference mockTimeSlotsCollection;
    @Mock
    private Query mockQuery;
    @Mock
    private Task<QuerySnapshot> mockGetTask;
    @Mock
    private QuerySnapshot mockQuerySnapshot;
    @Mock
    private Task<Void> mockUpdateTask;


    @Test
    public void tutorAverageRating_calculatesAndUpdatesAverage() {
        try (MockedStatic<FirebaseFirestore> firebaseMock = mockStatic(FirebaseFirestore.class)) {

            firebaseMock.when(FirebaseFirestore::getInstance).thenReturn(mockDb);
            TutorHandling tutorHandling = new TutorHandling();

            // mocking Firestore calls
            when(mockDb.collection("accounts")).thenReturn(mockAccountsCollection);
            when(mockAccountsCollection.document(anyString())).thenReturn(mockTutorDocument);
            when(mockTutorDocument.collection("timeSlots")).thenReturn(mockTimeSlotsCollection);
            when(mockTimeSlotsCollection.whereEqualTo("status", "completed")).thenReturn(mockQuery);
            when(mockQuery.get()).thenReturn(mockGetTask);

            //  provide fake ratings for the query result (ratings 5.0 and 4.0)
            DocumentSnapshot doc1 = mock(DocumentSnapshot.class);
            when(doc1.getDouble("rating")).thenReturn(5.0);
            DocumentSnapshot doc2 = mock(DocumentSnapshot.class);
            when(doc2.getDouble("rating")).thenReturn(4.0);
            List<DocumentSnapshot> docs = Arrays.asList(doc1, doc2);
            when(mockQuerySnapshot.getDocuments()).thenReturn(docs);

            // mock success listener for getting the documents
            doAnswer(invocation -> {
                OnSuccessListener<QuerySnapshot> listener = invocation.getArgument(0);
                listener.onSuccess(mockQuerySnapshot);
                return mockGetTask; // return the task itself
            }).when(mockGetTask).addOnSuccessListener(any());

            // mock update call
            when(mockTutorDocument.update("averageRating", 4.5)).thenReturn(mockUpdateTask);

            // mock the success listener for query
            doAnswer(invocation -> { //doAnswer to simulate .get() success
                OnSuccessListener<Void> listener = invocation.getArgument(0);
                listener.onSuccess(null);
                return mockUpdateTask;
            }).when(mockUpdateTask).addOnSuccessListener(any());// mock update to Firestore document

            Tutor tutor = new Tutor();
            tutor.setEmail("test@example.com");

            tutorHandling.tutorAverageRating(tutor, mockRatingCallback);

            // ArgumentCaptor checks the value passed to the onSuccess method of the callback
            ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);
            verify(mockRatingCallback).onSuccess(captor.capture());

            // assert the captured value is 4.5 (average rating)
            assertEquals(4.5, captor.getValue(), 0.01);

            // verify the update method on Firestore was also called with the correct value
            verify(mockTutorDocument).update("averageRating", 4.5);
        }
    }


    @Test
    public void createNewAvailability_invalidStartDate() {
        try (MockedStatic<FirebaseFirestore> firebaseMock = mockStatic(FirebaseFirestore.class)) {
            firebaseMock.when(FirebaseFirestore::getInstance).thenReturn(mockDb);
            TutorHandling tutorHandling = new TutorHandling();

            Tutor tutor = new Tutor();
            tutor.setEmail("test@example.com");

            //set invalid start date: in the past
            ZonedDateTime start = ZonedDateTime.of(2023, 11, 21, 10, 30, 0, 0, ZoneId.of("UTC"));
            ZonedDateTime end  = ZonedDateTime.of(2023, 11, 21, 11, 30, 0, 0, ZoneId.of("UTC"));

            //create mock call back
            AccountCallback callback = mock(AccountCallback.class);
            //call method with invalid start date
            tutorHandling.createNewAvailability(tutor, start, end, false, callback);
            // Verify that the callback's onFailure method was called with the expected message
            // This ensures that the method correctly rejects invalid timeslots
            verify(callback).onFailure("The start date must be in the future");
        }
    }



}
