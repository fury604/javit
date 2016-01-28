Javit News Client
=================

Javit is a binary usenet client similar to Grabit.

It requires the use of a mysql/mariadb server to hold the NNTP headers
for various groups as well as other information it needs to operate.

Javit supports single and multipart binaries and can decode both
yEnc and uuencoded articles.

Javit can also be configured to automatically retrieve headers for
subscribed newsgroups.

Building Javit
--------------

Javit is written in Java and requires both Ant and Ivy to compile.
Running tests will require that a **test.properties** file be present.

There is an **experimental** build target that builds Javit into
a folder from which it can be run. In this case, Javit will need a
**javit.conf** file that contains information about the DB connection.


