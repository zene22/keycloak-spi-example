# keycloak-spi-azurekeyvault

This is an example of the implementation of a Service Provider Interface (SPI) for keycloak to enable integration with [Azure Key Vault](https://azure.microsoft.com/en-us/products/key-vault). This is based on the excellent work done [here](|https://github.com/zene22/keycloak-spi-example) 

## Use-Case

Provide a way to securely store secrets into an Azure Key Vault of choice, rather than rely on the database.

This will allow easy secret rotation, allow limiting the visibility of these secrets to specific people/applications, and all the other benefits of using a vault. 

## Warning

Please note that the implementation uses an internal keycloak API to connect to Key Vault. Like all internal APIs, this one can change with any version and then the implementation will no longer work. 

More information can be found here:
[Service Provider Interfaces (SPI)](https://www.keycloak.org/docs/latest/server_development/#_providers)
[Vault SPI](https://www.keycloak.org/docs/latest/server_development/#_vault-spi)

## Building the Module from scratch

This is a Java maven project, so just execute `mvn clean package`.
This builds a `spi-keycloak-azurekeyvault-1.0.0.jar` and a  `spi-keycloak-azurekeyvault-1.0.0-assemblyModule.zip` in the target directory.

Follow with `mvn dependency:copy-dependencies` to copy all required dependencies to the `target/dependencies` folder. 

## Installing the module (containers)

Since it is common practice to extend docker containers, the following installation process was followed to enhance the existing keycloak container. 

More information on modifying keycloak containers read: [Running Keycloak in a container](https://www.keycloak.org/server/containers)

The following process is documented in detail here: [Configuring providers](https://www.keycloak.org/server/configuration-provider)

Copy the jar file using your `dockerfile` to the folder `/opt/keycloak/providers/`

Copy all the dependencies from `target/dependencies/` into `/opt/keycloak/providers/`

Modify the build command as follows to enable the new SPI:
```dockerfile
RUN /opt/keycloak/bin/kc.sh build --spi-vault-azure-key-vault-enabled=true
```

Issue a `docker build` command to compile your new container

### Configuring the module

The vault name that holds our secrets and the authentication method to connect to Azure Active Directory also needs to be configured before starting the container.

More information on [Azure Identity](https://learn.microsoft.com/en-us/dotnet/api/overview/azure/identity-readme?view=azure-dotnet) to familiarize with the methods to get into KV.
Also, proper access to Key Vault needs to be given, including Access Policies, and Network restrictions need to be configured to properly reach KV. 

docker-compose.yml
```yaml
services:      
  keycloak:
    image:  quay.io/keycloak/keycloak:19.0.1
    command: --verbose start --optimized --spi-vault-azure-key-vault-name=<KEY VAULT NAME>
    environment:
      # Azure SP Auth Example
      AZURE_TENANT_ID: <my-tenant>
      AZURE_CLIENT_ID: <my-service-principal-id>
      AZURE_CLIENT_SECRET: <my-service-principal-secret>
    [...]
```

You can check the configuration of the SPI in the list of providers in the server-info page of your keycloak administration console. If it's all okay it should show up there.

![alt text](Keycloak-Admin-Console.png "relevant part of the SPI list")

## Using the SPI

The new SPI allows you to use the following format `${vault.KEY}` where `KEY` corresponds to the secret name that we want to pull out from the keyvault.

e.g. `${vault.TEST}` will return the secret name `TEST` in your Key Vault.


## Acknowledgements

Thanks to [zenne22](https://github.com/zene22) for his example, gave me the base I needed to solve this issue.

I cleaned up this repo a bit due to my requirements being different. But check out his repo for the full picture.