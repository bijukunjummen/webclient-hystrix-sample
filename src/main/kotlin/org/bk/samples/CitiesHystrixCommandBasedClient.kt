package org.bk.samples

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixCommandProperties
import com.netflix.hystrix.HystrixObservableCommand
import org.bk.samples.model.City
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import rx.Observable
import rx.RxReactiveStreams
import rx.schedulers.Schedulers
import java.net.URI

class CitiesHystrixCommandBasedClient(
    private val webClientBuilder: WebClient.Builder,
    private val citiesBaseUrl: String
) {
    fun getCities(): Flux<City> {
        val citiesObservable: Observable<City> = CitiesHystrixCommand(webClientBuilder, citiesBaseUrl)
            .observe()
            .subscribeOn(Schedulers.io())

        return Flux
            .from(
                RxReactiveStreams
                    .toPublisher(citiesObservable)
            )
    }
}

class CitiesHystrixCommand(
    private val webClientBuilder: WebClient.Builder,
    private val citiesBaseUrl: String
) : HystrixObservableCommand<City>(
    Setter
        .withGroupKey(HystrixCommandGroupKey.Factory.asKey("cities-service"))
        .andCommandKey(HystrixCommandKey.Factory.asKey("cities-service"))
        .andCommandPropertiesDefaults(
            HystrixCommandProperties.Setter()
                .withExecutionTimeoutInMilliseconds(4000)
        )
) {
    override fun construct(): Observable<City> {
        val buildUri: URI = UriComponentsBuilder
            .fromUriString(citiesBaseUrl)
            .path("/cities")
            .build()
            .encode()
            .toUri()

        val webClient: WebClient = this.webClientBuilder.build()

        val result: Flux<City> = webClient.get()
            .uri(buildUri)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .flatMapMany { clientResponse ->
                clientResponse.bodyToFlux<City>()
            }

        return RxReactiveStreams.toObservable(result)
    }

    override fun resumeWithFallback(): Observable<City> {
        LOGGER.error("Falling back on cities call", executionException)
        return Observable.empty()
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(CitiesHystrixCommand::class.java)
    }
}