package smuldr.sslpin.test;

import android.test.AndroidTestCase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import smuldr.sslpin.PinnedSSLContextFactory;

public class PinnedSSLTest extends AndroidTestCase {

    private static final String TAG = "PinnedSSLTest";

    public void testFactory() {
        assertTrue("Should create an SSLContext", getPinnedSSLContext() != null);
    }

    public void testAllowCATestUrl() {
        SSLContext sslContext = getPinnedSSLContext();

        String response = null;
        boolean ioException = false;
        try {
            URL url = new URL("https://certs.cac.washington.edu/CAtest/");
            response = connect(sslContext, url);
        } catch (MalformedURLException e) {
            // ignore
        } catch (IOException e) {
            ioException = true;
        }

        assertFalse("Should be able to connect to CA test server", ioException);
        assertTrue("Should get response from CA test server", null != response &&
                response.contains("UW Services CA Test Page"));
    }

    public void testRefuseOtherAuthorities() {
        SSLContext sslContext = getPinnedSSLContext();

        boolean ioException = false;
        try {
            URL url = new URL("https://www.httpsnow.org/");
            connect(sslContext, url);
        } catch (MalformedURLException e) {
            // ignore
        } catch (IOException e) {
            ioException = true;
        }

        assertTrue("Should not be able to connect to hosts with different CAs", ioException);
    }

    public void testAllowRegularHttp() {
        SSLContext sslContext = getPinnedSSLContext();

        boolean ioException = false;
        try {
            URL url = new URL("http://google.com/");
            connect(sslContext, url);
        } catch (MalformedURLException e) {
            // ignore
        } catch (IOException e) {
            ioException = true;
        }

        assertFalse("Should be able to connect to non-SSL hosts", ioException);
    }

    /**
     * Creates a custom SSLContext instance that only accepts the CA from the University of
     * Washington test site.
     *
     * @return the created SSLContext instance.
     */
    private SSLContext getPinnedSSLContext() {
        SSLContext sslContext = null;
        try {
            InputStream input = openInputStream();
            sslContext = PinnedSSLContextFactory.getSSLContext(input);
        } catch (IOException e) {
            Log.e(TAG, "Failed to load certificate from assets", e);
        }
        return sslContext;
    }

    /**
     * Loads the custom certificate for the CA test site of University of Washington.
     *
     * @return InputStream to certificate data.
     * @throws IOException
     */
    private InputStream openInputStream() throws IOException {
        return getContext().getAssets().open("load-der.crt");
    }

    private String connect(SSLContext sslContext, URL url) throws IOException {
        URLConnection connection = url.openConnection();
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
        }
        InputStream in = connection.getInputStream();
        return readStream(in);
    }

    private String readStream(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8196);
        String line;
        StringBuilder responseContent = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            responseContent.append(line);
        }
        return responseContent.toString();
    }
}
