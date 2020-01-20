package org.bk.samples

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.bk.samples.model.City
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

class WiremockWebClientTest {

    @Test
    fun testARemoteCall() {
        val citiesJson = this.javaClass.getResource("/sample-cities.json").readText()
        WIREMOCK_SERVER.stubFor(
            WireMock.get(WireMock.urlMatching("/cities"))
                .withHeader("Accept", WireMock.equalTo("application/json"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(citiesJson)
                )
        )

        val citiesClient = CitiesClient(WebClient.builder(), "http://localhost:${WIREMOCK_SERVER.port()}")

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
    fun testATimeout() {
        WIREMOCK_SERVER.stubFor(
            WireMock.get(WireMock.urlMatching("/cities"))
                .withHeader("Accept", WireMock.equalTo("application/json"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withFixedDelay(5000)
                        .withHeader("Content-Type", "application/json")
                )
        )

        val citiesClient = CitiesClient(WebClient.builder(), "http://localhost:${WIREMOCK_SERVER.port()}")

        val cities: Flux<City> = citiesClient.getCities()

        StepVerifier
            .create(cities)
            .expectComplete()
            .verify()
    }

    companion object {
        private val WIREMOCK_SERVER = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            WIREMOCK_SERVER.start()
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            WIREMOCK_SERVER.stop()
        }
    }
}