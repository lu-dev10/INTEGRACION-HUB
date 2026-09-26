@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------

@IF "%DEBUG%" == "" @ECHO OFF
@REM set %ENABLE_DELAYED_EXPANSION% to true if you want to use !variable! syntax
setlocal enableextensions

set ERROR_CODE=0

@REM To isolate internal variables from possible post scripts, we use another setlocal
@setlocal

@REM ==== START VALIDATION ====
@REM *** JDK/JRE Check ***
IF "%JAVA_HOME%" == "" (
  where java >nul 2>&1
  IF ERRORLEVEL 1 (
    echo Error: JAVA_HOME not found in your environment. >&2
    echo Please set the JAVA_HOME variable in your environment to match the >&2
    echo location of your Java installation. >&2
    goto error
  )
  set "JAVACMD=java"
) ELSE (
  set "JAVACMD=%JAVA_HOME%\bin\java.exe"
)

IF NOT EXIST "%JAVACMD%" (
  echo Error: JAVA_HOME is set to an invalid directory. >&2
  echo JAVA_HOME = "%JAVA_HOME%" >&2
  echo Please set the JAVA_HOME variable in your environment to match the >&2
  echo location of your Java installation. >&2
  goto error
)

set MAVEN_CMD_LINE_ARGS=%*
set "BASE_DIR=%~dp0"

set "WRAPPER_JAR=%BASE_DIR%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"

"%JAVACMD%" -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" -jar "%WRAPPER_JAR%" %MAVEN_CMD_LINE_ARGS%
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
exit /B %ERROR_CODE%
