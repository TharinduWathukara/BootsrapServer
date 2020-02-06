# selects three random unique ports and query the whole list of queries one by one
random_unique_ports=($(shuf -i 55556-55565 -n 2 | sort -n))

for random_port in "${random_unique_ports[@]}"; do
  echo "LEAVE THE NETWORK BY NODE: 127.0.1.1:$random_port"
  read -p "Press any key to trigger the leave:" -n1 -s < /dev/tty
  echo ""   # new line
  echo -n "0018 UNREGNODE" | nc -u 127.0.1.1 "$random_port" &
done

