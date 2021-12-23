package chappie.tabulamcexporter;

import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextField;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextWrapper;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class WindowExportMinecraftJava extends Window<WorkspaceTabula> {
    public WindowExportMinecraftJava(WorkspaceTabula parent, Project project) {
        super(parent);
        this.setView(new ViewExportJava(this, project));
        this.disableDockingEntirely();
    }

    public static class ViewExportJava extends View<WindowExportMinecraftJava> {

        public ViewExportJava(@Nonnull WindowExportMinecraftJava parent, Project project) {
            super(parent, "export.javaMCClass.title");
            Consumer<String> enterResponder = (s) -> {
                if (!s.isEmpty()) {
                    this.submit(project);
                }
            };
            ElementTextWrapper text = new ElementTextWrapper(this);
            text.setNoWrap().setText("Mod ID");
            text.setConstraint((new Constraint(text)).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(this, Constraint.Property.Type.TOP, 10));
            this.elements.add(text);
            ElementTextField textField = new ElementTextField(this);
            textField.setId("modId");
            textField.setDefaultText("minecraft").setEnterResponder(enterResponder);
            textField.setConstraint((new Constraint(textField)).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(text, Constraint.Property.Type.BOTTOM, 3));
            this.elements.add(textField);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText("Layer name");
            text.setConstraint((new Constraint(text)).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(textField, Constraint.Property.Type.BOTTOM, 10));
            this.elements.add(text);
            textField = new ElementTextField(this);
            textField.setId("layerId");
            textField.setDefaultText("pig").setEnterResponder(enterResponder);
            textField.setConstraint((new Constraint(textField)).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(text, Constraint.Property.Type.BOTTOM, 3));
            this.elements.add(textField);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.get("export.javaClass.package"));
            text.setConstraint((new Constraint(text)).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(textField, Constraint.Property.Type.BOTTOM, 10));
            this.elements.add(text);
            textField = new ElementTextField(this);
            textField.setId("packageName");
            textField.setDefaultText("my.first.mod.model").setEnterResponder(enterResponder);
            textField.setConstraint((new Constraint(textField)).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(text, Constraint.Property.Type.BOTTOM, 3));
            this.elements.add(textField);

            text = new ElementTextWrapper(this);
            text.setNoWrap().setText(I18n.get("export.javaClass.className"));
            text.setConstraint((new Constraint(text)).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(textField, Constraint.Property.Type.BOTTOM, 10));
            this.elements.add(text);
            textField = new ElementTextField(this);
            textField.setId("className");
            textField.setDefaultText(project.name).setValidator(ElementTextField.FILE_SAFE.and((s) -> !s.contains(" "))).setEnterResponder(enterResponder);
            textField.setConstraint((new Constraint(textField)).left(this, Constraint.Property.Type.LEFT, 20).right(this, Constraint.Property.Type.RIGHT, 20).top(text, Constraint.Property.Type.BOTTOM, 3));
            this.elements.add(textField);
            ElementButton<?> button = new ElementButton<>(this, "gui.cancel", (elementClickable) -> this.getWorkspace().removeWindow(parent));
            button.setSize(60, 20);
            button.setConstraint((new Constraint(button)).bottom(this, Constraint.Property.Type.BOTTOM, 10).right(this, Constraint.Property.Type.RIGHT, 10));
            this.elements.add(button);
            ElementButton<?> button1 = new ElementButton<>(this, "gui.ok", (elementClickable) -> this.submit(project));
            button1.setSize(60, 20);
            button1.setConstraint((new Constraint(button1)).right(button, Constraint.Property.Type.LEFT, 10));
            this.elements.add(button1);
        }

        public void submit(Project project) {
            this.getWorkspace().removeWindow(this.parentFragment);
            String modId = ((ElementTextField)this.getById("modId")).getText();
            if (modId.isEmpty()) {
                modId = "ThereWasSupposedToBeAModId";
            }

            String layerId = ((ElementTextField)this.getById("layerId")).getText();
            if (layerId.isEmpty()) {
                layerId = "ThereWasSupposedToBeALayerName";
            }

            String packageName = ((ElementTextField)this.getById("packageName")).getText();
            if (packageName.isEmpty()) {
                packageName = "there.was.supposed.to.be.a.package";
            }

            String className = ((ElementTextField)this.getById("className")).getText();
            if (className.isEmpty()) {
                className = "ThereWasSupposedToBeAClassName";
            }

            if (!TabulaExporterMod.EXPORTER.export(project, packageName, className, modId, layerId)) {
                WindowPopup.popup(this.parentFragment.parent, 0.4D, 140.0D, null, I18n.get("export.failed"));
            }
        }
    }
}
