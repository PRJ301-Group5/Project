@echo off
%SystemRoot%\System32\chcp.com 65001 > nul 2>&1
echo ===================================================
echo   Running AI Grading Platform - Console CLI App
echo   Track 5: Git Analytics Teamwork Assessor
echo   Database: Microsoft SQL Server (localhost\MAYAO)
echo ===================================================
"C:\Program Files\Java\jdk1.8.0_202\bin\java.exe" "-Dfile.encoding=UTF-8" "-Djava.library.path=backend/lib" -cp "backend/bin;backend/lib/*" com.aigrading.ConsoleApp
pause
