@echo off
setlocal

set ALLURE_BIN=.allure\allure-2.24.0\bin\allure.bat
set RESULTS_DIR=target\allure-results
set REPORT_DIR=target\allure-report
set HISTORY_STORE=allure-history

if not exist "%ALLURE_BIN%" (
    echo ERROR: Allure CLI not found at %ALLURE_BIN%
    echo Run `mvn test` at least once so the allure-maven plugin downloads the CLI.
    pause
    exit /b 1
)

echo [1/3] Generating Allure report...
call "%ALLURE_BIN%" generate "%RESULTS_DIR%" --clean -o "%REPORT_DIR%"

if errorlevel 1 (
    echo ERROR: Allure report generation failed.
    pause
    exit /b 1
)

echo [2/3] Saving history for TREND on next run...
if not exist "%HISTORY_STORE%" mkdir "%HISTORY_STORE%"
if exist "%REPORT_DIR%\history" (
    xcopy /E /Y /Q "%REPORT_DIR%\history\*" "%HISTORY_STORE%\" >nul 2>&1
    echo     Saved %REPORT_DIR%\history  --^>  %HISTORY_STORE%\
) else (
    echo     No history yet - normal on the very first run.
)

echo [3/3] Starting local server and opening report in browser...
echo     (Press Ctrl+C in this window to stop the server when done)
echo.
call "%ALLURE_BIN%" open "%REPORT_DIR%"

endlocal
