@echo off
setlocal
cd /d "%~dp0"

echo Starting WarisanGo Local Discovery on http://127.0.0.1:8081 ...
start "WarisanGo Local Discovery" cmd /k call mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=admin-local"

echo Waiting for the local application to become available...
powershell.exe -NoProfile -Command "$deadline = (Get-Date).AddMinutes(3); while ((Get-Date) -lt $deadline) { try { $response = Invoke-WebRequest -UseBasicParsing -Uri 'http://127.0.0.1:8081/actuator/health' -TimeoutSec 2; if ($response.StatusCode -eq 200) { Start-Process 'http://127.0.0.1:8081/ai-discovery'; exit 0 } } catch { }; Start-Sleep -Seconds 2 }; Write-Error 'Local Discovery did not start within three minutes. Check the WarisanGo Local Discovery window.'; exit 1"

if errorlevel 1 pause
endlocal
