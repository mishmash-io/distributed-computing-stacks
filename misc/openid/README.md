# JAAS LoginModule for OpenID Connect (OIDC)

This artifact contains a JAAS (Java Authentication and Authorization Service) LoginModule for OpenID Connect (OIDC).

It implements the `OAUTHBEARER` SASL (Simple Authentication and Security Layer) mechanism for authentication using OpenID Connect tokens. The implementation follows [RFC7628](https://www.rfc-editor.org/info/rfc7628/) with a few optional additions.

A notable addition is the support of Diffie-Hellman key exachange and subsequent
encryption of OIDC token exchange steps when authenticating over non-encrypted channels.

Plug this module on SASL-enabled network protocols to enable OpenID Connect authentication and authorization.

As an example - the [Quorum server](../../quorum/quorum-server/) and [Quorum client](../../quorum/quorum-client/) can be configured to use this `LoginModule`, along with an OIDC Issuer (such as Keycloak), to authenticate 
**client->server** and **server->server** connections.

## How to use

Create an OIDC client and a secret for it in your OIDC issuer.

Add this artifact to your app's classpath, then configure your JAAS login context to use this `LoginModule`. For example, create a file called `jaas.conf`:

```
my-service {
    io.mishmash.stacks.oidc.login.OIDCClientLoginModule required
        issuer="https://keycloak.example.com/auth/realms/myrealm"
        clientId="my-client-id"
        clientSecret="my-client-secret";
};

(Replace the values of `issuer`, `clientId`, and `clientSecret` with your actual OIDC issuer URL, client ID, and client secret.)

Later, when launching your app set this system property to the path to your `jaas.conf` file:

```shell
java -Djava.security.auth.login.config=/path/to/jaas.conf -jar my-app.jar
```

> [!WARNING]
>
> Make sure that your code passes the correct `name` when instantiating its
> `LoginContext`. This `name` should match the one specified in your `jaas.conf`
> file (in the example above - `my-service`).

