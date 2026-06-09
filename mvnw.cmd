@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, kept small for this project.
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set WRAPPER_JAR=%~dp0.mvn\wrapper\maven-wrapper.jar
set WRAPPER_PROPS=%~dp0.mvn\wrapper\maven-wrapper.properties
set BASE_DIR=%~dp0
set BASE_DIR=%BASE_DIR:~0,-1%

if not exist "%WRAPPER_JAR%" (
  echo Maven Wrapper jar not found: %WRAPPER_JAR%
  exit /b 1
)

java -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" -Dwrapper.conf="%WRAPPER_PROPS%" -cp "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
endlocal
