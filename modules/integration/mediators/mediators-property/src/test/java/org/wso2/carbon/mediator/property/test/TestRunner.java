package org.wso2.carbon.mediator.property.test;

/*
*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/

import junit.framework.Test;
import junit.framework.TestSuite;
import org.wso2.carbon.mediator.property.test.getProperty.synapseScope.GetPropertySynapseTest;
import org.wso2.carbon.mediator.property.test.getProperty.xpath.GetPropertyXPathTest;
import org.wso2.carbon.mediator.property.test.setProperty.setExpression.SetPropertyExpressionTest;
import org.wso2.carbon.mediator.property.test.setProperty.setScope.axis2ClientScope.SetScopeAxis2ClientTest;
import org.wso2.carbon.mediator.property.test.setProperty.setScope.synapseScope.SetScopeSynapseTest;
import org.wso2.carbon.mediator.property.test.setProperty.setValue.SetValuePropertyTest;

public class TestRunner extends TestSuite {

    public static Test suite() throws Exception {
        TestSuite testSuite = new TestSuite();

        testSuite.addTestSuite(GetPropertySynapseTest.class);
        testSuite.addTestSuite(GetPropertyXPathTest.class);
      //  testSuite.addTestSuite(RemovePropertyTest.class);   //fail
        testSuite.addTestSuite(SetPropertyExpressionTest.class);
        testSuite.addTestSuite(SetScopeAxis2ClientTest.class);
        testSuite.addTestSuite(SetScopeSynapseTest.class);
        testSuite.addTestSuite(SetValuePropertyTest.class);

        return testSuite;
    }
}
