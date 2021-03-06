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

package org.openlmis.requisition.service;

import org.openlmis.requisition.domain.StatusLogEntry;
import org.openlmis.requisition.domain.Requisition;
import org.openlmis.requisition.domain.RequisitionStatus;
import org.openlmis.requisition.dto.ProcessingPeriodDto;
import org.openlmis.requisition.dto.ProgramDto;
import org.openlmis.requisition.dto.UserDto;
import org.openlmis.requisition.service.referencedata.PeriodReferenceDataService;
import org.openlmis.requisition.service.referencedata.ProgramReferenceDataService;
import org.openlmis.requisition.service.referencedata.UserReferenceDataService;
import org.openlmis.settings.service.ConfigurationSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Map;

import static org.openlmis.utils.ConfigurationSettingKeys.REQUISITION_EMAIL_CONVERT_TO_ORDER_CONTENT;
import static org.openlmis.utils.ConfigurationSettingKeys.REQUISITION_EMAIL_CONVERT_TO_ORDER_SUBJECT;

@Component
public class ConvertToOrderNotifier {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConvertToOrderNotifier.class);

  @Autowired
  private ProgramReferenceDataService programReferenceDataService;

  @Autowired
  private PeriodReferenceDataService periodReferenceDataService;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private ConfigurationSettingService configurationSettingService;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  /**
   * Notify requisition's creator that it was converted to order.
   *  @param requisition requisition that was converted
   *
   */
  public void notifyConvertToOrder(Requisition requisition) {
    ProgramDto program = programReferenceDataService.findOne(requisition.getProgramId());
    ProcessingPeriodDto period = periodReferenceDataService.findOne(
        requisition.getProcessingPeriodId());

    Map<String, StatusLogEntry> statusChanges = requisition.getStatusChanges();
    if (statusChanges == null) {
      LOGGER.warn("Could not find requisition audit data to notify for convert to order.");
      return;
    }

    StatusLogEntry initiateAuditEntry = statusChanges.get(RequisitionStatus.INITIATED.toString());
    if (initiateAuditEntry == null || initiateAuditEntry.getAuthorId() == null) {
      LOGGER.warn("Could not find requisition initiator to notify for convert to order.");
      return;
    }

    UserDto initiator = userReferenceDataService.findOne(initiateAuditEntry.getAuthorId());

    String subject = configurationSettingService
        .getStringValue(REQUISITION_EMAIL_CONVERT_TO_ORDER_SUBJECT);
    String content = configurationSettingService
        .getStringValue(REQUISITION_EMAIL_CONVERT_TO_ORDER_CONTENT);

    Object[] msgArgs = {initiator.getFirstName(), initiator.getLastName(),
        program.getName(), period.getName()};
    content = MessageFormat.format(content, msgArgs);

    notificationService.notify(initiator, subject, content);
  }
}
