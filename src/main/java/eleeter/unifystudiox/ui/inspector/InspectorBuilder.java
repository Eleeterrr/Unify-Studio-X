package eleeter.unifystudiox.ui.inspector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import eleeter.unifystudiox.i18n.list.Keys;
import eleeter.unifystudiox.ui.framework.render.UIPanel;
import eleeter.unifystudiox.ui.widgets.UILabel;
import eleeter.unifystudiox.ui.widgets.UINumberField;

public class InspectorBuilder
{
    private final UIPanel parentPanel;
    private final eleeter.unifystudiox.ui.framework.render.context.UIInputContext context;

    private float currentY = 5.0f;  // Relative to scroll panel top
    private float paddingX = 10.0f;  // Pixel-based
    private float headerH = 22.0f;   // Pixel-based
    private float labelH = 18.0f;    // Pixel-based
    private float fieldH = 22.0f;    // Pixel-based
    private float spacingY = 8.0f;   // Pixel-based

    // Horizontal layout stays mostly relative for width, but we use fixed margins
    private float fieldW_rel = 0.28f;
    private float col1_rel = 0.05f;
    private float col2_rel = 0.36f;
    private float col3_rel = 0.67f;

    private final List<Runnable> updateHooks = new ArrayList<>();

    public InspectorBuilder(UIPanel parentPanel, eleeter.unifystudiox.ui.framework.render.context.UIInputContext context)
    {
        this.parentPanel = parentPanel;
        this.context = context;
    }

    public void addHeader(String title)
    {
        UILabel header = new UILabel("header_" + title + "_" + (int) currentY);
        header.setText(title);
        header.setAlignment(UILabel.Align.LEFT);
        header.setTextColor(0.8f, 0.8f, 0.8f, 1.0f);
        // relX=0, relY=0, relW=0.9, relH=0 -> then add pixel height and padding
        header.getTransform().set(0, 0, 0.9f, 0)
                .setPixelOffset((int) paddingX, (int) currentY)
                .setPixelSize(0, headerH);

        this.parentPanel.addChild(header);
        this.currentY += headerH + 4.0f;
    }

    public void addVector3(String label, Supplier<Vector3f> getter, Consumer<Vector3f> setter)
    {
        if (label != null && !label.isEmpty())
        {
            UILabel lbl = new UILabel("lbl_" + label + "_" + (int) currentY);
            lbl.setText(label);
            lbl.setAlignment(UILabel.Align.LEFT);
            lbl.setTextColor(0.6f, 0.6f, 0.6f, 1.0f);
            lbl.getTransform().set(0, 0, 0.9f, 0)
                    .setPixelOffset((int) paddingX, (int) currentY)
                    .setPixelSize(0, labelH);
            this.parentPanel.addChild(lbl);
            this.currentY += labelH;
        }

        UINumberField xField = createField(String.valueOf(Keys.X_AXIS), 1.0f, 0.4f, 0.4f, col1_rel, currentY);
        UINumberField yField = createField("Y", 0.4f, 1.0f, 0.4f, col2_rel, currentY);
        UINumberField zField = createField("Z", 0.4f, 0.6f, 1.0f, col3_rel, currentY);

        xField.setOnValueChanged(val ->
        {
            Vector3f v = getter.get();
            v.x = val;
            setter.accept(v);
        });
        yField.setOnValueChanged(val ->
        {
            Vector3f v = getter.get();
            v.y = val;
            setter.accept(v);
        });
        zField.setOnValueChanged(val ->
        {
            Vector3f v = getter.get();
            v.z = val;
            setter.accept(v);
        });

        this.updateHooks.add(() ->
        {
            Vector3f v = getter.get();
            if (!xField.isInteracting()) xField.setValue(v.x);
            if (!yField.isInteracting()) yField.setValue(v.y);
            if (!zField.isInteracting()) zField.setValue(v.z);
        });

        this.currentY += labelH + fieldH + spacingY;
    }

    public void addRotation(String label, Supplier<Quaternionf> getter, Consumer<Quaternionf> setter)
    {
        if (label != null && !label.isEmpty())
        {
            UILabel lbl = new UILabel("lbl_" + label + "_" + (int) currentY);
            lbl.setText(label);
            lbl.setAlignment(UILabel.Align.LEFT);
            lbl.setTextColor(0.6f, 0.6f, 0.6f, 1.0f);
            lbl.getTransform().set(0, 0, 0.9f, 0)
                    .setPixelOffset((int) paddingX, (int) currentY)
                    .setPixelSize(0, labelH);
            this.parentPanel.addChild(lbl);
            this.currentY += labelH;
        }

        UINumberField xField = createField("X", 1.0f, 0.4f, 0.4f, col1_rel, currentY);
        UINumberField yField = createField("Y", 0.4f, 1.0f, 0.4f, col2_rel, currentY);
        UINumberField zField = createField("Z", 0.4f, 0.6f, 1.0f, col3_rel, currentY);

        Runnable updateSetter = () ->
        {
            float radX = (float) Math.toRadians(xField.getValue());
            float radY = (float) Math.toRadians(yField.getValue());
            float radZ = (float) Math.toRadians(zField.getValue());
            setter.accept(new Quaternionf().rotationXYZ(radX, radY, radZ));
        };

        xField.setOnValueChanged(val -> updateSetter.run());
        yField.setOnValueChanged(val -> updateSetter.run());
        zField.setOnValueChanged(val -> updateSetter.run());

        Vector3f tempEuler = new Vector3f();
        this.updateHooks.add(() ->
        {
            getter.get().getEulerAnglesXYZ(tempEuler);
            if (!xField.isInteracting()) xField.setValue((float) Math.toDegrees(tempEuler.x));
            if (!yField.isInteracting()) yField.setValue((float) Math.toDegrees(tempEuler.y));
            if (!zField.isInteracting()) zField.setValue((float) Math.toDegrees(tempEuler.z));
        });

        this.currentY += labelH + fieldH + spacingY;
    }

    public void addFloat(String label, Supplier<Float> getter, Consumer<Float> setter)
    {
        UILabel lbl = new UILabel("lbl_" + label + "_" + (int) currentY);
        lbl.setText(label);
        lbl.setAlignment(UILabel.Align.LEFT);
        lbl.setTextColor(0.8f, 0.8f, 0.8f, 1.0f);
        lbl.getTransform().set(0, 0, 0.4f, 0)
                .setPixelOffset((int) paddingX, (int) currentY)
                .setPixelSize(0, fieldH);
        this.parentPanel.addChild(lbl);

        UINumberField field = new UINumberField("field_" + label + "_" + (int) currentY, this.context);
        field.setValue(getter.get());
        field.setStep(0.1f);
        field.getTransform().set(0.45f, 0, 0.50f, 0)
                .setPixelOffset(0, (int) currentY)
                .setPixelSize(0, fieldH);
        field.setOnValueChanged(setter::accept);
        this.parentPanel.addChild(field);

        this.updateHooks.add(() ->
        {
            if (!field.isInteracting())
            {
                field.setValue(getter.get());
            }
        });

        this.currentY += fieldH + spacingY;
    }

    public void addColor(String label, Supplier<Vector3f> getter, Consumer<Vector3f> setter)
    {
        addVector3(label, getter, setter); // Fallback to Vector3 for now
    }

    private UINumberField createField(String axis, float r, float g, float b, float colX, float startY)
    {
        UILabel lbl = new UILabel("axis_" + axis + "_" + (int) startY);
        lbl.setText(axis);
        lbl.setAlignment(UILabel.Align.CENTER);
        lbl.setTextColor(r, g, b, 1.0f);
        lbl.getTransform().set(colX, 0, fieldW_rel, 0)
                .setPixelOffset(0, (int) startY)
                .setPixelSize(0, labelH);
        this.parentPanel.addChild(lbl);

        UINumberField field = new UINumberField("field_" + axis + "_" + (int) startY, this.context);
        field.setStep(0.1f);
        field.getTransform().set(colX, 0, fieldW_rel, 0)
                .setPixelOffset(0, (int) (startY + labelH))
                .setPixelSize(0, fieldH);
        this.parentPanel.addChild(field);
        return field;
    }

    public float getTotalHeight()
    {
        return this.currentY + 10.0f;
    }

    public void update()
    {
        for (Runnable hook : updateHooks)
        {
            hook.run();
        }
    }
}
