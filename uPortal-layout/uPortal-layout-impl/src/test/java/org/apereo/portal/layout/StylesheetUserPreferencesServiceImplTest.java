/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.IUserPreferencesManager;
import org.apereo.portal.IUserProfile;
import org.apereo.portal.layout.IStylesheetUserPreferencesService.PreferencesScope;
import org.apereo.portal.layout.dao.IStylesheetDescriptorDao;
import org.apereo.portal.layout.dao.IStylesheetUserPreferencesDao;
import org.apereo.portal.layout.om.ILayoutAttributeDescriptor;
import org.apereo.portal.layout.om.IOutputPropertyDescriptor;
import org.apereo.portal.layout.om.IStylesheetData.Scope;
import org.apereo.portal.layout.om.IStylesheetDescriptor;
import org.apereo.portal.layout.om.IStylesheetParameterDescriptor;
import org.apereo.portal.layout.om.IStylesheetUserPreferences;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.user.IUserInstance;
import org.apereo.portal.user.IUserInstanceManager;
import org.apereo.portal.utils.IFragmentDefinitionUtils;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/** */
public class StylesheetUserPreferencesServiceImplTest {

    /** @throws Exception */
    @Test
    public void testThemeStylesheetUserPreferences() throws Exception {
        // Setup mocks
        final HttpServletRequest request = new MockHttpServletRequest();
        request.getSession(); // initialize the session
        final IStylesheetDescriptorDao stylesheetDescriptorDao =
                mock(IStylesheetDescriptorDao.class);
        final IUserInstanceManager userInstanceManager = mock(IUserInstanceManager.class);
        final IStylesheetUserPreferencesDao stylesheetUserPreferencesDao =
                mock(IStylesheetUserPreferencesDao.class);
        final IFragmentDefinitionUtils fragmentUtils = mock(IFragmentDefinitionUtils.class);

        final IUserInstance userInstance = mock(IUserInstance.class);
        when(userInstanceManager.getUserInstance(request)).thenReturn(userInstance);

        final IPerson person = mock(IPerson.class);
        when(userInstance.getPerson()).thenReturn(person);

        final IUserPreferencesManager preferencesManager = mock(IUserPreferencesManager.class);
        when(userInstance.getPreferencesManager()).thenReturn(preferencesManager);

        final IUserProfile userProfile = mock(IUserProfile.class);
        when(preferencesManager.getUserProfile()).thenReturn(userProfile);

        final IUserLayoutManager userLayoutManager = mock(IUserLayoutManager.class);
        when(preferencesManager.getUserLayoutManager()).thenReturn(userLayoutManager);

        final IUserLayout userLayout = mock(IUserLayout.class);
        when(userLayoutManager.getUserLayout()).thenReturn(userLayout);

        when(userProfile.getThemeStylesheetId()).thenReturn(1);

        final IStylesheetDescriptor stylesheetDescriptor = mock(IStylesheetDescriptor.class);
        when(stylesheetDescriptorDao.getStylesheetDescriptor(1)).thenReturn(stylesheetDescriptor);

        final ILayoutAttributeDescriptor skinLayoutAttributeDescriptor =
                mock(ILayoutAttributeDescriptor.class);
        when(stylesheetDescriptor.getLayoutAttributeDescriptor("minimized"))
                .thenReturn(skinLayoutAttributeDescriptor);
        when(skinLayoutAttributeDescriptor.getName()).thenReturn("minimized");
        when(skinLayoutAttributeDescriptor.getScope()).thenReturn(Scope.REQUEST);
        when(skinLayoutAttributeDescriptor.getTargetElementNames())
                .thenReturn(Collections.singleton("folder"));

        final IOutputPropertyDescriptor mediaOutputPropertyDescriptor =
                mock(IOutputPropertyDescriptor.class);
        when(stylesheetDescriptor.getOutputPropertyDescriptor("media"))
                .thenReturn(mediaOutputPropertyDescriptor);
        when(mediaOutputPropertyDescriptor.getName()).thenReturn("media");
        when(mediaOutputPropertyDescriptor.getScope()).thenReturn(Scope.SESSION);

        final IStylesheetParameterDescriptor skinStylesheetParameterDescriptor =
                mock(IStylesheetParameterDescriptor.class);
        when(stylesheetDescriptor.getStylesheetParameterDescriptor("skin"))
                .thenReturn(skinStylesheetParameterDescriptor);
        when(skinStylesheetParameterDescriptor.getName()).thenReturn("media");
        when(skinStylesheetParameterDescriptor.getScope()).thenReturn(Scope.PERSISTENT);

        final IStylesheetUserPreferences persistentStylesheetUserPreferences =
                mock(IStylesheetUserPreferences.class);
        when(stylesheetUserPreferencesDao.createStylesheetUserPreferences(
                        stylesheetDescriptor, person, userProfile))
                .thenReturn(persistentStylesheetUserPreferences);
        when(stylesheetUserPreferencesDao.getStylesheetUserPreferences(
                        stylesheetDescriptor, person, userProfile))
                .thenReturn(persistentStylesheetUserPreferences);
        when(persistentStylesheetUserPreferences.getStylesheetParameter("skin"))
                .thenReturn(null)
                .thenReturn("red");

        // Create and initialize service bean
        final StylesheetUserPreferencesServiceImpl stylesheetUserPreferencesService =
                new StylesheetUserPreferencesServiceImpl();
        stylesheetUserPreferencesService.setStylesheetDescriptorDao(stylesheetDescriptorDao);
        stylesheetUserPreferencesService.setUserInstanceManager(userInstanceManager);
        stylesheetUserPreferencesService.setStylesheetUserPreferencesDao(
                stylesheetUserPreferencesDao);
        stylesheetUserPreferencesService.setFragmentDefinitionUtils(fragmentUtils);

        // Run test
        String actual;

        actual =
                stylesheetUserPreferencesService.getLayoutAttribute(
                        request, PreferencesScope.THEME, "u1l1n1", "minimized");
        assertNull(actual);
        actual =
                stylesheetUserPreferencesService.setLayoutAttribute(
                        request, PreferencesScope.THEME, "u1l1n1", "minimized", "true");
        assertNull(actual);
        actual =
                stylesheetUserPreferencesService.getLayoutAttribute(
                        request, PreferencesScope.THEME, "u1l1n1", "minimized");
        assertEquals("true", actual);

        actual =
                stylesheetUserPreferencesService.getStylesheetParameter(
                        request, PreferencesScope.THEME, "skin");
        assertNull(actual);
        actual =
                stylesheetUserPreferencesService.setStylesheetParameter(
                        request, PreferencesScope.THEME, "skin", "red");
        verify(persistentStylesheetUserPreferences).setStylesheetParameter("skin", "red");
        assertNull(actual);
        actual =
                stylesheetUserPreferencesService.getStylesheetParameter(
                        request, PreferencesScope.THEME, "skin");
        assertEquals("red", actual);
    }
}
