# boomi-connector
A Pravega connector for the [Boomi Atomsphere](https://boomi.com/platform/integration/applications/)

# Building
_Note: once the connector is published, it will be available to everyone in the Boomi Platform, and you can skip to the [Custom Connector Library](#custom-connector-library) section._

## Pre-requisites
* Java JDK
* Boomi Connector SDK
* Maven

### Install the Boomi Connector SDK
1. Download and unzip the Boomi Connector SDK (you must have a Boomi account)
1. In the unzipped folder, run the `install-boomi-sdk-local.sh` script (from this repository)

This will use Maven to install the Connector libraries into your local Maven repository.  If you are on Windows, you can run the following commands directly:  
```
mvn install:install-file -Dfile=boomi-util-2.3.8.jar -DgroupId=com.boomi -DartifactId=boomi-util -Dversion=2.3.8 -Dpackaging=jar
mvn install:install-file -Dfile=common-sdk-1.1.7.jar -DgroupId=com.boomi -DartifactId=common-sdk -Dversion=1.1.7 -Dpackaging=jar
mvn install:install-file -Dfile=connector-sdk-api-2.3.0.jar -DgroupId=com.boomi -DartifactId=connector-sdk-api -Dversion=2.3.0 -Dpackaging=jar
mvn install:install-file -Dfile=connector-sdk-util-2.3.0.jar -DgroupId=com.boomi -DartifactId=connector-sdk-util -Dversion=2.3.0 -Dpackaging=jar
mvn install:install-file -Dfile=connector-sdk-test-util-2.3.0.jar -DgroupId=com.boomi -DartifactId=connector-sdk-test-util -Dversion=2.3.0 -Dpackaging=jar
```
(use the appropriate version number)

## Build artifacts
```
./gradlew distZip
```
This will build a connector package which you can upload to your Boomi account. Note that this jar only includes the connector classes and no dependencies. This is because there is a 20MB size limitation on connector package uploads in the Boomi platform site and the connector dependencies total more than that (about 13MB). You will need to upload and create a custom library for the connector in your Boomi account before it will function properly.
```
./gradlew dependencyJar
```
This will build (only for version 1.0.0) a fat jar with all of the dependencies required for the connector to work. Upload this as an account library and create a custom library for the Pravega connector and deploy it to any environment that will run the connector.

# Create a Custom Connector
_Note: once the connector is published, it will be available to everyone in the Boomi Platform, and you can skip to the [Custom Connector Library](#custom-connector-library) section._

If you have just built the Pravega connector above, you must install it in your Boomi account as a custom connector type.  More details about this process can be found [here](https://help.boomi.com/bundle/connectors/page/c-atm-Connector_versioning_and_releasing_4ef53f03-4e3d-4637-9046-aa5f8b9506ba.html).  The basic steps are:

1. Add a connector group
    * Go to your Boomi account setup and under `Development Resources` on the left, click on `Developer`
    * Click on `Add Connector Group` and name it "Pravega"
1. Create a connector version
    * Still in the `Developer` page with the "Pravega" connector group selected, click on `Add Version`
    * Select the connector descriptor file - this is the `src/main/resources/connector-descriptor.xml` file in this repository
    * Select the connector archive file - after building, this is the zip file in `build/distributions`
    * Click `OK`
1. Create a connector
    * Still in the `Developer` page with the "Pravega" connector group selected, click on `Add Connector`
    * Call the connector "Custom Pravega" - you can optionally append " - prod" or " - test" or any other descriptive suffix
    * Give the connector a classification (i.e. "prod", "test", etc.)
    * Click `OK`
    
Now you should have a Pravega connector for your account that you can insert into any process.  However, before you can test or execute any of these processes, you will need to deploy the dependency libraries first (see next section).  

# Custom Connector Library
_Note: this step is necessary because the dependency libraries of this connector (when compressed in a fat jar) are larger than 20MiB, which is a current limit within the Boomi platform. In future, this limit will be increased and this step will no longer be necessary._

Before you can deploy any process that uses the Pravega connector, you must first create and deploy a custom library that contains its dependencies. This is accomplished in 3 steps (the process is described in more detail [here](https://help.boomi.com/bundle/integration/page/c-atm-Working_with_custom_libraries_96f10864-334e-4eba-ac3f-f52b4e65fdb2.html)):

1. Upload dependency jar
    * [download](#dependency-jar-download) the appropriate dependency jar that matches the version of the connector you want to deploy
    * Go to your Boomi account setup and under `Development Resources` on the left, click on `Account Libraries`
    * Click on "Upload a File" and select the dependency jar you downloaded in the above step
1. Create custom connector library
    * In your Boomi account, click on `Build`, then `+ New` and select `Custom Library`
    * Call your custom library something meaningful, like "Pravega Connector Dependencies v2" (use appropriate version)
    * Click `Create`
    * Select the `Connector` library type, and choose the Pravega connector you created above
    * Select the dependency jar that you uploaded
    * Click `Save and Close`
1. Create packaged components
    * In your Boomi account, click on `Deploy`, then below that, select `Packaged Components` and then `Create Packaged Components`
    * Click on the custom library you created from the left panel, then click on `Add Details`
    * Give a version number(optional) and then click on `Create Packaged Components`
1. Deploy the packaged components
    * In your Boomi account, click on `Deploy`, then below that, select `Deployments`
    * Click on `Deploy Packaged Componenet`
    * Select any environments on the right that will execute the Pravega connector
    * Click on the `Select Version`
    * Click on the packaged components you created from the left panel, then click on `Review`
    * Click `Deploy` 

At this point, you should be able to build and execute processes using the Pravega connector, pointing at an accessible Pravega storage cluster.

# Dependency Jar Download

If you require a custom connector library to deploy the Pravega connector (read above), then you can choose the appropriate library to download here.

|Github Release Version|Boomi Platform Version|Download Link|Checksums|
|---:|---:|---|---|
|1.0.0|2|[boomi-pravega-connector-1.0.0-dependencies.jar](https://132173853047869709.public.ecstestdrive.com/pravega-boomi/boomi-pravega-connector-1.0.0-dependencies.jar)|MD5: c604f50683d8df50c38f480bc8ae0fbd<br>SHA1: 845aa1a9c792553d42a01b59587a45f7d279febe<br>SHA256: 35c4e5e8b4ad0ba66d7b2d52218cd77ebd2208952ab22a69a1245fcc5c7e895b|
|2.0.0|2| No dependency jar needed.||
|2.1.0|2|[netty-tcnative-boringssl-static-2.0.17.Final.jar](https://repo1.maven.org/maven2/io/netty/netty-tcnative-boringssl-static/2.0.17.Final/netty-tcnative-boringssl-static-2.0.17.Final.jar)|MD5: 60f88be0fa76bc2d1b89f71725910441<br>SHA1: b1e5acbde8c444c656131238ac6ab9e73f694300<br>SHA256: a71bf392a955bbde8665fc5d44abff8e3fef3dcee74c2821aa5690d7e65bc2be|

# Using the Connector

For more details about how to use the Pravega connector, please refer to the [Boomi documentation](https://help.boomi.com/), and the [User's Guide](doc/Pravega%20Connector%20User's%20Guide.md) and [Getting Started Guide](doc/Pravega%20Connector%20Getting%20Started%20Guide.md) located in this repository.
