package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    static final String[] REMOTE_PORTS = {"11108", "11112", "11116", "11120", "11124"};
    static final int SERVER_PORT = 10000;
    static final String TAG = "Error";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */

        final TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        //taken from PA1
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }

        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */

        final EditText editText = (EditText) findViewById(R.id.editText1);

        final Button button = (Button) findViewById(R.id.button4);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    //taken from PA1
                    String msg = editText.getText().toString() + "\n";
                    editText.setText(""); // This is one way to reset the input box.

                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg);
                }
            });
        }


    private class ServerTask extends AsyncTask<ServerSocket, String, Void>  {
        int insertCount = 0;
        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            while (true) {
                try { // taken from PA1
                    Socket s = serverSocket.accept();
                    BufferedReader B = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String reader = B.readLine();
                    publishProgress(reader);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        protected void onProgressUpdate(String... strings)  {

            String strReceived = strings[0].trim();         //taken from PA1

            //As given in PA2A documentation
            ContentValues keyValueToInsert = new ContentValues();
            keyValueToInsert.put("key", insertCount);
            keyValueToInsert.put("value", strReceived);

            insertCount++;

            String authority="edu.buffalo.cse.cse486586.groupmessenger1.provider";
            String scheme="content";

            //taken from OnPTestClickListener.java

            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            Uri providerUri=uriBuilder.build();

            //As given in PA2A documentation

            Uri newUri = getContentResolver().insert(
                    providerUri,    // assume we already created a Uri object with our provider URI
                    keyValueToInsert
            );

            final TextView textview = (TextView) findViewById(R.id.textView1);
            textview.setText(strReceived + "\n");


        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void>  {
        @Override
        protected Void doInBackground(String... msgs)   {
            try {   //taken from PA1
                for (String callPort : REMOTE_PORTS) {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(callPort));

                    String msgToSend = msgs[0];
                    DataOutputStream OS = new DataOutputStream(socket.getOutputStream());
                    OS.writeBytes(msgToSend + "\n");

                    socket.close();
                }
            }
            catch (UnknownHostException e)   {
                Log.e(TAG, "Unknown Host ");
            }
            catch (IOException e)   {
                Log.e(TAG, "I/O Error");
            }
            return null;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }


}
