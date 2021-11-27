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
-----BEGIN CERTIFICATE-----
MIIFazCCA1OgAwIBAgIRAIIQz7DSQONZRGPgu2OCiwAwDQYJKoZIhvcNAQELBQAw
TzELMAkGA1UEBhMCVVMxKTAnBgNVBAoTIEludGVybmV0IFNlY3VyaXR5IFJlc2Vh
cmNoIEdyb3VwMRUwEwYDVQQDEwxJU1JHIFJvb3QgWDEwHhcNMTUwNjA0MTEwNDM4
WhcNMzUwNjA0MTEwNDM4WjBPMQswCQYDVQQGEwJVUzEpMCcGA1UEChMgSW50ZXJu
ZXQgU2VjdXJpdHkgUmVzZWFyY2ggR3JvdXAxFTATBgNVBAMTDElTUkcgUm9vdCBY
MTCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAK3oJHP0FDfzm54rVygc
h77ct984kIxuPOZXoHj3dcKi/vVqbvYATyjb3miGbESTtrFj/RQSa78f0uoxmyF+
0TM8ukj13Xnfs7j/EvEhmkvBioZxaUpmZmyPfjxwv60pIgbz5MDmgK7iS4+3mX6U
A5/TR5d8mUgjU+g4rk8Kb4Mu0UlXjIB0ttov0DiNewNwIRt18jA8+o+u3dpjq+sW
T8KOEUt+zwvo/7V3LvSye0rgTBIlDHCNAymg4VMk7BPZ7hm/ELNKjD+Jo2FR3qyH
B5T0Y3HsLuJvW5iB4YlcNHlsdu87kGJ55tukmi8mxdAQ4Q7e2RCOFvu396j3x+UC
B5iPNgiV5+I3lg02dZ77DnKxHZu8A/lJBdiB3QW0KtZB6awBdpUKD9jf1b0SHzUv
KBds0pjBqAlkd25HN7rOrFleaJ1/ctaJxQZBKT5ZPt0m9STJEadao0xAH0ahmbWn
OlFuhjuefXKnEgV4We0+UXgVCwOPjdAvBbI+e0ocS3MFEvzG6uBQE3xDk3SzynTn
jh8BCNAw1FtxNrQHusEwMFxIt4I7mKZ9YIqioymCzLq9gwQbooMDQaHWBfEbwrbw
qHyGO0aoSCqI3Haadr8faqU9GY/rOPNk3sgrDQoo//fb4hVC1CLQJ13hef4Y53CI
rU7m2Ys6xt0nUW7/vGT1M0NPAgMBAAGjQjBAMA4GA1UdDwEB/wQEAwIBBjAPBgNV
HRMBAf8EBTADAQH/MB0GA1UdDgQWBBR5tFnme7bl5AFzgAiIyBpY9umbbjANBgkq
hkiG9w0BAQsFAAOCAgEAVR9YqbyyqFDQDLHYGmkgJykIrGF1XIpu+ILlaS/V9lZL
ubhzEFnTIZd+50xx+7LSYK05qAvqFyFWhfFQDlnrzuBZ6brJFe+GnY+EgPbk6ZGQ
3BebYhtF8GaV0nxvwuo77x/Py9auJ/GpsMiu/X1+mvoiBOv/2X/qkSsisRcOj/KK
NFtY2PwByVS5uCbMiogziUwthDyC3+6WVwW6LLv3xLfHTjuCvjHIInNzktHCgKQ5
ORAzI4JMPJ+GslWYHb4phowim57iaztXOoJwTdwJx4nLCgdNbOhdjsnvzqvHu7Ur
TkXWStAmzOVyyghqpZXjFaH3pO3JLF+l+/+sKAIuvtd7u+Nxe5AW0wdeRlN8NwdC
jNPElpzVmbUq4JUagEiuTDkHzsxHpFKVK7q4+63SM1N95R1NbdWhscdCb+ZAJzVc
oyi3B43njTOQ5yOf+1CceWxG1bQVs5ZufpsMljq4Ui0/1lvh+wjChP4kqKOJ2qxq
4RgqsahDYVvTH9w7jXbyLeiNdd8XM2w9U/t7y0Ff/9yi0GE44Za4rF2LN9d11TPA
mRGunUHBcnWEvgJBQl9nJEiU0Zsnvgc/ubhPgXRR4Xq37Z0j4r7g1SgEEzwxA57d
emyPxgcYxn/eR44/KJ4EBs+lVDR3veyJm+kXQ99b21/+jh5Xos1AnX5iItreGCc=
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
    
    
    val httpResponse2 = httpClient.send(
        HttpRequest.newBuilder(URI.create("https://api.sampleapis.com/coffee/hot")).GET().build(),
        HttpResponse.BodyHandlers.ofString()
    )
    
    

    println(httpResponse.body())
    println(httpResponse2.body())
}

