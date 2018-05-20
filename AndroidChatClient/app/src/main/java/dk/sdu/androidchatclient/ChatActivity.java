package dk.sdu.androidchatclient;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

/**
 * A chat activity for two users.
 */
public class ChatActivity extends AppCompatActivity {

    // UI references.
    private EditText mMessageTextView;
    private TextView mConversation;

    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_Chat);

        try {
            socket = IO.socket("http://localhost:3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        mMessageTextView = (EditText) findViewById(R.id.messageText);
        mConversation = (TextView) findViewById(R.id.conversation);

        mMessageTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });

        Button mSendMessageButton = (Button) findViewById(R.id.send_message);
        mSendMessageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        socket.on("message", (data) -> {
            mConversation.setText(data.toString());
        });

    }
    /**
     * Sends a message.
     */

    private void sendMessage() {

        HashMap<String, String> message = new HashMap<>();

        message.put("message", mMessageTextView.toString());

        socket.emit("sendMessage", message);
    }


}

