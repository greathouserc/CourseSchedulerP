package com.firebase;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.model.Course;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutionException;

import com.model.User;

@Service
public class FirebaseService {


    // add queries here
    public User getUserByUid(int uid) {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        DocumentReference docRef = dbFirestore.collection("users").document(String.valueOf(uid));
        ApiFuture<DocumentSnapshot> future = docRef.get();

        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                return document.toObject(User.class);
            } else {
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    // New method to add a course
    public String addCourse(Course course) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        CollectionReference courses = dbFirestore.collection("courses");

        ApiFuture<DocumentReference> future = courses.add(course);

        try {
            return future.get().getId();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }


}
