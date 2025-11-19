/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.tree;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.vulcan.fields.NestedFieldsContext;
import com.liferay.portal.vulcan.util.NestedFieldsContextUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Daniel Szimko
 */
public class RootModelHierarchyNestedFieldsUtil {

	public static NestedFieldsContext customize(
		NestedFieldsContext nestedFieldsContext,
		ObjectDefinition objectDefinition,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectRelationshipLocalService objectRelationshipLocalService,
		Log log) {

		List<String> nestedFields = new ArrayList<>(
			nestedFieldsContext.getNestedFields());

		if (!nestedFields.contains("rootModelHierarchy")) {
			return nestedFieldsContext;
		}

		int treeHeight = 1;

		try {
			ObjectDefinitionTreeFactory objectDefinitionTreeFactory =
				new ObjectDefinitionTreeFactory(
					objectDefinitionLocalService,
					objectRelationshipLocalService);

			Tree tree = objectDefinitionTreeFactory.create(
				objectDefinition.getObjectDefinitionId());

			treeHeight += tree.getHeight(tree.getRootNode());

			Iterator<Node> iterator = tree.iterator();

			while (iterator.hasNext()) {
				Node node = iterator.next();

				List<Node> childNodes = node.getChildNodes();

				if (ListUtil.isEmpty(childNodes)) {
					continue;
				}

				for (int i = childNodes.size() - 1; i >= 0; i--) {
					Node childNode = childNodes.get(i);

					Edge edge = childNode.getEdge();

					if (edge == null) {
						continue;
					}

					ObjectRelationship objectRelationship =
						objectRelationshipLocalService.getObjectRelationship(
							edge.getObjectRelationshipId());

					nestedFields.add(objectRelationship.getName());
				}
			}
		}
		catch (Exception exception) {
			log.error(exception);

			return nestedFieldsContext;
		}

		ListUtil.distinct(nestedFields);

		return new NestedFieldsContext(
			NestedFieldsContextUtil.limitDepth(treeHeight), nestedFields);
	}

}