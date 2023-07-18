/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.staging.taglib.internal.display.context;

import com.liferay.exportimport.configuration.ExportImportServiceConfiguration;
import com.liferay.exportimport.kernel.lar.PortletDataHandlerBoolean;
import com.liferay.exportimport.kernel.lar.PortletDataHandlerKeys;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Daniel Szimko
 */
public class RenderControlsDisplayContext {

	public RenderControlsDisplayContext(HttpServletRequest httpServletRequest) {
		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public boolean includeThumbnailsAndPreviewsDuringStaging()
		throws ConfigurationException {

		ExportImportServiceConfiguration exportImportServiceConfiguration =
			ConfigurationProviderUtil.getCompanyConfiguration(
				ExportImportServiceConfiguration.class,
				CompanyThreadLocal.getCompanyId());

		if (!isStagingEnabled() ||
			exportImportServiceConfiguration.
				includeThumbnailsAndPreviewsDuringStaging()) {

			return true;
		}

		return false;
	}

	public boolean isControlCheckboxEnabled(
			PortletDataHandlerBoolean control,
			Map<String, String[]> parameterMap)
		throws ConfigurationException {

		String controlName = control.getControlName();

		if (FeatureFlagManagerUtil.isEnabled("LPS-189187") &&
			controlName.equals(_DOCUMENT_LIBRARY_PREVIEWS_AND_THUMBNAILS)) {

			return includeThumbnailsAndPreviewsDuringStaging();
		}

		if (MapUtil.getBoolean(
				parameterMap, controlName, control.getDefaultState()) ||
			MapUtil.getBoolean(
				parameterMap, PortletDataHandlerKeys.PORTLET_DATA_ALL)) {

			return true;
		}

		return false;
	}

	public boolean isStagingEnabled() {
		Group group = _themeDisplay.getScopeGroup();

		return group.isStaged();
	}

	private static final String _DOCUMENT_LIBRARY_PREVIEWS_AND_THUMBNAILS =
		"previews-and-thumbnails";

	private final ThemeDisplay _themeDisplay;

}