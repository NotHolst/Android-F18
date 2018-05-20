package dk.sdu.androidchatclient;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class NotificationService extends Service {
    private Socket socket;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            socket = IO.socket("http://localhost:3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        socket.on("message", (data) -> {
            NotificationChannel channel = new NotificationChannel(getString(R.string.app_name), "Test", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, getString(R.string.app_name))
                    .setSmallIcon(R.drawable.ic_menu_camera)
                    .setContentTitle("test")
                    .setContentText("test")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                    .setLights(Color.RED, 3000, 3000);

            notificationManager.notify(0, mBuilder.build());
        });

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        socket.close();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
