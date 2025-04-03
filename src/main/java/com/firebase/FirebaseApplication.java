package com.firebase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.model.User;

@SpringBootApplication
public class FirebaseApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(FirebaseApplication.class, args);
        FirebaseService firebaseService = context.getBean(FirebaseService.class);

        User user = firebaseService.getUserByUid(0);
        if (user != null) {
            System.out.println("User found: " + user);
        } else {
            System.out.println("User not found");
        }
    }
}
