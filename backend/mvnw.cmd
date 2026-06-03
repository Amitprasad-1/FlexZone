@echo off
setlocal
set MVN_VERSION=3.9.6
set MVN_DIR=%~dp0.maven
set MVN_HOME=%MVN_DIR%\apache-maven-%MVN_VERSION%

if not exist "%MVN_HOME%" (
    echo Downloading Apache Maven %MVN_VERSION%...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; if (-not (Test-Path '%MVN_DIR%')) { New-Item -ItemType Directory -Path '%MVN_DIR%' | Out-Null }; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/%MVN_VERSION%/binaries/apache-maven-%MVN_VERSION%-bin.zip' -OutFile '%MVN_DIR%\maven.zip'; Expand-Archive -Path '%MVN_DIR%\maven.zip' -DestinationPath '%MVN_DIR%' -Force; Remove-Item '%MVN_DIR%\maven.zip' -Force"
)

"%MVN_HOME%\bin\mvn.cmd" %*
