package org.bk.samples

import com.netflix.hystrix.HystrixCommandProperties
import org.bk.samples.model.City
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.netflix.hystrix.HystrixCommands
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import rx.schedulers.Schedulers
import java.net.URI

class CitiesFunctionalHystrixClient(
    private val webClientBuilder: WebClient.Builder,
    private val citiesBaseUrl: String
) {
    fun getCities(): Flux<City> {
        return HystrixCommands
            .from(callCitiesService())
            .commandName("cities-service")
            .groupName("cities-service")
            .commandProperties(
                HystrixCommandProperties.Setter()
                    .withExecutionTimeoutInMilliseconds(1000)
            )
            .toObservable { obs ->
                obs.observe()
                    .subscribeOn(Schedulers.io())
            }
            .fallback { t: Throwable ->
                LOGGER.error(t.message, t)
                Flux.empty()
            }
            .toFlux()
    }

    fun callCitiesService(): Flux<City> {
        val buildUri: URI = UriComponentsBuilder
            .fromUriString(citiesBaseUrl)
            .path("/cities")
            .build()
            .encode()
            .toUri()

        val webClient: WebClient = this.webClientBuilder.build()

        return webClient.get()
            .uri(buildUri)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .flatMapMany { clientResponse ->
                clientResponse.bodyToFlux<City>()
            }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(CitiesHystrixCommand::class.java)
    }
}
