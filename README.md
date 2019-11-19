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
This will build a connector package which you can upload to your Boomi account. Note that this jar only includes the connector classes and no dependencies. This is because there is a 10MB size limitation on connector package uploads in the Boomi platform site and the connector dependencies total more than that (about 13MB). You will need to upload and create a custom library for the connector in your Boomi account before it will function properly.
```
./gradlew dependencyJar
```
This will build a fat jar with all of the dependencies required for the connector to work. Upload this as an account library and create a custom library for the Pravega connector and deploy it to any environment that will run the connector.
