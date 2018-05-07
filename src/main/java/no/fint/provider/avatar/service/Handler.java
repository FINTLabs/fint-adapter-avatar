package no.fint.provider.avatar.service;

import no.fint.event.model.Event;
import no.fint.model.avatar.AvatarActions;
import no.fint.model.resource.FintLinks;

import java.util.EnumSet;
import java.util.function.Consumer;

public interface Handler extends Consumer<Event<FintLinks>> {

    default EnumSet<AvatarActions> actions() {
        return EnumSet.noneOf(AvatarActions.class);
    }

}
