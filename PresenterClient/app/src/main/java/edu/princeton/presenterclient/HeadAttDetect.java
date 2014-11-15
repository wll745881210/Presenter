package edu.princeton.presenterclient;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HeadAttDetect
{
    private static Handler head_broadcaster;
    private static SensorManager s_mngr;
    private static Sensor s;
    public HeadAttDetect( Handler head_broadcaster,
                          SensorManager s_mngr )
    {
        HeadAttDetect.head_broadcaster = head_broadcaster;
        HeadAttDetect.s_mngr            = s_mngr;
        HeadAttDetect.s
                = s_mngr.getDefaultSensor( Sensor.TYPE_GRAVITY );

    }

    private class sListener implements
            SensorEventListener
    {
        @Override
        public void onAccuracyChanged
                ( Sensor sensor, int accuracy )
        {
            return;
        }

        private long t_last = 0;
        @Override
        public void onSensorChanged( SensorEvent event )
        {
            Bundle bundle = new Bundle(  );
            Message msg = Message.obtain(  );
            long t_this = event.timestamp;

            double theta = bow_down_degree(event.values);
            if( theta < 30 )
                t_last = t_this;
            if( t_this - t_last < 5e8 )
                return;
            else
                t_last = t_this;

            bundle.putDouble("ang", theta);
            msg.what = 739;
            msg.setData( bundle );
            head_broadcaster.sendMessage( msg );
        }

        private double bow_down_degree( float[  ] g )
        {
            double g_mod = Math.sqrt( g[ 0 ] * g[ 0 ]
                    + g[ 1 ] * g[ 1 ] + g[ 2 ] * g[ 2 ] );
            return( Math.acos( g[ 1 ] / g_mod ) ) / 3.1416 * 180.;
        }
    };

    private sListener s_listener = null;

    public void register()
    {
        Log.w( "sensor", "Started" );
        s_listener = new sListener();
        s_mngr.registerListener( s_listener, s, 500000 );
        return;
    }

    public void unregister(  )
    {
        Log.w( "sensor", "Stopped" );
        s_mngr.unregisterListener( s_listener );
        s_listener = null;
    }
}
