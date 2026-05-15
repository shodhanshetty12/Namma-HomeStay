@ECHO OFF
SETLOCAL

SET DIRNAME=%~dp0
IF "%DIRNAME%"=="" SET DIRNAME=.
SET APP_BASE_NAME=%~n0
SET APP_HOME=%DIRNAME%

SET CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar;%APP_HOME%\gradle\wrapper\gradle-wrapper-shared.jar;%APP_HOME%\gradle\wrapper\gradle-cli.jar

IF NOT "%JAVA_HOME%"=="" GOTO findJavaFromJavaHome

SET JAVA_EXE=java.exe
GOTO execute

:findJavaFromJavaHome
SET JAVA_HOME=%JAVA_HOME:"=%
SET JAVA_EXE=%JAVA_HOME%\bin\java.exe

:execute
"%JAVA_EXE%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

ENDLOCAL
