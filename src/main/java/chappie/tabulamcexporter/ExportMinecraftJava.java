package chappie.tabulamcexporter;

import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.common.module.tabula.formats.types.Exporter;
import me.ichun.mods.ichunutil.common.module.tabula.project.Identifiable;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.tabula.client.core.ResourceHelper;
import me.ichun.mods.tabula.client.gui.WorkspaceTabula;
import me.ichun.mods.tabula.common.Tabula;
import net.minecraft.client.resources.I18n;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExportMinecraftJava extends Exporter {
    public ExportMinecraftJava() {
        super(I18n.get("export.javaMCClass.name"));
    }

    @Override
    public String getId() {
        return "javaMCClass";
    }

    @Override
    public boolean override(Workspace workspace, Project project) {
        workspace.openWindowInCenter(new WindowExportMinecraftJava(((WorkspaceTabula) workspace), project), 0.6D, 0.6D);
        return true;
    }

    @Override
    public boolean export(Project project, Object... params) {
        File file = new File(ResourceHelper.getExportsDir().toFile(), params[1] + ".java");

        StringBuilder sb = new StringBuilder();

        ArrayList<Project.Part> allCubes = project.getAllParts();

        HashMap<Project.Part, String> cubeFieldMap = new HashMap<>();

        sb.append("package ").append(params[0]).append(";\n\n");
        sb.append("import com.google.common.collect.ImmutableList;\n");
        sb.append("import net.minecraft.client.model.geom.ModelLayerLocation;\n");
        sb.append("import net.minecraft.resources.ResourceLocation;\n");
        sb.append("import com.mojang.blaze3d.vertex.PoseStack;\n");
        sb.append("import com.mojang.blaze3d.vertex.VertexConsumer;\n");
        sb.append("import net.minecraft.client.model.EntityModel;\n");
        sb.append("import net.minecraft.client.model.geom.ModelPart;\n");
        sb.append("import net.minecraft.client.model.geom.PartPose;\n");
        sb.append("import net.minecraft.client.model.geom.builders.*;\n");
        sb.append("import net.minecraft.world.entity.Entity;\n");
        sb.append("import net.minecraftforge.api.distmarker.Dist;\n");
        sb.append("import net.minecraftforge.api.distmarker.OnlyIn;\n");

        sb.append("\n/**\n * ").append(project.name).append(" - ").append(project.author).append("\n");
        sb.append(" * Created using Tabula " + Tabula.VERSION + "\n");
        sb.append(" */\n");
        sb.append("@OnlyIn(Dist.CLIENT)\n");
        sb.append("public class ").append(params[1]).append("<T extends Entity> extends EntityModel<T> {\n");
        sb.append("    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(\"").append(params[2]).append("\", \"").append(params[3]).append("\"), \"main\");\n\n");
        boolean hasProjectScale = !(project.scaleX == 1F && project.scaleY == 1F && project.scaleZ == 1F);
        if (hasProjectScale) {
            sb.append("    public float[] modelScale = new float[] { ").append(project.scaleX).append("F, ").append(project.scaleY).append("F, ").append(project.scaleZ).append("F };\n");
        }
        for (Project.Part cube : allCubes) {
            int count = 0;
            while (count == 0 && cubeFieldMap.containsValue(getFieldName(cube)) || count != 0 && cubeFieldMap.containsValue(getFieldName(cube) + "_" + count)) {
                count++;
            }
            String fieldName = getFieldName(cube);
            if (count != 0) {
                fieldName = fieldName + "_" + count;
            }
            cubeFieldMap.put(cube, fieldName);
            sb.append("    public ModelPart ").append(fieldName).append(";\n");
        }
        sb.append("\n    public ").append(params[1]).append("(ModelPart root) {\n");
        HashMap<Project.Part, Project.Part> parentMap = new HashMap<>();
        StringBuilder initSb = new StringBuilder();
        for (Project.Part cube : cubeFieldMap.keySet()) {
            for (Project.Part child : cube.children) {
                parentMap.put(child, cube);
            }
        }
        for (Map.Entry<Project.Part, String> e : cubeFieldMap.entrySet()) {
            Project.Part cube = e.getKey();
            String field = e.getValue();
            if (parentMap.get(cube) == null) {
                initSb.append("        ").append("this.").append(field).append(" = ").append("root").append(".getChild(\"").append(field).append("\");\n");
                for (Project.Part child : cube.children) {
                    initSb.append("        ").append("this.").append(cubeFieldMap.get(child)).append(" = ").append(field).append(".getChild(\"").append(cubeFieldMap.get(child)).append("\");\n");
                }
            }
        }

        sb.append(initSb);
        sb.append("    }\n\n");

        sb.append("    public static LayerDefinition createLayerDefinition() {\n");
        sb.append("        MeshDefinition mesh = new MeshDefinition();\n");
        sb.append("        PartDefinition root = mesh.getRoot();\n");

        for (Map.Entry<Project.Part, String> e : cubeFieldMap.entrySet()) {
            Project.Part cube = e.getKey();
            String field = e.getValue();

            if (parentMap.get(cube) == null) {
                createPartDefinition(sb, field, "root", cube);
                for (Project.Part child : cube.children) {
                    createPartDefinition(sb, cubeFieldMap.get(child), field, child);
                }
            }
        }

        sb.append("        return LayerDefinition.create(mesh, ").append(project.texWidth).append(", ").append(project.texHeight).append(");\n");
        sb.append("    }\n\n");

        for (Map.Entry<Project.Part, Project.Part> e : parentMap.entrySet()) {
            cubeFieldMap.remove(e.getKey());//removing it so children don't get called to render later on.
        }
        sb.append("    @Override\n");
        sb.append("    public void renderToBuffer(PoseStack poseStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) { \n");

        if (hasProjectScale) {
            sb.append("        poseStackIn.push();\n");
            sb.append("        poseStackIn.scale(modelScale[0], modelScale[1], modelScale[2]);\n");
        }
        sb.append("        ImmutableList.of(");
        int i = 0;
        for (Map.Entry<Project.Part, String> e : cubeFieldMap.entrySet()) {
            String field = e.getValue();
            sb.append("this.").append(field);
            if (i < cubeFieldMap.size() - 1) {
                sb.append(", ");
            }
            i++;
        }
        sb.append(").forEach((modelRenderer) -> { \n");
        sb.append("            modelRenderer.render(poseStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);\n");
        sb.append("        });\n");
        if (hasProjectScale) {
            sb.append("        poseStackIn.pop();\n");
        }
        sb.append("    }\n\n");

        sb.append("    @Override\n");
        sb.append("    public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}\n\n");
        sb.append("}\n");

        try {
            FileUtils.writeStringToFile(file, sb.toString(), StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void createPartDefinition(StringBuilder sb, String field, String parent, Project.Part cube) {
        sb.append("        ");
        if (!cube.children.isEmpty()) {
            sb.append("PartDefinition ").append(field).append(" = ");
        }
        sb.append(parent).append(".addOrReplaceChild(\"").append(field).append("\", CubeListBuilder.create().texOffs(").append(cube.texOffX).append(", ").append(cube.texOffY).append(")\n");
        if (cube.mirror) {
            sb.append("                .mirror()\n");
        }
        for (Project.Part.Box box : cube.boxes) {
            if (!(box.texOffX == 0 && box.texOffY == 0)) {
                sb.append("                .texOffs(").append(box.texOffX).append(", ").append(box.texOffY).append(")");
            }
            sb.append("                .addBox(").append(box.posX).append("F, ").append(box.posY).append("F, ").append(box.posZ).append("F, ").append(box.dimX).append("F, ").append(box.dimY).append("F, ").append(box.dimZ).append("F, new CubeDeformation(").append(box.expandX).append("F, ").append(box.expandY).append("F, ").append(box.expandZ).append("F))");
            if (cube.boxes.indexOf(box) != (cube.boxes.size() - 1)) {
                sb.append("\n");
            } else {
                sb.append(", ");
            }
        }

        if (!(cube.rotAX == 0.0D && cube.rotAY == 0.0D && cube.rotAZ == 0.0D)) {
            sb.append("PartPose.offsetAndRotation(").append(cube.rotPX).append("F, ").append(cube.rotPY).append("F, ").append(cube.rotPZ).append("F").append(", ").append(Math.toRadians(cube.rotAX)).append("F, ").append(Math.toRadians(cube.rotAY)).append("F, ").append(Math.toRadians(cube.rotAZ)).append("F").append("));\n");
        } else {
            sb.append("PartPose.offset(").append(cube.rotPX).append("F, ").append(cube.rotPY).append("F, ").append(cube.rotPZ).append("F").append("));\n");
        }
    }

    public String getFieldName(Identifiable<?> cube) {
        return cube.name.replaceAll("[^A-Za-z0-9_$]", "");
    }
}