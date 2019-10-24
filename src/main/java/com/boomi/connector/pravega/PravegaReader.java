package com.boomi.connector.pravega;

import com.boomi.connector.api.OperationContext;
import io.pravega.client.ClientFactory;
import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.*;
import io.pravega.client.stream.impl.JavaSerializer;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

final public class PravegaReader {

    private static PravegaReader singleInstance = null;
    private static OperationContext context = null;
    private EventStreamReader<String> reader = null;
    private String scope;
    private String streamName;
    private String routingKey;
    private Long readTimeout;
    private boolean isRoutingKeyNeeded;

    private PravegaReader(){

        Map<String, Object> connProps = context.getConnectionProperties();
        URI controllerURI = URI.create((String)connProps.get(Constants.URI_PROPERTY));
        scope = (String)connProps.get(Constants.SCOPE_PROPERTY);
        streamName = (String)connProps.get(Constants.NAME_PROPERTY);
        isRoutingKeyNeeded = (boolean)connProps.get(Constants.ROUTINGKEY_NEEDED_PROPERTY);

        Map<String, Object> opProps = context.getOperationProperties();
        readTimeout = (Long)opProps.get(Constants.READTIMEOUT_PROPERTY);


        StreamManager streamManager = StreamManager.create(controllerURI);
        try {
            final boolean scopeIsNew = streamManager.createScope(scope);
            StreamConfiguration streamConfig;

            if(isRoutingKeyNeeded){
                streamConfig = StreamConfiguration.builder().scalingPolicy(ScalingPolicy.byEventRate(20, 2, 2)).build();
            }else{
                streamConfig = StreamConfiguration.builder().scalingPolicy(ScalingPolicy.fixed(1)).build();
            }
            final boolean streamIsNew = streamManager.createStream(scope, streamName, streamConfig);

            final String readerGroup = UUID.randomUUID().toString().replace("-", "");
            final ReaderGroupConfig readerGroupConfig = ReaderGroupConfig.builder()
                    .stream(Stream.of(scope, streamName))
                    .build();
            try (ReaderGroupManager readerGroupManager = ReaderGroupManager.withScope(scope, controllerURI)) {
                readerGroupManager.createReaderGroup(readerGroup, readerGroupConfig);
            }

            ClientFactory clientFactory = ClientFactory.withScope(scope, controllerURI);

            reader = clientFactory.createReader("reader",
                    readerGroup,
                    new JavaSerializer<String>(),
                    ReaderConfig.builder().build());
        } catch(Exception e){

        }


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

    public EventStreamReader<String> getReader() {
        return reader;
    }

    public Long getReadTimeout() {
        return readTimeout;
    }

    public static PravegaReader getInstance(OperationContext context){
        PravegaReader.context = context;
        if(singleInstance == null)
            singleInstance = new PravegaReader();

        return singleInstance;
    }

}
