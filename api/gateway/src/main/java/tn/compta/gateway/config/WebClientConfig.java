package tn.compta.gateway.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration du WebClient pour les health checks et autres appels HTTP.
 */
@Configuration
public class WebClientConfig {

  @Value("${webclient.connect-timeout-ms:5000}")
  private int connectTimeoutMs;

  @Value("${webclient.response-timeout-seconds:5}")
  private int responseTimeoutSeconds;

  @Value("${webclient.read-timeout-seconds:5}")
  private int readTimeoutSeconds;

  @Value("${webclient.write-timeout-seconds:5}")
  private int writeTimeoutSeconds;

  @Value("${webclient.pool.max-connections:100}")
  private int maxConnections;

  @Value("${webclient.pool.pending-acquire-timeout-seconds:45}")
  private int pendingAcquireTimeoutSeconds;

  @Value("${webclient.pool.max-idle-time-seconds:30}")
  private int maxIdleTimeSeconds;

  @Bean
  public WebClient.Builder webClientBuilder() {
    ConnectionProvider connectionProvider = ConnectionProvider.builder("gateway-pool")
        .maxConnections(maxConnections)
        .pendingAcquireTimeout(Duration.ofSeconds(pendingAcquireTimeoutSeconds))
        .maxIdleTime(Duration.ofSeconds(maxIdleTimeSeconds))
        .build();

    HttpClient httpClient = HttpClient.create(connectionProvider)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
        .responseTimeout(Duration.ofSeconds(responseTimeoutSeconds))
        .doOnConnected(conn -> conn
            .addHandlerLast(new ReadTimeoutHandler(readTimeoutSeconds, TimeUnit.SECONDS))
            .addHandlerLast(new WriteTimeoutHandler(writeTimeoutSeconds, TimeUnit.SECONDS))
        );

    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient));
  }
}
