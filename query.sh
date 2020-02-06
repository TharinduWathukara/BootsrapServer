# selects three random unique ports and query the whole list of queries one by one
random_unique_ports=($(shuf -i 55556-55565 -n 3 | sort -n))
hop_count=3

for random_port in "${random_unique_ports[@]}"; do
  echo "$random_port"
done

for random_port in "${random_unique_ports[@]}"; do
    echo "READY TO QUERY THE NODE: 127.0.1.1:$random_port"
    read -p "Press any key to trigger the first query:" -n1 -s < /dev/tty
    echo ""   # new line
    while IFS= read -r p; do
        read -p "" -n1 -s < /dev/tty
        echo "SEARCHING : \"$p\""
        time=$(date +"%T")
        echo -e "$time REQ : \"$p\"" >> "./query_log.txt"
        query="SER 127.0.1.1 $random_port \"$p\" $hop_count"
        # set the message length
        query="$(printf '%04d' $((${#query}+5))) $query"
        echo -n "$query" | nc -u 127.0.1.1 $random_port &
    done <Queries.txt
done


