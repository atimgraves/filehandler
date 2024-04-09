#!/bin/bash
REPO=lhr.ocir.io/frsxwtjslf35/jib-filewriter
echo Using repository $REPO
mvn package
docker build --tag $REPO/jib-filewriter:1.0.0  --file Dockerfile .
docker push $REPO/jib-filewriter:1.0.0
