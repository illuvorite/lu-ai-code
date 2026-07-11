package com.lu.luaicode.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis聊天记忆存储配置类
 * 用于配置和创建Redis聊天记忆存储的Bean
 */
@Configuration // 标识这是一个配置类
@ConfigurationProperties(prefix = "spring.data.redis") // 绑定配置文件中以"spring.data.redis"为前缀的属性
@Data // Lombok注解，自动生成getter、setter、toString等方法
public class RedisChatMemoryStoreConfig {

    private String host; // Redis服务器主机地址

    private int port; // Redis服务器端口

    private String user = "default"; // Redis用户名（Redis 6+ ACL，默认为default）

    private String password; // Redis服务器密码

    private long ttl; // Redis中数据的生存时间(毫秒)



    /**
     * 创建并配置RedisChatMemoryStore Bean
     * @return 配置好的RedisChatMemoryStore实例
     */
    @Bean // 标识该方法返回一个Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        return RedisChatMemoryStore.builder()
                .host(host) // 设置主机地址
                .port(port) // 设置端口
                .user(user) // 设置用户名（必须有值，否则JedisPooled不会发送AUTH命令）
                .password(password) // 设置密码
                .ttl(ttl) // 设置生存时间
                .build(); // 构建并返回RedisChatMemoryStore实例
    }
}
