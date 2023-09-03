#!/bin/bash

./network.sh down
./network.sh up -ca -s couchdb
./network.sh createChannel
#sharing channel [deployCC1]
./network.sh deployCC1 -cci initLedger -ccn tayosharing -ccp ../chaincode/tayosharing/go -ccl go
#car channel [deployCC2]
./network.sh deployCC2 -cci initLedger -ccn tayocar -ccp ../chaincode/tayocar/go -ccl go


# couchdb 확인 / admin, adminpw
# localhost:5984/_utils/#login 