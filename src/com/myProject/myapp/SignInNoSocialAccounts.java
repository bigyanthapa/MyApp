package com.myProject.myapp;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class SignInNoSocialAccounts extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in_no_social_accounts);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sign_in_no_social_accounts, menu);
		return true;
	}

}
