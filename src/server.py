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

# List of connected clients - socket as a key, user header and name as data
clients = {}
print(f'Listening for connections on {IP}:{PORT}...')



def receive_message(client_socket):
    try:
        # Receive our "header" containing message length, it's size is defined and constant
        message_header = client_socket.recv(HEADER_LENGTH)

        # If we received no data, client gracefully closed a connectio
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

    # ready_to_read sockets: sockets we recieved some data on
    # exception_sockets sockets: sockets with some exceptions
    ready_to_read, ready_to_write, exception_sockets = select.select(sockets_list, [], [])

    # iterate over notified sockets
    for notified_socket in ready_to_read:

        # if notified socket is a server socket - new connection, accept it
        if notified_socket == server_socket:

            # Accept new connection
            # Gives us new socket - client socket
            # Other return object is ip/port set
            client_socket, client_address = server_socket.accept()

            # Client should send his name right away, receive it
            user = receive_message(client_socket)

            # If False - client disconnected before he sent his name
            if user is False:
                continue

            # Add accepted socket to select.select() list
            sockets_list.append(client_socket)

            # Also save username and username header
            clients[client_socket] = user

            client_socket.send(bytes("You are connected from:" + str(client_address) + user['data'], 'utf-8'))

        # Else existing socket is sending a message
        else:
            data = notified_socket.recv(1024)
            data = str(data, 'utf-8')
            print(data)
            if data.startswith("#"):
                clients[data[1:].lower()]=client_socket
                print ("User " + data[1:] +" added.")
                client_socket.send(bytes("Your user detail saved as : " + str(data[1:]), 'utf-8'))
            else:
                for connection in clients.values():
                    connection.send(bytes(data, 'utf-8'))
server_socket.close()