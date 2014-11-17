package edu.princeton.presenterclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;

import com.google.android.glass.touchpad.GestureDetector;

public class SetAngle extends Activity
{
    private GestureDetector gesture = null;
    private int critical_angle = 20;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.set_angle );
        gesture = create_gesture( this );
    }

    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event )
    {
        Intent ret_angle = new Intent(  );
        if( keyCode == KeyEvent.KEYCODE_DPAD_CENTER )
            ret_angle.putExtra( "critical_ang", critical_angle);
        else if( keyCode == KeyEvent.KEYCODE_BACK )
            ret_angle.putExtra( "critical_ang", -1 );
        setResult( MainPresenter.request_code_set_timer,
                ret_angle );
        finish(  );
        return false;
    }

    private void show_data( int angle )
    {
        TextView timer_val = ( TextView )
                findViewById( R.id.timer_val );
        timer_val.setText( Integer.toString( angle  ) );
    }

    private GestureDetector create_gesture( Context context )
    {
        GestureDetector ret = new GestureDetector( context );
        GestureDetector.ScrollListener scroll = new
                GestureDetector.ScrollListener(  )
                {
                    @Override
                    public boolean onScroll
                            ( float x, float dx, float v )
                    {
                        critical_angle += 2e-2 * dx;
                        if( critical_angle > 60 )
                            critical_angle = 60;
                        else if( critical_angle < 10 )
                            critical_angle = 10;

                        show_data(critical_angle);
                        return false;
                    }
                };
        ret.setScrollListener( scroll );
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
