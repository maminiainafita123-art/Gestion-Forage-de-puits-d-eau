@echo off
chcp 65001 > nul
echo ╔════════════════════════════════════════════════════════════╗
echo ║         IMPORTATION DES DONNÉES DE TEST                    ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

REM ============================================
REM CONFIGURATION
REM ============================================
set PSQL_PATH=C:\Program Files\PostgreSQL\17\bin\psql.exe
set PGUSER=postgres
set PGPASSWORD=123
set PGHOST=localhost
set PGPORT=5432
set DATABASE=forage

set SQL_DIR=%~dp0sql

REM ============================================
REM VÉRIFICATIONS
REM ============================================

if not exist "%PSQL_PATH%" (
    echo [ERREUR] psql non trouve a : %PSQL_PATH%
    pause
    exit /b 1
)

if not exist "%SQL_DIR%\DonnerEntre.sql" (
    echo [ERREUR] Fichier non trouve : %SQL_DIR%\DonnerEntre.sql
    pause
    exit /b 1
)

REM ============================================
REM IMPORT
REM ============================================

echo Import des donnees dans la base %DATABASE%...
echo.

"%PSQL_PATH%" -h %PGHOST% -p %PGPORT% -U %PGUSER% -d %DATABASE% -f "%SQL_DIR%\DonnerEntre.sql"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERREUR] Erreur lors de l'import
    pause
    exit /b 1
)

echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║         IMPORTATION TERMINÉE AVEC SUCCÈS !                 ║
echo ╚════════════════════════════════════════════════════════════╝
echo.
pause