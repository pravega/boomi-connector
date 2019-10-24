#!/usr/bin/env sh
##########
# Run this in the Boomi SDK distribution directory
##########
THIS_DIR="$(pwd)"
SDK_API_JAR=$(echo "${THIS_DIR}"/connector-sdk-api-?.?.?.jar)
CONNECTOR_VERSION=$(echo "${SDK_API_JAR}" | sed 's/^.*-\([0-9.]*\)\.jar/\1/' )
SDK_UTIL_JAR=$(echo "${THIS_DIR}/connector-sdk-util-${CONNECTOR_VERSION}.jar")
SDK_TEST_UTIL_JAR=$(echo "${THIS_DIR}/connector-sdk-test-util-${CONNECTOR_VERSION}.jar")
BOOMI_UTIL_JAR=$(echo "${THIS_DIR}/boomi-util-${CONNECTOR_VERSION}.jar")

echo "CONNECTOR_VERSION=${CONNECTOR_VERSION}"
echo "SDK_API_JAR=${SDK_API_JAR}"
echo "SDK_UTIL_JAR=${SDK_UTIL_JAR}"
echo "SDK_TEST_UTIL_JAR=${SDK_TEST_UTIL_JAR}"
echo "BOOMI_UTIL_JAR=${BOOMI_UTIL_JAR}"

mvn install:install-file -Dfile="${SDK_API_JAR}" -DartifactId=connector-sdk-api -Dpackaging=jar -Dversion=${CONNECTOR_VERSION} -DgroupId=com.boomi
mvn install:install-file -Dfile="${SDK_UTIL_JAR}" -DartifactId=connector-sdk-util -Dpackaging=jar -Dversion=${CONNECTOR_VERSION} -DgroupId=com.boomi
mvn install:install-file -Dfile="${SDK_TEST_UTIL_JAR}" -DartifactId=connector-sdk-test-util -Dpackaging=jar -Dversion=${CONNECTOR_VERSION} -DgroupId=com.boomi
mvn install:install-file -Dfile="${BOOMI_UTIL_JAR}" -DartifactId=boomi-util -Dpackaging=jar -Dversion=${CONNECTOR_VERSION} -DgroupId=com.boomi
