# Jessica, Ray

from server import Server
import threading

departments = {}

# We're going to assign each department a port
a, b = 'CSE', 12346
CSE = Server(a, b)
c, d = 'BIOE', 12347

departments[(a, b)] = CSE
departments[(c, d)] = Server(c, d)

threads = []

print(len(departments.values()))

for dept_server in departments.values():
    thrd = threading.Thread(target=dept_server.start_socket)
    threads.append(thrd)
    thrd.start()
    