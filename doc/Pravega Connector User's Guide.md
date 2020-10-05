# Pravega Boomi Connector

The Pravega Boomi connector allows you to read from or write to an enterprise data stream within Praveaga, taking advantage of Boomi&#39;s unique capability to provide a broad range of data source connectors and point them at Pravega for ingestion, or exposing stream data to the wealth of controls and outputs within the Boomi platform.

Pravega is a storage system that exposes  **Stream**  as the main primitive for continuous and unbounded data. A Pravega stream is a durable, elastic, append-only, unbounded sequence of bytes having good performance and strong consistency.

## Pravega Terms

The glossary of terms related to Pravega is given below, and also located [here](http://pravega.io/docs/latest/terminology/).  Understanding these terms is necessary to configure the Pravega connector.

[Stream](http://pravega.io/docs/latest/pravega-concepts/#streams):

Pravega organizes data into Streams. A Stream is a durable, elastic, append-only, unbounded sequence of bytes having good performance and strong consistency. A Pravega Stream is similar to but more flexible than a &quot;topic&quot; in popular message oriented middleware such as [RabbitMQ](https://www.rabbitmq.com/) or [ApacheKafka](https://kafka.apache.org/).

A Stream is unbounded in size – Pravega itself does not impose any limits on how many [Events](http://pravega.io/docs/latest/pravega-concepts/#events) (i.e., bytes) are stored in a Stream. Pravega&#39;s design horizontally scales from few machines to a whole datacenter&#39;s capacity.

[Events:](http://pravega.io/docs/latest/pravega-concepts/#events)

Pravega&#39;s client API allows applications to write and read data to/from Pravega in the form of **Events**. An Event is represented as a set of bytes within a Stream. For example, an Event could be as simple as a small number of bytes containing a temperature reading from an IoT sensor composed of a timestamp, a metric identifier and a value.

[Routing Key:](http://pravega.io/docs/latest/pravega-concepts/#events)

A Routing Key is a string used by developers to group similar Events. A Routing Key is often derived from data naturally occurring in the Event, like &quot;customer-id&quot; or &quot;machine-id&quot; or a declared/user-defined string. For example, a Routing Key could be a date (to group Events together by time) or it could be a IoT sensor id (to group Events by machine). A Routing Key is important in defining the read and write semantics that Pravega guarantees.

[Pravega Writer:](http://pravega.io/docs/latest/pravega-concepts/#writers-readers-reader-groups)

An application that creates Events and writes them into a Stream. All data is written by appending to the tail of a Stream.

[Pravega Reader](http://pravega.io/docs/latest/pravega-concepts/#writers-readers-reader-groups):

An application that reads Events from one or more Streams.  An example of a basic Pravega reader and writer application is located [here](http://pravega.io/docs/latest/basic-reader-and-writer/).

[Pravega Modes:](http://pravega.io/docs/latest/deployment/deployment/)

There are two modes for running Pravega.

- **Standalone** - Standalone mode is suitable for development and testing Pravega applications. It can either be run from the source code, from the distribution package or as a docker container.

- **Distributed** - Distributed mode runs each component separately on a single or multiple nodes. This is suitable for production in addition for development and testing. The deployment options in this mode include a manual installation, running in a docker swarm or DC/OS.

## Prerequisites

To use the Pravega connector, you must first have a running Pravega storage cluster. For more information on how to obtain and deploy Pravega, you can visit the Pravega website ([https://pravega.io](https://pravega.io/)).

### Pravega Security Configuration

For information on how to configure Pravega for secure connections, please visit [Pravega Security Configuration](https://github.com/pravega/pravega/blob/master/documentation/src/docs/security/pravega-security-configurations.md).

## Connector Configuration

To configure a connector to communicate with Pravega, you can provide the following options. The terminology for these options is described in more detail above.

_Connector Options_

- **URI** : (Required) The URI endpoint of the Pravega controller in the form tcp://host:port or tls://host:port
- **Scope** : (Required) The Pravega scope containing the data stream.
- **Stream Name** : (Required) The name of the data stream to read or write.
- **Create Scope** : When checked, Pravega scope will be automatically created. Only enable this if Pravega is running in stand-alone mode.
- **Pravega Authentication Type**: Specifies the type of authentication to use with Pravega. "None" means Pravega does not have any authentication enabled (only suitable for development environments). "Basic" means Pravega is running in an independent cluster and has basic auth enabled - this option requires you specify a valid username and password. "Keycloak" means Pravega is integrated with Keycloak (i.e. inside Streaming Data Platform) - this option requires you specify a valid Keycloak OIDC JSON installation file, including Keycloak endpoint and credentials. This is obtained from the Keycloak server.
- **Basic Username**: An authorized Pravega user. Only used when Authentication Type is set to "Basic".
- **Basic Password**: The Pravega user's password. Only used when Authentication Type is set to "Basic".
- **Keycloak OIDC**: A valid Keycloak OIDC JSON installation file, including Keycloak endpoint and credentials. This is obtained from the Keycloak server.
- **Listener Polling Interval** : The Listen operation uses a polling schedule.  This parameter specifies the interval between each poll.  For example, specifying 1 minute means that any Listen operations from this connector would poll for new events every minute.
- **Listener Polling Time Unit** : The time unit to use for the Listener Polling Interval.  For example, if the Polling Interval is 1 and the Time Unit is Minute, then the Listen operation would poll for new events every 1 Minute.

## Supported Versions

The Pravega connector currently supports Pravega v0.6 and v0.7. It should also support 0.8 once available.

## Supported Documents

The current version of Pravega Boomi connector supports only JSON documents.

## Tracked Properties

This connector has the following tracked properties that you can set or reference:

- Stream Name: The name of the stream to read and write. A stream name must be unique within a scope.
- Scope: A namespace for Streams.

## Pravega Connector Operations

The Pravega connector operations define how to interact with Pravega. An operation represents a specific action, such as Read and Write to be performed against a data stream in a Pravega system.

These operations use JSON format and support the following actions:

- Outbound: Write
- Inbound: Read
- Inbound: Listen

## Write

Write is an outbound action to write streaming data in Pravega. All the options are discussed in the Connector Configuration section.

- The user can specify the Stream name and, Scope name in a Pravega Connection.
- User needs to specify which routing key types they will use.
- For each document process, the data will be written as an event.

_Write Operation Options_

- **Routing Key Type** : The type of routing key to use for each event. &quot;Fixed&quot; means to use the literal value of the Routing Key below. &quot;JsonReference&quot; means the event data is JSON and the value will be extracted from the body using the Routing Key below as a JSON reference (i.e. a value of &quot;myVar&quot; will look for the &quot;myVar&quot; key in the JSON of each event, and use its corresponding value as the routing key for that event).
- **Routing Key** : The routing key to use for each event written to the stream. If the Routing Key Type is &quot;Fixed&quot;, this value will be used literally and will be the same for every event. If the Routing Key Type is &quot;JsonReference&quot;, the routing key for each event will be evaluated from the JSON body, using the reference provided here.

If the Write operation is successful, SUCCESS status will be associated with the document.

## Read

Read is an inbound action to read the streaming data from Pravega. All the options are discussed in the Connector Configuration section. Each event that is read from the stream is passed on as a data document.

Read Operation Options:

- **Pravega Reader Group** : (Required) The name of the reader group for this reader within Pravega. Each reader group will maintain its position in the stream between executions. Note: a reader group will distribute events between its readers. Be careful when naming your reader group so that you do not mistakenly share events with another reader. If different readers each need to process all events in the stream, they should use different reader groups.
- **Read Timeout** : The maximum amount of time the reader will wait for the next event from Pravega (in milliseconds).
- **Max Events per Execution** : For each execution of the operation, the maximum number of events the reader will read before moving to the next component in the process. For example, if this is set to 100000 events, the operation will read a maximum of 100000 events. If it hits the tail of the stream (no more events), or this limit (100000 events), it will exit execution and the collection of events will be sent to the next component in the process flow. If this is set to 0, the operation will continue reading from the stream until it hits the tail (there are no more events). Note that if there are a lot of events, this may overload the Boomi document cache.
- **Max Read Time per Execution** : For each execution of the operation, the maximum number of seconds the reader will read and collect events from the stream before moving to the next component in the process. For example, if this is set to 30 seconds, the operation will spend a maximum of 30 seconds reading events from the stream. If it hits the tail of the stream (no more events), or this time (30 seconds), it will exit execution and the collection of events will be sent to the next component in the process flow. If this is set to 0, the operation will continue reading from the stream until it hits the tail (there are no more events). Note that if there are a lot of events, this may overload the Boomi document cache.
- **Initial Reader Group Position** : The initial position from which to start reading from the stream. A value of &quot;Head&quot; means the first time the reader runs, it will read from the head (beginning) of the stream. NOTE: this will read the entire stream from start to finish and may cause a very high load while doing so. A value of &quot;Tail&quot; means the first time the reader runs, it will start reading from the tail (end) of the stream. Use this option to start reading real-time events, but be aware that all events that were written before the first process execution will be ignored.

## Listen

Listen operation is also an inbound action to read the streaming data from Pravega. All the options are discussed in the Connector Configuration section. Each event that is read from the stream will be passed on as a data document submitted to the listener.

Listen Operation Options:

- **Pravega Reader Group** : (Required) The name of the reader group for this reader within Pravega. Each reader group will maintain its position in the stream between executions. Note: a reader group will distribute events between its readers. Be careful when naming your reader group so that you do not mistakenly share events with another reader. If different readers each need to process all events in the stream, they should use different reader groups.
- **Initial Reader Group Position** : The initial position from which to start reading from the stream. A value of &quot;Head&quot; means the first time the listener runs, it will read from the head (beginning) of the stream. NOTE: this will read the entire stream from start to finish and may cause a very high load while doing so. A value of &quot;Tail&quot; means the first time the listener runs, it will start reading from the tail (end) of the stream. Use this option to start reading real-time events, but be aware that all events that were written before the first process execution will be ignored.

## Additional Resources

[Pravega Documentation](http://pravega.io/index.html)

[Pravega Developer API Reference](http://pravega.io/docs/latest/javadoc/)

[Pravega FAQ](http://pravega.io/docs/latest/faq/)
