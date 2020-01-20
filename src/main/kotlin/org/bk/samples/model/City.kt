package org.bk.samples.model

import java.time.Instant

data class City(
    val id: Long,
    val name: String,
    val country: String,
    val pop: Long,
    val creationDate: Instant = Instant.now()
)