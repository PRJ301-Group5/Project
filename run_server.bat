@echo off
echo ===================================================
echo   Starting AI Grading Platform - Java Backend
echo   Database: Microsoft SQL Server (localhost\MAYAO)
echo ===================================================
"C:\Program Files\Java\jdk1.8.0_202\bin\java.exe" "-Djava.library.path=backend/lib" -cp "backend/bin;backend/lib/*" com.aigrading.Main 8080
pause
