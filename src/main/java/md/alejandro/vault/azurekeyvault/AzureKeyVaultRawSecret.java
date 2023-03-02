package md.alejandro.vault.azurekeyvault;

import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.keycloak.vault.VaultRawSecret;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class AzureKeyVaultRawSecret implements VaultRawSecret {
    private KeyVaultSecret secret;
    public AzureKeyVaultRawSecret(KeyVaultSecret secret) {
        this.secret = secret;
    }

    @Override
    public Optional<ByteBuffer> get() {
        var array = secret.getValue().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(array);
        return Optional.of(buffer);
    }

    @Override
    public Optional<byte[]> getAsArray() {
        var array = secret.getValue().getBytes(StandardCharsets.UTF_8);
        return Optional.of(array);
    }

    @Override
    public void close() {
    }
}
