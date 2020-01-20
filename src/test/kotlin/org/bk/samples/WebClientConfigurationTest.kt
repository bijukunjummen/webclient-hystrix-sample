package org.bk.samples

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.bk.samples.model.City
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

@SpringBootTest
@AutoConfigureJson
class WebClientConfigurationTest {

    @Autowired
    private lateinit var webClientBuilder: WebClient.Builder

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun testAGet() {
        val citiesJson: String = loadResource("/sample-cities.json")
        val cities: List<City> = objectMapper.readValue(citiesJson, object : TypeReference<List<City>>() {})
        WIREMOCK_SERVER.stubFor(
            get(urlMatching("/cities"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(
                    aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(citiesJson)
                )
        )

        val citiesClient = CitiesClient(webClientBuilder, "http://localhost:${WIREMOCK_SERVER.port()}")

        val citiesFlux: Flux<City> = citiesClient.getCities()

        StepVerifier
            .create(citiesFlux)
            .expectNext(cities[0])
            .expectNext(cities[1])
            .expectNext(cities[2])
            .expectComplete()
            .verify()
    }

    @Test
    fun testAPost() {
        val dateAsString = "1985-02-01T10:10:10Z"

        val city = City(
            id = 1L, name = "some city",
            country = "some country",
            pop = 1000L,
            creationDate = Instant.parse(dateAsString)
        )
        WIREMOCK_SERVER.stubFor(
            post(urlMatching("/cities"))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.CREATED.value())
                        .withBody(objectMapper.writeValueAsString(city))
                )
        )

        val citiesClient = CitiesClient(webClientBuilder, "http://localhost:${WIREMOCK_SERVER.port()}")

        val citiesMono: Mono<City> = citiesClient.createCity(city)

        StepVerifier
            .create(citiesMono)
            .expectNext(city)
            .expectComplete()
            .verify()


        //Ensure that date field is in ISO-8601 format..
        WIREMOCK_SERVER.verify(
            postRequestedFor(urlPathMatching("/cities"))
                .withRequestBody(matchingJsonPath("$.creationDate", equalTo(dateAsString)))
        )
    }

    @Test
    fun testATimeout() {
        WIREMOCK_SERVER.stubFor(
            get(urlMatching("/cities"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(
                    aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withFixedDelay(5000)
                        .withHeader("Content-Type", "application/json")
                )
        )

        val citiesClient = CitiesClient(webClientBuilder, "http://localhost:${WIREMOCK_SERVER.port()}")

        val cities: Flux<City> = citiesClient.getCities()

        StepVerifier
            .create(cities)
            .expectComplete()
            .verify()
    }

    companion object {
        private val WIREMOCK_SERVER =
            WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort().notifier(ConsoleNotifier(true)))

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