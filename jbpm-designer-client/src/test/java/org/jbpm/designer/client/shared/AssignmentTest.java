/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.designer.client.shared;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.URLDecoder;

import org.jbpm.designer.client.shared.util.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(StringUtils.class)
public class AssignmentTest {

    AssignmentData ad = Mockito.mock(AssignmentData.class);

    @Before
    public void setUp() {
        PowerMockito.mockStatic(StringUtils.class);
        PowerMockito.when(StringUtils.urlEncode(Mockito.anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return urlEncode((String) args[0]);
            }
        });
        PowerMockito.when(StringUtils.urlDecode(Mockito.anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return urlDecode((String) args[0]);
            }
        });
    }


    /**
     * Uses mock implementation of urlEncodeConstant and urlDecodeConstant
     */
    @Test
    public void testSerializeDeserialize() {
        Assignment a = new Assignment(ad, "input1", Variable.VariableType.INPUT, "String", null, null, null);

        serializeDeserialize(ad, a, "-_.!~*'( )  ");
        serializeDeserialize(ad, a, ";/?:&=+$,#");
        serializeDeserialize(ad, a, "http://www.test.com/getit?a=1&b=2");
        serializeDeserialize(ad, a, "a,b=c:aa,,bb==cc");
        serializeDeserialize(ad, a, "a|=b=|c:a[=b=[c:a]=b=]c");
        serializeDeserialize(ad, a, "C:\\home\\joe bloggs\\test\\stuff.txt");
        serializeDeserialize(ad, a, "a bb  ");
    }

    public void serializeDeserialize(AssignmentData ad, Assignment assignment, String constant) {
        assignment.setConstant(constant);

        String s = assignment.toString();
        Assignment newA = Assignment.deserialize(ad, s);
        String deserializedConstant = newA.getConstant();

        assertEquals(constant, deserializedConstant);
    }

    /**
     * Uses prepared examples of constants encoded by com.google.gwt.http.client.URL, which is mocked
     * in the tests.
     */
    @Test
    public void testDeserialize() {
        Assignment a = new Assignment(ad, "input1", Variable.VariableType.INPUT, "String", null, null, null);

        deserialize(ad, a, "-_.!~*'( )", "-_.!~*'(+)", "-_.%21%7E*%27%28+%29");
        deserialize(ad, a, ";/?:&=+$,#", "%3B%2F%3F%3A%26%3D%2B%24%2C%23", "%3B%2F%3F%3A%26%3D%2B%24%2C%23");
        deserialize(ad, a, "http://www.test.com/getit?a=1&b=2", "http%3A%2F%2Fwww.test.com%2Fgetit%3Fa%3D1%26b%3D2",
                "http%3A%2F%2Fwww.test.com%2Fgetit%3Fa%3D1%26b%3D2");
        deserialize(ad, a, "a,b=c:aa,,bb==cc", "a%2Cb%3Dc%3Aaa%2C%2Cbb%3D%3Dcc", "a%2Cb%3Dc%3Aaa%2C%2Cbb%3D%3Dcc");
        deserialize(ad, a, "a|=b=|c:a[=b=[c:a]=b=]c", "a%7C%3Db%3D%7Cc%3Aa%5B%3Db%3D%5Bc%3Aa%5D%3Db%3D%5Dc",
                "a%7C%3Db%3D%7Cc%3Aa%5B%3Db%3D%5Bc%3Aa%5D%3Db%3D%5Dc");
        deserialize(ad, a, "C:\\home\\joe bloggs\\test\\stuff.txt", "C%3A%5Chome%5Cjoe+bloggs%5Ctest%5Cstuff.txt",
                "C%3A%5Chome%5Cjoe+bloggs%5Ctest%5Cstuff.txt");

        deserialize(ad, a, "a bb  ", "a+bb++",  "a%20bb%20%20");
        deserialize(ad, a, "a+bb++", "a%2Bbb%2B%2B", "a%2Bbb%2B%2B");
        deserialize(ad, a, "a+ a +bb++  bb  ++",
                "a%2B%20a%20%2Bbb%2B%2B%20%20bb%20%20%2B%2B",
                "a%2B%20a%20%2Bbb%2B%2B%20%20bb%20%20%2B%2B");

    }

    public void deserialize(AssignmentData ad, Assignment assignment, String constant, String jsonEncodedConstant,
            String bpmn2EncodedConstant) {
        assignment.setConstant(constant);

        String s = assignment.toString();
        // replace the mocked encoded constant with the one that would occur at runtime
        s = s.replace(bpmn2EncodedConstant, jsonEncodedConstant);
        Assignment newA = Assignment.deserialize(ad, s);
        String deserializedConstant = newA.getConstant();

        assertEquals(constant, deserializedConstant);
    }

    /**
     * Implementation of urlEncode for PowerMocked StringUtils
     *
     * @param s
     * @return
     */
    public String urlEncode(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }

        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        }

    }

    /**
     * Implementation of urlDecode for PowerMocked StringUtils
     *
     * @param s
     * @return
     */
    public String urlDecode(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        try {
            return URLDecoder.decode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            return s;
        }
    }
}
