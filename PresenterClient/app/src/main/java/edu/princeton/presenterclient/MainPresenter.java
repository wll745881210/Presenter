package edu.princeton.presenterclient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class MainPresenter extends Activity
{
    private BluetoothReceiver bluetooth_recv = null;
    private BluetoothSender   bluetooth_send = null;
    private HeadAttDetect     head_att_dect  = null;
    private SensorManager     s_mngr         = null;
    private CountDown         count_down     = null;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_presenter);

        s_mngr = ( SensorManager )
                getSystemService( SENSOR_SERVICE );
        show_flip_prog( 0 );

        bluetooth_recv = new BluetoothReceiver
                ( bt_broadcaster );
        bluetooth_recv.start(  );

        bluetooth_send = new BluetoothSender(  );

        count_down = new CountDown
                ( countdown_broadcaster );
        count_down.set_time( 1 );
        count_down.start(  );

    }

    @Override
    public void onResume(  )
    {
        super.onResume(  );
        head_att_dect  = new HeadAttDetect
                ( head_broadcaster, s_mngr );
        head_att_dect.register(  );
    }

    @Override
    public void onPause(  )
    {
        head_att_dect.unregister(  );
        head_att_dect = null;
        super.onPause(  );
    }

    @Override
    public void finish(  )
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
    public boolean onKeyDown( int keyCode, KeyEvent event )
    {
        if( keyCode == KeyEvent.KEYCODE_DPAD_CENTER )
        {
            openOptionsMenu();
            return true;
        }
        else if( keyCode == KeyEvent.KEYCODE_BACK )
        {
            finish();
            System.exit( 0 );
        }
        return false;
    }

    // Timer setting-up activity
    private static final int menu_set_timer = Menu.FIRST;

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        super.onCreateOptionsMenu( menu );
        menu.add( 0, menu_set_timer, 0,
                "Set timer" );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        super.onOptionsItemSelected( item );
        switch( item.getItemId(  ) )
        {
            case menu_set_timer:
                start_set_timer();
                break;
        }
        return true;
    }

    public static final int request_code_set_timer = 60;
    private void start_set_timer(  )
    {
        Intent set_timer = new Intent(  );
        set_timer.setClass( MainPresenter.this,
                SetTimer.class );

        startActivityForResult( set_timer,
                request_code_set_timer );
    }

    @Override
    protected void onActivityResult
            ( int requestCode, int resultCode,
              Intent intent )
    {
        switch( requestCode )
        {
            case request_code_set_timer:
                Bundle alti_info = intent.getExtras();
                int min_left = alti_info.getInt( "min_left" );
                if( min_left > 0 )
                    count_down.set_time( min_left );
                break;
        }
    }
    // Present

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

    private void show_time( String s )
    {
        TextView text = ( TextView )
                findViewById( R.id.timer );
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

    // Handlers of messages.

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
                        bluetooth_send.send( "Forward" );
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
                        show_flip_prog( 5 );
                    }
                    break;
                case 738:
                    show_flip_prog( 0 );
                    break;
            }
        }
    };

    private Handler countdown_broadcaster
            = new Handler(  )
    {
        @Override
        public void handleMessage( Message msg )
        {
            this.obtainMessage(  );
            switch ( msg.what )
            {
                case 740:
                    String s = ( String ) msg.obj;
                    show_time( s );
                    break;
            }
        }
    };
}
