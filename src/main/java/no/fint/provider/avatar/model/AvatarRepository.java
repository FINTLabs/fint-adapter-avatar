package no.fint.provider.avatar.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.event.model.Status;
import no.fint.model.avatar.AvatarActions;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.Link;
import no.fint.model.resource.avatar.AvatarResource;
import no.fint.provider.avatar.EntityNotFoundException;
import no.fint.provider.avatar.behaviour.Behaviour;
import no.fint.provider.avatar.service.Handler;
import no.fint.provider.avatar.service.IdentifikatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Slf4j
@Repository
public class AvatarRepository implements Handler {


    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    IdentifikatorFactory identifikatorFactory;

    @Autowired
    List<Behaviour<AvatarResource>> behaviours;

    Key signingKey;
    Mac mac;

    private Collection<AvatarResource> repository = new ConcurrentLinkedQueue<>();

    @Getter
    private ConcurrentMap<String, String> filenames = new ConcurrentSkipListMap<>();

    @Value("${fint.adapter.avatar.authorization.enabled:true}")
    private boolean enableAuthorization;

    @Value("${fint.adapter.avatar.basedir}")
    private Path basedir;

    @Value("${fint.adapter.avatar.root}")
    private String root;

    @Value("${fint.adapter.avatar.algorithm:HmacSHA1}")
    private String algorithm;

    @Value("${fint.adapter.avatar.key:This Is Really Not Very Secret!?}")
    private String key;

    @PostConstruct
    public void init() throws NoSuchAlgorithmException {
        if (!Files.isDirectory(basedir))
            throw new IllegalArgumentException("Not a directory: " + basedir);

        signingKey = new SecretKeySpec(key.getBytes(), algorithm);
        mac = Mac.getInstance(algorithm);
    }

    @Scheduled(initialDelay = 10000, fixedDelayString = "${fint.adapter.avatar.scan-interval:1500000}")
    public void scan() {
        try {
            repository.clear();
            Files.walk(basedir).filter(Files::isRegularFile).map(this::createAvatar).filter(Objects::nonNull).forEach(repository::add);
            log.info("Repository contents:\n{}", objectMapper.writeValueAsString(repository));
        } catch (IOException e) {
            log.error("During scan", e);
        }
    }

    private AvatarResource createAvatar(Path path) {
        String filnavn = path.toAbsolutePath().toString();
        String id = path.getFileName().toString();
        id = id.substring(0, id.lastIndexOf('.'));

        AvatarResource avatarResource = new AvatarResource();
        Link link;
        if (filnavn.contains("ansattnummer")) {
            link = Link.with("${administrasjon.personal.personalressurs}/ansattnummer/" + id);
            avatarResource.addPersonalressurs(link);
        } else if (filnavn.contains("elevnummer")) {
            link = Link.with("${utdanning.elev.elev}/elevnummer/" + id);
            avatarResource.addElev(link);
        } else if (filnavn.contains("fodselsnummer")) {
            link = Link.with("${felles.person}/fodselsnummer/" + id);
            avatarResource.addPerson(link);
        } else {
            log.warn("No valid relation found, ignoring file {}", filnavn);
            return null;
        }

        Identifikator identifikator = new Identifikator();
        String systemId = digest(link.getHref());
        identifikator.setIdentifikatorverdi(systemId);
        avatarResource.setSystemId(identifikator);
        avatarResource.setFilnavn(UriComponentsBuilder.fromUriString(root).pathSegment("avatar", systemId).toUriString());

        filenames.put(systemId, filnavn);

        return avatarResource;
    }

    private String digest(String input) {
        try {
            mac.init(signingKey);
            return Base64.getUrlEncoder().encodeToString(mac.doFinal(input.getBytes()));
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EnumSet<AvatarActions> actions() {
        return EnumSet.of(AvatarActions.GET_ALL_AVATAR);
    }

    @Override
    public void accept(Event<FintLinks> response) {
        log.debug("Handling {} ...", response);
        log.trace("Event data: {}", response.getData());
        try {
            if (AvatarActions.valueOf(response.getAction()) == AvatarActions.GET_ALL_AVATAR) {
                if (enableAuthorization) {
                    repository.forEach(r -> r.setAutorisasjon(
                            digest(response.getCorrId() + r.getFilnavn())));
                }
                response.setData(new ArrayList<>(repository));
            } else {
                response.setStatus(Status.ADAPTER_REJECTED);
                response.setResponseStatus(ResponseStatus.REJECTED);
                response.setStatusCode("INVALID_ACTION");
                response.setMessage("Invalid action");
            }
        } catch (Exception e) {
            log.error("Error!", e);
            response.setResponseStatus(ResponseStatus.ERROR);
            response.setMessage(e.getMessage());
        }
    }

    public boolean authorize(String id, String authorization) {
        if (!this.enableAuthorization)
            return true;
        AvatarResource result = repository.stream().filter(r -> r.getSystemId().getIdentifikatorverdi().equals(id)).findAny().orElseThrow(() -> new EntityNotFoundException(id));
        return Objects.isNull(result.getAutorisasjon()) || result.getAutorisasjon().equals(authorization);
    }
}
