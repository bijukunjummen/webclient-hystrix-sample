package org.bk.samples

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfiguration {

    @Bean
    fun webClientBuilder(customizerProvider: ObjectProvider<WebClientCustomizer>): WebClient.Builder {
        val webClientBuilder: WebClient.Builder = WebClient
            .builder()
            .filter { req, next ->
                LOGGER.error("Custom filter invoked..")
                next.exchange(req)
            }

        customizerProvider.orderedStream()
            .forEach { customizer -> customizer.customize(webClientBuilder) }

        return webClientBuilder;
    }

    companion object {
        val LOGGER = loggerFor<WebClientConfiguration>()
    }
}