package no.fint.provider.profilbilde.behaviour;

import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.profilbilde.ProfilbildeResource;
import org.springframework.stereotype.Service;

@Service
public class RejectAllUpdates implements Behaviour<ProfilbildeResource> {

    @Override
    public void accept(Event event, ProfilbildeResource profilbilde) {
        event.setResponseStatus(ResponseStatus.REJECTED);
        event.setMessage("Update not supported by adapter.");
    }
}
