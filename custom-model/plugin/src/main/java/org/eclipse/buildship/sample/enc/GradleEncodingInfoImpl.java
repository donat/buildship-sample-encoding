package org.eclipse.buildship.sample.enc;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import org.eclipse.buildship.sample.enc.GradleEncodingInfo;

public class GradleEncodingInfoImpl implements Serializable, GradleEncodingInfo {

    private static final long serialVersionUID = 1L;
    private final Map<File, String> encoding;

    public GradleEncodingInfoImpl(Map<File, String> encoding) {
        this.encoding = encoding;
    }

    @Override
    public Map<File, String> getJavaCompileEncoding() {
        return this.encoding;
    }

}
