from threading import Lock, Thread, Condition
from collections import defaultdict
import socket

lock = Lock()
condition = Condition(lock)

connectionToCourse = {}
courseToConnections = defaultdict(list)
courseToChatHistory = defaultdict(list)

# Send the required chat history to all client connections
def sendChats():
    while True:
        condition.acquire()
        condition.wait()
        print("Sending messages")
        for course, connections in courseToConnections.items():
            chat_history_items = courseToChatHistory[course]
            chat_history = "\n".join(chat_history_items)
            if not chat_history:
                chat_history = "Empty chat history"
            chat_history_bytes = chat_history.encode('utf-8')
            for conn in connections:
                conn.send(chat_history_bytes)
                print("Sent chat history for course {}".format(course))
        condition.release()

# Removes a connection from the mapping, if it's there
def removeConnection(conn):
    if conn in connectionToCourse:
        course = connectionToCourse[conn]
        courseToConnections[course].remove(conn)
        del connectionToCourse[conn]

# Listens for messages from client and then takes appropriate action, updating chat history
# or which course the client is viewing
def listenMessages(conn):
    with conn:
        while True:
            data = conn.recv(1024)
            if not data:
                print("Connection closed")
                removeConnection(conn)
                conn.close()
                break
            msg = data.decode("utf-8")

            lock.acquire()
            if msg.startswith("refresh"):
                # Update which chat history this client will receive
                removeConnection(conn)
                new_course = msg.split(': ')[1]
                connectionToCourse[conn] = new_course
                courseToConnections[new_course].append(conn)
                print("Updated client to receive messages from course {}".format(new_course))
            else:
                # Updates chat history with new message
                course = connectionToCourse[conn]
                courseToChatHistory[course].append(msg)
                print("Received message: {}".format(msg))
            condition.notify()  # Sends possibly updated chat history to all clients
            lock.release()

def main():
    Thread(target=sendChats).start()
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind(('', 21217))
    s.listen(5)
    try:
        while True:
            conn, _ = s.accept()
            connectionToCourse[conn] = 'udub'
            courseToConnections['udub'].append(conn)
            Thread(target=listenMessages, args=(conn,)).start()
            print("Started new thread with connection")
    except Exception as e:
        print(e)
    finally:
        s.close()

if __name__ == '__main__':
    main()