package edu.princeton.presenterclient;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class CountDown extends Thread
{
    Handler countdown_broadcaster = null;
    public CountDown( Handler countdown_broadcaster )
    {
        this.countdown_broadcaster =
                countdown_broadcaster;
    }

    private long time_target = 0;
    public void set_time( int minutes )
    {
        this.time_target = System.currentTimeMillis(  )
                + minutes * 60 * 1000;
    }

    private long time_remain_ms(  )
    {
        return time_target -
                System.currentTimeMillis();
    }

    public String ms_to_mmss( long dt )
    {
        String sign = "-";
        if( dt < 0 )
        {
            dt   = - dt;
            sign = "+";
        }
        dt = dt / 1000;
        long mm = dt / 60;
        long ss = dt - mm * 60;
        String res = String.format( "%02d:%02d", mm, ss );
        return sign + res;
    }

    @Override
    public void run(  )
    {
        while( true )
        {
            Message msg = Message.obtain(  );
            msg.what = 740;
            msg.obj = ms_to_mmss( time_remain_ms(  ) );
            countdown_broadcaster.sendMessage( msg );
            try
            {
                sleep(1000);
            }
            catch( InterruptedException e )
            {
                Log.e("timer", "Interrupted");
            }
        }

    }
}
