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

    @Value("${rabbitmq.email.queue1.name}")
    private String emailQueue1;

    @Value("${rabbitmq.email.queue2.name}")
    private String emailQueue2;

    @Value("${rabbitmq.email.queue1.exchange}")
    private String emailExchange;

    @Value("${rabbitmq.email.routing.key.queue1}")
    private String emailRoutingKey1;

    @Value("${rabbitmq.email.routing.key.queue2}")
    private String emailRoutingKey2;


    @Bean
    Queue emailQueue1() {
        return new Queue(emailQueue1, true);
    }

    @Bean
    Queue emailQueue2() {
        return new Queue(emailQueue2, true);
    }

    DirectExchange emailDirectExchange1() {
        return new DirectExchange(emailExchange);
    }

    @Bean
    Binding emailBinding1() {
        return BindingBuilder.bind(emailQueue1()).to(emailDirectExchange1()).with(emailRoutingKey1);
    }

    @Bean
    Binding emailBinding2() {
        return BindingBuilder.bind(emailQueue2()).to(emailDirectExchange1()).with(emailRoutingKey2);
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

    @Value("${rabbitmq.log.name}")
    private String logQueue;
    @Value("${rabbitmq.log.exchange}")
    private String logExchange;

    @Value("${rabbitmq.routing.key.log}")
    private String logRoutingKey;

    @Bean
    Queue logQueue() {
        return new Queue(logQueue, true);
    }

    @Bean
    DirectExchange logDirectExchange() {
        return new DirectExchange(logExchange);
    }

    @Bean
    Binding logBinding() {
        return BindingBuilder.bind(logQueue()).to(logDirectExchange()).with(logRoutingKey);
    }

}
