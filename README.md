#EXASOL UDF that reads from Amazon SQS

This is a User Defined Function (UDF) for EXASOL 6, written in Java.
It facilitates reading from an AWS SQS (Simple Queueing Service) queue and returns the messages
in a result set that can be written into a table.

## Status

Working prototype. 

Tested with a queue in EU_WEST_1 (Ireland) - for
another region the init() function needs to be adapted before building
the JAR. The defaultCredentialsProviderChain is used from the AWS Java SDK; adivsed
is to assign an AIM role to the EXASOL node instances that grants access to SQS.

## Installation

Build a "fat jar" that includes all dependencies by...

```
$ mvn package
```
...and upload it to an EXABucket called 'jars' (e.g.) by...
```
$ curl -X PUT -T SQSconsumer-1.0-SNAPSHOT.jar http(s)://w:<write_passwd>@<active_node_ip/dns>:<bucket_port>/jars/SQSconsumer.jar
```
Finally, setup the UDF as shown in file *SQSconsumer_UDF.sql*
