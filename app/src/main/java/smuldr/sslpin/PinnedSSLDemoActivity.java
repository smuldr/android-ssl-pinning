package smuldr.sslpin;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import javax.net.ssl.SSLContext;

/**
 * Demo activity that shows a form to let the user play around with SSL pinning.
 * <p/>
 * The user can choose to connect to several URLs:
 * <ol>
 * <li>a regular HTTP URL (example.com),</li>
 * <li>a regular HTTPS URL (the HTTPSNow website),</li>
 * <li>an HTTPS URL with custom CA (the CA test site for University of Washington).</li>
 * </ol>
 * The app is bundled with the certificate that has to be used to connect to the custom CA URL.
 * If the user enables SSL pinning, connections to the custom CA URL should succeed,
 * and connections to other HTTPS hosts should fail.
 *
 * @see <a href="http://developer.android.com/training/articles/security-ssl.html#UnknownCa">
 * Unknown CA at Android Developers</a>.
 */
public class PinnedSSLDemoActivity extends ActionBarActivity {

    private static final String TAG = "PinnedSSLDemoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing the actual view.
     */
    public static class PlaceholderFragment extends Fragment implements TestConnectionTask
            .Listener {

        private Spinner mUrlsSpinner;
        private Checkable mEnablePinning;
        private TextView mResultView;

        public PlaceholderFragment() {
            // required default constructor
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_main, container, false);
            if (null != root) {

                mUrlsSpinner = (Spinner) root.findViewById(R.id.urls);
                mEnablePinning = (CheckBox) root.findViewById(R.id.enable_pinning);
                mResultView = (TextView) root.findViewById(R.id.result);

                // listen for clicks on the submit button
                Button submitButton = (Button) root.findViewById(R.id.submit);
                submitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onSubmit();
                    }
                });
            }
            return root;
        }

        private SSLContext getPinnedSSLContext() throws IOException {
            InputStream input = null;
            try {
                input = getActivity().getAssets().open("load-der.crt");
                return PinnedSSLContextFactory.getSSLContext(input);
            } finally {
                if (null != input) {
                    input.close();
                }
            }
        }

        private void onSubmit() {
            mResultView.setText(getString(R.string.loading));

            SSLContext sslContext = null;
            if (mEnablePinning.isChecked()) {
                try {
                    sslContext = getPinnedSSLContext();
                } catch (IOException e) {
                    Log.e(TAG, "Failed create pinned SSL context", e);
                }
            }
            String url = (String) mUrlsSpinner.getSelectedItem();
            new TestConnectionTask(sslContext, this).execute(url);
        }

        @Override
        public void onConnectionSuccess(String result) {
            mResultView.setText(getString(R.string.success));
        }

        @Override
        public void onConnectionFailure() {
            mResultView.setText(getString(R.string.refused));
        }
    }
}
