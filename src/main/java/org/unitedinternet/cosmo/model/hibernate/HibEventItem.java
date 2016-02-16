package org.unitedinternet.cosmo.model.hibernate;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author Kamill Sokol
 */
@Entity
@DiscriminatorValue("event")
public class HibEventItem extends HibICalendarItem {

    public HibEventItem() {
        setType(Type.VEVENT);
    }
}