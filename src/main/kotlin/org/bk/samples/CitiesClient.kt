package org.bk.samples

import org.bk.samples.model.City
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import java.net.URI

class CitiesClient(
        private val webClientBuilder: WebClient.Builder,
        private val citiesBaseUrl: String
) {

    fun getCities(): Flux<City> {
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
}
