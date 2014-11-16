package edu.princeton.presenterclient;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothSender
{
    private BluetoothSocket client_socket = null;
    private OutputStream client_ostream = null;

    public BluetoothSender(  )
    {
//        Set<BluetoothDevice> bonded_dev
//                = bluetooth_adapter.getBondedDevices();
//        BluetoothDevice[  ] devices = ( BluetoothDevice[ ] )
//                bonded_dev.toArray(  );
//
//        BluetoothDevice dev = devices[ 0 ];
        BluetoothAdapter bluetooth_adapter =
                BluetoothAdapter.getDefaultAdapter(  );
        BluetoothDevice dev = bluetooth_adapter.
                getRemoteDevice( "00:25:56:D0:3F:5C" );

//        ParcelUuid[  ] uuids = dev.getUuids(  );
        try
        {
            UUID uuid = UUID.fromString
                    ( "29919d10-6d44-11e4-9803-0800200c9a66" );
            client_socket = dev.createRfcommSocketToServiceRecord
                    ( uuid );
            client_socket.connect(  );
            client_ostream = client_socket.getOutputStream();
        }
        catch( IOException e )
        {
            Log.e( "btflip", "Unable to start client" );
        }
    }


    public void send( String s )
    {
        try
        {
            client_ostream.write( s.getBytes(  ) );
        }
        catch( IOException e )
        {
            Log.e("btprt", "Error sending string");
        }
    }
}
