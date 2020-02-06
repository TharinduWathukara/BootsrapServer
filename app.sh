#!/bin/sh

#compile java files
javac bs/*.java
javac node/*.java

#run boostrap server
gnome-terminal --geometry 90x25+100+100 -- java bs.BootstrapServer

#run client nodes
timeout 1 gnome-terminal --geometry 80x20+1000+100 -- java node.Node 55556 Node1
timeout 1 gnome-terminal --geometry 80x20+1020+150 -- java node.Node 55557 Node2
timeout 1 gnome-terminal --geometry 80x20+1040+200 -- java node.Node 55558 Node3
timeout 1 gnome-terminal --geometry 80x20+1060+250 -- java node.Node 55559 Node4
timeout 1 gnome-terminal --geometry 80x20+1080+300 -- java node.Node 55560 Node5
timeout 1 gnome-terminal --geometry 80x20+1100+350 -- java node.Node 55561 Node6
timeout 1 gnome-terminal --geometry 80x20+1120+400 -- java node.Node 55562 Node7
timeout 1 gnome-terminal --geometry 80x20+1140+450 -- java node.Node 55563 Node8
timeout 1 gnome-terminal --geometry 80x20+1160+500 -- java node.Node 55564 Node9
timeout 1 gnome-terminal --geometry 80x20+1180+550 -- java node.Node 55565 Node10

#open terminal to send requests
gnome-terminal --geometry 90x20+100+650

