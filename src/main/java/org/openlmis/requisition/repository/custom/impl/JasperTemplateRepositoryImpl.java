/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.requisition.repository.custom.impl;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.openlmis.requisition.repository.custom.JasperTemplateRepositoryCustom;

public class JasperTemplateRepositoryImpl implements JasperTemplateRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  /**
   * Executes the (arbitrary) SQL string passed in as a parameter and returns a list of string 
   * values. If no results, the list is an empty list.
   */
  public List<String> runArbitrarySql(String arbitrarySql) {

    Query query = entityManager.createNativeQuery(arbitrarySql);
    return (List<String>) query.getResultList();
  }
}
