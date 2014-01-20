package smuldr.sslpin;

import android.os.AsyncTask;
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

/**
 * Task that tries to connect to a URL with the given SSLContext. The connection only succeeds if
 * the SSLContext has a trust manager that allows the CA of the URL.
 */
public class TestConnectionTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "TestConnectionTask";
    private final SSLContext mSSLContext;
    private final Listener mListener;

    public TestConnectionTask(SSLContext sslContext, Listener listener) {
        mSSLContext = sslContext;
        mListener = listener;
    }

    @Override
    protected String doInBackground(String... params) {

        URL url;
        try {
            url = new URL(params[0]);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Failed to create test URL", e);
            return null;
        }

        try {
            return connect(url);
        } catch (IOException e) {
            return null;
        }
    }

    private String connect(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        if (null != mSSLContext && connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(mSSLContext.getSocketFactory());
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

    @Override
    protected void onPostExecute(String result) {
        if (null == result) {
            mListener.onConnectionFailure();
        } else {
            mListener.onConnectionSuccess(result);
        }
    }

    public interface Listener {
        public abstract void onConnectionSuccess(String response);
        public abstract void onConnectionFailure();
    }
}
