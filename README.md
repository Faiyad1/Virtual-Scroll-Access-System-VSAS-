# Virtual Scroll Access System (VSAS)

**SOFT2412 - Agile Software Development Practices**  
The University of Sydney  
Assignment 2 – Agile Software Development with Scrum and Agile Tools  
25 October 2024

<mark>**Academic Integrity:** This is an group assignment. The use of AI tools (e.g., ChatGPT, GitHub Copilot) is not permitted.</mark>

**Group Members:**
- Faiyad Ahmed, The University of Sydney
- Varrent Nathaniel Woodrow, The University of Sydney
- Achira Tantisuwannakul, The University of Sydney
- Po-An Lin, The University of Sydney

A JavaFX desktop application for managing and accessing digital scrolls ("binary files") with user authentication and role-based access control. Developed using Scrum methodology over 3 sprints with CI/CD practices.

## Development Methodology

This project was developed using Scrum methodology:
- **Sprint 0**: Team formation, role assignment, initial setup and user stories
- **Sprint 1**: Core user management and authentication
- **Sprint 2**: Scroll management features
- **Sprint 3**: Admin features, search functionality, and final polish

CI/CD tools used: GitHub, Gradle, JUnit, Jenkins, Docker

## Technology Stack

- **Language**: Java 21
- **UI Framework**: JavaFX 20
- **Database**: MongoDB
- **Build Tool**: Gradle 8.10
- **Testing**: JUnit 5
- **Code Coverage**: JaCoCo
- **CI/CD**: Jenkins
- **Containerization**: Docker

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [MongoDB Configuration](#mongodb-configuration)
- [Building and Running](#building-and-running)
- [Testing & Code Coverage](#testing--code-coverage)
- [Usage](#usage)
- [Logs](#logs)
- [Database Design](#database-design)
- [Project Structure](#project-structure)
- [Sample Assets](#sample-assets)

## Features

### User Management
- **User Registration**: Create accounts with username, email, phone, and password
- **User Authentication**: Secure login with encrypted passwords
- **Profile Management**: Update personal details and profile pictures
- **Password Management**: Change passwords with confirmation

### Role-Based Access Control
- **Guest**: Browse and preview scrolls (read-only)
- **Member**: Full scroll management (upload, download, edit, delete own scrolls)
- **Admin**: User management, view all user profiles, access activity logs

### Scroll Management
- **Upload**: Add new scrolls (text files) to the system
- **Download**: Save scrolls to local storage
- **Preview**: View scroll contents without downloading
- **Edit**: Modify scroll content and rename
- **Delete**: Remove owned scrolls
- **Search**: Filter scrolls by uploader, ID, title, or upload date

### Admin Features
- View list of all members
- View detailed user profiles (uploads/downloads count)
- Delete user accounts
- Add new users
- View activity logs

## Prerequisites

- Java 21 or higher
- MongoDB instance running
- Gradle 8.10+

## MongoDB Configuration

> **IMPORTANT**: You must configure the MongoDB connection before running the application.

The connection string is hardcoded in `app/src/main/java/VirtualScrollAccessSystem/Database.java`. You must update the URL and database names to match your MongoDB cluster setup.

## Building and Running

> **Note**: Use `.\gradlew` on Windows and `./gradlew` on macOS/Linux.

### Building
```bash
./gradlew build
```

### Running
```bash
./gradlew :app:run
```

## Testing & Code Coverage

### Run tests
```bash
./gradlew :app:test
```

### Generate test coverage report
```bash
./gradlew :app:jacocoTestReport
```
Coverage reports are generated at `app/build/reports/jacoco/test/html/index.html`

## Usage

1. **Launch**: Run the application to see the login screen
2. **Guest Access**: Click "Guest" to browse scrolls without an account
3. **Sign Up**: Create a new account with valid credentials
4. **Login**: Enter username and password to access member features
5. **Browse Scrolls**: View all available scrolls on the home screen
6. **Search**: Use the search fields to filter scrolls
7. **Manage Scrolls**: Upload, edit, or delete your own scrolls via "My Scrolls"
8. **Admin Panel**: Admin users can manage members and view logs

## Logs

Activity logs are appended to `app/src/main/resources/log.txt` when using the production collections.

## Database Design

All data is stored in MongoDB.

### User Profiles
| Field              | Type   | Description                 |
|--------------------|--------|-----------------------------|
| _id                | String | Unique user ID              |
| full_name          | String | User's full name            |
| email              | String | User's email address        |
| phone              | String | Phone number                |
| uploaded_scrolls   | Int64  | Count of uploaded scrolls   |
| downloaded_scrolls | Int64  | Count of downloaded scrolls |
| encrypted_password | String | Encrypted password          |
| type               | String | User role (Member/Admin)    |

### Scrolls (GridFS)
| Field        | Type     | Description                 |
|--------------|----------|-----------------------------|
| scroll_id    | ObjectId | Unique scroll identifier    |
| name         | String   | Scroll name                 |
| uploader_id  | String   | ID of the uploading user    |
| downloads    | Int64    | Download count              |
| uploaded     | Date     | Upload timestamp            |
| last_updated | Date     | Last modification timestamp |

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/VirtualScrollAccessSystem/
│   │   │   ├── App.java           # Application entry point
│   │   │   ├── Controller.java    # JavaFX UI controller
│   │   │   ├── Login.java         # User authentication logic
│   │   │   ├── Scroll.java        # Scroll management logic
│   │   │   ├── Admin.java         # Admin functionality
│   │   │   ├── Database.java      # MongoDB connection
│   │   │   └── Encryptor.java     # Password encryption
│   │   └── resources/
│   │       ├── *.fxml             # JavaFX UI layouts
│   │       └── log.txt            # Activity log file
│   └── test/
│       └── java/VirtualScrollAccessSystem/
│           ├── AppTest.java
│           ├── LoginTest.java
│           ├── ScrollTest.java
│           ├── AdminTest.java
│           └── EncryptorTest.java
└── build.gradle
```

## Sample Assets

- Sample scrolls: `app/src/main/resources/SampleScrolls`
- Sample profile pictures: `app/src/main/resources/SamplePics`
