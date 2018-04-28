package com.example.farhan.photocompanionapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    public final String TAG = MainActivity.class.getSimpleName();

    //public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static final String CLIENT_TOKEN = "f6xu-kgYzto:APA91bGcZsfW6N0Q-c0DHefEjmemQ6lgtGKKwWd105itinVHH7zHTTUGAaJZSrUnlD5YOH-F_ob5U0kQfXAuWD1qAUrYe4N6dL-oV25Bgccg9ya4KetIuj87zk8ibMbkVA94TkEF-qVL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                //sendMessage(null,"test krlo bhai", "test krlo bhai", "test krlo bhai", "test krlo bhai");

                HashMap<String,String> hashMap = new HashMap<String, String>();
                hashMap.put("message","message");
                sendPushToSingleInstance(MainActivity.this, hashMap , CLIENT_TOKEN);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        return super.onOptionsItemSelected(item);
    }

    public static void sendPushToSingleInstance(final Context activity, final HashMap dataValue /*your data from the activity*/, final String instanceIdToken /*firebase instance token you will find in documentation that how to get this*/ ) {


        final String url = "https://fcm.googleapis.com/fcm/send";
        StringRequest myReq = new StringRequest(Request.Method.POST,url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(activity, "Bingo Success", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(activity, "Oops error", Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            public byte[] getBody() throws com.android.volley.AuthFailureError {
                Map<String, Object> rawParameters = new Hashtable();
                rawParameters.put("data", new JSONObject(dataValue));
                rawParameters.put("to", instanceIdToken);
                return new JSONObject(rawParameters).toString().getBytes();
            };

            public String getBodyContentType()
            {
                return "application/json; charset=utf-8";
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "key="+"AIzaSyBsYzvLIv2l0zLkFYN753Hi9xWAE-yrNKE");
                return headers;
            }

        };

        Volley.newRequestQueue(activity).add(myReq);
    }



//
//    public static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
//    OkHttpClient mClient = new OkHttpClient();
//    public void sendMessage(final JSONArray recipients, final String title, final String body, final String icon, final String message) {
//
//        new AsyncTask<String, String, String>() {
//            @Override
//            protected String doInBackground(String... params) {
//                try {
//                    JSONObject root = new JSONObject();
//                    JSONObject notification = new JSONObject();
//                    notification.put("body", body);
//                    notification.put("title", title);
//                    //notification.put("icon", icon);
//
//                    JSONObject data = new JSONObject();
//                    data.put("message", message);
//
//                    root.put("notification", notification);
//                    root.put("data", data);
//                    //root.put("registration_ids", recipients);
//
//                    String result = postToFCM(root.toString());
//                    Log.d(TAG, "Result: " + result);
//                    return result;
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(String result) {
//                try {
//                    Log.d(TAG,"result: "+result);
//                    JSONObject resultJson = new JSONObject(result);
//                    int success, failure;
//                    success = resultJson.getInt("success");
//                    failure = resultJson.getInt("failure");
//                    Toast.makeText(MainActivity.this, "Message Success: " + success + "Message Failed: " + failure, Toast.LENGTH_LONG).show();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    Toast.makeText(MainActivity.this, "Message Failed, Unknown error occurred.", Toast.LENGTH_LONG).show();
//                }
//            }
//        }.execute();
//    }
//
//    String postToFCM(String bodyString) throws IOException {
//
//        RequestBody body = RequestBody.create(JSON, bodyString);
//        Request request = new Request.Builder()
//                .url(FCM_MESSAGE_URL)
//                .post(body)
//                .addHeader("Authorization", "key=" + SERVER_KEY)
//                .build();
//        Response response = mClient.newCall(request).execute();
//        return response.body().string();
//    }
}
