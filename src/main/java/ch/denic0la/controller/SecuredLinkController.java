package ch.denic0la.controller;

import ch.denic0la.db.model.SecuredLink;
import ch.denic0la.util.KeyGenerator;
import ch.denic0la.validation.annotations.OptionalPattern;
import ch.denic0la.validation.annotations.SecondFactorKey;
import io.smallrye.common.constraint.NotNull;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.quarkus.elytron.security.common.BcryptUtil;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.validator.constraints.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;

@Path("/secured")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SecuredLinkController {

    private static final Logger log = LoggerFactory.getLogger(SecuredLinkController.class);
    @Inject
    @ConfigProperty(name = "app.base-url")
    String baseUrl;

    private String normalizedBaseUrl;

    @jakarta.annotation.PostConstruct
    void init() {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("app.base-url configuration property is mandatory");
        }
        this.normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private final SecureRandom random = new SecureRandom();

    public static class SecuredLinkRequest {
        @OptionalPattern(regexp = "^[a-zA-Z0-9-]+$", message = "accessKey must be alphanumeric or null")
        public String accessKey;
        @SecondFactorKey(length = 6, allowNull = true, message = "secondFactorKey must be 6 digits or null")
        public String secondFactorKey;
        @jakarta.validation.constraints.NotNull(message = "targetLink must not be null")
        @NotBlank(message = "targetLink must not be blank")
        @URL(message = "targetLink must be a valid URL")
        public String targetLink;
    }


    public static class SecuredLinkResponse {
        public String accessKey;
        public String secondFactorKeyRaw;
        public String secondFactorKeyHashed;
        public String targetLink;
        public String accessLink;

        public SecuredLinkResponse(SecuredLink link, String rawKey, String normalizedBaseUrl) {
            this.accessKey = link.accessKey;
            this.secondFactorKeyRaw = rawKey;
            this.secondFactorKeyHashed = link.secondFactorKey;
            this.targetLink = link.targetLink;
            this.accessLink = normalizedBaseUrl + "/secured/" + link.accessKey;
        }
    }

    @POST
    @Transactional
    public Response create(@Valid SecuredLinkRequest request) {
        String secondFactorKey = request.secondFactorKey != null ?
                request.secondFactorKey :
                KeyGenerator.generateNumericCode(6);

        try {
            SecuredLink link = new SecuredLink();
            link.accessKey = request.accessKey != null ?
                    request.accessKey :
                    KeyGenerator.generateAccessKey(8);

            link.secondFactorKey = BcryptUtil.bcryptHash(secondFactorKey);
            link.targetLink = request.targetLink;
            link.hasBeenAccessed = false;

            link.persistAndFlush();

            return Response.status(Response.Status.CREATED)
                    .entity(new SecuredLinkResponse(link, secondFactorKey, normalizedBaseUrl))
                    .build();

        } catch (PersistenceException e) {
            log.error("There WAS a collision while creating a SecuredLink:", e);
            return Response.status(Response.Status.CONFLICT)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of(
                            "message", "Resource already exists (Unique Constraint Violation)",
                            "details", e.getMessage(),
                            "status", Response.Status.CONFLICT.getStatusCode()
                    ))
                    .build();
        }
    }

    private boolean hashesTo(@NotNull String key, @NotNull SecuredLink entry) {
        return !entry.secondFactorKey.isBlank() && hashesTo(key, entry.secondFactorKey);
    }

    private boolean hashesTo(@NotNull String key, @NotNull String hashedKey) {
        return BcryptUtil.matches(key, hashedKey);
    }

    public static class SecuredLinkMinimalResponse {
        public String accessKey;
        public String response;

        public SecuredLinkMinimalResponse(String accessKey, String response) {
            this.accessKey = accessKey;
            this.response = response;
        }
    }

    public static class SecuredLinkUnlockedResponse {
        public String accessKey;
        public String targetLink;

        public SecuredLinkUnlockedResponse(String accessKey, String targetLink) {
            this.accessKey = accessKey;
            this.targetLink = targetLink;
        }
    }

    @GET
    @Path("/{accessKey}")
    @Transactional
    public Response get(
            @PathParam("accessKey") @NotBlank(message = "Blank Access Key was provided") String accessKey, @QueryParam("secondFactorKey") @SecondFactorKey(length = 6, allowNull = false, message = "There was no valid Second Factor Key provided") String secondFactorKey) {

        SecuredLink link = SecuredLink.find("accessKey", accessKey).firstResult();

        if (link == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity( Map.of("error","Link not found"))
                    .build();
        }

        if (!hashesTo(secondFactorKey, link)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Invalid second factor key"))
                    .build();
        }

        link.hasBeenAccessed = true;

        return Response.ok(new SecuredLinkUnlockedResponse(accessKey, link.targetLink)).build();
    }
}
