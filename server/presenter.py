#!/usr/bin/env python

from pylab import *
from time       import sleep
import bluetooth as bt
from threading import Thread
import pdb

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


class bt_trans: # ( Thread ):
    def __init__( self ):
        self.i           = 0;
        self.send_socket = None;
        self.serv_socket = None;
    #

    def build_send_socket( self ):
        print "Building send socket...";
        addr = 'F8:8F:CA:11:E9:59';
        svc_dicts = bt.find_service\
                    ( address = addr,
                      name = 'btprt' );
        port = svc_dicts[ 0 ][ 'port' ];
        self.send_socket \
            = bt.BluetoothSocket( bt.RFCOMM );
        self.send_socket.connect( ( addr, port ) );
        print "Done.";
        return;
    #

    def send( self ):
        a = '';
        with open( 'example_png/%d.png' % self.i,\
                   'rb' ) as f:
            a = f.read(  );
        #
        self.send_socket.send( 'F' );
        self.send_socket.send(  a  );
        print len( a );

        with open( 'example_png/%d.txt' % self.i, 'r' ) \
             as f:
            a = f.read(  );
        #
        self.send_socket.send( 'T' );
        self.send_socket.send(  a  );
        
        self.i += 1;
        if self.i > 4:
            self.i = 0;

        return;
    #

    def build_recv_socket( self ):
        print "Building recv socket...";
        self.serv_socket = bt.BluetoothSocket( bt.RFCOMM );
        self.serv_socket.bind( ( "", bt.PORT_ANY ) );
        self.serv_socket.listen( 1 );
        uuid = '29919d10-6d44-11e4-9803-0800200c9a66';
        bt.advertise_service\
            ( self.serv_socket, "btflip", \
              service_id = uuid );
        self.recv_client_sock, client_info \
            = self.serv_socket.accept(  );
        print "Connection built on ", client_info;
        return;
    #

    def run( self ):
        while True:
            try:
                self.build_recv_socket(  );
                self.build_send_socket(  );
                break;
            except:
                continue;
            #
        #

        while True:
            try:
                data = self.recv_client_sock.recv( 1024 );
                self.send(  );
            except:
                while True:
                    try:
                        self.build_recv_socket(  );
                        self.build_send_socket(  );
                        break;
                    except:
                        continue;
                    #
                #
            #
        #

        print "Close";
        self.recv_client_sock.close(  );
        
        self.serv_socket.close(  );
        self.send_socket.close(  );
    #
#

    

if __name__ == '__main__':
    t = bt_trans(  );
    t.run(  );
    
    

    
