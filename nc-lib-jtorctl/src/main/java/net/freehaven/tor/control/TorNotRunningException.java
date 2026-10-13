// Copyright 2005 Nick Mathewson, Roger Dingledine
// See LICENSE file for copying information
package net.freehaven.tor.control;

import java.io.IOException;

/**
 * An exception raised when Tor is no longer running.
 */
public class TorNotRunningException extends IOException {

    public TorNotRunningException() {
        super();
    }

    public TorNotRunningException(String message) {
        super(message);
    }
}

