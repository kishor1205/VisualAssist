package com.example.visualassist;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import com.google.mlkit.vision.objects.DetectedObject;
import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {
    private final Paint boxPaint;
    private final Paint textPaint;
    private List<DetectedObject> detectedObjects = new ArrayList<>();

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        boxPaint = new Paint();
        boxPaint.setColor(Color.GREEN);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(8f);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40f);
        textPaint.setStyle(Paint.Style.FILL);
    }

    public void setDetectedObjects(List<DetectedObject> objects) {
        detectedObjects = objects;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (DetectedObject obj : detectedObjects) {
            Rect box = obj.getBoundingBox();
            canvas.drawRect(box, boxPaint);
            if (!obj.getLabels().isEmpty()) {
                canvas.drawText(obj.getLabels().get(0).getText(),
                        box.left, box.top - 10, textPaint);
            }
        }
    }
}
