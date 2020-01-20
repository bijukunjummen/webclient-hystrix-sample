package org.bk.samples

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.bk.samples.model.City
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.net.URI

class CitiesWebClientMockTest {


    @Test
    fun testCleanResponse() {
        val mockWebClientBuilder: WebClient.Builder = mock()
        val mockWebClient: WebClient = mock()
        whenever(mockWebClientBuilder.build()).thenReturn(mockWebClient)

        val mockRequestSpec: WebClient.RequestBodyUriSpec = mock()
        whenever(mockWebClient.get()).thenReturn(mockRequestSpec)
        val mockRequestBodySpec: WebClient.RequestBodySpec = mock()

        whenever(mockRequestSpec.uri(any<URI>())).thenReturn(mockRequestBodySpec)

        whenever(mockRequestBodySpec.accept(any())).thenReturn(mockRequestBodySpec)

        val citiesJson: String = this.javaClass.getResource("/sample-cities.json").readText()

        val clientResponse: ClientResponse = ClientResponse
            .create(HttpStatus.OK)
            .header("Content-Type", "application/json")
            .body(citiesJson).build()

        whenever(mockRequestBodySpec.exchange()).thenReturn(Mono.just(clientResponse))

        val citiesClient = CitiesClient(mockWebClientBuilder, "http://somebaseurl")

        val cities: Flux<City> = citiesClient.getCities()

        StepVerifier
            .create(cities)
            .expectNext(City(1L, "Portland", "USA", 1_600_000L))
            .expectNext(City(2L, "Seattle", "USA", 3_200_000L))
            .expectNext(City(3L, "SFO", "USA", 6_400_000L))
            .expectComplete()
            .verify()
    }

}