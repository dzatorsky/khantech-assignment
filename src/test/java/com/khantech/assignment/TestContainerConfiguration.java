package com.khantech.assignment;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

@Slf4j
@TestConfiguration(proxyBeanMethods = false)
public class TestContainerConfiguration {

    @Bean
    @ServiceConnection(name = "khantech-db")
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>("postgres:17-alpine")
                .withDatabaseName("khantech")
                .withUsername("khantech")
                .withPassword("khantech")
                .withLogConsumer(new Slf4jLogConsumer(log))
                .withReuse(true)
                .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                        new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(15432), new ExposedPort(5432)))
                ));
    }

}
