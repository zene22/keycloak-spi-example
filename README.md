# keycloak-spi-example
An example of implementing a Service Provider Interface (SPI) for keycloak

When exposing keycloak in the DMZ of a project we also wanted to allow self-registration of the customers. 
In order to keep control of the registrations business asked us to provide them with email notifications, as 
soon as a new user registers. 

We didn't find a suitable way to do this with standard means, so I wrote an implementation of a SPI to achieve the goal.

## Building the Module

This is a Java maven project, so just execute ```mvn clean package```. 
This builds a ```spi-keycloak-1.0-assemblyModule.zip``` in the target directory. 

## Installing the module

Unpack the zip-File in ```${KEYCLOAK_HOME}/modules/system/layers/keycloak/```. 
In order to have this module loaded by keycloak you have to adjust the ```standalone.xml```:

~~~
        <subsystem xmlns="urn:jboss:domain:keycloak-server:1.1">
            ...
            <providers>
                <provider>
                    classpath:${jboss.home.dir}/providers/*
                </provider>
                <provider>module:info.furbach.keycloak.provider</provider>
            </providers>
            ...
~~~

## Configuring the module

The email address for  the addressee of the notifications has also to be configured in ```standalone.xml```, 
you have to add the SPI in the subsystem keycloak-server:

~~~
            <spi name="form-action">
               <provider name="registration-admin-email-action" enabled="true">
                  <properties>
                      <property name="emailAdmin" value="whoever@somewhere.com"/>
                  </properties>
               </provider>
            </spi>
~~~

You can check the configuration of the SPI in the list of providers in the server-info page of
your keycloak administration console. 

![alt text](Keycloak-Admin-Console.png "relevant part of the SPI list")


## Using the SPI

The new SPI fits in the registration flow of keycloaks registration.

Switch to your realm in the keycloak administration console. Make sure that you have correctly configured 
a mail server in the corresponding tab for the realm. 

Switch to the "Authentication" configuration and copy the original registration flow, giving the copy it a reasonable name, maybe "RegistrationWithNotification". 
Then add an execution to the Registration Form and select the new "Email to Admin" SPI. 
Save the result. Now enable the action (REQUIRED) and move it one step up.

Having done so you have to select your copy of the registration in the bindings tab for the registration. 

Good luck :)


 
