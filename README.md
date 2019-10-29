# boomi-connector
A Pravega connector for the Boomi Atomsphere

## Pre-requisites
* Java JDK
* Boomi Connector SDK
* Maven

### Install the Boomi Connector SDK
1. Download and unzip the Boomi Connector SDK (you must have a Boomi account)
1. In the unzipped folder, run the `install-boomi-sdk-local.sh` script (from this repository)

This will use Maven to install the Connector libraries into your local Maven repository.  If you are on Windows, you can run the following commands directly:  
```
mvn install:install-file -Dfile=connector-sdk-api-1.3.2.jar -DgroupId=com.boomi -DartifactId=connector-sdk-api -Dversion=1.3.2 -Dpackaging=jar
mvn install:install-file -Dfile=connector-sdk-util-1.3.2.jar -DgroupId=com.boomi -DartifactId=connector-sdk-util -Dversion=1.3.2 -Dpackaging=jar
mvn install:install-file -Dfile=connector-sdk-test-util-1.3.2.jar -DgroupId=com.boomi -DartifactId=connector-sdk-test-util -Dversion=1.3.2 -Dpackaging=jar
```
(use the appropriate version number)

## Build
```
./gradlew distZip
```
This will build a connector package which you can upload to your Boomi account.
