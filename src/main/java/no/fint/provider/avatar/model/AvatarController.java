package no.fint.provider.avatar.model;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.HeaderConstants;
import no.fint.model.resource.avatar.AvatarResource;
import no.fint.provider.avatar.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping(value = "/avatar", produces = {MediaType.IMAGE_JPEG_VALUE})
public class AvatarController {

    @Autowired
    AvatarRepository repository;

    @GetMapping("/{id}")
    public FileSystemResource getAvatarByFileName(@PathVariable String id,
                                                  @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                  @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
                                                  @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client) {
        log.info("{}, authorization {}, orgId {}, client {}", id, authorization, orgId, client);
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
