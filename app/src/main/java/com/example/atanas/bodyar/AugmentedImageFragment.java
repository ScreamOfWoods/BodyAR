package com.example.atanas.bodyar;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;


import java.io.IOException;
import java.io.InputStream;

public class AugmentedImageFragment extends ArFragment {

    private static final String TAG = "BodyAR_AIF";
    private static final String IMAGE_DATABASE = "body.imgdb";
    private static final double MIN_OPENGL_VERSION = 3.0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e(TAG, "Attached to custom fragment");
        // Check for Sceneform being supported on this device.  This check will be integrated into
        // Sceneform eventually.
        String openGlVersionString =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 or later");
            Toast.makeText(getContext(), "Sceneform requires OpenGL ES 3.0 or later",
                    Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Turn off the plane discovery since we're only looking for images
        getPlaneDiscoveryController().hide();
        getPlaneDiscoveryController().setInstructionView(null);
        getArSceneView().getPlaneRenderer().setEnabled(false);
        return view;
    }

    @Override
    protected Config getSessionConfiguration(Session session) {
        Config config = super.getSessionConfiguration(session);
        if (!setupAugmentedImageDatabase(config, session)) {
            Toast.makeText(getContext(), "Could not setup augmented image database",
                    Toast.LENGTH_LONG).show();
        }
        return config;
    }
    private boolean setupAugmentedImageDatabase(Config config, Session arSession) {
        AugmentedImageDatabase augmentedImageDatabase;
        AssetManager assetManager = getContext() != null ? getContext().getAssets() : null;
        if (assetManager == null) {
            Log.e(TAG, "Context is null, cannot intitialize image database.");
            return false;
        }

        try (InputStream is = getContext().getAssets().open(IMAGE_DATABASE)) {
            augmentedImageDatabase = AugmentedImageDatabase.deserialize(arSession, is);
            Log.e(TAG, "LOADED IMGDB");
        } catch (IOException e) {
            Log.e(TAG, "IO exception loading augmented image database.", e);
            return false;
        }

        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }
}
