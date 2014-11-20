#!/usr/bin/env python

from subprocess import Popen, PIPE

from time        import sleep
import bluetooth as bt
from threading   import Thread
from pyscreenshot import grab, grab_to_file

import sys
import os
import re

import pdb

# from impressive import *

############################################################

def key_simulate( key ):
    p = Popen( [ 'xte' ], stdin = PIPE );
    p.communicate( input = 'key ' + key + '\n' );
    return;
#

class bt_trans ( Thread ):
    def __init__( self, txt_name ):
        super( bt_trans, self ).__init__(  );
        self.txt_name    = txt_name;

        self.page_data   = 'Initial...';
        self.i           = 0;
        self.send_socket = None;
        self.serv_socket = None;

        self.headlines = {  };
        self.parse_txt(  );
        # pdb.set_trace(  );
    #

    def parse_txt( self ):
        with open( self.txt_name, 'r' ) as f:
            data = f.readlines(  );
        #

        pat  = re.compile( r'(?<=SlideBegin)\d*' );
        temp = '';
        idx  = 0;
        
        for l in data:
            if 'SlideBegin' in l:
                temp = '';
                try:
                    idx = int( pat.search( l ).group(  ) );
                except:
                    idx = 0;
                #
            #
            elif 'SlideEnd' in l:
                self.headlines[ '%d' % idx ] = temp;
            #
            else:
                temp = temp + l
            #
        #
        return;
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
        self.send(  );
        return;
    #

    def send( self, i = -1 ):
        if i > 0:
            self.i = i;
        #
        else:
            self.i = 1;
        #
        
        a = '';
        file_name = 'temp_scr_shot.png';
        grab_to_file( file_name );
        cmd = 'convert %s -resize 280x140 %s' % \
              ( file_name, file_name );
        os.system( cmd );
        with open( file_name, 'rb' ) as f:
            a = f.read(  );
        #

        self.send_socket.send( 'F' );
        self.send_socket.send(  a  );
        print len( a );

        a = 'Page %d\n' % self.i;
        try:
            a = a + self.headlines[ '%d' % self.i ];
        except:
            pass;
        #
        self.send_socket.send( 'T' );
        self.send_socket.send(  a  );
        
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
            except KeyboardInterrupt:
                break;
            except:
                continue;
            #
        #

        while True:
            try:
                data = self.recv_client_sock.recv( 1024 );
                if "Forward" in data:
                    key_simulate( 'Right' );
                elif "Backward" in data:
                    key_simulate( 'Left' );
                #
            except KeyboardInterrupt:
                break;
            except:
                while True:
                    try:
                        self.build_recv_socket(  );
                        self.build_send_socket(  );
                        break;
                    except KeyboardInterrupt:
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
    t = bt_trans( sys.argv[ 1 ] );
    t.run(  );

    
#
    
    
    

    
