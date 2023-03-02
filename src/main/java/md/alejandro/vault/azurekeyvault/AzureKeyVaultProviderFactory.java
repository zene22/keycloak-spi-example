package md.alejandro.vault.azurekeyvault;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.vault.VaultProvider;
import org.keycloak.vault.VaultProviderFactory;

public class AzureKeyVaultProviderFactory implements VaultProviderFactory {
    private String keyVaultName;

    @Override
    public VaultProvider create(KeycloakSession keycloakSession) {

        String keyVaultUri = "https://" + this.keyVaultName + ".vault.azure.net";
        SecretClient secretClient = new SecretClientBuilder()
                .vaultUrl(keyVaultUri)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        return new AzureKeyVaultProvider(secretClient, keycloakSession.getContext().getRealm().getName());
    }

    @Override
    public void init(Config.Scope scope) {
        this.keyVaultName = scope.get("name");
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "azure-key-vault";
    }
}
