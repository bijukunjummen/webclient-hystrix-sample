package org.bk.samples

fun loadResource(location: String) = String.javaClass.getResource(location).readText()