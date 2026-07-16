# CD_WarisanGo

Install
- Java JDK 21 (or JDK 17)
- Maven
- Git

verify command
java -version
mvn -version
git --version

how to install maven
Check that Java is installed by running java -version in Command Prompt.
Download the apache-maven-x.x.x-bin.zip file from the Apache Maven Downloads page. link (https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip)
Extract the zip file and move the folder to a permanent location (e.g., C:\Program Files\Apache\Maven).
Search for "Environment Variables" in your Windows search bar and open Edit the system environment variables.
Click Environment Variables.
Under System variables, select the Path variable, click Edit, and add C:\Program Files\Apache\Maven\apache-maven-3.9.16\bin to the list.
Open a new Command Prompt or PowerShell and type mvn -version to verify the installation.

VS code extension
Spring Boot Extension Pack

PS
Spring Boot serves:
static/ for CSS, JavaScript, images, and other static assets.
templates/ for HTML pages rendered by Thymeleaf.

Suggested development order

To minimise dependencies and simplify testing, develop the modules in this sequence:

User Account Management (Google Sign-In, authentication, profiles)
Heritage Business Directory (CRUD, search, filters)
Interactive Map and Navigation (Google Maps integration)
Review and Rating
Reward and Badge
Admin Verification
AI Heritage Discovery (video retrieval, speech-to-text, information extraction)
