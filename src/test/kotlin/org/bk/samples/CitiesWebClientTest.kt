package org.bk.samples

import org.bk.samples.model.City
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class CitiesWebClientTest {

    @Test
    fun testCleanResponse() {
        val citiesJson: String = this.javaClass.getResource("/sample-cities.json").readText()

        val clientResponse: ClientResponse = ClientResponse
                .create(HttpStatus.OK)
                .header("Content-Type","application/json")
                .body(citiesJson).build()
        val shortCircuitingExchangeFunction = ExchangeFunction {
            Mono.just(clientResponse)
        }

        val webClientBuilder: WebClient.Builder = WebClient.builder().exchangeFunction(shortCircuitingExchangeFunction)
        val citiesClient = CitiesClient(webClientBuilder, "http://somebaseurl")

        val cities: Flux<City> = citiesClient.getCities()

        StepVerifier
                .create(cities)
                .expectNext(City(1L, "Portland", "USA", 1_600_000L))
                .expectNext(City(2L, "Seattle", "USA", 3_200_000L))
                .expectNext(City(3L, "SFO", "USA", 6_400_000L))
                .expectComplete()
                .verify()
    }

    @Test
    fun testWithServerError() {
        val clientResponse: ClientResponse = ClientResponse
                .create(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Server error!").build()
        val shortCircuitingExchangeFunction = ExchangeFunction {
            Mono.just(clientResponse)
        }

        val webClientBuilder: WebClient.Builder = WebClient.builder().exchangeFunction(shortCircuitingExchangeFunction)
        val citiesClient = CitiesClient(webClientBuilder, "http://somebaseurl")

        val cities: Flux<City> = citiesClient.getCities()

        StepVerifier
                .create(cities)
                .expectError()
                .verify()
    }
}