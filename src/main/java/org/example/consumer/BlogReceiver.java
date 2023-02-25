package org.example.consumer;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.Scanner;


public class BlogReceiver {
    private static final String EXCHANGE_NAME = "it_blog";


    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        String queueName = channel.queueDeclare().getQueue();

        Scanner in = new Scanner(System.in);
        String command = "";

        while (!command.equals("exit")) {
            System.out.println("Input command");
            String s = in.nextLine();
            command = s.split(" ")[0];
            if (command.equals("set_topic")) {
                subscribeOn(channel, queueName, s.replace("set_topic ", "") + ".#");
            }
        }
        channel.close();
        connection.close();
    }

    private static void subscribeOn(Channel channel, String queueName, String topic) throws IOException {
        channel.queueBind(queueName, BlogReceiver.EXCHANGE_NAME, topic);
        System.out.printf("Subscribed on %s\n", topic);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.printf("Received message '%s' on topic '%s'\n", message, delivery.getEnvelope().getRoutingKey());
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
    }
}
