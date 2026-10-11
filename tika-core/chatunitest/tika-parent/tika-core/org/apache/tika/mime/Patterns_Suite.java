package org.apache.tika.mime;

import org.junit.runner.RunWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;

@RunWith(value = JUnitPlatform.class)
@SelectClasses(value = { Patterns_add_1_0_Test.class, Patterns_matches_5_2_Test.class })
public class Patterns_Suite {
}
