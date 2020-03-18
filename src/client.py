import socket
import threading
import sys

def sending_thrd(socket):
    while True:
        send_msg = input("Message:")
        if send_msg == 'exit':
            socket.close()
            return False
        socket.send(bytes(send_msg, 'utf-8'))

def receiving_thrd(socket):
    socket.setblocking(0)
    while True:
        try:
            recv_msg = socket.recv(1024)
            print (str(recv_msg, 'utf-8'))
        except:
            continue

try:
    if len(sys.argv) == 2:
        port = int(sys.argv[1])
    else:
        print('Incorrect number of arguments')
        print('Usage: client.py [port]')
        exit()
except:
    print('The port you entered is invalid')
    print('Usage: client.py [port]')
    exit()

client_socket = socket.socket()
ATTU6 = '128.208.1.135'
client_socket.connect((ATTU6,port))
#receive connection message from server
recv_msg = client_socket.recv(1024)
print (recv_msg)

#receive and send message from/to different user/s

threads = []
receiving_thrd = threading.Thread(target=receiving_thrd, args=(client_socket,))
receiving_thrd.start()
send_thread = threading.Thread(target=sending_thrd, args=(client_socket,))
send_thread.start()
