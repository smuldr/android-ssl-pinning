package smuldr.sslpin;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Factory for creating pinned SSLContext instances, that only accept a certain CA.
 */
public class PinnedSSLContextFactory {

    private static final String TAG = "PinnedSSLContextFactory";

    /**
     * Creates a new SSLContext instance, loading the CA from the input stream.
     *
     * @param input InputStream with CA certificate.
     * @return The new SSLContext instance.
     */
    public static SSLContext getSSLContext(InputStream input) {
        try {
            Certificate ca = loadCertificate(input);
            KeyStore keyStore = createKeyStore(ca);
            TrustManager[] trustManagers = createTrustManager(keyStore);
            return createSSLContext(trustManagers);
        } catch (CertificateException e) {
            Log.e(TAG, "Failed to create certificate factory", e);
        } catch (KeyStoreException e) {
            Log.e(TAG, "Failed to get key store instance", e);
        } catch (KeyManagementException e) {
            Log.e(TAG, "Failed to initialize SSL Context", e);
        }
        return null;
    }

    /**
     * Loads CAs from an InputStream. Could be from a resource or ByteArrayInputStream or from
     * https://www.washington.edu/itconnect/security/ca/load-der.crt.
     *
     * @param input InputStream with CA certificate.
     * @return Certificate
     * @throws CertificateException If certificate factory could not be created.
     */
    private static Certificate loadCertificate(InputStream input) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return cf.generateCertificate(input);
    }

    /**
     * Creates a key store using the certificate.
     *
     * @param ca Certificate to trust
     * @return KeyStore containing our trusted CAs.
     * @throws KeyStoreException
     */
    private static KeyStore createKeyStore(Certificate ca) throws KeyStoreException {
        try {
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
            return keyStore;
        } catch (IOException e) {
            Log.e(TAG, "Could not load key store", e);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Could not load key store", e);
        } catch (CertificateException e) {
            Log.e(TAG, "Could not load key store", e);
        }
        return null;
    }

    /**
     * Creates a TrustManager that trusts the CAs in our KeyStore.
     *
     * @param keyStore Key store with certificates to trust.
     * @return TrustManager that trusts the CAs in our key store.
     * @throws KeyStoreException If initialization fails.
     */
    private static TrustManager[] createTrustManager(KeyStore keyStore) throws KeyStoreException {
        try {
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);
            return tmf.getTrustManagers();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Failed to get trust manager factory with default algorithm", e);
        }
        return null;
    }

    /**
     * Creates an SSL Context that uses a specific trust manager.
     *
     * @param trustManagers Trust manager to use.
     * @return SSLContext that uses the trust manager.
     * @throws KeyManagementException
     */
    private static SSLContext createSSLContext(TrustManager[] trustManagers) throws
            KeyManagementException {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustManagers, null);
            return context;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Failed to initialize SSL context with TLS algorithm", e);
        }
        return null;
    }
}
