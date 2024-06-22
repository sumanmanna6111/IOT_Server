package com.suman.iotserver;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

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

public class MainService extends Service {
    AsyncHttpServer server = new AsyncHttpServer();
    ArrayList<WebSocket> sockets = new ArrayList<WebSocket>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID).setContentTitle("IOT Server Running").setContentText("").setSmallIcon(R.mipmap.ic_launcher).setContentIntent(pendingIntent).build();
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {}*/
        startForeground(1, notification);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createServer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        server.stop();
    }

    private void createServer() {
        server.listen(AsyncServer.getDefault(), 5000);
        server.websocket("/", new AsyncHttpServer.WebSocketRequestCallback() {
            @Override
            public void onConnected(WebSocket webSocket, AsyncHttpServerRequest request) {
                String secret = request.getHeaders().get("secret");
                String token = request.getQuery().getString("token");
                if (!token.equals("sumanmanna") || !secret.equals("suman123")) {
                    webSocket.close();
                    return;
                }
                Log.d("TAG", "getHeaders: " + request.getHeaders().get("Sec-WebSocket-Key"));
                Log.d("TAG", "getHeaders: " + secret);//suman123
                Log.d("TAG", "getQuery: " + token);//sumanmanna
                sockets.add(webSocket);
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

                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        Log.d("TAG", "setClosedCallback: " + request.getHeaders().get("Sec-WebSocket-Key"));
                        Log.d("TAG", "setClosedCallback: " + ex);
                        sockets.remove(webSocket);
                    }
                });

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
            if (client == webSocket) continue;
            client.send(json.toString());
        }
    }
}
