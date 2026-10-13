// Copyright 2005 Nick Mathewson, Roger Dingledine
// See LICENSE file for copying information
package net.freehaven.tor.control;

import java.io.IOException;

/**
 * An exception raised when Tor behaves in an unexpected way.
 */
public class TorControlSyntaxException extends IOException {

    static final long serialVersionUID = 3;

    public TorControlSyntaxException(String s) {
        super(s);
    }
}

