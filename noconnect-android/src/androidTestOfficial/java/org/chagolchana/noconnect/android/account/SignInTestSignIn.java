package org.chagolchana.noconnect.android.account;

import android.view.Gravity;

import org.chagolchana.noconnect.R;
import org.chagolchana.noconnect.android.NoConnectUiTestComponent;
import org.chagolchana.noconnect.android.UiTest;
import org.chagolchana.noconnect.android.login.StartupActivity;
import org.chagolchana.noconnect.android.navdrawer.NavDrawerActivity;
import org.chagolchana.noconnect.android.splash.SplashScreenActivity;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.chagolchana.noconnect.android.ViewActions.waitFor;
import static org.hamcrest.CoreMatchers.allOf;

/**
 * This relies on class sorting to run after {@link SignInTestCreateAccount}.
 */
@RunWith(AndroidJUnit4.class)
public class SignInTestSignIn extends UiTest {

	@Override
	protected void inject(NoConnectUiTestComponent component) {
		component.inject(this);
	}

	@Test
	public void signIn() throws Exception {
		startActivity(SplashScreenActivity.class);

		waitFor(StartupActivity.class);

		// enter password
		onView(withId(R.id.edit_password))
				.check(matches(isDisplayed()))
				.perform(replaceText(PASSWORD));
		onView(withId(R.id.btn_sign_in))
				.check(matches(allOf(isDisplayed(), isEnabled())))
				.perform(click());

		lifecycleManager.waitForStartup();
		waitFor(NavDrawerActivity.class);

		// ensure nav drawer is visible
		onView(withId(R.id.drawer_layout))
				.check(matches(isClosed(Gravity.START)));
	}
}
