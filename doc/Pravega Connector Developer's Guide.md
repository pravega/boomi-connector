# boomi-pravega-connector
A Pravega connector for the [Boomi Atomsphere](https://boomi.com/platform/integration/applications/)

## Pre-requisites
  - **Accounts**
    - A boomi platform account
  - **Tools**
    - Java 11
    - An SDP installation
    - kubectl client configured to access the SDP cluster
    - A docker installation for running boomi Atoms


## Steps to configure Boomi Platform
1. Create a boomi platform account and save the credentials
2. Create a boomi environment on your boomi account. This environment will be used to test processes using the connector.
3. Create a local boomi Atom using the official boomi Atom docker image. Use the following command to create a boomi Atom running inside docker
   - ```sh 
     docker run --privileged -p 9090:9090 -h localhost -e BOOMI_USERNAME=<your-boomi-account-username> -e BOOMI_PASSWORD=<your-boomi-account-password> -e BOOMI_ACCOUNTID=<your-boomi-accountID> -e BOOMI_ATOMNAME="Pravega Boomi Atom" -e ATOM_LOCALHOSTID="Pravega Boomi Atom ID" -v /opt/boomi:/run:Z --rm -it boomi/atom:3.2.12
     ```
   - This will run a local boomi Atom docker container on your machine
4. On the boomi environments page, you should see a running boomi Atom named 'Pravega Boomi Atom' in the list of 'Unattached Atoms'. Click on the atom and attach it to the environment created earlier.

***Note:** Since SDP uses self-signed certificates across the installation, your local boomi Atom may not be able to connect to SDP Pravega. Either use valid a set of certificates or create a custom boomi Atom docker image with the mounted self-signed certificates. Refer to the [documentation](#create-a-custom-docker-image) on steps to generate a docker image.*

## Steps to create and upload a custom boomi connector to boomi platform
- Please refer to the section 'Create a Custom Connector' under the [boomi connector](../README.md)


## Steps to get details from SDP cluster to connect with boomi Platform
- Create a project named `boomi`
- Fetch SDP Pravega Endpoint
  - ```shell
    kubectl get ingress -n nautilus-pravega pravega-controller
    ```
  - Output of the above command should give you a URL pointing to the pravega-controller
- Fetch SDP Pravega Keycloak OIDC (Keycloak JSON)
  - ```shell
    kubectl get secrets -n boomi boomi-ext-pravega -o jsonpath='{.data.keycloak\.json}' | base64 -d
    ```
  - The output of the above command to be used as the value for `Keycloak OIDC` in the boomi connector

## Steps to create process on boomi platform
- Please refer to the [Getting Started Guide](Pravega%20Connector%20Getting%20Started%20Guide.md) for creating process on Boomi with boomi-pravega-connectors

## Create a custom docker image with self-signed certificates
- Obtain the certificates you want to add to the Linux/JVM trust store
- Place the certificates in the directory `/usr/share/ca-certificates`
- Run `dpkg-reconfigure ca-certificates` (This may require you to have admin privileges)
- Select the certificates you want to import
- Once finished, the command will update the system-wide ca-bundle located at `/etc/ssl/certs/ca-bundle.crt`
- Copy the ca-bundle to the same directory as your Dockerfile
- Add the ca-bundle to your Dockerfile and move it to `/etc/ssl/certs/ca-bundle.crt`
  - ```shell
    COPY ca-bundle.crt .
    RUN mv ca-bundle.crt /etc/ssl/certs/ca-bundle.crt
    ```
- To update the JVM trust store, add the following command to your Dockerfile, this will update the java keystore in your docker image with the certificates provided
  - ```shell
    RUN keytool -importcert -keystore /usr/java/latest/lib/security/cacerts -storepass changeit -noprompt -trustcacerts -file /etc/ssl/certs/ca-bundle.crt
    ```
- Build the docker image!