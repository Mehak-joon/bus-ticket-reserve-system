@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script for Windows CMD / PowerShell
@REM ----------------------------------------------------------------------------

@IF "%DEBUG%" == "" @ECHO OFF
@SETLOCAL ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION

SET "MAVEN_BASEDIR=%~dp0"
IF "%MAVEN_BASEDIR:~-1%"=="\" SET "MAVEN_BASEDIR=%MAVEN_BASEDIR:~0,-1%"

IF NOT "%JAVA_HOME%" == "" (
  IF EXIST "%JAVA_HOME%\bin\java.exe" (
    SET "JAVACMD=%JAVA_HOME%\bin\java.exe"
    GOTO checkJava
  )
)

SET "JAVACMD=java.exe"

:checkJava
where %JAVACMD% >nul 2>nul
IF ERRORLEVEL 1 (
  echo Error: JAVA_HOME is not defined correctly or java.exe is not found in PATH.
  exit /b 1
)

SET "DIST_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip"
SET "MVN_VERSION=3.9.6"
SET "TARGET_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MVN_VERSION%"
SET "MVN_BIN=%TARGET_DIR%\apache-maven-%MVN_VERSION%\bin\mvn.cmd"

IF NOT EXIST "%MVN_BIN%" (
  echo Maven %MVN_VERSION% not found locally. Downloading...
  if not exist "%TARGET_DIR%" mkdir "%TARGET_DIR%"
  SET "ZIP_FILE=%TARGET_DIR%\apache-maven-%MVN_VERSION%-bin.zip"
  
  powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%ZIP_FILE%'"
  if errorlevel 1 (
    echo Failed to download Maven wrapper zip.
    exit /b 1
  )

  echo Extracting Maven...
  powershell -Command "Expand-Archive -Path '%ZIP_FILE%' -DestinationPath '%TARGET_DIR%' -Force"
  del "%ZIP_FILE%" >nul 2>nul
)

"%MVN_BIN%" %*
