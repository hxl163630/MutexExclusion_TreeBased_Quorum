# MutexExclusion_TreeBased_Quorum
This program implement a tree-based quorum system, in a client-server model.

How to run this program:

Put the two (Mutex_Client.jar, Mutex_Server.jar) files and three configuration TXT files (clients.txt, servers.txt, config.txt) in the same directory

Connect to 12 lab machines. dc21 - dc27 for servers, dc31 - dc35 for clients.

If you want to change the base time unit, you can change config.txt configuration file. The last number of each client is the base time unit.

If you want to change the lamport clock start time, you can change servers.txt or clients.txt. The last number in each line the lamport clock time.

Run servers and clients or just the config.bat file:

-In dc21, run this line from configure: java -jar Mutex_Server.jar Server-01 servers.txt files.txt

-In dc22, run this line from configure: java -jar Mutex_Server.jar Server-02 servers.txt files.txt

-In dc23, run this line from configure: java -jar Mutex_Server.jar Server-03 servers.txt files.txt

-In dc24, run this line from configure: java -jar Mutex_Server.jar Server-04 servers.txt files.txt

-In dc25, run this line from configure: java -jar Mutex_Server.jar Server-05 servers.txt files.txt

-In dc26, run this line from configure: java -jar Mutex_Server.jar Server-06 servers.txt files.txt

-In dc27, run this line from configure: java -jar Mutex_Server.jar Server-07 servers.txt files.txt

-In dc31, run this line from configure: java -jar Mutex_Client.jar Client-01 servers.txt clients.txt 100

-In dc32, run this line from configure: java -jar Mutex_Client.jar Client-02 servers.txt clients.txt 100

-In dc33, run this line from configure: java -jar Mutex_Client.jar Client-03 servers.txt clients.txt 100

-In dc34, run this line from configure: java -jar Mutex_Client.jar Client-04 servers.txt clients.txt 100

-In dc35, run this line from configure: java -jar Mutex_Client.jar Client-05 servers.txt clients.txt 100

In the five clients progrem, press 's' to start the program.
