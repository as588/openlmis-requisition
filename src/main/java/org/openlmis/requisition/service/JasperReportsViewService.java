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

import static java.io.File.createTempFile;
import static net.sf.jasperreports.engine.export.JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN;
import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.openlmis.requisition.i18n.MessageKeys.ERROR_CLASS_NOT_FOUND;
import static org.openlmis.requisition.i18n.MessageKeys.ERROR_IO;
import static org.openlmis.requisition.i18n.MessageKeys.ERROR_JASPER_FILE_CREATION;
import static org.openlmis.requisition.i18n.MessageKeys.ERROR_JASPER_FILE_FORMAT;

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.openlmis.requisition.domain.JasperTemplate;
import org.openlmis.requisition.domain.Requisition;
import org.openlmis.requisition.domain.RequisitionTemplate;
import org.openlmis.requisition.domain.RequisitionTemplateColumn;
import org.openlmis.requisition.dto.RequisitionReportDto;
import org.openlmis.requisition.exception.JasperReportViewException;
import org.openlmis.requisition.web.RequisitionReportDtoBuilder;
import org.openlmis.utils.ReportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

@Service
public class JasperReportsViewService {
  private static final String REQUISITION_REPORT_DIR = "/jasperTemplates/requisition.jrxml";
  private static final String REQUISITION_LINE_REPORT_DIR = "/reports/requisitionLines.jrxml";

  @Autowired
  private DataSource replicationDataSource;

  @Autowired
  private RequisitionReportDtoBuilder requisitionReportDtoBuilder;

  /**
   * Create Jasper Report View.
   * Create Jasper Report (".jasper" file) from bytes from Template entity.
   * Set 'Jasper' exporter parameters, JDBC data source, web application context, url to file.
   *
   * @param jasperTemplate template that will be used to create a view
   * @param request  it is used to take web application context
   * @return created jasper view.
   * @throws JasperReportViewException if there will be any problem with creating the view.
   */
  public JasperReportsMultiFormatView getJasperReportsView(
      JasperTemplate jasperTemplate, HttpServletRequest request) throws JasperReportViewException {
    JasperReportsMultiFormatView jasperView = new JasperReportsMultiFormatView();
    setExportParams(jasperView);
    jasperView.setUrl(getReportUrlForReportData(jasperTemplate));
    jasperView.setJdbcDataSource(replicationDataSource);

    if (getApplicationContext(request) != null) {
      jasperView.setApplicationContext(getApplicationContext(request));
    }
    return jasperView;
  }

  /**
   * Create custom Jasper Report View for printing a requisition.
   *
   * @param requisition requisition to render report for.
   * @param request  it is used to take web application context.
   * @return created jasper view.
   * @throws JasperReportViewException if there will be any problem with creating the view.
   */
  public ModelAndView getRequisitionJasperReportView(
      Requisition requisition, HttpServletRequest request) throws JasperReportViewException {
    RequisitionReportDto reportDto = requisitionReportDtoBuilder.build(requisition);
    RequisitionTemplate template = requisition.getTemplate();

    Map<String, Object> params = ReportUtils.createParametersMap();
    params.put("subreport", createCustomizedRequisitionLineSubreport(template));
    params.put("datasource", Collections.singletonList(reportDto));
    params.put("template", template);

    JasperReportsMultiFormatView jasperView = new JasperReportsMultiFormatView();
    setExportParams(jasperView);
    setCustomizedJasperTemplateForRequisitionReport(jasperView);

    if (getApplicationContext(request) != null) {
      jasperView.setApplicationContext(getApplicationContext(request));
    }
    return new ModelAndView(jasperView, params);
  }

  private JasperDesign createCustomizedRequisitionLineSubreport(RequisitionTemplate template)
      throws JasperReportViewException {
    try (InputStream inputStream = getClass().getResourceAsStream(REQUISITION_LINE_REPORT_DIR)) {
      JasperDesign design = JRXmlLoader.load(inputStream);
      JRBand detail = design.getDetailSection().getBands()[0];
      JRBand header = design.getColumnHeader();

      Map<String, RequisitionTemplateColumn> columns =
          ReportUtils.getSortedTemplateColumnsForPrint(template.getColumnsMap());

      ReportUtils.customizeBandWithTemplateFields(detail, columns, design.getPageWidth(), 10);
      ReportUtils.customizeBandWithTemplateFields(header, columns, design.getPageWidth(), 10);

      return design;
    } catch (IOException err) {
      throw new JasperReportViewException(err, ERROR_IO, err.getMessage());
    } catch (JRException err) {
      throw new JasperReportViewException(err, ERROR_JASPER_FILE_FORMAT, err.getMessage());
    }
  }

  private void setCustomizedJasperTemplateForRequisitionReport(
      JasperReportsMultiFormatView jasperView) throws JasperReportViewException {
    try (InputStream inputStream = getClass().getResourceAsStream(REQUISITION_REPORT_DIR)) {
      File reportTempFile = createTempFile("requisitionReport_temp", ".jasper");
      JasperReport report = JasperCompileManager.compileReport(inputStream);

      try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
           ObjectOutputStream out = new ObjectOutputStream(bos)) {

        out.writeObject(report);
        writeByteArrayToFile(reportTempFile, bos.toByteArray());

        jasperView.setUrl(reportTempFile.toURI().toURL().toString());
      }
    } catch (IOException err) {
      throw new JasperReportViewException(err, ERROR_IO, err.getMessage());
    } catch (JRException err) {
      throw new JasperReportViewException(err, ERROR_JASPER_FILE_FORMAT, err.getMessage());
    }
  }

  /**
   * Set export parameters in jasper view.
   */
  private void setExportParams(JasperReportsMultiFormatView jasperView) {
    Map<JRExporterParameter, Object> reportFormatMap = new HashMap<>();
    reportFormatMap.put(IS_USING_IMAGES_TO_ALIGN, false);
    jasperView.setExporterParameters(reportFormatMap);
  }

  /**
   * Get application context from servlet.
   */
  public WebApplicationContext getApplicationContext(HttpServletRequest servletRequest) {
    ServletContext servletContext = servletRequest.getSession().getServletContext();
    return WebApplicationContextUtils.getWebApplicationContext(servletContext);
  }

  /**
   * Create ".jasper" file with byte array from Template.
   *
   * @return Url to ".jasper" file.
   */
  private String getReportUrlForReportData(JasperTemplate jasperTemplate)
      throws JasperReportViewException {
    File tmpFile;

    try {
      tmpFile = createTempFile(jasperTemplate.getName() + "_temp", ".jasper");
    } catch (IOException exp) {
      throw new JasperReportViewException(
          exp, ERROR_JASPER_FILE_CREATION
      );
    }

    try (ObjectInputStream inputStream =
             new ObjectInputStream(new ByteArrayInputStream(jasperTemplate.getData()))) {
      JasperReport jasperReport = (JasperReport) inputStream.readObject();

      try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
           ObjectOutputStream out = new ObjectOutputStream(bos)) {

        out.writeObject(jasperReport);
        writeByteArrayToFile(tmpFile, bos.toByteArray());

        return tmpFile.toURI().toURL().toString();
      }
    } catch (IOException exp) {
      throw new JasperReportViewException(exp, ERROR_IO, exp.getMessage());
    } catch (ClassNotFoundException exp) {
      throw new JasperReportViewException(exp, ERROR_CLASS_NOT_FOUND, JasperReport.class.getName());
    }
  }
}
