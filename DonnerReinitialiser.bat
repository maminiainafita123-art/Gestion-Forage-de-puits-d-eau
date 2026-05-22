@echo off
chcp 65001 > nul
echo ╔════════════════════════════════════════════════════════════╗
echo ║         RÉINITIALISATION DE LA BASE DE DONNÉES             ║
echo ╚════════════════════════════════════════════════════════════╝
echo.
echo ATTENTION : Cette operation va supprimer toutes les donnees !
echo.

set /p CONFIRM="Etes-vous sur de vouloir continuer ? (O/N) : "
if /i not "%CONFIRM%"=="O" (
    echo Annule.
    pause
    exit /b 0
)

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
    echo [ERREUR] psql non trouve
    pause
    exit /b 1
)

REM ============================================
REM RÉINITIALISATION
REM ============================================

echo.
echo [1/2] Suppression de la base existante...
"%PSQL_PATH%" -h %PGHOST% -p %PGPORT% -U %PGUSER% -c "DROP DATABASE IF EXISTS %DATABASE%;"
echo [OK]

echo [2/2] Import du schema...
"%PSQL_PATH%" -h %PGHOST% -p %PGPORT% -U %PGUSER% -f "%SQL_DIR%\forage.sql"
echo [OK]

echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║         RÉINITIALISATION TERMINÉE !                        ║
echo ╚════════════════════════════════════════════════════════════╝
echo.
pause