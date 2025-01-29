package np.com.parts.API


import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIOEngineConfig
import java.security.cert.X509Certificate
import javax.net.ssl.*
import java.security.cert.CertificateException
import java.util.concurrent.TimeUnit

class EnhancedTrustManager : X509TrustManager {
    companion object {
        private const val MAX_CERTIFICATE_VALIDITY_DAYS = 730 // 2 years
         val TRUSTED_HOSTNAMES = setOf("a.khalti.com")
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        if (chain.isEmpty()) {
            throw CertificateException("Certificate chain is empty")
        }

        // Validate each certificate in the chain
        chain.forEachIndexed { index, cert ->
            validateCertificate(cert, index == 0, chain.size)
        }
    }

    private fun validateCertificate(cert: X509Certificate, isLeafCertificate: Boolean, chainLength: Int) {
        // Check certificate validity period
        try {
            cert.checkValidity()
        } catch (e: Exception) {
            throw CertificateException("Certificate is not valid: ${e.message}")
        }

        // Limit maximum certificate validity
        val certValidityDays = TimeUnit.MILLISECONDS.toDays(
            cert.notAfter.time - cert.notBefore.time
        )
        if (certValidityDays > MAX_CERTIFICATE_VALIDITY_DAYS) {
            throw CertificateException("Certificate validity exceeds allowed duration")
        }

        // Additional checks for leaf certificate
        if (isLeafCertificate) {
            validateLeafCertificate(cert)
        }

        // Verify chain length
        if (chainLength > 5) {
            throw CertificateException("Certificate chain too long")
        }
    }

    private fun validateLeafCertificate(cert: X509Certificate) {
        // Hostname validation (simplified example)
        val subjectAlternativeNames = cert.subjectAlternativeNames
        val validHostname = subjectAlternativeNames?.any {
            it.size >= 2 && TRUSTED_HOSTNAMES.contains(it[1].toString())
        } ?: false

        if (!validHostname) {
            throw CertificateException("Certificate hostname does not match trusted domains")
        }
    }

    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        throw CertificateException("Client certificate validation not supported")
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
}

// SSL Configuration Extension
fun HttpClientConfig<CIOEngineConfig>.configureSslValidation() {
    engine {
        https {
            trustManager = EnhancedTrustManager()

             HostnameVerifier { hostname, _ ->
                hostname in EnhancedTrustManager.TRUSTED_HOSTNAMES
            }
        }
    }
}