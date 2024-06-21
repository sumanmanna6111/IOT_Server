package com.suman.iotserver;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.TextView;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;

import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<WebSocket> sockets = new ArrayList<WebSocket>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createServer();
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        TextView textView = findViewById(R.id.wifi_ip);
        textView.setText(Formatter.formatIpAddress(wifiInfo.getIpAddress()));
    }

    private void createServer() {
        AsyncHttpServer server = new AsyncHttpServer();
        server.listen(AsyncServer.getDefault(), 5000);
        server.websocket("/", new AsyncHttpServer.WebSocketRequestCallback() {
            @Override
            public void onConnected(WebSocket webSocket, AsyncHttpServerRequest request) {
                sockets.add(webSocket);
                Log.d("TAG", "getHeaders: " + request.getHeaders().get("Sec-WebSocket-Key"));
                Log.d("TAG", "getHeaders: " + request.getHeaders().get("Secret"));//suman123
                Log.d("TAG", "getQuery: " + request.getQuery().getString("token"));//sumanmanna
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override
                    public void onStringAvailable(String s) {
                        Log.d("TAG", "onStringAvailable: " + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            if (json.optString("type").equals("broadcast")) {
                                broadCast(webSocket, json);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                webSocket.setDataCallback(new DataCallback() {
                    @Override
                    public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {

                    }
                });
                webSocket.setClosedCallback(
                        new CompletedCallback() {
                            @Override
                            public void onCompleted(Exception ex) {
                                Log.d("TAG", "setClosedCallback: " + ex);
                                sockets.remove(webSocket);
                            }
                        }
                );

                webSocket.setEndCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        Log.d("TAG", "setEndCallback: " + ex);
                    }
                });
            }

        });

    }

    private void broadCast(WebSocket webSocket, JSONObject json) {
        for (WebSocket client : sockets) {
            Log.d("TAG", "broadCast: ");
            if (client == webSocket) continue;
            client.send(json.toString());
        }
    }
}