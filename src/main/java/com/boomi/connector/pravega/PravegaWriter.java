package com.boomi.connector.pravega;

import com.boomi.connector.api.OperationContext;
import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.ScalingPolicy;
import io.pravega.client.stream.StreamConfiguration;
import io.pravega.client.stream.impl.DefaultCredentials;
import io.pravega.client.stream.impl.JavaSerializer;

import java.net.URI;
import java.util.Map;

final public class PravegaWriter {

    private static PravegaWriter singleInstance = null;
    private EventStreamWriter<String> writer = null;
    private static OperationContext context = null;
    private String scope;
    private String streamName;
    private String fixedRoutingKey;
    private boolean isRoutingKeyNeeded;
    private  String routingKeyConfigValue;
    private boolean enableAuth;
    private String userName;
    private String password;
    private boolean isPravegaStandalone;

    private PravegaWriter(){

        Map<String, Object> connProps = this.context.getConnectionProperties();
        URI controllerURI = URI.create((String)connProps.get(Constants.URI_PROPERTY));
        scope = (String)connProps.get(Constants.SCOPE_PROPERTY);
        streamName = (String)connProps.get(Constants.NAME_PROPERTY);
        isRoutingKeyNeeded = (boolean)connProps.get(Constants.ROUTINGKEY_NEEDED_PROPERTY);

        isPravegaStandalone = (boolean) connProps.get(Constants.IS_PRAVEGA_STANDALONE_PROPERTY);
        enableAuth = (boolean) connProps.get(Constants.ENABLE_AUTH_PROPERTY);
        userName = (String) connProps.get(Constants.USER_NAME_PROPERTY);
        password = (String) connProps.get(Constants.PASSWORD_PROPERTY);

        Map<String, Object> opProps = this.context.getOperationProperties();
        isRoutingKeyNeeded = (boolean) opProps.get(Constants.ROUTINGKEY_NEEDED_PROPERTY);
        fixedRoutingKey = (String)opProps.get(Constants.FIXED_ROUTINGKEY_PROPERTY);
        if(isRoutingKeyNeeded)
            routingKeyConfigValue = (String)opProps.get(Constants.ROUTINGKEY_CONFIG_VALUE_PROPERTY);

        // configure client
        ClientConfig.ClientConfigBuilder clientBuilder = ClientConfig.builder().controllerURI(URI.create(controllerURI.toString()));
        if (enableAuth) clientBuilder.credentials(new DefaultCredentials(password, userName));
        ClientConfig clientConfig = clientBuilder.build();

        // create stream manager
        StreamManager streamManager = StreamManager.create(clientConfig);

        // create scope
        if (isPravegaStandalone) streamManager.createScope(scope);

        // configure stream
        StreamConfiguration.StreamConfigurationBuilder streamBuilder = StreamConfiguration.builder();
        if (isRoutingKeyNeeded) streamBuilder.scalingPolicy(ScalingPolicy.byEventRate(20, 2, 1));
        else streamBuilder.scalingPolicy(ScalingPolicy.fixed(1));

        //  create stream
        streamManager.createStream(scope, streamName, streamBuilder.build());

        // create event writer
        writer = EventStreamClientFactory.withScope(scope, clientConfig).createEventWriter(
                streamName, new JavaSerializer(), EventWriterConfig.builder().build());
    }

    public String getScope() {
        return scope;
    }

    public String getStreamName(){
        return streamName;
    }

    public String getFixedRoutingKey() {
        return fixedRoutingKey;
    }

    public EventStreamWriter<String> getWriter() {
        return writer;
    }

    public boolean getIsRoutingKeyNeeded() {
        return isRoutingKeyNeeded;
    }

    public String getRoutingKeyConfigValue() {
        return routingKeyConfigValue;
    }

    public void close(){
        writer.close();
    }

    public static PravegaWriter getInstance(OperationContext context){
        PravegaWriter.context = context;
        if(singleInstance == null)
            singleInstance = new PravegaWriter();

        return singleInstance;
    }

}
