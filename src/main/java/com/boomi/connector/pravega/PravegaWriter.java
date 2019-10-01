package com.boomi.connector.pravega;

import com.boomi.connector.api.OperationContext;
import io.pravega.client.ClientFactory;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.impl.JavaSerializer;

import java.net.URI;
import java.util.Map;

final public class PravegaWriter {

    private static PravegaWriter singleInstance = null;
    private EventStreamWriter<String> writer = null;
    private static OperationContext context = null;
    private String scope;
    private String streamName;
    private String routingKey;

    private PravegaWriter(){

        Map<String, Object> connProps = this.context.getConnectionProperties();
        URI controllerURI = URI.create((String)connProps.get(Constants.URI_PROPERTY));
        scope = (String)connProps.get(Constants.SCOPE_PROPERTY);
        streamName = (String)connProps.get(Constants.NAME_PROPERTY);

        Map<String, Object> opProps = this.context.getOperationProperties();
        routingKey = (String)opProps.get(Constants.ROUTINGKEY_PROPERTY);

        ClientFactory clientFactory = ClientFactory.withScope(scope, controllerURI);

        writer = clientFactory.createEventWriter(streamName,
                new JavaSerializer<String>(),
                EventWriterConfig.builder().build());

    }

    public String getScope() {
        return scope;
    }

    public String getStreamName(){
        return streamName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public EventStreamWriter<String> getWriter() {
        return writer;
    }

    public static PravegaWriter getInstance(OperationContext context){
        PravegaWriter.context = context;
        if(singleInstance == null)
            singleInstance = new PravegaWriter();

        return singleInstance;
    }

}
