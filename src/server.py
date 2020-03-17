# CSE 461 Project 3
# Jessica, Jeffrey, Ray

import socket
import select

HEADER_LENGTH = 10

PORT = 12345
IP = "128.208.1.135"

# Create a socket
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
server_socket.bind(('', PORT))
# Listen to new connections
server_socket.listen(5)
# List of sockets
sockets_list = [server_socket]

# list of connected clients -socket as a key, user header and name as data
clients = {}

print(f'Listening for connections on {IP}:{PORT}...')

# Handles message receiving
def receive_message(client_socket):
    try:
        # Receive our "header" containing message length, it's size is defined and constant
        message_header = client_socket.recv(HEADER_LENGTH)

        # If we received no data, client gracefully closed a connection
        if not len(message_header):
            return False

        # Convert header to int value
        message_length = int(message_header.decode('utf-8').strip())

        # Return an object of message header and message data
        return {'header': message_header, 'data': client_socket.recv(message_length)}

    except:

        # If we are here, client closed connection violently, for example by pressing ctrl+c on his script
        # or just lost his connection
        return False



while True:
    # ready_to_read sockets are sockets we received some data on
    # exception_sockets sockets are sockets to be monitored for exception
    ready_to_read, _, exception_sockets = select.select(sockets_list, [], [], 0)

    # Iterate over notified sockets
    for notified_socket in ready_to_read:

        # If notified socket is a server socket - new connection, accept it
        if notified_socket == server_socket:

            # Accept new connection
            # Gives us new socket, the other returned object is ip/port
            client_socket, client_address = server_socket.accept()

            # # Client should send his name right away, receive it
            # user = receive_message(client_socket)

            # # If False - client disconnected before he sent his name
            # if user is False:
            #     continue

            # Add accepted socket to select.select() list
            sockets_list.append(client_socket)

            # Also save username and username header
            # clients[client_socket] = user

            client_socket.send(bytes("You are connected from:" + str(client_address), 'utf-8'))

        # Else existing socket is sending a message
        else:
            # Receive message
            # data = notified_socket.recv(1024)
            # data = str(data, 'utf-8')
            # print(data)

            # Receive message
            message = receive_message(notified_socket)

            # If false, client disconnected, cleanup
            if message is False:
                print('Closed connection from: {}'.format(clients[notified_socket]['data'].decode('utf-8')))
                # Remove from list for socket.socket()
                sockets_list.remove(notified_socket)
                # Remove from our list of users
                del clients[notified_socket]
                continue

            # Get user by notified socket, so we know who sent the message
            user = clients[notified_socket]

            print(f'Received message from {user["data"].decode("utf-8")}: {message["data"].decode("utf-8")}')

            # Iterate over connected clients and broadcast message
            for client_socket in clients:
                # But don't sent it to sender
                if client_socket != notified_socket:
                    #Send user and message
                    client_socket.send(user['header'] + user['data'] + message['header'] + message['data'])



            # if data.startswith("#"):
            #     clients[data[1:].lower()]=client_socket
            #     print ("User " + data[1:] +" added.")
            #     client_socket.send(bytes("Your user detail saved as : " + str(data[1:]), 'utf-8'))
#             else:
#                 # Iterate over connected clients and broadcast message
#                 for client_socket in clients:
#
#                     connection.send(bytes(data, 'utf-8'))
# server_socket.close()