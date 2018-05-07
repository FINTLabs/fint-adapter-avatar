package no.fint.provider.avatar;

import no.fint.model.avatar.AvatarActions;
import no.fint.provider.adapter.AbstractSupportedActions;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SupportedActions extends AbstractSupportedActions {

    @PostConstruct
    public void addSupportedActions() {
        addAll(AvatarActions.class);
    }

}
