#!/usr/bin/env python

from pylab import *
from time       import sleep
import bluetooth as bt

############################################################

class slide_presenter_basic:

    def __init__( self, file_list = None ):

        self.png_files = [];
        self.contents  = {};
        self.l         = 0;
        self.i         = 0;
        
        if file_list != None:
            self.set_file_list( file_list );
        #
        return;
    #

    # To be overriden by, e.g., org-mode parser
    def set_file_list( self, file_list ):
        for n in file_list:
            self.png_files.append( n )
            with open( n.replace( '.png', '.txt' ) ) as f:
                self.contents[ n ] = f.readlines(  );
            #
        #
        self.l = len( self.png_files );
        return;
    #

    # To be overridden by a full-screen mode
    def present_this( self ):
        png_name = self.png_files[ self.i ];
        img      = imread( png_name );
        txt      = self.contents[ png_name ];
        print txt;
        imshow( img );
        return;
    #

    def move_forward( self, n_forward = 1 ):
        for n in n_forward:
            self.i -= 1;
            self.present_this(  );
        #
        return;
    #
#

def test_bluetooth(  ):
    addr = 'F8:8F:CA:11:E9:59';

    s = bt.BluetoothSocket( bt.RFCOMM );
    
    svc_dicts = bt.find_service\
                ( address = addr,
                  name = 'btprt' );
    port = svc_dicts[ 0 ][ 'port' ];

    s.connect( ( addr, port ) );

    for i in arange( 5 ):
        a = '';
        with open( 'example_png/%d.png' % i, 'rb' ) as f:
            a = f.read(  );
        #
        s.send( 'H' );
        s.send( a );
        print len( a );
        sleep( 2 );
    #

    s.close(  );

if __name__ == '__main__':
    test_bluetooth(  );
    

    