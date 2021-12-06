#!/bin/bash
############## WIREDTIGER ########################
# MAX-TXN-LEN == 4
for (( i = 0; i < 10; i++ )); do
    ./register_test.sh 10 4
done

for (( i = 0; i < 10; i++ )); do
    ./register_test.sh 20 4
done

for (( i = 0; i < 10; i++ )); do
    ./register_test.sh 30 4
done

for (( i = 0; i < 10; i++ )); do
    ./register_test.sh 40 4
done

for (( i = 0; i < 10; i++ )); do
    ./register_test.sh 50 4
done

for (( i = 0; i < 10; i++ )); do
    ./register_test.sh 60 4
done

# MAX-TXN-LEN == 8
for (( i = 0; i < 10; i++ )); do
    ./register_test.sh 10 8
done

for (( i = 0; i < 10; i++ )); do
    ./register_test.sh 20 8
done

for (( i = 0; i < 10; i++ )); do
    ./register_test.sh 30 8
done

for (( i = 0; i < 10; i++ )); do
    ./register_test.sh 40 8
done

for (( i = 0; i < 10; i++ )); do
    ./register_test.sh 50 8
done

for (( i = 0; i < 10; i++ )); do
    ./register_test.sh 60 8
done


# MAX-TXN-LEN == 12
for (( i = 0; i < 10; i++ )); do
    ./register_test.sh 10 12
done

for (( i = 0; i < 10; i++ )); do
    ./register_test.sh 20 12
done

for (( i = 0; i < 10; i++ )); do
    ./register_test.sh 30 12
done

for (( i = 0; i < 10; i++ )); do
    ./register_test.sh 40 12
done

for (( i = 0; i < 10; i++ )); do
    ./register_test.sh 50 12
done

for (( i = 0; i < 10; i++ )); do
    ./register_test.sh 60 12
done
