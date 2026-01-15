@echo off
set "JAVA_PATH=C:\Users\piodo\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.8.9-hotspot\bin"
if not exist out mkdir out
"%JAVA_PATH%\javac.exe" -encoding UTF-8 -d out src/main/uj/wmii/pwj/battleships/*.java
if %errorlevel% neq 0 (
    pause
    exit /b
)
start "Battleship SERVER" cmd /k ""%JAVA_PATH%\java.exe" -cp out main.uj.wmii.pwj.battleships.BattleshipGame -mode server -port 5000"
timeout /t 2 /nobreak > nul
"%JAVA_PATH%\java.exe" -cp out main.uj.wmii.pwj.battleships.BattleshipGame -mode client -port 5000 -host localhost
pause