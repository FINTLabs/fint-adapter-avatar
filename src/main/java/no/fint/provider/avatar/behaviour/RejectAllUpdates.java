package no.fint.provider.avatar.behaviour;

import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.avatar.AvatarResource;
import org.springframework.stereotype.Service;

@Service
public class RejectAllUpdates implements Behaviour<AvatarResource> {

    @Override
    public void accept(Event event, AvatarResource avatar) {
        event.setResponseStatus(ResponseStatus.REJECTED);
        event.setMessage("Update not supported by adapter.");
    }
}
