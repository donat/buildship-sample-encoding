package org.eclipse.buildship.sample.enc;

import java.io.File;
import java.util.Map;

public interface GradleEncodingInfo {
    Map<File, String> getJavaCompileEncoding();
}
