package xal.schemas;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({	DeviceMappingTest.class,
				HardwareStatusTest.class,
				MainTest.class,
				ModelConfigTest.class,
				OpticsTest.class,
				TableGroupTest.class,
				TimingSourceTest.class	})
public class AllTests {
	//EMPTY
}
