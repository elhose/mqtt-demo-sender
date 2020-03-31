package com.ttpsc.mqttdemo;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@SpringBootApplication
public class MqttDemoApplication {

    private static String MESSAGE;
    private final String HOST;
    private final Integer PORT;
    private final String DEFAULT_TOPIC;
    private final String CLIENT_ID;
    private final String USERNAME;
    private final String PASSWORD;

    public MqttDemoApplication(@Value("${mqtt.message}") String message, @Value("${mqtt.hostname}") String host, @Value("${mqtt.port}") Integer port,
                               @Value("${mqtt.topic}") String defaultTopic, @Value("${mqtt.clientId}") String clientId, @Value("${mqtt.username}") String username,
                               @Value("${mqtt.password}") String password) {
        this.MESSAGE = message;
        this.HOST = host;
        this.PORT = port;
        this.DEFAULT_TOPIC = defaultTopic;
        this.CLIENT_ID = clientId;
        this.USERNAME = username;
        this.PASSWORD = password;
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(MqttDemoApplication.class)
                .run(args);

        MyGateway gateway = context.getBean(MyGateway.class);
        gateway.sendToMqtt(MESSAGE);
    }

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{"tcp://" + HOST + ":" + PORT});
//        options.setUserName(USERNAME);
//        options.setPassword(PASSWORD.toCharArray());
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler(CLIENT_ID, mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic(DEFAULT_TOPIC);
        return messageHandler;
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
    public interface MyGateway {
        void sendToMqtt(String data);
    }
}
