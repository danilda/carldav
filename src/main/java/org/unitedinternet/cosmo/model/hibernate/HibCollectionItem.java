/*
 * Copyright 2006 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.model.hibernate;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Entity
@DiscriminatorValue("collection")
public class HibCollectionItem extends HibItem {

    private static final long serialVersionUID = 1L;

    @OneToMany(targetEntity=HibItem.class, mappedBy="collection", fetch=FetchType.LAZY, orphanRemoval=true)
    private Set<HibItem> items = new HashSet<>();

    public Set<HibItem> getItems() {
        return items;
    }

    public void setItems(final Set<HibItem> items) {
        this.items = items;
    }
}
