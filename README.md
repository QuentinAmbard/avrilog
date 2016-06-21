##Avrilog-server and Avrilog-commun are the only working modules. Other modules are for test only.

Avrilog consume rabbitmq traces, sign the traces with custom or remote timestamp certificate and save them to HBASE

Signature & Timestamp are done in common project, see Sign.scala

Installation detail with certificate generation are provided README.me of the avrilog-server module.
