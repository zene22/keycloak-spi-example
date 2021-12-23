# keycloak-spi-example

This is an example of the implementation of a Service Provider Interface (SPI) for keycloak. We couldn't find a suitable way to accomplish the following use-case by standard means, so I wrote an implementation of an SPI to achieve the goal.

## Use-Case

When exposing keycloak in the DMZ for a project, we also wanted to enable self-registration for customers. In order to keep control of the registrations, the product owners asked us to send email notifications to an administrator as soon as a new user registers.

## Warning

Please note that the implementation uses an internal keyclaok API to send emails. Like all internal APIs, this one can change with any version and then the implementation will no longer work. To put your mind at ease: Sending emails is one of the basic functions of self-registration - so there will be an option to send emails from keycloak in the foreseeable future.

## Building the Module from scratch

This is a Java maven project, so just execute `mvn clean package`.
This builds a `spi-keycloak-emailnotification-1.0.1.jar` and a  `spi-keycloak-emailnotification-1.0.1-assemblyModule.zip` in the target directory.

## Installing the module (manual)

Unpack the zip-File in `${KEYCLOAK_HOME}/modules/system/layers/keycloak/`.
In order to have this module loaded by keycloak you have to adjust the `standalone.xml` (assuming that you are using this one and not domain.xml or others):

```
        <subsystem xmlns="urn:jboss:domain:keycloak-server:1.1">
            ...
            <providers>
                <provider>
                    classpath:${jboss.home.dir}/providers/*
                </provider>
                <provider>module:info.furbach.keycloak.provider</provider>
            </providers>
            ...
```

### Configuring the module

The email address for  the addressee of the notifications has also to be configured in `standalone.xml`,
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

You can check the configuration of the SPI in the list of providers in the server-info page of your keycloak administration console.

![alt text](Keycloak-Admin-Console.png "relevant part of the SPI list")

## Installing the module (scriptable)

It is not automatic, because you have to provide the scripts to automate the installation.

You can automatically load the provider in keycloak by putting the `spi-keycloak-emailnotification-1.0.1.jar` to the directory `${KEYCLOAK_HOME}/standalone/deployments`. It will then be loaded during startup of the container. For the lazy people I also added this jar as a release for the project.

### Configuring the module

Before the server is started you can execute the following script to configure the email address that gets the notifications with the `${KEYCLOAK_HOME}/standalone/jboss-cli.sh -f configure-provider.cli`. Don't forget to replace xxx@xxx.xxx with your desired value.

```
embed-server --server-config=standalone.xml --std-out=echo
/subsystem=keycloak-server/spi=form-action/:add
/subsystem=keycloak-server/spi=form-action/provider=registration-admin-email-action/:add(enabled=true)
/subsystem=keycloak-server/spi=form-action/provider=registration-admin-email-action/:map-put(name=properties,key=emailAdmin,value=xxx@xxx.xxx)
stop-embedded-server
```

If you want configure a running server you would need `connect` instead of `embed-server` and you will have to `reload` the server in the end.

## Using the SPI

The new SPI fits in the registration flow of keycloaks registration.

Switch to your realm in the keycloak administration console. Make sure that you have correctly configured
a mail server in the corresponding tab for the realm.

Switch to the "Authentication" configuration and copy the original registration flow, giving the copy it a reasonable name, maybe "RegistrationWithNotification".
Then add an execution to the Registration Form and select the new "Email to Admin" SPI.
Save the result. Now enable the action (REQUIRED) and move it one step up.

Having done so you have to select your copy of the registration in the bindings tab for the registration.

## Addendum: Running this Implementation with the keycloak-operator

When asked to fix a problem for this year-old project, I realized that the world had changed now. The use case is still valid, but the way software is deployed has changed. There is now also an operator for keycloak who takes over the deployment in the k8s environment. Goodbye snow-flake servers where the manual configuration is no longer traceable. But now the automation of the deployments is at the core of things, the Keycloak server is restarted from scratch with every restart (of the pod in which it is running). The configuration of the standalone.xml is not part of the persistence in the keycloak database. So, with the help of a very clever colleague, I found out how to configure the keycloak instance for the operator so that the implementation is loaded and also configured.

This is the yaml file for the keycloak instance:

```
apiVersion: keycloak.org/v1alpha1
kind: Keycloak
metadata:
  name: mykeycloak
spec:
  extensions:
  - https://github.com/zene22/keycloak-spi-example/releases/download/V1.0.1/spi-keycloak-emailnotification-1.0.1.jar
  externalAccess:
    enabled: true
  instances: 1
  keycloakDeploymentSpec:
    experimental:
      env:
      - name: ADMIN_EMAIL
        value: xxx@xxx.xxx
      volumes:
        defaultMode: 493
        items:
        - configMaps:
          - cli-script
          mountPath: /opt/jboss/startup-scripts
          name: cli-script
  podDisruptionBudget:
    enabled: true
```

 The provider implementetion is loaded under extensions. The environment variable `ADMIN_EMAIL` configures the email address for the notifications and you will have to add a ConfigMap `cli-script` mounted in the instance to configure the module with this email address. This is the needed ConfigMap:

 ```
apiVersion: v1
data:
  postconfigure.sh: |+
    #!/bin/bash
    echo "embed-server --server-config=standalone-ha.xml --std-out=echo" > /tmp/configure-provider.cli
    echo "/subsystem=keycloak-server/spi=form-action/:add" >> /tmp/configure-provider.cli
    echo "/subsystem=keycloak-server/spi=form-action/provider=registration-admin-email-action/:add(enabled=true)" >> /tmp/configure-provider.cli
    echo "/subsystem=keycloak-server/spi=form-action/provider=registration-admin-email-action/:map-put(name=properties,key=emailAdmin,value=${ADMIN_EMAIL})" >> /tmp/configure-provider.cli
    echo "stop-embedded-server" >> /tmp/configure-provider.cli
    ${JBOSS_HOME}/bin/jboss-cli.sh --file=/tmp/configure-provider.cli
kind: ConfigMap
metadata:
  name: cli-script
 ```

 For convenience I added those configurations to the `k8s` subdirectory.

 Have fun and good luck!
