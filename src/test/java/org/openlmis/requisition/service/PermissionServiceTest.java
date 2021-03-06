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

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openlmis.requisition.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;
import static org.openlmis.requisition.service.PermissionService.ORDERS_EDIT;
import static org.openlmis.requisition.service.PermissionService.REQUISITION_APPROVE;
import static org.openlmis.requisition.service.PermissionService.REQUISITION_AUTHORIZE;
import static org.openlmis.requisition.service.PermissionService.REQUISITION_CREATE;
import static org.openlmis.requisition.service.PermissionService.REQUISITION_DELETE;
import static org.openlmis.requisition.service.PermissionService.REQUISITION_TEMPLATES_MANAGE;
import static org.openlmis.requisition.service.PermissionService.REQUISITION_VIEW;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.requisition.domain.Requisition;
import org.openlmis.requisition.domain.RequisitionStatus;
import org.openlmis.requisition.dto.ConvertToOrderDto;
import org.openlmis.requisition.dto.ResultDto;
import org.openlmis.requisition.dto.RightDto;
import org.openlmis.requisition.dto.UserDto;
import org.openlmis.requisition.repository.RequisitionRepository;
import org.openlmis.requisition.service.referencedata.UserReferenceDataService;
import org.openlmis.requisition.web.PermissionMessageException;
import org.openlmis.utils.AuthenticationHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class PermissionServiceTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @Mock
  private AuthenticationHelper authenticationHelper;

  @Mock
  private RequisitionRepository requisitionRepository;

  @InjectMocks
  private PermissionService permissionService;

  @Mock
  private UserDto user;

  @Mock
  private RightDto requisitionCreateRight;

  @Mock
  private RightDto requisitionApproveRight;

  @Mock
  private RightDto requisitionAuthorizeRight;

  @Mock
  private RightDto requisitionDeleteRight;

  @Mock
  private RightDto requisitionViewRight;

  @Mock
  private RightDto requisitionConvertRight;

  @Mock
  private RightDto manageRequisitionTemplateRight;

  @Mock
  private Requisition requisition;

  private SecurityContext securityContext;
  private OAuth2Authentication trustedClient;
  private OAuth2Authentication userClient;

  private UUID userId = UUID.randomUUID();
  private UUID requisitionCreateRightId = UUID.randomUUID();
  private UUID requisitionApproveRightId = UUID.randomUUID();
  private UUID requisitionAuthorizeRightId = UUID.randomUUID();
  private UUID requisitionDeleteRightId = UUID.randomUUID();
  private UUID requisitionViewRightId = UUID.randomUUID();
  private UUID requisitionConvertRightId = UUID.randomUUID();
  private UUID manageRequisitionTemplateRightId = UUID.randomUUID();
  private UUID requisitionId = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();
  private UUID facilityId = UUID.randomUUID();
  private ConvertToOrderDto convertToOrderDto = new ConvertToOrderDto();
  private List<ConvertToOrderDto> convertToOrderDtos = new ArrayList<>();

  @Before
  public void setUp() {
    securityContext = mock(SecurityContext.class);
    SecurityContextHolder.setContext(securityContext);
    trustedClient = new OAuth2Authentication(mock(OAuth2Request.class), null);
    userClient = new OAuth2Authentication(mock(OAuth2Request.class), mock(Authentication.class));

    convertToOrderDto.setRequisitionId(requisitionId);
    convertToOrderDto.setSupplyingDepotId(facilityId);
    convertToOrderDtos.add(convertToOrderDto);

    when(user.getId()).thenReturn(userId);

    when(requisitionCreateRight.getId()).thenReturn(requisitionCreateRightId);
    when(requisitionApproveRight.getId()).thenReturn(requisitionApproveRightId);
    when(requisitionAuthorizeRight.getId()).thenReturn(requisitionAuthorizeRightId);
    when(requisitionDeleteRight.getId()).thenReturn(requisitionDeleteRightId);
    when(requisitionViewRight.getId()).thenReturn(requisitionViewRightId);
    when(requisitionConvertRight.getId()).thenReturn(requisitionConvertRightId);
    when(manageRequisitionTemplateRight.getId()).thenReturn(manageRequisitionTemplateRightId);

    when(requisition.getId()).thenReturn(requisitionId);
    when(requisition.getProgramId()).thenReturn(programId);
    when(requisition.getFacilityId()).thenReturn(facilityId);
    when(requisition.getSupplyingFacilityId()).thenReturn(facilityId);

    when(authenticationHelper.getCurrentUser()).thenReturn(user);

    when(authenticationHelper.getRight(REQUISITION_CREATE)).thenReturn(requisitionCreateRight);
    when(authenticationHelper.getRight(REQUISITION_APPROVE)).thenReturn(requisitionApproveRight);
    when(authenticationHelper.getRight(REQUISITION_AUTHORIZE))
        .thenReturn(requisitionAuthorizeRight);
    when(authenticationHelper.getRight(REQUISITION_DELETE)).thenReturn(requisitionDeleteRight);
    when(authenticationHelper.getRight(REQUISITION_VIEW)).thenReturn(requisitionViewRight);
    when(authenticationHelper.getRight(ORDERS_EDIT)).thenReturn(
        requisitionConvertRight);
    when(authenticationHelper.getRight(REQUISITION_TEMPLATES_MANAGE)).thenReturn(
        manageRequisitionTemplateRight);

    when(requisitionRepository.findOne(requisitionId)).thenReturn(requisition);
  }

  @Test
  public void canInitRequisition() throws Exception {
    hasRight(requisitionCreateRightId, true);

    permissionService.canInitRequisition(programId, facilityId);

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifySupervisionRight(order, REQUISITION_CREATE, requisitionCreateRightId);
  }

  @Test
  public void cannotInitRequisition() throws Exception {
    expectException(REQUISITION_CREATE);

    permissionService.canInitRequisition(programId, facilityId);
  }

  @Test
  public void canUpdateRequisition() throws Exception {
    hasRight(requisitionCreateRightId, true);

    when(requisition.getStatus()).thenReturn(RequisitionStatus.INITIATED);

    permissionService.canUpdateRequisition(requisitionId);

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifySupervisionRight(order, REQUISITION_CREATE, requisitionCreateRightId);
  }

  @Test
  public void cannotUpdateRequisition() throws Exception {
    expectException(REQUISITION_CREATE);

    when(requisition.getStatus()).thenReturn(RequisitionStatus.INITIATED);

    permissionService.canUpdateRequisition(requisitionId);
  }

  @Test
  public void canSubmitRequisition() throws Exception {
    hasRight(requisitionCreateRightId, true);

    permissionService.canSubmitRequisition(requisitionId);

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifySupervisionRight(order, REQUISITION_CREATE, requisitionCreateRightId);
  }

  @Test
  public void cannotSubmitRequisition() throws Exception {
    expectException(REQUISITION_CREATE);

    permissionService.canSubmitRequisition(requisitionId);
  }

  @Test
  public void canApproveRequisition() throws Exception {
    hasRight(requisitionApproveRightId, true);

    permissionService.canApproveRequisition(requisitionId);

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifySupervisionRight(order, REQUISITION_APPROVE, requisitionApproveRightId);
  }

  @Test
  public void cannotApproveRequisition() throws Exception {
    expectException(REQUISITION_APPROVE);

    permissionService.canApproveRequisition(requisitionId);
  }

  @Test
  public void canAuthorizeRequisition() throws Exception {
    hasRight(requisitionAuthorizeRightId, true);

    permissionService.canAuthorizeRequisition(requisitionId);

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifySupervisionRight(order, REQUISITION_AUTHORIZE, requisitionAuthorizeRightId);
  }

  @Test
  public void cannotAuthorizeRequisition() throws Exception {
    expectException(REQUISITION_AUTHORIZE);

    permissionService.canAuthorizeRequisition(requisitionId);
  }

  @Test
  public void canDeleteRequisition() throws Exception {
    hasRight(requisitionDeleteRightId, true);

    permissionService.canDeleteRequisition(requisitionId);

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifySupervisionRight(order, REQUISITION_DELETE, requisitionDeleteRightId);
  }

  @Test
  public void cannotDeleteRequisition() throws Exception {
    expectException(REQUISITION_DELETE);

    permissionService.canDeleteRequisition(requisitionId);
  }

  @Test
  public void canViewRequisition() throws Exception {
    hasRight(requisitionViewRightId, true);

    permissionService.canViewRequisition(requisitionId);

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifySupervisionRight(order, REQUISITION_VIEW, requisitionViewRightId);
  }

  @Test
  public void cannotViewRequisition() throws Exception {
    expectException(REQUISITION_VIEW);

    permissionService.canViewRequisition(requisitionId);
  }

  @Test
  public void canConvertToOrder() throws Exception {
    hasRight(requisitionConvertRightId, true);

    permissionService.canConvertToOrder(convertToOrderDtos);

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifyFulfillmentRight(order, ORDERS_EDIT, requisitionConvertRightId);
  }

  @Test
  public void cannotConvertToOrder() throws Exception {
    expectException(ORDERS_EDIT);

    permissionService.canConvertToOrder(convertToOrderDtos);
  }

  @Test
  public void canManageRequisitionTemplate() throws Exception {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    hasRight(manageRequisitionTemplateRightId, true);

    permissionService.canManageRequisitionTemplate();

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifyGeneralAdminRight(order, REQUISITION_TEMPLATES_MANAGE, manageRequisitionTemplateRightId);
  }

  @Test
  public void cannotManageRequisitionTemplate() throws Exception {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    expectException(REQUISITION_TEMPLATES_MANAGE);

    permissionService.canManageRequisitionTemplate();
  }

  @Test
  public void shouldAllowTrustedClientsManageRequisitionTemplate() {
    when(securityContext.getAuthentication()).thenReturn(trustedClient);

    permissionService.canManageRequisitionTemplate();
  }

  private void hasRight(UUID rightId, boolean assign) {
    ResultDto<Boolean> resultDto = new ResultDto<>(assign);
    when(userReferenceDataService
        .hasRight(userId, rightId, programId, facilityId, null)
    ).thenReturn(resultDto);
    when(userReferenceDataService
        .hasRight(userId, rightId, null, null, facilityId)
    ).thenReturn(resultDto);
    when(userReferenceDataService
        .hasRight(userId, rightId, null, null, null)
    ).thenReturn(resultDto);
  }

  private void expectException(String rightName) {
    exception.expect(PermissionMessageException.class);
    exception.expectMessage(ERROR_NO_FOLLOWING_PERMISSION + ": " + rightName);
  }

  private void verifySupervisionRight(InOrder order, String rightName, UUID rightId) {
    order.verify(authenticationHelper).getCurrentUser();
    order.verify(authenticationHelper).getRight(rightName);
    order.verify(userReferenceDataService).hasRight(userId, rightId, programId, facilityId,
        null);
  }

  private void verifyFulfillmentRight(InOrder order, String rightName, UUID rightId) {
    order.verify(authenticationHelper).getCurrentUser();
    order.verify(authenticationHelper).getRight(rightName);
    order.verify(userReferenceDataService).hasRight(userId, rightId, null, null,
        facilityId);
  }

  private void verifyGeneralAdminRight(InOrder order, String rightName, UUID rightId) {
    order.verify(authenticationHelper).getCurrentUser();
    order.verify(authenticationHelper).getRight(rightName);
    order.verify(userReferenceDataService).hasRight(userId, rightId, null, null, null);
  }

}
