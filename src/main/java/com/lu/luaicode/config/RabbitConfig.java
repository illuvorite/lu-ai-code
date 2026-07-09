package com.lu.luaicode.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RabbitConfig {

    // ==================== Exchanges ====================

    @Bean
    public DirectExchange screenshotExchange() {
        return new DirectExchange("screenshot.direct", true, false);
    }

    @Bean
    public DirectExchange screenshotDlx() {
        return new DirectExchange("screenshot.dlx", true, false);
    }

    // ==================== Queues ====================

    @Bean
    public Queue screenshotTaskQueue() {
        return QueueBuilder.durable("screenshot.task.queue")
                .deadLetterExchange("screenshot.dlx")
                .deadLetterRoutingKey("screenshot.task.dlq")
                .build();
    }

    @Bean
    public Queue screenshotTaskDlq() {
        return QueueBuilder.durable("screenshot.task.dlq").build();
    }

    @Bean
    public Queue screenshotResultQueue() {
        return QueueBuilder.durable("screenshot.result.queue").build();
    }

    // ==================== Bindings ====================

    @Bean
    public Binding taskBinding() {
        return BindingBuilder.bind(screenshotTaskQueue())
                .to(screenshotExchange())
                .with("screenshot.task");
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(screenshotTaskDlq())
                .to(screenshotDlx())
                .with("screenshot.task.dlq");
    }

    @Bean
    public Binding resultBinding() {
        return BindingBuilder.bind(screenshotResultQueue())
                .to(screenshotExchange())
                .with("screenshot.result");
    }

    // ==================== Message Converter ====================

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    // ==================== Retry Interceptor ====================

    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000L, 2.0, 10000L)
                .build();
    }

    // ==================== Listener Container Factory ====================

    @Bean
    public SimpleRabbitListenerContainerFactory screenshotListenerFactory(
            ConnectionFactory connectionFactory,
            RetryOperationsInterceptor retryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setAdviceChain(retryInterceptor);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(4);
        factory.setPrefetchCount(1);
        return factory;
    }

    // ==================== RabbitTemplate ====================

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
