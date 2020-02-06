Use shell scripts to simplyfy the commands

**need package "nc" - to send udp messages from terminal


Go to src folder, run following shell scripts

./app.shell
        This will run the ,
        Boostrap server in port 55555
        10 Peer nodes in ports 55556-55565

./query.sh
        This will select three random unique ports from range (55556 - 55565).
        Run all queries in all randomly selected nodes
        Need press any key to continue running queries.

./leave.sh
        This will remove 2 randomly selected peer nodes safely from the network.
