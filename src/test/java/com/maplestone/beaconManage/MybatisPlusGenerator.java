package com.maplestone.beaconManage;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.FileType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.maplestone.dataCollect.common.utils.StrUtils;
import com.maplestone.dataCollect.dao.BaseEntity;
import com.maplestone.dataCollect.service.BaseIService;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @Author hmx
 * @CreateTime 2021-06-21 9:20
 */

public class MybatisPlusGenerator {

    @Test
    public void mySqlGenerator() {
        String tableName = "upload_data_mng";// 要生成的表名
        String moduleName = "";// 模块名称 用作再提出一个包此处前面加. 不添加则为空字符
        String projectPath = "E:\\workspace-idea\\beacon-manage-back";// 输出目录 此处只要给你的项目文件根目录
        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(
                "jdbc:mysql://127.0.0.1:3306/test_beaconmanage?serverTimezone=Asia/Shanghai&characterEncoding=utf-8&useSSL=false&useUnicode=true");
        // dsc.setSchemaName("public");
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        dsc.setUsername("root");
        dsc.setPassword("root");
        codeGenerator(dsc, tableName, moduleName, projectPath);
    }

    /** 代码生成器 生成entity mapper mapper.xml service */
    public static void codeGenerator(DataSourceConfig dsc, String tableName, String moduleName, String projectPath) {
        String tableClassName = StrUtils.captureName(StringUtils.underlineToCamel(tableName));
        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();
        mpg.setDataSource(dsc);

        // 包配置
        PackageConfig pc = new PackageConfig();
        // pc.setModuleName(scanner("模块名"));
        pc.setParent("com.maplestone.dataCollect");
        pc.setEntity("dao.entity" + moduleName);
        pc.setMapper("dao.mapper" + moduleName);
        // pc.setXml("dao.mapper"+moduleName);
        pc.setService("service.impl" + moduleName);
        // pc.setServiceImpl("service."+moduleName);
        mpg.setPackageInfo(pc);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();
        /** 如要生成此部分内容 注释掉即可 */
        // 控制 不生成 controller
        templateConfig.setController(null);
        // 此处设置模板文件不需要添加文件后缀
        templateConfig.setService("template/service");
        templateConfig.setServiceImpl(null);
        // templateConfig.setMapper(null);
        templateConfig.setXml(null);
        mpg.setTemplate(templateConfig);
        /** 如要生成此部分内容 注释掉即可 */

        // 策略配置
        StrategyConfig strategyConfig = new StrategyConfig();
        // pojo类超类
        strategyConfig.setSuperEntityClass(BaseEntity.class);
        // 去掉父类属性
        strategyConfig.setSuperEntityColumns("id", "created_user", "created_time", "last_modified_user",
                "last_modified_time");

        // strategyConfig.setSuperServiceClass(BaseIService.class);
        strategyConfig.setSuperServiceImplClass(BaseIService.class);

        // 表名转驼峰
        strategyConfig.setNaming(NamingStrategy.underline_to_camel);
        // 字段驼峰命名
        strategyConfig.setColumnNaming(NamingStrategy.underline_to_camel);
        // set方法builder模式
        strategyConfig.setChainModel(true);
        // 使用lombok注解
        strategyConfig.setEntityLombokModel(true);
        // 不生成serial version uuid
        strategyConfig.setEntitySerialVersionUID(false);
        // 要生成的表名
        strategyConfig.setInclude(tableName);
        mpg.setStrategy(strategyConfig);

        // 自定义配置，在模版中cfg.superColums 获取
        InjectionConfig in = new InjectionConfig() {
            @Override
            public void initMap() {
                Map<String, Object> map = new HashMap<String, Object>();
                // TODO 这里解决子类会生成父类属性的问题，在模版里会用到该配置
                map.put("superColums", strategyConfig.getSuperEntityColumns());
                map.put("parentPackage", pc.getParent());
                map.put("moduleName", moduleName);
                map.put("tableName", tableClassName);
                this.setMap(map);
            }
        };

        // 自定义输出配置
        String templatePath = "/templates/mapper.xml.vm";
        List<FileOutConfig> focList = new ArrayList<>();
        // 自定义配置会被优先输出
        focList.add(new FileOutConfig(templatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
                return projectPath + "/src/main/resources/mapper/" + tableInfo.getEntityName() + "Mapper.xml";
            }
        });
        in.setFileOutConfigList(focList);

        in.setFileCreate((configBuilder, fileType, filePath) -> {
            // 如果是Entity或xml 直接覆盖重写
            if (fileType == FileType.ENTITY || fileType == FileType.XML || filePath.contains(".xml")) {
                return true;
            }
            // 否则先判断文件是否存在
            File file = new File(filePath);
            boolean exist = file.exists();
            if (!exist) {
                file.getParentFile().mkdirs();
            }
            // 文件不存在或者全局配置的fileOverride为true才写文件
            return !exist || configBuilder.getGlobalConfig().isFileOverride();
        });

        mpg.setCfg(in);

        // 全局配置
        GlobalConfig globalConfig = new GlobalConfig();
        // 输出目录
        globalConfig.setOutputDir(projectPath + "/src/main/java");
        globalConfig.setAuthor("zpc");
        globalConfig.setOpen(false);
        globalConfig.setBaseResultMap(true);
        globalConfig.setServiceName("%sService");
        // 指定生成日期类型
        globalConfig.setDateType(DateType.ONLY_DATE);
        mpg.setGlobalConfig(globalConfig);
        // 使用beetl模版引擎
        // mpg.setTemplateEngine(new BeetlTemplateEngine());
        mpg.execute();
    }

}
