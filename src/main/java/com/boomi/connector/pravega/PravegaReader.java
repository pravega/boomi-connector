package com.boomi.connector.pravega;

import com.boomi.connector.api.OperationContext;
import io.pravega.client.ClientConfig;
import io.pravega.client.ClientFactory;
import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.*;
import io.pravega.client.stream.impl.DefaultCredentials;
import io.pravega.client.stream.impl.JavaSerializer;
import lombok.Cleanup;

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
    private boolean enableAuth;
    private  String userName;
    private  String password;
    private boolean isPravegaStandalone;

    private PravegaReader(){

        Map<String, Object> connProps = context.getConnectionProperties();
        URI controllerURI = URI.create((String)connProps.get(Constants.URI_PROPERTY));
        scope = (String)connProps.get(Constants.SCOPE_PROPERTY);
        streamName = (String)connProps.get(Constants.NAME_PROPERTY);

        isPravegaStandalone =  (boolean)connProps.get(Constants.IS_PRAVEGA_STANDALONE_PROPERTY);
        enableAuth = (boolean)connProps.get(Constants.ENABLE_AUTH_PROPERTY);
        userName = (String)connProps.get(Constants.USER_NAME_PROPERTY);
        password = (String)connProps.get(Constants.PASSWORD_PROPERTY);

        Map<String, Object> opProps = context.getOperationProperties();
        readTimeout = (Long)opProps.get(Constants.READTIMEOUT_PROPERTY);

        ClientConfig clientConfig = null;
        if (enableAuth) {
            clientConfig = ClientConfig.builder().controllerURI(URI.create(controllerURI.toString()))
                    .credentials(new DefaultCredentials(password, userName))
                    .build();
        } else {
            clientConfig = ClientConfig.builder().controllerURI(URI.create(controllerURI.toString())).build();
        }

        StreamManager streamManager = StreamManager.create(clientConfig);

        if(isPravegaStandalone)
            streamManager.createScope(scope);

        streamManager.createStream(scope, streamName, StreamConfiguration.builder()
                    .build());

        final String readerGroup = UUID.randomUUID().toString().replace("-", "");
        final ReaderGroupConfig readerGroupConfig = ReaderGroupConfig.builder()
                .stream(Stream.of(scope, streamName))
                .build();

        @Cleanup
        ReaderGroupManager readerGroupManager = ReaderGroupManager.withScope(scope, clientConfig);
        readerGroupManager.createReaderGroup(readerGroup, readerGroupConfig);

        @Cleanup
        ClientFactory clientFactory = ClientFactory.withScope(scope, clientConfig);

        reader = clientFactory.createReader("reader",
                readerGroup,
                new JavaSerializer<String>(),
                ReaderConfig.builder().build());

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
