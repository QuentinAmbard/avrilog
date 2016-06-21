##Avrilog server is the only working module. Other modules are for test only.

Avrilog consume rabbitmq traces, sign the traces with custom or remote certificate and save them to HBASE

##RabbitMQ installation
See documentation on <a href="http://www.rabbitmq.com/download.html">http://www.rabbitmq.com/download.html</a>

We advise to use the last rabbitMQ version configured with HA mode on at least 2 nodes : see <a href="http://www.rabbitmq.com/ha.html">http://www.rabbitmq.com/ha.html</a>
Configure the server to user rabbitmq on ha mode with :

Add a ha polity on the avrilog-trace queue : "ha-mode":"all"
Check http://www.rabbitmq.com/ha.html to see how to declare a ha queue (bottom)

Web interface migth be installed to control queue status, users access etc.  see <a href="http://www.rabbitmq.com/management.html">http://www.rabbitmq.com/management.html</a>.

    http://server-name:15672/

Override the default configuration with your values if needed :

    rabbitmq: {
    	host: "localhost",
    	username: "guest",
    	password: "guest",
    	port: 5673,
    	trace={
    	    //Queue name
    	    queue = "avrilog-trace"
    	    //Default exchange
    	    exchange = ""
    	    //Durable queues remain active when a server restarts
    	    durable = true
    	    //Exclusive queues may only be accessed by the current connection, and are deleted when that connection closes. 
    	    exclusive = false
    	    //If set, the queue is deleted when all consumers have finished using it
    	    autodelete = false
    	    //ha parameters /!\ WARNING : with rabbitmq > 3.0, this isn't used anymore. Check http://www.rabbitmq.com/ha.html to see how to declare a ha queue (bottom)
    	    ha-mode = "all"
    	    ha-params = ""
    	}
    }

##Startup

first try to start the server with your custom conf using

    java -Dlog4j.configuration=file:/home/avrilog/log4j.properties -Dconfig.file=/home/avrilog/application.conf -jar avrilog-server.jar 

edit the init.d script with your config (sample at the root of the project), change the SERVER_HOME and add your custom parameters on AVRILOG_PARAMETER 

register the script for startup :

    update-rc.d avrilog-server defaults

create an application.conf file with your custom config (default parameters in the src/main/resources/reference.conf file)

    vim $SERVER_HOME application.conf

create a log4j file with your configuration (sample at the root of the project)

    vim $SERVER_HOME log4j.properties

-XX:OnOutOfMemoryError=kill -9
##Monitoring
At least a primitive java process monitoring might be a good idea, for example with shinken and jstat http://exchange.nagios.org/directory/Plugins/Java-Applications-and-Servers/check_jstat/details

Monitoring the size of the rabbitmq queue might be a good idea too https://github.com/jamesc/nagios-plugins-rabbitmq

A monitoring cycle will be available with the web app.

We advice to monitor server log files using for example the nagios plugin check_logfiles : http://labs.consol.de/nagios/check_logfiles/ with nrpe
use nrpe : http://nagios.sourceforge.net/docs/nrpe/NRPE.pdf

#### nrpe on remote hosts :
manual installation :

    wget http://freefr.dl.sourceforge.net/project/nagiosplug/nagiosplug/1.4.16/nagios-plugins-1.4.16.tar.gz
    tar -xzf nagios-plugins-1.4.16.tar.gz
    cd nagios-plugins-1.4.16
    sudo apt-get install libssl-dev
    ./configure --with-nagios-user=nagios --with-nagios-group=users
    make
    make install

puppet installation : see https://github.com/example42/puppet-nrpe

####Rabbitmq disconnection
If a rabbitmq node is down and doesn't restart, we delete mnesia files (backup it before).
Stop it and make sure it's stopped.
   rabbitmqctl stop_app 

   rm -rf /var/lib/rabbitmq/mnesia/*

restart the rabbitmq server

    rabbitmq-server -detached

Check cluster status (on the master and the slave)

    rabbitmqctl cluster_status

Check your cluster conf file and make sure hosts names are OK.

    less /etc/rabbitmq/rabbitmq.config

If the instance isn't in the cluster, add it
 
   rabbitmqctl join_cluster <<rabbit@rabbit1>>
   rabbitmqctl cluster_status

##Build
If the generated jar can't be lauched because of the exception 

    Caused by: java.io.FileNotFoundException: /avrilog-server_2.9.1-0.1-one-jar.jar (Aucun fichier ou dossier de ce type)

That's because you need to add hbase default file to the jar (at the root level, the file can be found in the hadoop/hbase jars in /lib folder) :

    jar -uf avrilog-server.jar core-default.xml
    jar -uf avrilog-server.jar hbase-default.xml

TODO : make it automatic

##Certificat generation

####Generating you own sign-certificate pkcs12 file :

First we create the root certificate. 
If you want to password-protect this key, add option -des3.

    openssl genrsa -out ca.key 4096

Next, we create our self-signed root CA certificate ca.crt.
You’ll need to provide an identity for your root CA:

    openssl req -new -x509 -days 1826 -key ca.key -out ca.crt

    You are about to be asked to enter information that will be incorporated
    into your certificate request.
    What you are about to enter is what is called a Distinguished Name or a DN.
    There are quite a few fields but you can leave some blank
    For some fields there will be a default value,
    If you enter '.', the field will be left blank.
    -----
    Country Name (2 letter code) [GB]:BE
    State or Province Name (full name) [Berkshire]:Brussels
    Locality Name (eg, city) [Newbury]:Brussels
    Organization Name (eg, company) [My Company Ltd]:https://DidierStevens.com
    Organizational Unit Name (eg, section) []:
    Common Name (eg, your name or your server's hostname) []:Didier Stevens (https://DidierStevens.com)
    Email Address []:didier stevens Google mail


The -x509 option is used for a self-signed certificate. 1826 days gives us a cert valid for 5 years.

Next step: create our subordinate CA that will be used for the actual signing. First, generate the key:

    openssl genrsa -des3 -out ia.key 4096
    
Then, request a certificate for this subordinate CA:

    openssl req -new -key ia.key -out ia.csr
    
    You are about to be asked to enter information that will be incorporated
    into your certificate request.
    What you are about to enter is what is called a Distinguished Name or a DN.
    There are quite a few fields but you can leave some blank
    For some fields there will be a default value,
    If you enter '.', the field will be left blank.
    -----
    Country Name (2 letter code) [GB]:BE
    State or Province Name (full name) [Berkshire]:Brussels
    Locality Name (eg, city) [Newbury]:Brussels
    Organization Name (eg, company) [My Company Ltd]:https://DidierStevens.com
    Organizational Unit Name (eg, section) []:Didier Stevens Code Signing (https://DidierStevens.com)
    Common Name (eg, your name or your server's hostname) []:
    Email Address []:didier stevens Google mail
    
    Please enter the following 'extra' attributes
    to be sent with your certificate request
    A challenge password []:
    An optional company name []:
    
Next step: process the request for the subordinate CA certificate and get it signed by the root CA.

    openssl x509 -req -days 730 -in ia.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out ia.crt

    Signature ok
    subject=/C=FR/ST=Some-State/L=Paris/O=Avrilog/OU=avrilog/CN=avrilog
    Getting CA Private Key
    
   
The cert will be valid for 2 years (730 days) and I decided to choose my own serial number 01 for this cert (-set_serial 01). For the root CA, I let OpenSSL generate a random serial number.


You can next package the files in a pkcs12 :

    openssl pkcs12 -export -out ia.p12 -inkey ia.key -in ia.crt -chain -CAfile ca.crt
    
    Enter Export Password:
    Verifying - Enter Export Password:
    
    
more info here : http://blog.didierstevens.com/2008/12/30/howto-make-your-own-cert-with-openssl/


####Generating you own timestamp certificate file :

generate your root certificate (or re-use the last if you have it)

    openssl genrsa -out tsaroot.key 4096
    openssl req -new -x509 -days 1826 -key tsaroot.key -out tsaroot.crt

Adding the timestamping extendedKeyUsage in the openssl.cnf with a [v3_tsa] and using the  -config openssl.cnf -extensions v3_tsa
create a file extKey.cnf with the extendedKeyUsage inside

    vim extKey.cnf
    extendedKeyUsage = critical,timeStamping

    openssl genrsa -des3 -out tsa.key 4096
    openssl req -new -key tsa.key -out tsa.csr
    openssl x509 -req -days 730 -in tsa.csr -CA tsaroot.crt -CAkey tsaroot.key -set_serial 01 -out tsa.crt -extfile extKey.cnf
    openssl pkcs12 -export -out tsa.p12 -inkey tsa.key -in tsa.crt -chain -CAfile tsaroot.crt
