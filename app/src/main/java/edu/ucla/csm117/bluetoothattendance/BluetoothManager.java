package edu.ucla.csm117.bluetoothattendance;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

/**
 * Created by matthew on 5/9/15.
 */
public class BluetoothManager {
    //discovery time in seconds minimum 0, maximum 3600, set to 0 for indefinite
    private static final int DISCOVERY_TIME=300;


    BluetoothAdapter adapter;
    HostActivity my_host_activity;
    GuestActivity my_guest_activity;
    Activity test_activity;
    final String NAME = "BluetoothAttendance";
    final UUID SERVICE_UUID = UUID.fromString("D952EB9F-7AD2-4B1B-B3CE-386735205990");
    ArrayList<UUID> UUID_LIST=new ArrayList<UUID>();
    boolean isHost;
    final int MAX_CONNECTIONS=7;
    String name;
    String studentid;
    Handler hostHandler;
    boolean keep_running=true;
    //  boolean valid_socket=false;
    final int REQUEST_ENABLE_BT = 1;



    private class AcceptThread extends Thread{
        private BluetoothServerSocket mmServerSocket;
        private InputStream mmInStream;
        private final UUID connect_uuid;
        public AcceptThread(UUID uuid) {
            connect_uuid=uuid;
        }

        public void run() {
            while (true) {
                BluetoothServerSocket tmp = null;
                try {
                    // connect_uuid is one of 7 uuids used by app, also used by the client code
                    tmp = adapter.listenUsingRfcommWithServiceRecord(NAME,connect_uuid);
                } catch (IOException e) {
                }
                mmServerSocket = tmp;
                BluetoothSocket socket = null;
                // Keep listening until exception occurs or a socket is returned
                while (true) {
                    try {
                        socket = mmServerSocket.accept();
                    } catch (Exception e) {
                        break;
                    }
                    // If a connection was accepted
                    if (socket != null) {
                        // Do work to manage the connection (in a separate thread)

                        InputStream tmpIn = null;
                        try {
                            tmpIn = socket.getInputStream();
                        } catch (IOException e) {
                            cancel();
                            break;
                        }
                        mmInStream = tmpIn;

                        String tmp_name = readStream();


                        if (tmp_name != null) {
                            Message msg = Message.obtain();
                            msg.obj = tmp_name;
                            msg.what = 1;
                            // myHandler.sendMessage(msg);
                            msg.setTarget(hostHandler);
                            msg.sendToTarget();
                            try {
                                mmInStream.close();
                            }catch(Exception e){

                            }
                        }

                        try {
                            mmServerSocket.close();
                        } catch (IOException e) {
                            break;
                        }
                        break;
                    }
                    break;
                }
            }
        }
        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }

        public String readStream(){
            int bytesRead=-1;
            int buffersize=256;
            byte[] buffer=new byte[buffersize];
            String message="";
            try {
                bytesRead = mmInStream.read(buffer);
                if (bytesRead!=-1){
                    while((bytesRead==buffersize)&&(buffer[buffersize-1]!=0)){
                        message=message+new String(buffer,0,bytesRead);
                        bytesRead=mmInStream.read(buffer);
                    }
                    message=message+new String (buffer,0,bytesRead);
                }

            }catch(Exception e){

            }
            return message;

        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private OutputStream mmOutStream;

        public boolean success=true;
        public ConnectThread(BluetoothDevice device) {
            mmSocket = null;
            mmDevice = device;
        }

        //connect to server: if socket found, manage socket
        //if socket not found or other errors, throw exception
        public void run() {
            // Cancel discovery because it will slow down the connection
            adapter.cancelDiscovery();
            BluetoothSocket tmp=null;
            //TODO:try to connect using each of the seven UUID
            for(int i=0;i<MAX_CONNECTIONS &&tmp==null;i++) {
                //used to try to connect to same socket with same uuid again
                //  for (int j = 0; j < 3 && tmp == null; j++) {
                UUID temp_uuid=UUID_LIST.get(i);
                tmp = getBluetoothSocket(mmDevice, temp_uuid);
                if (tmp != null) {
                    break;
                }

                //  }
            }
            //none of the UUIDs worked on this device
            if (tmp == null){
                success=false;
                return;
            }
            mmSocket = tmp;

            // Do work to manage the connection (in a separate thread)
            // manageConnectedSocket(mmSocket);


            OutputStream tmpOut = null;
            try {
                tmpOut = mmSocket.getOutputStream();
            }catch(IOException e){

            }
            mmOutStream=tmpOut;

            //write to outstream, set success to false on failure

            String info=name+studentid+" "+adapter.getAddress();
            if(!write(info))
                success=false;
        }


        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }

        private BluetoothSocket getBluetoothSocket(BluetoothDevice device, UUID test_uuid) {
            BluetoothSocket sock=null;
            try {
                sock = device.createInsecureRfcommSocketToServiceRecord(test_uuid);
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                sock.connect();
                return sock;
            }catch(IOException connectException){
                try{
                    sock.close();
                }catch (Exception closeException){
                }
            }
            return null;
        }

        public boolean write(String string){

            try{
                byte[] bytes=string.getBytes(Charset.forName("UTF-8"));
                int messageLength=string.getBytes(Charset.forName("UTF-8")).length;
                mmOutStream.write(bytes,0,messageLength);
            }catch(Exception e){
                return false;
            }
            return true;
        }


    }

    //constructor for the guest
    public BluetoothManager(GuestActivity activity) {
        // upon construction, get the bluetooth device
        adapter = BluetoothAdapter.getDefaultAdapter();
        this.my_guest_activity= activity;
        //7 UUIDs to use
        isHost=false;
        HelperConstructor();
    }
    //constructor for the host
    public BluetoothManager(HostActivity activity, Handler handler){
        this.adapter = BluetoothAdapter.getDefaultAdapter();
        this.my_host_activity = activity;
        Intent discoverableIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        //0 will make device discoverable indefinitely
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,DISCOVERY_TIME);
        activity.startActivity(discoverableIntent);
        isHost=true;
        hostHandler=handler;
        HelperConstructor();
    }

    private void HelperConstructor(){
        // prep bluetooth device
        //7 uuids to use
        UUID temp_UUID = UUID.fromString("D952EB9F-7AD2-4B1B-B3CE-386735205990");
        UUID_LIST.add(temp_UUID);
        temp_UUID=UUID.fromString("6109c720-fe5a-11e4-bd7f-0002a5d5c51b");
        UUID_LIST.add(temp_UUID);
        temp_UUID=UUID.fromString("77d4bd20-fe5a-11e4-9931-0002a5d5c51b");
        UUID_LIST.add(temp_UUID);
        temp_UUID=UUID.fromString("7ee1da80-fe5a-11e4-bc0c-0002a5d5c51b");
        UUID_LIST.add(temp_UUID);
        temp_UUID=UUID.fromString("847db4a0-fe5a-11e4-ac89-0002a5d5c51b");
        UUID_LIST.add(temp_UUID);
        temp_UUID=UUID.fromString("8ae2f940-fe5a-11e4-b19b-0002a5d5c51b");
        UUID_LIST.add(temp_UUID);
        temp_UUID=UUID.fromString("8fbe90a0-fe5a-11e4-b7c2-0002a5d5c51b");
        UUID_LIST.add(temp_UUID);


        if (adapter != null && !adapter.isEnabled()) {
            // if adapter isn't enabled, request for it to be turned on
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if(isHost)
                my_host_activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            else
                my_guest_activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }

    }


    public boolean server() {

       for(int i=0;i<UUID_LIST.size();i++) {
           AcceptThread connectionAcceptor = new AcceptThread(UUID_LIST.get(i));
           try {
               connectionAcceptor.start();
           } catch (Exception e) {
               return false;
           }
       }
        return true;
    }

    public boolean setName(String name) {
        return adapter.setName(name);
    }

    public boolean client(BluetoothDevice device){
        ConnectThread connectionThread = new ConnectThread(device);
        try {
            connectionThread.start();
            connectionThread.join();
        } catch (Exception e) {

            return false;
        }
        //failure

        if(connectionThread.success==false)
            return false;
        //socket was found and managed
        return true;

    }

    public boolean ready() {

        if (adapter == null || !adapter.isEnabled()) {
            // if the bluetooth device is not ready
            // throw out an error dialog
            AlertDialog alertDialog;
            if(isHost==true)
                alertDialog = new AlertDialog.Builder(this.my_host_activity).create();
            else
                alertDialog = new AlertDialog.Builder(this.my_guest_activity).create();

            // alertDialog = new AlertDialog.Builder(this.test_activity).create();
            alertDialog.setTitle("Error");
            alertDialog.setMessage("Could not access Bluetooth on this device.  Please enable Bluetooth.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();

            return false;
        }

        // device is fine
        return true;
    }
}

