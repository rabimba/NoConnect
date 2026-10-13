package org.chagolchana.noconnect.privategroup.invitation;

import org.chagolchana.chagol.api.sync.Group.Visibility;

interface State {

	int getValue();

	Visibility getVisibility();

	boolean isAwaitingResponse();
}
