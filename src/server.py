# Jessica, Jeffrey, Ray

import socket,select

class Server:
    def __init__(self, department, port):
        super.__init__()
        this.department = department
        this.port = port
        this.outward_sockets = []
        this.users = {}
        this.socket_list = []
        start_socket()

    def start_socket(self):
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
                    connect.send(bytes("You are connected from:" + str(addr), 'utf-8'))
                else:
                    
                    data = sock.recv(1024)
                    data = str(data, 'utf-8')
                    print(data)
                    '''
                    if data.startswith("#"):
                        users[data[1:].lower()]=connect
                        print ("User " + data[1:] +" added.")
                        connect.send(bytes("Your user detail saved as : "+str(data[1:]), 'utf-8'))
                    else:
                    '''
                    for connection in self.outward_sockets:
                        if sock != connection:
                            connection.send(bytes(data, 'utf-8'))
        server_socket.close()