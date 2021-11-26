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

fun main(args: Array<String>) {

    val sslCaCert = """
-----BEGIN CERTIFICATE-----
MIIDWTCCAkGgAwIBAgIEQXzvNDANBgkqhkiG9w0BAQsFADBdMQswCQYDVQQGEwJV
UzENMAsGA1UECBMEVGVzdDENMAsGA1UEBxMEVGVzdDENMAsGA1UEChMEVGVzdDEN
MAsGA1UECxMEVGVzdDESMBAGA1UEAxMJbG9jYWxob3N0MB4XDTIxMTEyNjEyNTcy
MloXDTMxMTEyNDEyNTcyMlowXTELMAkGA1UEBhMCVVMxDTALBgNVBAgTBFRlc3Qx
DTALBgNVBAcTBFRlc3QxDTALBgNVBAoTBFRlc3QxDTALBgNVBAsTBFRlc3QxEjAQ
BgNVBAMTCWxvY2FsaG9zdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEB
AKeY3lgBAxzx9j35aHolsHLVs7ybxV+YruiRDsnBn2tTqLeV9hELryQnjms4qenc
5lu3ttL4wCwijYIK27SrazK97dkLMQ7pAgeelCGn0swjjXljTyepWpHkD+CZ7CAX
qLm4y+iPwkpwpu1nHV6vLQfpplphkj0GoOtuvKfFVF9GxWipHwFAOA9f9EedyHAB
DNPTokuwMuhvmCgAgQHFxIdiGiO7twbTQQbbgQeJRfty7e1FHp3WQtjq5v1Gc30n
gboCoOc2yjxclUvcpuL35vQ4TdnynAEY3vS2SUy2ATUTZ5BixrdrOy5sx2rMLucB
FYq5u2v4LcMMaNY1NaNgk68CAwEAAaMhMB8wHQYDVR0OBBYEFGHgsL1Qk+z44IJh
9lVS6W0bSaWrMA0GCSqGSIb3DQEBCwUAA4IBAQATmtcxmBiESidYBjllqBjHtcOd
ljS3JhDVYzcrjQdppz/aQOJUV4Q7PFkOEGiZnxVbOHXTNZ5Y0dTMVu4DSfz0yvMa
vUHUEDMkqxapmTf3HVM2kPqwrIn3v0nCysbFi0DGAcpQ3/aGmpDwcZpfWMh+LKed
wKXLVr+JI8CMe0jv2mFWqK8kT2OUlWbw1MB9VlTiA5F6n/tdbZTVdsb1UTPF8VhV
/Za6qU50L4LGSIUnnUNEnCUNIwAZddDvI2v6wkaOClH1sxCdy+opyeA1O1enHqMu
xdfGVkKjZM1RxC1esntx72p9S1D3YeFzukjnq5UASswQ8bLFlkhEXfvmVwdQ
-----END CERTIFICATE-----
    """.trimIndent().byteInputStream()

    // ------------------------------------------------
    // setting up TLS trust
    // ------------------------------------------------
    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    val password: CharArray? = null // Any password will work.
    val certificateFactory = CertificateFactory.getInstance("X.509")
    val certificates = certificateFactory.generateCertificates(sslCaCert)
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

    println(httpResponse.body())
}

