# Project Setup Guide

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

Spring Boot Extension Pack

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


templates/
- Used for HTML pages rendered using Thymeleaf.

Example:

src/main/resources/templates/


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
