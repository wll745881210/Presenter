package edu.princeton.presenterclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BluetoothReceiver extends Thread
{
    public static final String PROTOCOL_SCHEME_RFCOMM
            = "btprt";
    private UUID uuid = UUID.fromString
            ( "2B7ECB3A-71EE-4CDA-9D25-C287D6F957B6" );

    private BluetoothAdapter bluetooth_adapter  = null;
    private BluetoothServerSocket server_socket = null;

    private static Handler bt_broadcaster;
    public BluetoothReceiver( Handler bt_broadcaster )
    {
        BluetoothReceiver.bt_broadcaster = bt_broadcaster;
    }

    private void starter(  )
    {
        bluetooth_adapter
                = BluetoothAdapter.getDefaultAdapter(  );
        if( ! bluetooth_adapter.isEnabled(  ) )
            bluetooth_adapter.enable();
    }

    private void listen(  )
    {
        Bundle bundle = new Bundle(  );

        try
        {
            server_socket = bluetooth_adapter.
                    listenUsingRfcommWithServiceRecord
                            ( PROTOCOL_SCHEME_RFCOMM, uuid );
            BluetoothSocket socket =  server_socket.accept(  );
            Log.i("btprt", "Server sockect (re)started.");
            if( socket != null )
            {
                InputStream iStream_fig
                        = socket.getInputStream(  );

                while( iStream_fig.read( ) > -1 )
                {
                    Message msg = Message.obtain(  );
                    Bitmap img = BitmapFactory.decodeStream
                            ( iStream_fig );
                    msg.what = 738;
                    msg.obj  = img;
                    msg.setData( bundle );
                    bt_broadcaster.sendMessage( msg );
                }
            }
        }
        catch( IOException e )
        {
            Log.e( "btprt", "Connection is lost", e );
            bt_broadcaster.sendEmptyMessage( -1 );
        }
    }

    public void shutdown_server(  )
    {
        Log.i("btprt", "Server driver is interrupted.");
        if( server_socket != null )
        {
            try
            {
                server_socket.close(  );
                Log.i( "btprt", "Server socket is closed." );
            }
            catch( IOException e )
            {
                Log.e( "btprt", "Unable to close socket", e );
            }
            server_socket = null;
        }
    }

    protected void finalize(  ) throws Throwable
    {
        super.finalize(  );
        shutdown_server(  );
    }

    @Override
    public void run(  )
    {
        starter();
        listen();
    }
}