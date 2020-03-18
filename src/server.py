# Jessica, Jeffrey, Ray

import socket,select

class Server:
    def __init__(self, department, port):
        self.department = department
        self.port = port
        self.outward_sockets = []
        self.users = {}
        self.socket_list = []

    def start_socket(self):
        print(self.department + " on port " + str(self.port) + " starting up.")
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server_socket.bind(('',self.port))
        server_socket.listen(5)
        self.socket_list.append(server_socket)

        while True:
            ready_to_read,ready_to_write,in_error = select.select(self.socket_list,[],[],0)
            for sock in ready_to_read:
                if sock == server_socket:
                    connect, addr = server_socket.accept()
                    self.socket_list.append(connect)
                    self.outward_sockets.append(connect)
                    connect.send(bytes("You are connected from:" + str(addr) + " to department " + self.department, 'utf-8'))
                else:
                    data = sock.recv(1024)
                    data = str(data, 'utf-8')
                    print(data)

                    for connection in self.outward_sockets:
                        if sock != connection:
                            connection.send(bytes(data, 'utf-8'))

        server_socket.close()

    def test(self):
        print('test')

# NOTE: Uncomment this for single instance testing. It currently works!
'''
import socket,select
port = 12345
socket_list = []
users = {}
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
server_socket.bind(('',port))
server_socket.listen(5)
socket_list.append(server_socket)
outward_sockets = []


while True:
    ready_to_read,ready_to_write,in_error = select.select(socket_list,[],[],0)
    for sock in ready_to_read:
        if sock == server_socket:
            connect, addr = server_socket.accept()
            socket_list.append(connect)
            outward_sockets.append(connect)
            connect.send(bytes("You are connected from:" + str(addr), 'utf-8'))
        else:
            data = sock.recv(1024)
            data = str(data, 'utf-8')
            print(data)

            for connection in outward_sockets:
                if sock != connection:
                    connection.send(bytes(data, 'utf-8'))

server_socket.close() 
'''