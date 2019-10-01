# boomi-connector
A Pravega connector for the Boomi Atomsphere

### Build and Run

## Go to lib directory and publish boomi jars to local maven

mvn install:install-file -Dfile=connector-sdk-api-1.3.2.jar -DgroupId=connector.sdk -DartifactId=connector.sdk.api -Dversion=1.3.2 -Dpackaging=jar

mvn install:install-file -Dfile=connector-sdk-util-1.3.2.jar -DgroupId=connector.sdk -DartifactId=connector.sdk.util -Dversion=1.3.2 -Dpackaging=jar

mvn install:install-file -Dfile=connector-sdk-test-util-1.3.2.jar -DgroupId=connector.sdk -DartifactId=connector.sdk.test.util -Dversion=1.3.2 -Dpackaging=jar

mvn install:install-file -Dfile=boomi-util.jar -DgroupId=boomi.util -DartifactId=boomi.util -Dversion=1.3.2 -Dpackaging=jar


