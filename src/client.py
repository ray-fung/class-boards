import socket
import sys
import threading
import socket
import select
import errno

HEADER_LENGTH = 10
ATTU6 = '128.208.1.135'
port = 12345
my_username = input("Username: ")

# Create a socket
client_socket = socket.socket()
# Connect to a given ip and port
client_socket.connect((ATTU6,port))
# Prepare username and header and send them
username = my_username.encode('utf-8')
username_header = "{len(username):<{HEADER_LENGTH}}".encode('utf-8')
client_socket.send(username_header + username)

while True:
    # Wait for user to input a message
    message = input(f'{my_username} > ')

    # IF message is not empty - send it
    if message:
        # Encode message to bytes, prepare header and convert to bytes like username above
        message = message.encode('utf-8')
        message_header = f"{len(message):<{HEADER_LENGTH}}".encode('utf-8')
        client_socket.send(message_header + message)

    try:
        # Now we want to loop over received messages (there might be more than one) and print them
        while True:

            # Receive our "header" containing username length, it's size is defined and constant
            username_header = client_socket.recv(HEADER_LENGTH)

            # If we received no data, server gracefully closed a connection, for example using socket.close() or socket.shutdown(socket.SHUT_RDWR)
            if not len(username_header):
                print('Connection closed by the server')
                sys.exit()

            # Convert header to int value
            username_length = int(username_header.decode('utf-8').strip())

            # Receive and decode username
            username = client_socket.recv(username_length).decode('utf-8')

            # Now do the same for message (as we received username, we received whole message, there's no need to check if it has any length)
            message_header = client_socket.recv(HEADER_LENGTH)
            message_length = int(message_header.decode('utf-8').strip())
            message = client_socket.recv(message_length).decode('utf-8')

            # Print message
            print(f'{username} > {message}')

    except IOError as e:
        # This is normal on non blocking connections - when there are no incoming data error is going to be raised
        # Some operating systems will indicate that using AGAIN, and some using WOULDBLOCK error code
        # We are going to check for both - if one of them - that's expected, means no incoming data, continue as normal
        # If we got different error code - something happened
        if e.errno != errno.EAGAIN and e.errno != errno.EWOULDBLOCK:
            print('Reading error: {}'.format(str(e)))
            sys.exit()

        # We just did not receive anything
        continue

    except Exception as e:
        # Any other exception - something happened, exit
        print('Reading error: '.format(str(e)))
        sys.exit()




# def sending_thrd(socket):
#     while True:
#         send_msg = input("Message:")
#         if send_msg == 'exit':
#             socket.close()
#             return False
#         socket.send(bytes(send_msg, 'utf-8'))
#
# def receiving_thrd(socket):
#     while True:
#         recv_msg = socket.recv(1024)
#         print (str(recv_msg, 'utf-8'))
#
#
#
# #recieve connection message from server
# recv_msg = client_socket.recv(1024)
# print (recv_msg)
# #send user details to server
# send_msg = input("Enter your user name(prefix with #):")
# client_socket.send(bytes(send_msg, 'utf-8'))
# user = send_msg[1:]
# #receive and send message from/to different user/s
#
# threads = []
# send_thread = threading.Thread(target=sending_thrd, args=(client_socket,))
# send_thread.start()
# receiving_thrd = threading.Thread(target=receiving_thrd, args=(client_socket,))
# receiving_thrd.start()
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
