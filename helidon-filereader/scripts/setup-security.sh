#!/bin/bash -f
SCRIPT_NAME=`basename $0`
if [ $# -lt 2 ]
then
  echo "This $SCRIPT_NAME script usage is "
  echo "Name of user"
  echio "Password for user"
  echo "IP address of LB"
  exit -1
fi
if [ -z "$DEFAULT_CLUSTER_CONTEXT_NAME" ]
then
  CLUSTER_CONTEXT_NAME=one
else
  CLUSTER_CONTEXT_NAME="$DEFAULT_CLUSTER_CONTEXT_NAME"
fi
FILE_HANDLER_USER=$1
FILE_HANDLER_PASSWORD=$1
EXTERNAL_IP=$3

FILE_HANDLER_AUTH_NAME=file-handler-auth
FILE_HANDLER_CERT_NAME=file-handler-cert

echo "Creating filehandler auth details"
htpasswd -c -b $FILE_HANDLER_AUTH_NAME.$CLUSTER_CONTEXT_NAME $FILE_HANDLER_USER $FILE_HANDLER_PASSWORD

echo "Deleting any old file handler auth secret in cluster $CLUSTER_CONTEXT_NAME"
kubectl delete secret $FILE_HANDLER_AUTH_NAME  --context $CLUSTER_CONTEXT_NAME --ignore-if-not-found=true 
echo "Creating file handler auth secret in cluster $CLUSTER_CONTEXT_NAME"
kubectl create secret generic $FILE_HANDLER_AUTH_NAME --from-file=auth=$FILE_HANDLER_AUTH_NAME.$CLUSTER_CONTEXT_NAME --context $CLUSTER_CONTEXT_NAME

echo "Create file handler certificate"
$HOME/keys/step certificate create filehandler.$EXTERNAL_IP.nip.io tls-filehandler-$EXTERNAL_IP.crt tls-filehandler-$EXTERNAL_IP.key --profile leaf  --not-after 8760h --no-password --insecure --kty=RSA --ca $HOME/keys/root.crt --ca-key $HOME/keys/root.key

kubectl delete secret $  --context $CLUSTER_CONTEXT_NAME --ignore-if-not-found=true 
echo "Upload filehandler certificate secret to cluster $CLUSTER_CONTEXT_NAME"
kubectl create secret tls tls-filehandler --key tls-filehandler-$EXTERNAL_IP.key --cert tls-filehandler-$EXTERNAL_IP.crt --context $CLUSTER_CONTEXT_NAME
