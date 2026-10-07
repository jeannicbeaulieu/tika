package org.apache.tika.config;

import org.junit.runner.RunWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;

@RunWith(value = JUnitPlatform.class)
@SelectClasses(value = { TikaExtras_appendJarsToClasspath_2_1_Test.class, TikaExtras_extrasDir_3_1_Test.class })
public class TikaExtras_Suite {
}
