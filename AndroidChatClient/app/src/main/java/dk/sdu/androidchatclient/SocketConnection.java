package dk.sdu.androidchatclient;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URI;
import java.net.URISyntaxException;

public class SocketConnection {
    private static Socket socket = null;

    public static Socket getSocket() {
        if(socket == null) {
            try {
                socket = IO.socket("http://212.130.118.231:3000");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        return socket;
    }
}
