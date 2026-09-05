# WarisanGo Administrator Manual

## 1. Purpose

This manual explains how to configure, deploy, and operate WarisanGo. The system uses two environments:

- **Render production** serves tourists and the regular administration pages.
- **Local Discovery** performs YouTube/TikTok search, audio extraction, transcription, and AI extraction on an
  administrator's Windows computer.

Both environments connect to the same Firebase project. Locally discovered businesses are saved as pending records
and reviewed through the deployed administration site.

## 2. Architecture

| Environment | Address | Responsibilities |
| --- | --- | --- |
| Render | `https://warisango.onrender.com` | Tourist site, login, pending list, approval, audit log, reports, badges and challenges |
| Local Discovery | `http://127.0.0.1:8081` | Video search, yt-dlp, FFmpeg, AssemblyAI, Gemini and pending-record creation |

The Render AI Discovery page links to Local Discovery. The local launcher must be running before that link works.
A browser cannot directly start a Windows command file.

## 3. Required Access

The administrator needs access to:

- The WarisanGo Git repository
- The Render service
- The Firebase project
- A YouTube Data API v3 key
- An AssemblyAI API key
- A Google Gemini API key
- A Google account assigned the WarisanGo `ADMIN` role

Never commit API keys, Firebase credentials, cookies, passwords, or tokens.

## 4. Install Local Tools

Install these tools on the Windows computer that runs Local Discovery:

1. Java Development Kit 21
2. Git
3. yt-dlp
4. FFmpeg
5. Deno
6. Maven, or the included Maven Wrapper
7. Playwright Chromium for TikTok discovery

Open a new PowerShell window and verify:

```powershell
java -version
git --version
yt-dlp --version
ffmpeg -version
deno --version
.\mvnw.cmd -version
```

Java and Maven must use Java 21. The other commands must be available through the Windows `PATH`.

Install Playwright Chromium from the project directory when TikTok discovery is required:

```powershell
.\mvnw.cmd exec:java "-Dexec.mainClass=com.microsoft.playwright.CLI" "-Dexec.args=install chromium"
```

## 5. Application Configuration

| File | Purpose | Commit |
| --- | --- | --- |
| `application.properties` | Shared defaults | Yes |
| `application-prod.properties` | Render overrides | Yes |
| `application-dev.properties` | Development overrides | Yes |
| `application-admin-local.properties` | Local Discovery settings | Yes |
| `application-local.properties` | Optional private overrides | No |

Committed property files must contain environment-variable references, not real credentials.

## 6. Configure Windows Environment Variables

Open **Edit environment variables for your account**. Under **User variables**, configure:

| Variable | Required | Purpose |
| --- | --- | --- |
| `YOUTUBE_API_KEY` | Yes | YouTube search |
| `ASSEMBLYAI_API_KEY` | Yes | Speech-to-text |
| `GEMINI_API_KEY` | Yes | Business-information extraction |
| `FIREBASE_PROJECT_ID` | Yes | Shared Firebase project |
| `FIREBASE_STORAGE_BUCKET` | Yes | Shared Firebase Storage bucket |

Close and reopen the terminal and IDE after changing variables. Existing processes do not receive new values.

Check configuration presence without displaying secrets:

```powershell
@(
  'YOUTUBE_API_KEY',
  'ASSEMBLYAI_API_KEY',
  'GEMINI_API_KEY',
  'FIREBASE_PROJECT_ID',
  'FIREBASE_STORAGE_BUCKET'
) | ForEach-Object {
  $configured = -not [string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($_, 'User'))
  "$_ configured: $configured"
}
```

## 7. Configure Firebase Locally

1. Open Firebase Console and select WarisanGo.
2. Open **Project settings > Service accounts**.
3. Obtain an authorized service-account JSON file.
4. Save it as:

   ```text
   src/main/resources/firebase-service-account.json
   ```

This file is ignored by Git. If it is stored elsewhere, set:

```text
FIREBASE_CREDENTIALS_LOCATION=file:C:/secure/warisango/firebase-service-account.json
```

## 8. Optional YouTube Cookies

Configure cookies only when yt-dlp reports that authentication is required:

1. Open a new private/incognito browser window.
2. Sign in to YouTube and confirm the target video plays.
3. Open `https://www.youtube.com/robots.txt` in the same window.
4. Export YouTube cookies in Netscape `cookies.txt` format.
5. Close the complete private browser session immediately.
6. Store the file outside the Git repository.
7. Set `YOUTUBE_COOKIES_FILE` to its absolute path.

Cookies are sensitive account credentials. Replace them only when yt-dlp reports that they expired. Stop repeated
requests when YouTube returns HTTP 429. (Unessary for backup plan)

## 9. Start Local Discovery

Double-click `start-local-discovery.cmd`, or run:

```powershell
.\start-local-discovery.cmd
```

The launcher:

1. Starts Spring Boot with profile `admin-local`.
2. Binds only to `127.0.0.1:8081`.
3. Waits for the application to respond.
4. Opens `http://127.0.0.1:8081/ai-discovery`.

Keep the **WarisanGo Local Discovery** command window open. Press `Ctrl+C` in that window to stop it safely.

## 10. Daily Discovery Procedure

1. Start Local Discovery.
2. Sign in with a WarisanGo administrator account.
3. Search for a heritage-food video.
4. Select **Process Video** and keep the application open.
5. Wait for audio extraction, transcription, AI extraction, and Firestore persistence.
6. Open the Render administration site.
7. Review the new record in **Pending List**.
8. Approve, correct, or reject it.

Only approved businesses are shown to tourists.

## 11. Configure Render

Render must define:

```text
SPRING_PROFILES_ACTIVE=prod
```

The startup log must say the `prod` profile is active. Render also requires:

```text
FIREBASE_ENABLED=true
FIREBASE_PROJECT_ID=<project ID>
FIREBASE_STORAGE_BUCKET=<bucket>
FIREBASE_CREDENTIALS_LOCATION=file:/etc/secrets/firebase-service-account.json
```

Keep the Render secret file `firebase-service-account.json`. After moving discovery locally, Render does not require
`YOUTUBE_COOKIES_FILE` or `youtube-cookies.txt`. YouTube, AssemblyAI, and Gemini keys can also be removed from Render
when no other deployed feature uses them.

## 12. Files That Must Remain Private

Do not commit:

```text
.env
application-local.properties
firebase-service-account.json
youtube-cookies.txt
cookies.txt
*.pem
*.p12
*.pfx
```

The shared profile files and `start-local-discovery.cmd` should be committed.

## 13. Troubleshooting

### Local server does not start

Check the launcher window. Confirm Java 21, the Firebase credential, and the required environment variables.

### Render button does not open Local Discovery

Start the launcher first, then open `http://127.0.0.1:8081/heritage-gate` directly.

### Port 8081 is occupied

Stop the previous launcher with `Ctrl+C`, or inspect the port:

```powershell
Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue
```

### yt-dlp cannot find Deno or FFmpeg

Open a new terminal and verify:

```powershell
deno --version
ffmpeg -version
yt-dlp --version
```

Correct the Windows `PATH` if a command is unavailable.

### YouTube returns HTTP 403

Update yt-dlp and verify Deno. Export fresh cookies only when the log reports invalid or missing authentication.

### YouTube returns HTTP 429

Stop retrying and wait. HTTP 429 means YouTube is rate-limiting the network address.

### An expired processing job appears

Open browser developer tools and run:

```javascript
localStorage.removeItem('warisango.activeDiscoveryJob');
location.reload();
```

Discovery jobs are currently held in application memory and do not survive a local server restart.

## 15. Security Checklist

- Run Local Discovery only on a trusted administrator computer.
- Keep it bound to `127.0.0.1` and do not forward port 8081.
- Protect the Firebase service-account file with Windows permissions.
- Do not use a primary personal Google account for automated processing.
- Rotate credentials immediately if they are accidentally committed or shared.
- Process only content that WarisanGo is authorized to access and use.
