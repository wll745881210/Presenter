package edu.princeton.presenterclient;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothSender
{
    private BluetoothSocket client_socket = null;
    private OutputStream client_ostream = null;
    private BluetoothAdapter bluetooth_adapter = null;

    public BluetoothSender(  )
    {
        bluetooth_adapter =
                BluetoothAdapter.getDefaultAdapter(  );
        set_addr();
    }

    private static String old_addr = "00:25:56:D0:3F:5C";
    public void set_addr(  )
    {
        try
        {
            set_addr( old_addr );
        }
        catch ( Exception e )
        {
            Log.e( "btflip", "Unable to set old_addr" );
        }
    }

    public void set_addr( String addr ) throws IOException
    {
        BluetoothDevice dev = bluetooth_adapter.
                getRemoteDevice( addr );
        UUID uuid = UUID.fromString
                ( "29919d10-6d44-11e4-9803-0800200c9a66" );
        client_socket = dev.createRfcommSocketToServiceRecord
                ( uuid );
        client_socket.connect(  );
        client_ostream = client_socket.getOutputStream();
        Log.e( "btflip", "Unable to start client" );
        BluetoothSender.old_addr = addr;
    }

    public void send( String s )
    {
        try
        {
            client_ostream.write( s.getBytes(  ) );
        }
        catch( Exception e )
        {
            Log.e("btprt", "Error sending string");
        }
    }
    public void close(  )
    {
        try
        {
            client_socket.close();
        }
        catch( Exception e)
        {
            Log.e( "btflip", "Unable to close." );
        }
    }
}
