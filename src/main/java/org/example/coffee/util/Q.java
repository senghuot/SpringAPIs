package org.example.coffee.util;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

import java.time.Duration;

public class Q {
    static String connectionString = System.getenv("Q_CONNECTION_STRING");
    static String queueName = "op";

    static ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .buildClient();

    static ServiceBusReceiverClient receiverClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .queueName(queueName)
            .buildClient();

    public static boolean push(final String message) {
        if (message == null || message.isEmpty())
            return false;
        senderClient.sendMessage(new ServiceBusMessage(message));
        return true;
    }

    public static String pop() {
        var messages = receiverClient.receiveMessages(1, Duration.ofSeconds(5));
        for (var message: messages)
            return message.getBody().toString();
        return "NO_NEW_MESSAGE";
    }
}
