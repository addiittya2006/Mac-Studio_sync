package science.aditya.cameraupload.camera;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TextureView;

public class CustomTextureView extends TextureView {

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    protected CameraCharacteristics cameraCharacteristics;
    protected CameraCaptureSession captureSession;
    protected CaptureRequest.Builder previewRequestBuilder;

    //Zooming
    protected float fingerSpacing = 0;
    protected float zoomLevel = 1f;
    protected float maximumZoomLevel;
    private Rect zoom;

    public CustomTextureView(Context context) {
        this(context, null);
    }

    public CustomTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

//    public void setCameraChar(CameraCharacteristics cchar) {
//        this.cameraCharacteristics = cchar;
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        if(cameraCharacteristics!= null) {
//            maximumZoomLevel = cameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
//        }
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        try {
//            Rect rect = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
//            if (rect == null) return false;
//            float currentFingerSpacing;
//
//            if (event.getPointerCount() == 2) { //Multi touch.
//                currentFingerSpacing = getFingerSpacing(event);
//                float delta = 0.05f; //Control this value to control the zooming sensibility
//                if (fingerSpacing != 0) {
//                    if (currentFingerSpacing > fingerSpacing) { //Don't over zoom-in
//                        if ((maximumZoomLevel - zoomLevel) <= delta) {
//                            delta = maximumZoomLevel - zoomLevel;
//                        }
//                        zoomLevel = zoomLevel + delta;
//                    } else if (currentFingerSpacing < fingerSpacing) { //Don't over zoom-out
//                        if ((zoomLevel - delta) < 1f) {
//                            delta = zoomLevel - 1f;
//                        }
//                        zoomLevel = zoomLevel - delta;
//                    }
//                    float ratio = (float) 1 / zoomLevel; //This ratio is the ratio of cropped Rect to Camera's original(Maximum) Rect
//                    //croppedWidth and croppedHeight are the pixels cropped away, not pixels after cropped
//                    int croppedWidth = rect.width() - Math.round((float) rect.width() * ratio);
//                    int croppedHeight = rect.height() - Math.round((float) rect.height() * ratio);
//                    //Finally, zoom represents the zoomed visible area
//                    zoom = new Rect(croppedWidth / 2, croppedHeight / 2,
//                            rect.width() - croppedWidth / 2, rect.height() - croppedHeight / 2);
//                    previewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
//                }
//                fingerSpacing = currentFingerSpacing;
//            } else { //Single touch point, needs to return true in order to detect one more touch point
//                return true;
//            }
////            captureSession.setRepeatingRequest(previewRequestBuilder.build(), captureCallback, null);
//            return true;
//        } catch (final Exception e) {
//            //Error handling up to you
//            return true;
//        }
//    }
//
//    private float getFingerSpacing(MotionEvent event) {
//        float x = event.getX(0) - event.getX(1);
//        float y = event.getY(0) - event.getY(1);
//        return (float) Math.sqrt(x * x + y * y);
//    }

//    public Rect getZoom() {
//        return zoom;
//    }

}
