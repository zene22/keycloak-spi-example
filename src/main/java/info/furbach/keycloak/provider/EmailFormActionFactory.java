package info.furbach.keycloak.provider;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ServerInfoAwareProviderFactory;
import org.keycloak.storage.adapter.InMemoryUserAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EmailFormActionFactory implements FormAction, FormActionFactory, ServerInfoAwareProviderFactory {

    public static final String PROVIDER_ID = "registration-admin-email-action";

    private static final Logger LOG = Logger.getLogger(EmailFormActionFactory.class);

    private String emailAdmin;

    @Override
    public String getDisplayType() {
        return "Email to Admin";
    }

    @Override
    public String getReferenceCategory() {
        return "email";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Sends an email to the admin as soon as a new user registers for the application";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    public FormAction create(KeycloakSession session) {
        LOG.debug("EmailFormActionFactory create");
        LOG.debug("session: " + session);
        return this;
    }

    @Override
    public void init(Config.Scope config) {
        LOG.debug("EmailFormActionFactory init, config: " + config);
        String emailAdmin = config.get("emailAdmin");
        LOG.info("EmailFormActionFactory init, read emailAdmin: " + emailAdmin);
        this.emailAdmin = emailAdmin;
    }

    @Override
    public void postInit(KeycloakSessionFactory sessionFactory) {
        LOG.debug("EmailFormActionFactory postInit, sessionFactory: " + sessionFactory);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {
    }

    @Override
    public void validate(ValidationContext context) {
        context.success();
    }

    @Override
    public void success(FormContext context) {
        UserModel user = context.getUser();
        RealmModel realm = context.getRealm();
        UserModel admin = new InMemoryUserAdapter(context.getSession(), realm, "-1");
        admin.setEmail(emailAdmin);
        LOG.info("EmailFormActionFactory success, sent email to " + emailAdmin + " mentioning that " + user.getEmail() + " has registered!" );
        EmailSenderProvider emailSender = context.getSession().getProvider(EmailSenderProvider.class);
        try {
            emailSender.send(realm.getSmtpConfig(), admin, "Self Registration with Keycloak", "Hi Admin, a new user with the email "
                            + user.getEmail() + " has just registered with keycloak! " +
                            "This is an automatic notice.",
                    "<h3>Hi Admin,</h3>" +
                    "<p>a new user with the email " + user.getEmail() + " has just registered with keycloak! </p>" +
                    "<p>This is an automatic notice." );
        } catch (EmailException e) {
            LOG.error("EmailFormActionFactory success, could not send notification to admin", e);
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {

    }

    @Override
    public void close() {

    }

    /**
     * Return actual info about the provider. This info contains informations about providers configuration and operational conditions (eg. errors in connection to remote systems etc) which is
     * shown on "Server Info" page then.
     *
     * @return Map with keys describing value and relevant values itself
     */
    @Override
    public Map<String, String> getOperationalInfo() {
        Map<String, String> ret = new LinkedHashMap<>();
        ret.put("emailAdmin", "The address " + emailAdmin + " is configured in the standalone.xml for receiving registration notifications.");
        return ret;
    }
}
