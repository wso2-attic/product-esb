package org.wso2.carbon.mediator.route.test;

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

import junit.framework.TestSuite;
import org.wso2.carbon.mediator.route.test.anon_seq_and_epr.AnonSeqEPRTest;
import org.wso2.carbon.mediator.route.test.break_Router.BreakRouterTest;
import org.wso2.carbon.mediator.route.test.continue_after.ContinueAfterTest;
import org.wso2.carbon.mediator.route.test.expression.ExpressionTest;
import org.wso2.carbon.mediator.route.test.match.MatchTest;
import org.wso2.carbon.mediator.route.test.ref_target.RefTargetTest;
public class TestRunner extends TestSuite {

    public static junit.framework.Test suite() throws Exception {
        TestSuite testSuite = new TestSuite();
        testSuite.addTestSuite(AnonSeqEPRTest.class);
        testSuite.addTestSuite(BreakRouterTest.class);
        testSuite.addTestSuite(ContinueAfterTest.class);
        testSuite.addTestSuite(ExpressionTest.class);
        testSuite.addTestSuite(MatchTest.class);
        testSuite.addTestSuite(RefTargetTest.class);
        return testSuite;
    }
}