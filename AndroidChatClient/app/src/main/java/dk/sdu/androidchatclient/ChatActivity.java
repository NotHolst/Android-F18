package dk.sdu.androidchatclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.URISyntaxException;
import java.util.HashMap;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A chat activity for two users.
 */
public class ChatActivity extends AppCompatActivity {

    // UI references.
    private EditText mMessageTextView;
    private TextView mConversation;

    private int roomID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mMessageTextView = (EditText) findViewById(R.id.messageText);
        mConversation = (TextView) findViewById(R.id.conversation);

        roomID = getIntent().getExtras().getInt("roomID", -1);
        if(roomID == -1){
            finish();
        }

        SocketService.getSocket().on("roomMessages", data -> {
            JSONArray messages = ((JSONArray)data[0]);
            for(int i = 0; i < messages.length(); i++){
                try {
                    String text = (String) messages.getJSONObject(i).getString("Content");
                    mConversation.append(text + "\r\n");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        SocketService.emit("getMessages", new HashMap<String, String>(){{
            put("roomID", String.valueOf(roomID));
        }});



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

        Button mSendMessageButton = findViewById(R.id.send_message);
        mSendMessageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        SocketService.getSocket().on("message", (data) -> {
            JSONObject msg = ((JSONObject)data[0]);
            try {
                mConversation.append(msg.getString("Content") + "\r\n");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

    }
    /**
     * Sends a message.
     */

    private void sendMessage() {

        HashMap<String, String> message = new HashMap<>();

        message.put("message", mMessageTextView.getText().toString());
        message.put("roomID", String.valueOf(roomID));

        SocketService.emit("sendMessage", message);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}

