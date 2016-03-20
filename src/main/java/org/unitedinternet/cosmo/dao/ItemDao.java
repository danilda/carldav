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
package org.unitedinternet.cosmo.dao;

import org.springframework.data.repository.CrudRepository;
import org.unitedinternet.cosmo.model.hibernate.HibItem;

import java.util.List;

public interface ItemDao extends CrudRepository<HibItem, Long>, ItemDaoCustom {

    List<HibItem> findByCollectionIdAndType(Long id, HibItem.Type type);

    List<HibItem> findByCollectionId(Long id);

    HibItem findByOwnerEmailAndName(String owner, String name);

}
