package no.fint.provider.profilbilde.service;

import no.fint.event.model.Event;
import no.fint.model.profilbilde.ProfilbildeActions;
import no.fint.model.resource.FintLinks;

import java.util.EnumSet;
import java.util.function.Consumer;

public interface Handler extends Consumer<Event<FintLinks>> {

    default EnumSet<ProfilbildeActions> actions() {
        return EnumSet.noneOf(ProfilbildeActions.class);
    }

}
