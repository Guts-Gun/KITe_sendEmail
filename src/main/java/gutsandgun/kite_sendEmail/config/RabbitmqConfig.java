package gutsandgun.kite_sendEmail.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitmqConfig {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.virtual-host}")
    private String vhost;



    @Value("${rabbitmq.queue1.name}")
    private String queue1;

    @Value("${rabbitmq.queue2.name}")
    private String queue2;

    @Value("${rabbitmq.queue3.name}")
    private String queue3;


    @Value("${rabbitmq.queue1.exchange}")
    private String exchange1;

    @Value("${rabbitmq.queue2.exchange}")
    private String exchange2;

    @Value("${rabbitmq.queue3.exchange}")
    private String exchange3;


    @Value("${rabbitmq.routing.key.queue1}")
    private String routingKey1;

    @Value("${rabbitmq.routing.key.queue2}")
    private String routingKey2;

    @Value("${rabbitmq.routing.key.queue3}")
    private String routingKey3;


    @Bean
    Queue queue1() {
        return new Queue(queue1, true);
    }
    @Bean
    Queue queue2() {
        return new Queue(queue2, true);
    }
    @Bean
    Queue queue3() {
        return new Queue(queue3, true);
    }


    @Bean
    DirectExchange directExchange1() {
        return new DirectExchange(exchange1);
    }

    @Bean
    DirectExchange directExchange2() {
        return new DirectExchange(exchange2);
    }

    @Bean
    DirectExchange directExchange3() {
        return new DirectExchange(exchange3);
    }


    @Bean
    Binding binding1() {
        return BindingBuilder.bind(queue1()).to(directExchange1()).with(routingKey1);
    }

    @Bean
    Binding binding2() {
        return BindingBuilder.bind(queue2()).to(directExchange2()).with(routingKey2);
    }

    @Bean
    Binding binding3() {
        return BindingBuilder.bind(queue3()).to(directExchange3()).with(routingKey3);
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(vhost);
        return connectionFactory;
    }



}
