# Project Setup Guide

## WarisanGo Introduction

WarisanGo is a Spring Boot web application for discovering Malaysian heritage food businesses. Its AI discovery
workflow searches YouTube and TikTok, downloads video media, converts speech to text with AssemblyAI, extracts
structured business information with Gemini, and stores approved records in Firebase Cloud Firestore.

## Prerequisites

Install these tools before running the project:

- Java Development Kit (JDK) 21
- Git
- Maven 3.9 or use the included Maven Wrapper (`mvnw.cmd` on Windows)
- `yt-dlp`, available on the system `PATH`, for downloading and extracting YouTube audio
- FFmpeg, available on the system `PATH`, for media conversion
- Playwright Chromium for TikTok search and video downloading

You will also need:

- A Firebase project and Firebase service-account JSON file
- A YouTube Data API v3 key
- An AssemblyAI API key
- A Google Gemini API key

## Quick Setup

1. Clone the repository and enter its directory:

   ```powershell
   git clone <repository-url>
   cd CD_WarisanGo
   ```

2. Confirm that the required command-line tools are available:

   ```powershell
   java -version
   .\mvnw.cmd -version
   git --version
   yt-dlp --version
   ffmpeg -version
   ```

3. Install `yt-dlp` and FFmpeg if they are unavailable. Both executables must be accessible through `PATH`.

4. Download the Playwright Chromium browser used by the TikTok services:

   ```powershell
   .\mvnw.cmd exec:java "-Dexec.mainClass=com.microsoft.playwright.CLI" "-Dexec.args=install chromium"
   ```

5. Download a Firebase Admin SDK service-account key from your Firebase project and save it locally as:

   ```text
   src/main/resources/firebase-service-account.json
   ```

   This file is ignored by Git and must never be committed or shared.

6. Configure API keys through environment variables. Reference them from `application.properties` as follows:

   ```properties
   youtube.api.key=${YOUTUBE_API_KEY}
   youtube.base.url=https://www.googleapis.com/youtube/v3
   assemblyai.api.key=${ASSEMBLYAI_API_KEY}
   assemblyai.api.url=https://api.assemblyai.com/v2
   gemini.api.key=${GEMINI_API_KEY}
   gemini.model=gemini-3.6-flash
   ```

   Then define `YOUTUBE_API_KEY`, `ASSEMBLYAI_API_KEY`, and `GEMINI_API_KEY` in the IDE run configuration or the
   operating system environment. Never commit real API keys.

7. Build and run the application:

   ```powershell
   .\mvnw.cmd clean test
   .\mvnw.cmd spring-boot:run
   ```

8. Open `http://localhost:8080` in a browser.

The first TikTok operation opens a visible Chromium window and automatically creates `.tiktok-profile/`. Complete
any TikTok login or CAPTCHA challenge manually. The folder retains the local browser session to reduce repeated
verification and is ignored by Git.

## 1. Required Software Installation

Before running the project, install the following software:

- Java JDK 21 (or JDK 17)
- Apache Maven
- Git


## 2. Verify Installation

After installation, open Command Prompt or PowerShell and run the following commands:

java -version
mvn -version
git --version

The output should display the installed Java version, Maven version, and Git version.


## 3. Install Apache Maven (Windows)

Step 1: Check Java Installation

Verify that Java is installed by running:

java -version

If Java is not installed, install Java JDK 21 or JDK 17 first.


Step 2: Download Apache Maven

Download the Apache Maven binary zip file from the official Maven download page:

https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip


Step 3: Extract Maven

Extract the downloaded apache-maven-x.x.x-bin.zip file.

Move the extracted folder to a permanent location.

Example:

C:\Program Files\Apache\Maven

The final Maven installation path should be:

C:\Program Files\Apache\Maven\apache-maven-3.9.16


Step 4: Configure Environment Variables

1. Search for "Environment Variables" in the Windows search bar.
2. Open "Edit the system environment variables".
3. Click "Environment Variables".
4. Under System Variables, select the Path variable.
5. Click Edit and add the Maven bin directory:

C:\Program Files\Apache\Maven\apache-maven-3.9.16\bin

6. Click OK to save the changes.


Step 5: Verify Maven Installation

Open a new Command Prompt or PowerShell window and run:

mvn -version

The output should display the installed Maven version.


## 4. Recommended VS Code Extensions

Install the following extension:

- Spring Boot Extension Pack
- Java Extension Pack

This extension provides support for:

- Spring Boot project management
- Spring Initializr
- Spring Boot Dashboard
- Java development tools


## 5. Spring Boot Project Structure

Spring Boot uses the following folders for web development:

static/
- Used for CSS, JavaScript, images, and other static resources.

Example:

src/main/resources/static/


view/
- Used for HTML pages rendered using Thymeleaf.

Example:

src/main/resources/view/


## 6. Recommended Development Order

To minimise dependencies and simplify testing, develop the system modules in the following sequence:

1. User Account Management

Functions:
- Google Sign-In
- User authentication
- User profile management
- Account-related functions


2. Heritage Business Directory

Functions:
- Create, Read, Update, Delete (CRUD)
- Search heritage businesses
- Filtering functions
- Heritage business information management


3. Interactive Map and Navigation

Functions:
- Google Maps integration
- Location display
- Navigation support
- Heritage business mapping


4. Review and Rating

Functions:
- User reviews
- Rating system
- Feedback management


5. Reward and Badge System

Functions:
- Reward point calculation
- User achievements
- Badge collection


6. Admin Verification

Functions:
- Review submitted heritage records
- Approve or reject heritage listings
- Manage system content


7. AI Heritage Discovery

Functions:
- Video retrieval
- Speech-to-text conversion
- AI-based information extraction
- Automatic heritage data generation
- Database record creation
