package edu.princeton.presenterclient;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;

public class MainPresenter extends Activity
{
    private BluetoothReceiver bluetooth_recv = null;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        this.requestWindowFeature(Window.FEATURE_NO_TITLE );
        setContentView( R.layout.main_presenter );

        bluetooth_recv =
                new BluetoothReceiver( bt_broadcaster );
        bluetooth_recv.start();
    }


    private void show_figure( Bitmap img )
    {
        ImageView image = ( ImageView )
                findViewById( R.id.slide_show );
        image.setImageBitmap( img );
    }

    private Handler bt_broadcaster = new Handler(  )
    {
        @Override
        public void handleMessage( Message msg )
        {
            this.obtainMessage(  );
            switch( msg.what )
            {
                case 738:
                    Bitmap img = ( Bitmap ) msg.obj;
                    show_figure( img );
                    break;
                case -1:
                    Log.e( "btprt", "Restarting" );
                    bluetooth_recv.shutdown_server();
                    bluetooth_recv = null;
                    bluetooth_recv = new BluetoothReceiver( this );
                    bluetooth_recv.start();
                    break;
            }
        }
    };
}
