package org.chagolchana.noconnect.android.reporting;

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.config.ACRAConfiguration;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderFactory;
import org.chagolchana.noconnect.android.NoConnectApplication;

public class NoConnectReportSenderFactory implements ReportSenderFactory {

	@NonNull
	@Override
	public ReportSender create(@NonNull Context ctx,
			@NonNull ACRAConfiguration config) {
		// ACRA passes in the Application as context
		NoConnectApplication app = (NoConnectApplication) ctx;
		return new NoConnectReportSender(app.getApplicationComponent());
	}
}
