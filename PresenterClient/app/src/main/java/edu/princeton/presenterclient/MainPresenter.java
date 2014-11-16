package edu.princeton.presenterclient;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class MainPresenter extends Activity
{
    private BluetoothReceiver bluetooth_recv = null;
    private BluetoothSender   bluetooth_send = null;
    private HeadAttDetect     head_att_dect  = null;
    private SensorManager     s_mngr         = null;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_presenter);

        s_mngr = ( SensorManager )
                getSystemService( SENSOR_SERVICE );
        show_flip_prog( 0 );
    }

    @Override
    public void onPause(  )
    {
        try
        {
            head_att_dect.unregister(  );
            head_att_dect = null;

            bluetooth_recv.finalize(  );
            bluetooth_recv = null;

            bluetooth_send = null;

            super.onPause(  );
        }
        catch( Throwable e )
        {
            bluetooth_recv = null;
            Log.e( "btprt", "Unable to stop" );
        }
    }

    @Override
    public void onResume(  )
    {
        super.onResume(  );

        head_att_dect  = new HeadAttDetect
                ( head_broadcaster, s_mngr );
        head_att_dect.register(  );

        bluetooth_recv = new BluetoothReceiver
                ( bt_broadcaster );
        bluetooth_recv.start(  );

        bluetooth_send = new BluetoothSender(  );
    }

    private void show_figure( Bitmap img )
    {
        ImageView image = ( ImageView )
                findViewById( R.id.slide_show );
        image.setImageBitmap( img );
    }

    private void show_text( String s )
    {
        TextView text = ( TextView )
                findViewById( R.id.head_lines );
        text.setText( s );
    }

    private final int[  ] flip_id = {
            R.drawable.flip0,
            R.drawable.flip1,
            R.drawable.flip2,
            R.drawable.flip3,
            R.drawable.flip4,
            R.drawable.flip5,
    };
    private void show_flip_prog( int i )
    {
        ImageView image = ( ImageView )
                findViewById( R.id.flipper );
        Drawable dwg = getResources().getDrawable( flip_id[ i ] );
        image.setImageDrawable( dwg );
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
                case 739:
                    String s = ( String ) msg.obj;
                    show_text( s );
                    break;
                case -1:
                    Log.e( "btprt", "Restarting" );
                    bluetooth_recv.shutdown_server(  );
                    bluetooth_recv = null;
                    bluetooth_recv = new BluetoothReceiver( this );
                    bluetooth_recv.start(  );
                    bluetooth_send = null;
                    bluetooth_send = new BluetoothSender(  );
                    break;
            }
        }
    };

    private Handler head_broadcaster = new Handler(  )
    {
        private boolean lock = false;
        @Override
        public void handleMessage( Message msg )
        {
            this.obtainMessage(  );
            switch( msg.what )
            {
                case 739:
                    Bundle bundle = msg.getData(  );
                    int counter = bundle.getInt("counter");
                    Log.i( "sensor", Integer.toString( counter ) );

                    if( ( counter > 4 ) && ( ! lock ) )
                    {
                        bluetooth_send.send("Flip");
                        lock = true;
                        show_flip_prog( 5 );
                    }
                    else if( counter <= 4 )
                    {
                        show_flip_prog( counter );
                        lock = false;
                    }
                    else if( lock )
                    {
                        show_flip_prog( 0 );
                    }
                    break;
            }
        }
    };
}
