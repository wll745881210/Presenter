package edu.princeton.presenterclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
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
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainPresenter extends Activity
{
    private BluetoothReceiver bluetooth_recv = null;
    private BluetoothSender   bluetooth_send = null;
    private HeadAttDetect     head_att_dect  = null;
    private SensorManager     s_mngr         = null;
    private CountDown         count_down     = null;
    private GestureDetector   gesture        = null;

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
        count_down.set_time( 0 );
        count_down.start(  );

        head_att_dect  = new HeadAttDetect
                ( head_broadcaster, s_mngr );

        this.get_bluetooth_paired();

        gesture = create_gesture( this );
    }

    @Override
    public void onResume(  )
    {
        super.onResume(  );
        head_att_dect.register(  );
    }

    @Override
    public void onPause(  )
    {
        head_att_dect.unregister(  );
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

            bluetooth_send.close(  );
            bluetooth_send = null;

            super.finish(  );
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

    // Setting-up activity
    private static final int menu_set_timer = Menu.FIRST;
    private static final int menu_set_angle = Menu.FIRST + 1;
    private static final int menu_set_bt    = Menu.FIRST + 10;
    private Map<String, Integer> bonded_dev_idx
            = new HashMap<String, Integer>();
    private Map<Integer, String> bonded_dev_addr
            = new HashMap<Integer, String>();


    private void get_bluetooth_paired(  )
    {
        BluetoothAdapter bluetooth_adapter =
                BluetoothAdapter.getDefaultAdapter(  );
        Set<BluetoothDevice> bt_set
                = bluetooth_adapter.getBondedDevices(  );

        int id_current = menu_set_bt + 1;
        for( BluetoothDevice d : bt_set )
        {
            String name = d.getName(  );
            String addr = d.getAddress(  );
            bonded_dev_idx.put( name, id_current );
            bonded_dev_addr.put( id_current, addr );
            ++ id_current;
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        super.onCreateOptionsMenu( menu );

        menu.add(0, menu_set_timer, 0, "Set Timer");
        menu.add(0, menu_set_angle, 0, "Set Critical Angle");

        SubMenu bt_submenu = menu.addSubMenu
                ( 0, menu_set_bt, 0, "Set Bluetooth" );

        for( String name : bonded_dev_idx.keySet(  ) )
        {
            int id_this = bonded_dev_idx.get( name );
            bt_submenu.addSubMenu( 0, id_this, 0, name );
            Log.i( "btprt", name );
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        super.onOptionsItemSelected( item );
        int item_id = item.getItemId(  );

        if( item_id == menu_set_timer)
        {
            start_set_timer();
        }
        else if(  item_id == menu_set_angle )
        {
            start_set_angle();
        }
        else if( item_id == menu_set_bt )
        {

        }
        else
        {
            String addr = bonded_dev_addr.get( item_id );
            Log.i( "btprt", addr );
            try
            {
                bluetooth_send.close(  );
//                bluetooth_send = new BluetoothSender();
                bluetooth_send.set_addr( addr );
            }
            catch( IOException e )
            {
                Log.e( "btprt", e.getMessage() );
                return false;
            }
            show_alert( "Bluetooth connection succeeded!", addr );
        }

        return true;
    }

    public static final int request_code_set_timer = 60;
    private void start_set_timer(  )
    {
        Intent set_timer = new Intent(  );
        set_timer.setClass( MainPresenter.this,
                SetTimer.class );

        startActivityForResult(set_timer,
                request_code_set_timer);
    }

    public static final int request_code_set_angle = 61;
    private void start_set_angle(  )
    {
        Intent set_angle = new Intent(  );
        set_angle.setClass( MainPresenter.this,
                SetAngle.class );

        startActivityForResult(set_angle,
                request_code_set_angle);
    }

    @Override
    protected void onActivityResult
            ( int requestCode, int resultCode,
              Intent intent )
    {
        switch( requestCode )
        {
            case request_code_set_timer:
                Bundle timer_info = intent.getExtras();
                int min_left = timer_info.getInt("min_left");
                if( min_left > 0 )
                    count_down.set_time( min_left );
                break;
            case request_code_set_angle:
                Bundle ang_info = intent.getExtras();
                int ang = ang_info.getInt("critical_ang");
                Log.i("angle_set", Integer.toString(ang));

                if( ang > 0 )
                    head_att_dect.set_critical_angle( ang );

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
                    bluetooth_send.close(  );
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
                        try
                        {
                            bluetooth_send.send("Forward");
                        }
                        catch ( Exception e )
                        {
                            Log.e( "btprt", "Unable to send" );
                        }
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

    private void show_alert( String title, String msg )
    {
        AlertDialog alert ;

        AlertDialog.Builder builder = new AlertDialog.Builder
                ( MainPresenter.this );
        builder.setTitle( title );
        builder.setCancelable( true );
        builder.setMessage( msg );

        alert = builder.create(  );
        alert.show(  );
    }


    private GestureDetector create_gesture( Context context )
    {
        GestureDetector ret = new GestureDetector( context );
        GestureDetector.BaseListener base = new
                GestureDetector.BaseListener(  )
                {
                    @Override
                    public boolean onGesture( Gesture gesture_loc )
                    {
                        if( gesture_loc == Gesture.SWIPE_LEFT )
                        {
                            bluetooth_send.send( "Backward" );
                            return true;
                        }
                        else if( gesture_loc == Gesture.SWIPE_RIGHT )
                        {
                            bluetooth_send.send( "Forward" );
                            return true;
                        }
                        return false;
                    }
                };
        ret.setBaseListener( base );
        return ret;
    }

    @Override
    public boolean onGenericMotionEvent( MotionEvent event )
    {
        if( gesture != null )
            return gesture.onMotionEvent( event );
        return false;
    }
}
