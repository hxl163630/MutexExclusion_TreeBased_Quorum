start java -jar Mutex_Server.jar Server-01 servers.txt
start java -jar Mutex_Server.jar Server-02 servers.txt
start java -jar Mutex_Server.jar Server-03 servers.txt
start java -jar Mutex_Server.jar Server-04 servers.txt
start java -jar Mutex_Server.jar Server-05 servers.txt
start java -jar Mutex_Server.jar Server-06 servers.txt
start java -jar Mutex_Server.jar Server-07 servers.txt

start java -jar Mutex_Client.jar Client-01 servers.txt clients.txt 1000
start java -jar Mutex_Client.jar Client-02 servers.txt clients.txt 1000
start java -jar Mutex_Client.jar Client-03 servers.txt clients.txt 1000
start java -jar Mutex_Client.jar Client-04 servers.txt clients.txt 1000
start java -jar Mutex_Client.jar Client-05 servers.txt clients.txt 1000