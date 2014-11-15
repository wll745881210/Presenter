package edu.princeton.presenterclient;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;

public class MainPresenter extends Activity
{
    private BluetoothReceiver bluetooth_recv = null;
    private HeadAttDetect     head_att_dect  = null;
    private SensorManager     s_mngr          = null;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        this.requestWindowFeature(Window.FEATURE_NO_TITLE );
        setContentView( R.layout.main_presenter );

        s_mngr = ( SensorManager )
                getSystemService( SENSOR_SERVICE );

//        bluetooth_recv =
//                new BluetoothReceiver( bt_broadcaster );
//        bluetooth_recv.start(  );
    }

    @Override
    public void onPause(  )
    {
        head_att_dect.unregister(  );
        head_att_dect = null;
        super.onPause(  );
//        bluetooth_recv.finalize();
    }

    @Override
    public void onResume(  )
    {
        super.onResume();
        head_att_dect =
                new HeadAttDetect( head_broadcaster, s_mngr );
        head_att_dect.register();
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

    private Handler head_broadcaster = new Handler(  )
    {
        @Override
        public void handleMessage( Message msg )
        {
            this.obtainMessage(  );
            switch( msg.what )
            {
                case 739:
                    Bundle bundle = msg.getData(  );
                    double theta = bundle.getDouble("ang");
                    Log.i( "sensor", Double.toString( theta ) );
                    break;
            }
        }
    };
}
