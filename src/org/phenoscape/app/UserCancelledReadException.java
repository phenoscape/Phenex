package org.phenoscape.app;

import java.io.IOException;

public class UserCancelledReadException extends IOException {

    public UserCancelledReadException() {
        super();
    }

    public UserCancelledReadException(String s) {
        super(s);
    }

}
