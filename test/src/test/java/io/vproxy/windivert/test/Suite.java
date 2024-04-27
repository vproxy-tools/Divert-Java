package io.vproxy.windivert.test;

import io.vproxy.windivert.test.cases.TestHostsParser;
import io.vproxy.windivert.test.cases.TestResponder;
import org.junit.runner.RunWith;

@RunWith(org.junit.runners.Suite.class)
@org.junit.runners.Suite.SuiteClasses({
    TestHostsParser.class,
    TestResponder.class,
})
public class Suite {
}
