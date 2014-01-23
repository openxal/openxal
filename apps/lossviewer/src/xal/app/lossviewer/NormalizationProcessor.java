package gov.sns.apps.lossviewer2;

import gov.sns.apps.lossviewer2.signals.*;
import java.util.*;

public interface NormalizationProcessor {

    SignalValue getValue(Set<String> norm, Dispatcher d, LossDetector ld, SignalValue loss);
}
