package com.caixy.adminSystem;

import cn.hutool.core.io.FileUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 代码生成器
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.CodeGeneratorTest
 * @since 2024-06-04 21:26
 **/
public class CodeGeneratorTest
{
    @Data
    @AllArgsConstructor
    public static class Task {
        private final String packageName = this.getClass().getPackage().getName();
        private String modelPackageName;
        private String modelDesc;
        private String lowerModelName;
        private String modelName;
        private final String outputPathName = "codeGenerator";
    }

    private static final List<Task> allTasks = Arrays.asList(
            new Task("user", "用户信息", "user", "User")
    );
    
    public static void main(String[] args) throws TemplateException, IOException {
        for (Task task : allTasks) {
            generateFilesForTask(task);
        }
    }

    private static void generateFilesForTask(Task task) throws TemplateException, IOException {
        String projectPath = System.getProperty("user.dir");
        String outputRootPath = String.format("%s/%s", projectPath, task.getOutputPathName());
        String inputRootPath = projectPath + File.separator;

        // 数据模型设置
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("modelPackageName", task.getModelPackageName());
        dataModel.put("packageName", task.getPackageName());
        dataModel.put("dataName", task.getModelDesc());
        dataModel.put("dataKey", task.getLowerModelName());
        dataModel.put("upperDataKey", task.getModelName());

        // 模板路径和生成路径配置
        generateFile(inputRootPath + "src/main/resources/generate-templates/TemplateController.java.ftl",
                String.format("%s/controller/%sController.java", outputRootPath, task.getModelName()), dataModel);
        generateFile(inputRootPath + "src/main/resources/generate-templates/TemplateService.java.ftl",
                String.format("%s/service/%sService.java", outputRootPath, task.getModelName()), dataModel);
        generateFile(inputRootPath + "src/main/resources/generate-templates/TemplateServiceImpl.java.ftl",
                String.format("%s/service/impl/%sServiceImpl.java", outputRootPath, task.getModelName()), dataModel);
        generateFile(inputRootPath + "src/main/resources/generate-templates/model/TemplateAddRequest.java.ftl",
                String.format("%s/model/dto/%s/%sAddRequest.java", outputRootPath, task.getModelPackageName(), task.getModelName()), dataModel);
        generateFile(inputRootPath + "src/main/resources/generate-templates/model/TemplateQueryRequest.java.ftl",
                String.format("%s/model/dto/%s/%sQueryRequest.java", outputRootPath, task.getModelPackageName(), task.getModelName()), dataModel);
        generateFile(inputRootPath + "src/main/resources/generate-templates/model/TemplateEditRequest.java.ftl",
                String.format("%s/model/dto/%s/%sEditRequest.java", outputRootPath, task.getModelPackageName(), task.getModelName()), dataModel);
        generateFile(inputRootPath + "src/main/resources/generate-templates/model/TemplateUpdateRequest.java.ftl",
                String.format("%s/model/dto/%s/%sUpdateRequest.java", outputRootPath, task.getModelPackageName(), task.getModelName()), dataModel);
        generateFile(inputRootPath + "src/main/resources/generate-templates/model/TemplateVO.java.ftl",
                String.format("%s/model/vo/%s/%sVO.java", outputRootPath, task.getModelPackageName(), task.getModelName()), dataModel);

        System.out.println("所有文件生成成功，实体类：" + task.getModelName());
    }

    /**
     * 通用生成文件方法
     *
     * @param inputPath  模板文件输入路径
     * @param outputPath 输出路径
     * @param model      数据模型
     * @throws IOException
     * @throws TemplateException
     */
    private static void generateFile(String inputPath, String outputPath, Map<String, Object> model) throws IOException, TemplateException {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
        File templateDir = new File(inputPath).getParentFile();
        configuration.setDirectoryForTemplateLoading(templateDir);
        configuration.setDefaultEncoding("utf-8");

        Template template = configuration.getTemplate(new File(inputPath).getName());

        if (!FileUtil.exist(outputPath)) {
            FileUtil.touch(outputPath);
        }

        try (Writer out = new FileWriter(outputPath)) {
            template.process(model, out);
        }

        System.out.println("生成成功：" + outputPath);
    }
}
