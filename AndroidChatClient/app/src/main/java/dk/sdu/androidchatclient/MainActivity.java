package dk.sdu.androidchatclient;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.github.nkzawa.emitter.Emitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Menu menu;
    private HashMap<String, Integer> friendlist; // Username and ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SocketService.setSharedPreferences(getSharedPreferences("AndroidChatApplication", 0));
        SocketService.emit("authenticate");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Please enter your friend's name");

                final EditText input = new EditText(view.getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String txt = input.getText().toString();
                        System.out.println("----> " + txt);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        SocketService.getSocket().on("newFriendAdded", (Object... data) -> {
            JSONArray friend = ((JSONArray)data[0]);

            this.runOnUiThread(() -> {
                try {
                    friendlist.putIfAbsent((String) friend.getJSONArray(0).get(1), (Integer) friend.getJSONArray(0).get(0));
                    menu.add((String) friend.getJSONArray(0).get(1));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        menu = navigationView.getMenu();
        friendlist = new HashMap<>();

        SocketService.getSocket().on("friendlistReturned", (Object... data) -> {
            JSONArray friends = ((JSONArray)data[0]);
            friendlist.clear();

            for(int i = 0; i < friends.length(); i++) {
                try {
                    friendlist.putIfAbsent((String) friends.getJSONArray(i).get(1), (Integer) friends.getJSONArray(i).get(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            this.runOnUiThread(() -> {
                menu.clear();
                menu.add(" - Friends - ");

                friendlist.forEach((key, value) -> {
                    menu.add(key);
                });
            });
        });

        SocketService.emit("getFriends");

        if(getSharedPreferences("AndroidChatApplication", 0)
                .getString("token", null) == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_logout) {
            getSharedPreferences("AndroidChatApplication", 0)
                    .edit()
                    .remove("token")
                    .commit();

            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if(friendlist.containsKey(item.getTitle())) {
            // Open chat activity w/ user0
            SocketService.emit("createRoom", new HashMap<String,String>(){{
                put("otherUserID", String.valueOf((int)friendlist.get(item.getTitle())));
            }});

            //SocketService.get
            return true;

        } else {
            return false;
        }
    }
}
