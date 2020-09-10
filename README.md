# boomi-connector
A Pravega connector for the [Boomi Atomsphere](https://boomi.com/platform/integration/applications/)

# Building
_Note: once the connector is published, it will be available to everyone in the Boomi Platform, and you can use it without building or creating a custom connector._

## Pre-requisites
* Java JDK

## Build artifacts
```
./gradlew distZip
```
This will build a connector package which you can upload to your Boomi account (see the [Create a Custom Connector](#custom-connector-library) section below).
# Create a Custom Connector
_Note: once the connector is published, it will be available to everyone in the Boomi Platform, and and you can use it without building or creating a custom connector._

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
    
Now you should have a Pravega connector for your account that you can insert into any process.
# Using the Connector

For more details about how to use the Pravega connector, please refer to the [Boomi documentation](https://help.boomi.com/), and the [User's Guide](doc/Pravega%20Connector%20User's%20Guide.md) and [Getting Started Guide](doc/Pravega%20Connector%20Getting%20Started%20Guide.md) located in this repository.
