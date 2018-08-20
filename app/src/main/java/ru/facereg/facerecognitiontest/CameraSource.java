package ru.facereg.facerecognitiontest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.SystemClock;
import android.support.annotation.GuardedBy;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.google.android.gms.common.images.Size;
import com.google.android.gms.common.util.VisibleForTesting;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Markin Andrey on 20.08.2018.
 */
public class CameraSource {
    @SuppressLint({"InlinedApi"})
    public static final int CAMERA_FACING_BACK = 0;
    @SuppressLint({"InlinedApi"})
    public static final int CAMERA_FACING_FRONT = 1;
    private Context mContext;
    private final Object zzd;
    @GuardedBy("mCameraLock")
    private Camera zze;
    private int zzf;
    private int zzg;
    private Size zzh;
    private float zzi;
    private int zzj;
    private int zzk;
    private boolean zzl;
    private SurfaceTexture zzm;
    private boolean zzn;
    private Thread zzo;
    private CameraSource.zzb zzp;
    private Map<byte[], ByteBuffer> zzq;

    public void release() {
        Object var1 = this.zzd;
        synchronized(this.zzd) {
            this.stop();
            this.zzp.release();
        }
    }

    @RequiresPermission("android.permission.CAMERA")
    public CameraSource start() throws IOException {
        Object var1 = this.zzd;
        synchronized(this.zzd) {
            if (this.zze != null) {
                return this;
            } else {
                this.zze = this.zza();
                this.zzm = new SurfaceTexture(100);
                this.zze.setPreviewTexture(this.zzm);
                this.zzn = true;
                this.zze.startPreview();
                this.zzo = new Thread(this.zzp);
                this.zzp.setActive(true);
                this.zzo.start();
                return this;
            }
        }
    }

    @RequiresPermission("android.permission.CAMERA")
    public CameraSource start(SurfaceHolder var1) throws IOException {
        Object var2 = this.zzd;
        synchronized(this.zzd) {
            if (this.zze != null) {
                return this;
            } else {
                this.zze = this.zza();
                this.zze.setPreviewDisplay(var1);
                this.zze.startPreview();
                this.zzo = new Thread(this.zzp);
                this.zzp.setActive(true);
                this.zzo.start();
                this.zzn = false;
                return this;
            }
        }
    }

    public void stop() {
        Object var1 = this.zzd;
        synchronized(this.zzd) {
            this.zzp.setActive(false);
            if (this.zzo != null) {
                try {
                    this.zzo.join();
                } catch (InterruptedException var6) {
                    Log.d("CameraSource", "Frame processing thread interrupted on release.");
                }

                this.zzo = null;
            }

            if (this.zze != null) {
                this.zze.stopPreview();
                this.zze.setPreviewCallbackWithBuffer((Camera.PreviewCallback)null);

                try {
                    if (this.zzn) {
                        this.zze.setPreviewTexture((SurfaceTexture)null);
                    } else {
                        this.zze.setPreviewDisplay((SurfaceHolder)null);
                    }
                } catch (Exception var5) {
                    String var3 = String.valueOf(var5);
                    Log.e("CameraSource", (new StringBuilder(32 + String.valueOf(var3).length())).append("Failed to clear camera preview: ").append(var3).toString());
                }

                this.zze.release();
                this.zze = null;
            }

            this.zzq.clear();
        }
    }

    public Size getPreviewSize() {
        return this.zzh;
    }

    public int getCameraFacing() {
        return this.zzf;
    }

    public boolean takePicture(ShutterCallback var1, PictureCallback var2, boolean safeToTakePicture) {
        Object var3 = this.zzd;
        synchronized(this.zzd) {
            if (this.zze != null && safeToTakePicture) {
                CameraSource.zzd var4;
                (var4 = new CameraSource.zzd()).zzz = var1;
                CameraSource.zzc var5;
                (var5 = new CameraSource.zzc()).zzy = var2;
                //this.zze.startPreview();
                this.zze.takePicture(var4, (android.hardware.Camera.PictureCallback)null, (android.hardware.Camera.PictureCallback)null, var5);
                safeToTakePicture = false;
            }
        }
        return safeToTakePicture;
    }

    private CameraSource() {
        this.zzd = new Object();
        this.zzf = 0;
        this.zzi = 30.0F;
        this.zzj = 1024;
        this.zzk = 768;
        this.zzl = false;
        this.zzq = new HashMap();
    }

    @SuppressLint({"InlinedApi"})
    private final Camera zza() throws IOException {
        int var7 = this.zzf;
        Camera.CameraInfo var8 = new Camera.CameraInfo();
        int var9 = 0;

        int var10000;
        while(true) {
            if (var9 >= Camera.getNumberOfCameras()) {
                var10000 = -1;
                break;
            }

            Camera.getCameraInfo(var9, var8);
            if (var8.facing == var7) {
                var10000 = var9;
                break;
            }

            ++var9;
        }

        int var1 = var10000;
        if (var10000 == -1) {
            throw new IOException("Could not find requested camera.");
        } else {
            Camera var2;
            Camera var40 = var2 = Camera.open(var1);
            var9 = this.zzk;
            int var31 = this.zzj;
            Camera.Parameters var18;
            List var19 = (var18 = var40.getParameters()).getSupportedPreviewSizes();
            List var20 = var18.getSupportedPictureSizes();
            ArrayList var21 = new ArrayList();
            Iterator var22 = var19.iterator();

            while(true) {
                android.hardware.Camera.Size var23;
                while(var22.hasNext()) {
                    float var24 = (float)(var23 = (android.hardware.Camera.Size)var22.next()).width / (float)var23.height;
                    Iterator var25 = var20.iterator();

                    while(var25.hasNext()) {
                        android.hardware.Camera.Size var26;
                        float var27 = (float)(var26 = (android.hardware.Camera.Size)var25.next()).width / (float)var26.height;
                        if (Math.abs(var24 - var27) < 0.01F) {
                            var21.add(new CameraSource.zze(var23, var26));
                            break;
                        }
                    }
                }

                if (var21.size() == 0) {
                    Log.w("CameraSource", "No preview sizes have a corresponding same-aspect-ratio picture size");
                    var22 = var19.iterator();

                    while(var22.hasNext()) {
                        var23 = (android.hardware.Camera.Size)var22.next();
                        var21.add(new CameraSource.zze(var23, (android.hardware.Camera.Size)null));
                    }
                }

                CameraSource.zze var11 = null;
                int var12 = 2147483647;
                ArrayList var28;
                int var29 = (var28 = (ArrayList)var21).size();
                int var30 = 0;
                Iterator var13 = null;

                int var16;
                while(var30 < var29) {
                    Object var41 = var28.get(var30);
                    ++var30;
                    CameraSource.zze var14;
                    Size var15;
                    if ((var16 = Math.abs((var15 = (var14 = (CameraSource.zze)var41).zzb()).getWidth() - var31) + Math.abs(var15.getHeight() - var9)) < var12) {
                        var11 = var14;
                        var12 = var16;
                    }
                }

                if (var11 == null) {
                    throw new IOException("Could not find suitable preview size.");
                }

                Size var4 = var11.zzc();
                this.zzh = var11.zzb();
                float var32 = this.zzi;
                var9 = (int)(var32 * 1000.0F);
                int[] var10 = null;
                int var33 = 2147483647;
                var13 = var2.getParameters().getSupportedPreviewFpsRange().iterator();

                int var39;
                while(var13.hasNext()) {
                    int[] var37 = (int[])var13.next();
                    var39 = var9 - var37[0];
                    var16 = var9 - var37[1];
                    int var17;
                    if ((var17 = Math.abs(var39) + Math.abs(var16)) < var33) {
                        var10 = var37;
                        var33 = var17;
                    }
                }

                if (var10 == null) {
                    throw new IOException("Could not find suitable preview frames per second range.");
                }

                Camera.Parameters var6 = var2.getParameters();
                if (var4 != null) {
                    var6.setPictureSize(var4.getWidth(), var4.getHeight());
                }

                var6.setPreviewSize(this.zzh.getWidth(), this.zzh.getHeight());
                var6.setPreviewFpsRange(var10[0], var10[1]);
                var6.setPreviewFormat(17);
                WindowManager var34 = (WindowManager)this.mContext.getSystemService("window");
                short var35 = 0;
                int var36;
                switch(var36 = var34.getDefaultDisplay().getRotation()) {
                    case 0:
                        var35 = 0;
                        break;
                    case 1:
                        var35 = 90;
                        break;
                    case 2:
                        var35 = 180;
                        break;
                    case 3:
                        var35 = 270;
                        break;
                    default:
                        Log.e("CameraSource", (new StringBuilder(31)).append("Bad rotation value: ").append(var36).toString());
                }

                Camera.CameraInfo var38 = new Camera.CameraInfo();
                Camera.getCameraInfo(var1, var38);
                if (var38.facing == 1) {
                    var39 = (var38.orientation + var35) % 360;
                    var16 = (360 - var39) % 360;
                } else {
                    var16 = var39 = (var38.orientation - var35 + 360) % 360;
                }

                this.zzg = var39 / 90;
                var2.setDisplayOrientation(var16);
                var6.setRotation(var39);
                if (this.zzl) {
                    if (var6.getSupportedFocusModes().contains("continuous-video")) {
                        var6.setFocusMode("continuous-video");
                    } else {
                        Log.i("CameraSource", "Camera auto focus is not supported on this device.");
                    }
                }

                var2.setParameters(var6);
                var2.setPreviewCallbackWithBuffer(new CameraSource.zza());
                var2.addCallbackBuffer(this.zza(this.zzh));
                var2.addCallbackBuffer(this.zza(this.zzh));
                var2.addCallbackBuffer(this.zza(this.zzh));
                var2.addCallbackBuffer(this.zza(this.zzh));
                return var2;
            }
        }
    }

    @SuppressLint({"InlinedApi"})
    private final byte[] zza(Size var1) {
        int var2 = ImageFormat.getBitsPerPixel(17);
        byte[] var3;
        ByteBuffer var4;
        if ((var4 = ByteBuffer.wrap(var3 = new byte[(int)Math.ceil((double)((long)(var1.getHeight() * var1.getWidth() * var2)) / 8.0D) + 1])).hasArray() && var4.array() == var3) {
            this.zzq.put(var3, var4);
            return var3;
        } else {
            throw new IllegalStateException("Failed to create valid buffer for camera source.");
        }
    }

    private class zzb implements Runnable {
        private Detector<?> zzr;
        private long zzu = SystemClock.elapsedRealtime();
        private final Object mLock = new Object();
        private boolean mActive = true;
        private long zzv;
        private int zzw = 0;
        private ByteBuffer zzx;

        zzb(Detector<?> var1) {
            this.zzr = var1;
        }

        @SuppressLint({"Assert"})
        final void release() {
            this.zzr.release();
            this.zzr = null;
        }

        final void setActive(boolean var1) {
            Object var2 = this.mLock;
            synchronized(this.mLock) {
                this.mActive = var1;
                this.mLock.notifyAll();
            }
        }

        final void zza(byte[] var1, Camera var2) {
            Object var3 = this.mLock;
            synchronized(this.mLock) {
                if (this.zzx != null) {
                    var2.addCallbackBuffer(this.zzx.array());
                    this.zzx = null;
                }

                if (!CameraSource.this.zzq.containsKey(var1)) {
                    Log.d("CameraSource", "Skipping frame. Could not find ByteBuffer associated with the image data from the camera.");
                } else {
                    this.zzv = SystemClock.elapsedRealtime() - this.zzu;
                    ++this.zzw;
                    this.zzx = (ByteBuffer) CameraSource.this.zzq.get(var1);
                    this.mLock.notifyAll();
                }
            }
        }

        @SuppressLint({"InlinedApi"})
        public final void run() {
            while(true) {
                Object var3 = this.mLock;
                Frame var1;
                ByteBuffer var2;
                synchronized(this.mLock) {
                    while(this.mActive && this.zzx == null) {
                        try {
                            this.mLock.wait();
                        } catch (InterruptedException var13) {
                            Log.d("CameraSource", "Frame processing loop terminated.", var13);
                            return;
                        }
                    }

                    if (!this.mActive) {
                        return;
                    }

                    var1 = (new com.google.android.gms.vision.Frame.Builder()).setImageData(this.zzx, CameraSource.this.zzh.getWidth(), CameraSource.this.zzh.getHeight(), 17).setId(this.zzw).setTimestampMillis(this.zzv).setRotation(CameraSource.this.zzg).build();
                    var2 = this.zzx;
                    this.zzx = null;
                }

                try {
                    this.zzr.receiveFrame(var1);
                } catch (Throwable var11) {
                    Log.e("CameraSource", "Exception thrown from receiver.", var11);
                } finally {
                    CameraSource.this.zze.addCallbackBuffer(var2.array());
                }
            }
        }
    }

    private class zza implements Camera.PreviewCallback {
        private zza() {
        }

        public final void onPreviewFrame(byte[] var1, Camera var2) {
            CameraSource.this.zzp.zza(var1, var2);
        }
    }

    @VisibleForTesting
    static class zze {
        private Size zzaa;
        private Size zzab;

        public zze(android.hardware.Camera.Size var1, @Nullable android.hardware.Camera.Size var2) {
            this.zzaa = new Size(var1.width, var1.height);
            if (var2 != null) {
                this.zzab = new Size(var2.width, var2.height);
            }

        }

        public final Size zzb() {
            return this.zzaa;
        }

        @Nullable
        public final Size zzc() {
            return this.zzab;
        }
    }

    private class zzc implements android.hardware.Camera.PictureCallback {
        private CameraSource.PictureCallback zzy;

        private zzc() {
        }

        public final void onPictureTaken(byte[] var1, Camera var2) {
            if (this.zzy != null) {
                this.zzy.onPictureTaken(var1);
            }

            synchronized(CameraSource.this.zzd) {
                if (CameraSource.this.zze != null) {
                    CameraSource.this.zze.startPreview();
                }

            }
        }
    }

    private static class zzd implements android.hardware.Camera.ShutterCallback {
        private CameraSource.ShutterCallback zzz;

        private zzd() {
        }

        public final void onShutter() {
            if (this.zzz != null) {
                this.zzz.onShutter();
            }

        }
    }

    public interface PictureCallback {
        void onPictureTaken(byte[] var1);
    }

    public interface ShutterCallback {
        void onShutter();
    }

    public static class Builder {
        private final Detector<?> zzr;
        private CameraSource zzs = new CameraSource();

        public Builder(Context var1, Detector<?> var2) {
            if (var1 == null) {
                throw new IllegalArgumentException("No context supplied.");
            } else if (var2 == null) {
                throw new IllegalArgumentException("No detector supplied.");
            } else {
                this.zzr = var2;
                this.zzs.mContext = var1;
            }
        }

        public CameraSource.Builder setRequestedFps(float var1) {
            if (var1 <= 0.0F) {
                throw new IllegalArgumentException((new StringBuilder(28)).append("Invalid fps: ").append(var1).toString());
            } else {
                this.zzs.zzi = var1;
                return this;
            }
        }

        public CameraSource.Builder setRequestedPreviewSize(int var1, int var2) {
            if (var1 > 0 && var1 <= 1000000 && var2 > 0 && var2 <= 1000000) {
                this.zzs.zzj = var1;
                this.zzs.zzk = var2;
                return this;
            } else {
                throw new IllegalArgumentException((new StringBuilder(45)).append("Invalid preview size: ").append(var1).append("x").append(var2).toString());
            }
        }

        public CameraSource.Builder setFacing(int var1) {
            if (var1 != 0 && var1 != 1) {
                throw new IllegalArgumentException((new StringBuilder(27)).append("Invalid camera: ").append(var1).toString());
            } else {
                this.zzs.zzf = var1;
                return this;
            }
        }

        public CameraSource.Builder setAutoFocusEnabled(boolean var1) {
            this.zzs.zzl = var1;
            return this;
        }

        public CameraSource build() {
            CameraSource var10000 = this.zzs;
            CameraSource var10003 = this.zzs;
            this.zzs.getClass();
            var10000.zzp = var10003.new zzb(this.zzr);
            return this.zzs;
        }
    }
}