package caliniya.armadax;

import android.os.Bundle;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import caliniya.armadax.Armadax;
import caliniya.armadax.ErrorActivity;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        CaocConfig.Builder.create()
            .enabled(true)
            .errorActivity(ErrorActivity.class)
            .apply();
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new Armadax(), config);
	}
}
