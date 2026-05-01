package xal.app.lossviewer;

import xal.app.lossviewer.signals.*;
import java.util.*;

public interface NormalizationProcessor {

    SignalValue getValue(Set<String> norm, Dispatcher d, LossDetector ld, SignalValue loss);
}
