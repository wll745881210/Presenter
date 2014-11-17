package edu.princeton.presenterclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;

import com.google.android.glass.touchpad.GestureDetector;

public class SetTimer extends Activity
{
    private GestureDetector gesture = null;
    private int time_available = 0;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.set_timer );
        gesture = create_gesture( this );
        return;
    }

    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event )
    {
        Intent ret_timer = new Intent(  );
        if( keyCode == KeyEvent.KEYCODE_DPAD_CENTER )
            ret_timer.putExtra( "min_left", time_available );
        else if( keyCode == KeyEvent.KEYCODE_BACK )
            ret_timer.putExtra( "min_left", -1 );
        setResult( MainPresenter.request_code_set_timer,
                ret_timer );
        finish(  );
        return false;
    }

    private void show_data( int time )
    {
        TextView timer_val = ( TextView )
                findViewById( R.id.timer_val );
        timer_val.setText( Integer.toString( time ) );
    }


    private GestureDetector create_gesture( Context context )
    {
        GestureDetector ret = new GestureDetector( context );
        GestureDetector.ScrollListener scroll = new
                GestureDetector.ScrollListener(  )
                {
                    @Override
                    public boolean onScroll( float x, float dx, float v )
                    {
                        time_available += 2e-2 * dx;
                        if( time_available > 60 )
                            time_available = 60;
                        else if( time_available < 0 )
                            time_available = 0;

                        show_data( time_available );
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
