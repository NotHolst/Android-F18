package dk.sdu.androidchatclient;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;

public class SocketService extends Service {
    private static Socket _socket = null;
    private static SharedPreferences _sharedPreferences = null;

    public static void setSharedPreferences(SharedPreferences sharedPreferences) {
        _sharedPreferences = sharedPreferences;
    }

    private static Socket getSocket() {
        if(_socket == null) {
            try {
                _socket = IO.socket("http://212.130.118.231:3000");
                _socket.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        return _socket;
    }

    public static void emit(String eventName, HashMap<String, String> args) {
        JSONObject json = new JSONObject();

        try {
            json.put("token", _sharedPreferences.getString("token", null));

            args.forEach((identifier, value) -> {
                try {
                    json.put(identifier, value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }



        getSocket().emit(eventName, json);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}