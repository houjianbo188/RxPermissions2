package com.houjianbo.rxpermissions2;

import android.Manifest;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.houjianbo.library.Permission;
import com.houjianbo.library.RxPermissions;

import java.io.IOException;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "RxPermissionsSample";

    private Camera camera;
    private SurfaceView surfaceView;
    private Button btnCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.setLogging(true);

        setContentView(R.layout.activity_main);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        btnCamera = (Button) findViewById(R.id.enableCamera);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rxPermissions.requestEach(Manifest.permission.CAMERA)
                        .subscribe(new Consumer<Permission>() {
                            @Override
                            public void accept(Permission permission) throws Exception {
                                Log.i(TAG, "Permission result " + permission);
                                if (permission.granted) {
                                    releaseCamera();
                                    camera = Camera.open(0);
                                    try {
                                        camera.setPreviewDisplay(surfaceView.getHolder());
                                        camera.startPreview();
                                    } catch (IOException e) {
                                        Log.e(TAG, "Error while trying to display the camera preview", e);
                                    }
                                } else if (permission.shouldShowRequestPermissionRationale) {
                                    // Denied permission without ask never again
                                    Toast.makeText(MainActivity.this,
                                            "Denied permission without ask never again",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    // Denied permission with ask never again
                                    // Need to go to the settings
                                    Toast.makeText(MainActivity.this,
                                            "Permission denied, can't enable the camera",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.e(TAG, "onError", throwable);
                            }
                        }, new Action() {
                            @Override
                            public void run() throws Exception {
                                Log.i(TAG, "OnComplete");
                            }
                        });

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseCamera();
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}
