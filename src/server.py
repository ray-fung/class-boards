import socket
import threading

def sending_thrd(socket):
    while True:
        send_msg = input("Message:")
        if send_msg == 'exit':
            socket.close()
            return False
        socket.send(bytes(send_msg, 'utf-8'))

def receiving_thrd(socket):
    while True:
        recv_msg = socket.recv(1024)
        print (str(recv_msg, 'utf-8'))

client_socket = socket.socket()
port = 12345
ATTU6 = '128.208.1.135'
client_socket.connect((ATTU6,port))
#recieve connection message from server
recv_msg = client_socket.recv(1024)
print (recv_msg)
#send user details to server
send_msg = input("Enter your user name(prefix with #):")
client_socket.send(bytes(send_msg, 'utf-8'))
user = send_msg[1:]
#receive and send message from/to different user/s

threads = []
send_thread = threading.Thread(target=sending_thrd, args=(client_socket,))
send_thread.start()
receiving_thrd = threading.Thread(target=receiving_thrd, args=(client_socket,))
receiving_thrd.start()
"""
while True:
    recv_msg = client_socket.recv(1024)
    print (str(recv_msg, 'utf-8'))
    send_msg = input("Send your message in format [@user:message] ")
    if send_msg == 'exit':
        break
    else:
        client_socket.send(bytes(send_msg, 'utf-8'))
"""

#client_socket.close()