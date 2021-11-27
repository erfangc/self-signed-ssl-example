package org.example

import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

private fun newEmptyKeyStore(password: CharArray?): KeyStore {
    return try {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, password)
        keyStore
    } catch (e: IOException) {
        throw AssertionError(e)
    }
}

class Driver

fun main(args: Array<String>) {

    // ------------------------------------------------
    // setting up TLS trust
    // ------------------------------------------------
    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    val password: CharArray? = null // Any password will work.
    val certificateFactory = CertificateFactory.getInstance("X.509")
    val certificates =
        certificateFactory.generateCertificates(Driver::class.java.classLoader.getResourceAsStream("cacerts.pem"))
    require(!certificates.isEmpty()) { "expected non-empty set of trusted certificates" }
    val caKeyStore: KeyStore = newEmptyKeyStore(password)
    for ((index, certificate) in certificates.withIndex()) {
        val certificateAlias = "ca$index"
        caKeyStore.setCertificateEntry(certificateAlias, certificate)
    }
    trustManagerFactory.init(caKeyStore)
    val trustManagers = trustManagerFactory.trustManagers
    // ------------------------------------------------
    // end of setting up TLS trust
    // ------------------------------------------------

    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(emptyArray(), trustManagers, SecureRandom())

    val httpClient = HttpClient
        .newBuilder()
        .sslContext(sslContext)
        .build()

    val httpResponse = httpClient.send(
        HttpRequest.newBuilder(URI.create("https://localhost:8080/api/v1")).GET().build(),
        HttpResponse.BodyHandlers.ofString()
    )

    val httpResponse2 = httpClient.send(
        HttpRequest.newBuilder(URI.create("https://api.sampleapis.com/coffee/hot")).GET().build(),
        HttpResponse.BodyHandlers.ofString()
    )

    println(httpResponse.body())
    println(httpResponse2.body())
    
    val response = HttpClient.newBuilder().build().send(
        HttpRequest.newBuilder(URI.create("https://www.google.com")).GET().build(),
        HttpResponse.BodyHandlers.ofString()
    )
    println(response.body())
}
