package caliniya.armadax;

import android.os.Bundle;

import arc.backend.android.AndroidApplicationConfiguration;
import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;
import arc.backend.android.AndroidApplication;
import caliniya.armadax.Armadax;
import caliniya.armadax.ErrorActivity;

public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        CaocConfig.Builder.create()
            .enabled(true)
            .errorActivity(ErrorActivity.class)
            .apply();
            
        initialize(new Armadax(),
        new AndroidApplicationConfiguration(){{
            useImmersiveMode = true;
            hideStatusBar = true;
            useGL30 = true;
            }}
        );
    }
}