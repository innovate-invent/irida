IRIDA API
---------
This project contains *only* the back-end Java API and database interaction projects. This project is the core of project IRIDA. You can use an existing front-end for IRIDA (such as ```irida-rest-api```) or implement your own front-end.

Setting Up OWLIM-Lite for IRIDA
===============================

Installing OWLIM-Lite
---------------------

1. Download Tomcat7 or your favorite servlet container.  These instructions will be written with Ubuntu and Tomcat7 in mind.
1. * On Ubuntu run *sudo apt-get install tomcat7*
2. Get the OWLIM-Lite package.
2. * OWLIM-Lite can be downloaded at *http://www.ontotext.com/owlim*.  You must first register which will email the developers.  They will (eventually) send you a download link once they get to your registration.
2. * Otherwise you can talk to Tom and he'll get you the required files.
3. Install the OWLIM-Lite .war files into your tomcat7 webapps directory.  The .war files are located in *owlim-lite/sesame_owlim/*.
3. * On Ubuntu the webapps directory is located at */var/lib/tomcat7/webapps/*.
4. Ensure tomcat7's home directory */usr/share/tomcat7* is owned by *tomcat7:tomcat7*.  If it isn't, chown the directory.  When installing with apt-get in Ubuntu this will be owned by root by default.
5. Change tomcat's port to 8888.
5. * Edit the file /etc/tomcat7/server.xml.  Change the line Connector port="8080" ... to Connector port="8888".

6. Restart tomcat7.  *sudo service tomcat7 restart*
6. * If you get a warning that you need to set JAVA_HOME, it can be modified in */etc/default/tomcat7*.  Set the variable to your JVM directory (ex: /usr/lib/jvm/java-7-oracle/).  Restart tomcat7 again after setting this variable.
7. Open a web browser and go to *http://localhost:8888/openrdf-workbench*.  This is the main web interface for accessing the OWLIM Sesame workbench.  
7. * It may ask you to set your Sesame server.  The default suggestion should work (*http://localhost:8888/openrdf-sesame*).  You may substitude localhost with your computer's name if you wish.
8. Click *New repository* on the left side.  Here you will create your OWLIM-Lite repository.
8. * **Type** - Select OWLIM-Lite.
8. * **ID** - This will be the name of this repository.  Suggested ID is *IRIDA*.
8. * **Title** - Human readable description of this repository.
8. * Click *Next*
9. Change *Base URL* to be http://*yourcomputer*/owlim.  Leave all other defaults.  Click *Create* to create your repsository.
10. Repeat Steps 7 & 8 to create a second repository with a different *ID* for testing purposes.  Suggested test ID is *test*.

Configuring IRIDA-api to use OWLIM-Lite
---------------------------------------
There are two Spring profiles for the IRIDA-api project.  One for a persistant database that will be maintained between runs (**production**), and one that will be re-populated with test data between each run (**dev**).  The *production* profile will point to your production repository, *dev* will point to your test repository.  We will set these repositories up in a configuration file.

If you used the default settings in the previous step, you shouldn't have to change any of these settings.  If you changed the name of your computer, your port number, or your repository ids, you will have to change them here.

1. Navigate to the config directory at *irida-api/src/main/resources/ca/corefacility/bioinformatics/irida/config/*
2. Edit the file *sesame.properties*
2. * Set db.uri to http://localhost:8888/openrdf-sesame.
2. * Set db.production.instance to the ID of your production repository
2. * Set db.test.instance to the ID of your test repository
2. * Set db.base_uri to http://localhost:8888/
