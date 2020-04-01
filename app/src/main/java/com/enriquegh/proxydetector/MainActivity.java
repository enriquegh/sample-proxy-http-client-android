package com.enriquegh.proxydetector;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private String TAG = "proxydetectorhttpclient";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
    }

    public String getProxyDetails() {

        String proxyAddress;
        int proxyPort;

        proxyAddress = System.getProperty( "http.proxyHost" );

        String portStr = System.getProperty( "http.proxyPort" );
        proxyPort = Integer.parseInt( ( portStr != null ? portStr : "-1" ) );


        return String.format("%s:%s",proxyAddress, proxyPort);

    }

    public void sendPublicHTTPSRequest() {
        try {
            sendRequest("http://httpbin.org/get");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendPrivateHTTPSRequest() {
        String privateURL = "ENTER PRIVATE URL HERE";
        try {
            sendRequest(privateURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshProxyData(View view) {
        String proxyURL = getProxyDetails();
        sendPublicHTTPSRequest();
        sendPrivateHTTPSRequest();

        TextView proxyResultsView = findViewById(R.id.proxyResults);
        proxyResultsView.setText(proxyURL);

    }

    public void sendRequest(String requestURI) throws Exception{

        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {

            String proxyAddress = System.getProperty( "http.proxyHost" );
            String portStr = System.getProperty( "http.proxyPort" );

            final HttpGet httpget = new HttpGet(requestURI);


            if (proxyAddress != null) {
                HttpHost proxy = new HttpHost("http", proxyAddress, Integer.parseInt( ( portStr != null ? portStr : "-1" ) ));


                RequestConfig config = RequestConfig.custom()
                        .setProxy(proxy)
                        .build();
                httpget.setConfig(config);
            }

            Log.d(TAG,"Executing request " + httpget.getMethod() + " " + httpget.getUri());

            // Create a custom response handler
            final HttpClientResponseHandler<String> responseHandler = new HttpClientResponseHandler<String>() {

                @Override
                public String handleResponse(
                        final ClassicHttpResponse response) throws IOException {
                    final int status = response.getCode();
                    if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION) {
                        final HttpEntity entity = response.getEntity();
                        try {
                            return entity != null ? EntityUtils.toString(entity) : null;
                        } catch (final ParseException ex) {
                            throw new ClientProtocolException(ex);
                        }
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            final String responseBody = httpclient.execute(httpget, responseHandler);
            Log.d(TAG,"----------------------------------------");
            Log.d(TAG,responseBody);
        }
    }


}
