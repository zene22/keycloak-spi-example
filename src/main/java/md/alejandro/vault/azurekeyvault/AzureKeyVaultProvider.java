package md.alejandro.vault.azurekeyvault;

import org.keycloak.vault.VaultProvider;
import org.keycloak.vault.VaultRawSecret;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

public class AzureKeyVaultProvider implements VaultProvider {

    private SecretClient secretClient;
    private String realm;

    public AzureKeyVaultProvider(SecretClient secretClient, String realm) {
        this.secretClient = secretClient;
        this.realm = realm;
    }

    @Override
    public VaultRawSecret obtainSecret(String s) {
        String format = String.format(s, realm);
        KeyVaultSecret retrievedSecret = secretClient.getSecret(format);
        return new AzureKeyVaultRawSecret(retrievedSecret);
    }

    @Override
    public void close() {

    }
}
