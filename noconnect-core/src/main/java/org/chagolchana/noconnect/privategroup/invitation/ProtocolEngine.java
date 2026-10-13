package org.chagolchana.noconnect.privategroup.invitation;

import org.chagolchana.chagol.api.FormatException;
import org.chagolchana.chagol.api.db.DbException;
import org.chagolchana.chagol.api.db.Transaction;
import org.chagolchana.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

@NotNullByDefault
interface ProtocolEngine<S extends Session<?>> {

	S onInviteAction(Transaction txn, S session, @Nullable String text,
			long timestamp, byte[] signature, long autoDeleteTimer)
			throws DbException;

	S onJoinAction(Transaction txn, S session) throws DbException;

	/**
	 * Leaves the group or declines an invitation.
	 *
	 * @param isAutoDecline true if automatically declined due to deletion
	 * and false if initiated by the user.
	 */
	S onLeaveAction(Transaction txn, S session, boolean isAutoDecline)
			throws DbException;

	S onMemberAddedAction(Transaction txn, S session) throws DbException;

	S onInviteMessage(Transaction txn, S session, InviteMessage m)
			throws DbException, FormatException;

	S onJoinMessage(Transaction txn, S session, JoinMessage m)
			throws DbException, FormatException;

	S onLeaveMessage(Transaction txn, S session, LeaveMessage m)
			throws DbException, FormatException;

	S onAbortMessage(Transaction txn, S session, AbortMessage m)
			throws DbException, FormatException;

}
