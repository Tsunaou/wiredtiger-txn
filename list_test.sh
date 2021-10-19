#!/bin/bash
lein run test -n 127.0.0.1 --username young --password guazi13110 -w list-append --time-limit 60 --max-writes-per-key 100 --concurrency 5 --leave-db-running
