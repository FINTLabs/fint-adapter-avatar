package no.fint.provider.profilbilde.model;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.HeaderConstants;
import no.fint.model.resource.profilbilde.ProfilbildeResource;
import no.fint.provider.profilbilde.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping(value = "/profilbilde", produces = {MediaType.IMAGE_JPEG_VALUE})
public class ProfilbildeController {

    @Autowired
    ProfilbildeRepository repository;

    @GetMapping("/{id}")
    public FileSystemResource getProfilbildeByFileName(@PathVariable String id,
                                                  @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                  @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
                                                  @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client) {
        log.info("{}, authorization {}, orgId {}, client {}", id, authorization, orgId, client);
        if (!repository.authorize(id, authorization)) {
            throw new AccessDeniedException("Authorization failure");
        }
        String result = repository.getFilenames().get(id);
        if (result == null)
            throw new EntityNotFoundException(id);
        return new FileSystemResource(result);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity handleEntityNotFound(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e);
    }

}
