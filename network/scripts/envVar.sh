#!/bin/bash
#
# Copyright IBM Corp All Rights Reserved
#
# SPDX-License-Identifier: Apache-borrower.0
#

# This is a collection of bash functions used by different scripts

# imports
. scripts/utils.sh

export CORE_PEER_TLS_ENABLED=true
export ORDERER_CA=${PWD}/organizations/ordererOrganizations/example.com/tlsca/tlsca.example.com-cert.pem
export PEER0_lender_CA=${PWD}/organizations/peerOrganizations/lender.example.com/tlsca/tlsca.lender.example.com-cert.pem
export PEER0_borrower_CA=${PWD}/organizations/peerOrganizations/borrower.example.com/tlsca/tlsca.borrower.example.com-cert.pem
export ORDERER_ADMIN_TLS_SIGN_CERT=${PWD}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/tls/server.crt
export ORDERER_ADMIN_TLS_PRIVATE_KEY=${PWD}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/tls/server.key

# Set environment variables for the peer org
setGlobals() {
  local USING_ORG=""
  if [ -z "$OVERRIDE_ORG" ]; then
    USING_ORG=$1
  else
    USING_ORG="${OVERRIDE_ORG}"
  fi
  infoln "Using organization ${USING_ORG}"
  if [ $USING_ORG == "lender" ]; then
    export CORE_PEER_LOCALMSPID="lenderMSP"
    export CORE_PEER_TLS_ROOTCERT_FILE=$PEER0_lender_CA
    export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/lender.example.com/users/Admin@lender.example.com/msp
    export CORE_PEER_ADDRESS=localhost:7051
  elif [ $USING_ORG == "borrower" ]; then
    export CORE_PEER_LOCALMSPID="borrowerMSP"
    export CORE_PEER_TLS_ROOTCERT_FILE=$PEER0_borrower_CA
    export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/borrower.example.com/users/Admin@borrower.example.com/msp
    export CORE_PEER_ADDRESS=localhost:9051
  else
    errorln "ORG Unknown"
  fi

  if [ "$VERBOSE" == "true" ]; then
    env | grep CORE
  fi
}

# Set environment variables for use in the CLI container
setGlobalsCLI() {
  setGlobals $1

  local USING_ORG=""
  if [ -z "$OVERRIDE_ORG" ]; then
    USING_ORG=$1
  else
    USING_ORG="${OVERRIDE_ORG}"
  fi
  if [ $USING_ORG == "lender" ]; then
    export CORE_PEER_ADDRESS=peer0.lender.example.com:7051
  elif [ $USING_ORG == "borrower" ]; then
    export CORE_PEER_ADDRESS=peer0.borrower.example.com:9051
  else
    errorln "ORG Unknown"
  fi
}

# parsePeerConnectionParameters $@
# Helper function that sets the peer connection parameters for a chaincode
# operation
parsePeerConnectionParameters() {
  PEER_CONN_PARMS=()
  PEERS=""
  while [ "$#" -gt 0 ]; do
    setGlobals $1
    PEER="peer0.$1"
    ## Set peer addresses
    if [ -z "$PEERS" ]
    then
	PEERS="$PEER"
    else
	PEERS="$PEERS $PEER"
    fi
    PEER_CONN_PARMS=("${PEER_CONN_PARMS[@]}" --peerAddresses $CORE_PEER_ADDRESS)
    ## Set path to TLS certificate
    CA=PEER0_ORG$1_CA
    TLSINFO=(--tlsRootCertFiles "${PWD}/organizations/peerOrganizations/$1.example.com/peers/peer0.$1.example.com/tls/ca.crt")
    # TLSINFO=(--tlsRootCertFiles "${!CA}")
    PEER_CONN_PARMS=("${PEER_CONN_PARMS[@]}" "${TLSINFO[@]}")
    # shift by one to get to the next organization
    shift
  done
}

verifyResult() {
  if [ $1 -ne 0 ]; then
    fatalln "$1"
  fi
}
