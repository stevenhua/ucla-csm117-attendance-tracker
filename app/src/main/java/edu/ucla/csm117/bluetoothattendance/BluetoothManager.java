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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by matthew on 5/9/15.
 */
public class BluetoothManager {

    BluetoothAdapter adapter;
    Activity activity;
    final String NAME = "BluetoothAttendance";
    final UUID SERVICE_UUID = UUID.fromString("D952EB9F-7AD2-4B1B-B3CE-386735205990");

    boolean valid_socket=false;
    final int REQUEST_ENABLE_BT = 1;

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // SERVICE_UUID is the app's UUID string, also used by the client code
                tmp = adapter.listenUsingRfcommWithServiceRecord(NAME, SERVICE_UUID);
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)



                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
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
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(SERVICE_UUID);
            } catch (IOException e) {
            //i think this means that the other device isnt broadcasting the same uuid
                //or device no longer has bluetooth
            }
            mmSocket = tmp;
            if(mmSocket!=null)
                valid_socket=true;
/*

            try {
                mmSocket.connect();
            }catch (Exception e){
                //connection failed, maybe incorrect uuid

            }
*/
        }


        public void run() {
            // Cancel discovery because it will slow down the connection
            adapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            // manageConnectedSocket(mmSocket);

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
    }


    public BluetoothManager(Activity activity) {
        // upon construction, get the bluetooth device

        this.adapter = BluetoothAdapter.getDefaultAdapter();
        this.activity = activity;

        // prep bluetooth device
        if (adapter != null && !adapter.isEnabled()) {
            // if adapter isn't enabled, request for it to be turned on
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public boolean server() {

        // start a new AcceptThread to accept and handle connections
        AcceptThread connectionAcceptor = new AcceptThread();
        try {
            connectionAcceptor.start();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public boolean client(BluetoothDevice device){
        ConnectThread connectionThread = new ConnectThread(device);
        try {
            connectionThread.start();
        } catch (Exception e) {
            return false;
        }
        return true;

    }

    public boolean ready() {

        if (adapter == null || !adapter.isEnabled()) {
            // if the bluetooth device is not ready
            // throw out an error dialog

            AlertDialog alertDialog = new AlertDialog.Builder(this.activity).create();
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
