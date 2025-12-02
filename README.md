# Project Group 33

CircleCI Build Status[![CircleCI](https://dl.circleci.com/status-badge/img/gh/uOttawaSEG/project-group-33/tree/main.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/uOttawaSEG/project-group-33/tree/main)

A full-stack course-tutoring management platform built for the SEG project at the University of Ottawa.  
This system handles tutor scheduling, student interactions, time-slot management, and administrator oversight.

---

## 🛠️ Iterations Summary

- **Iteration 1:** Repository setup, registration, login, welcome screens  
- **Iteration 2:** Administrator approval workflow  
- **Iteration 3:** Tutor availability and scheduling logic  
- **Iteration 4:** Student booking flow, rating, and full integration  

---

## 🔐 Administrator Login (Demo)

Use these credentials when accessing the admin interface during development or grading:

- **Email:** `admin33@seg.com`  
- **Password:** `admin2105`

---

## 🔥 Firebase Project

Backend data and authentication are powered by Firebase.

[![Firebase](https://img.shields.io/badge/Firebase-Console-orange?logo=firebase)](https://console.firebase.google.com/)

---

## ✔️ Automated Test Summary

All core unit tests pass and validate the expected behavior of the system components.

| Test File                  | What It Verifies                                                      | Status |
|----------------------------|-----------------------------------------------------------------------|--------|
| **TutorTest.java**         | Tutor model initialization and field consistency.                     | ✅ Pass |
| **TutorHandlingTest.java** | Tutor-management logic such as adding, updating, and handling data.  | ✅ Pass |
| **TimeSlotTest.java**      | Time-slot creation, validation, and formatting.                       | ✅ Pass |
| **StudentTest.java**       | Student model properties and session/list-modification behavior.      | ✅ Pass |

---

## 📁 Project Structure
```
project-group-33/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/              # Application source code
│   │   │   ├── res/               # XML layouts, drawables, UI components
│   │   │   └── resources/         # Firebase config files
│   │   └── test/                  # Local JUnit tests
│   └── build.gradle
│
├── documentation/
│   ├── diagrams/
│   └── reports/
│
├── README.md
└── build.gradle / settings.gradle
```

