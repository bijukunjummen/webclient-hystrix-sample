package org.bk.samples

import org.bk.samples.model.City
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI

class CitiesClient(
    private val webClientBuilder: WebClient.Builder,
    private val citiesBaseUrl: String
) {

    fun getCities(): Flux<City> {
        val uri: URI = UriComponentsBuilder
            .fromUriString(citiesBaseUrl)
            .path("/cities")
            .build()
            .encode()
            .toUri()

        val webClient: WebClient = this.webClientBuilder.build()

        return webClient.get()
            .uri(uri)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .flatMapMany { clientResponse ->
                clientResponse.bodyToFlux<City>()
            }
    }

    fun createCity(city: City): Mono<City> {
        val uri: URI = UriComponentsBuilder
            .fromUriString(citiesBaseUrl)
            .path("/cities")
            .build()
            .encode()
            .toUri()

        val webClient: WebClient = this.webClientBuilder.build()

        return webClient.post()
            .uri(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(city)
            .exchange()
            .flatMap { clientResponse ->
                clientResponse.bodyToMono(City::class.java)
            }

    }

}
