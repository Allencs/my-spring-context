package com.allen.spring;

import com.allen.spring.annotations.*;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllenApplicationContext {

    private final Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
    private final Map<String, Object> singletonObjects = new HashMap<>();
    private final List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>(100);
    private final List<File> classFiles = new ArrayList<>(100);

    public AllenApplicationContext(Class<?> configClass) {

        // 扫描容器需要加载的包
        scan(configClass);

        // 遍历beanDefinition列表并实例化bean
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if (beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }

    }

    private void scan(Class<?> configClass) {

        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            // 获取ComponentScan注解类实例
            ComponentScan componentScanAnnotation = configClass.getAnnotation(ComponentScan.class);
            String path = componentScanAnnotation.value();
            // 将com.allen.service路径转换成url类型路径
            String url = path.replace(".", "/");
            ClassLoader classLoader = this.getClass().getClassLoader();
            // 获取扫描路径URL对象
            URL resource = classLoader.getResource(url);
            assert resource != null;
            File file = new File(resource.getFile());
            listFile(file);
            for (File f : this.classFiles) {
                // 获取String类型绝对路径
                String absolutePath = f.getAbsolutePath();
                // 截取绝对路径中字符串的子串
                absolutePath = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
                // 替换成com.allen.application.OrderService类型路径
                absolutePath = absolutePath.replace("/", ".");

                try {
                    Class<?> clazz = classLoader.loadClass(absolutePath);
                    // 判断类是否使用@Component注解
                    if (clazz.isAnnotationPresent(Component.class)) {

                        // 判断是否实现BeanPostProcessor接口
                        // ***********
                        if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                            BeanPostProcessor instance = (BeanPostProcessor) clazz.getConstructor().newInstance();
                            beanPostProcessorList.add(instance);
                        }

                        Component componentAnnotation = (Component) clazz.getAnnotation(Component.class);
                        // 获取自定义beanName
                        String beanName = componentAnnotation.value();
                        // 如果没有配置自定义beanName，自动生成beanName
                        if ("".equals(beanName)) {
                            // Spring原生方法
                            beanName = Introspector.decapitalize(clazz.getSimpleName());
                        }

                        // 生成BeanDefinition
                        BeanDefinition beanDefinition = new BeanDefinition();
                        // 设置beanType为当前被注解类的类型
                        beanDefinition.setType(clazz);
                        // 设置bean的作用域
                        if (clazz.isAnnotationPresent(Scope.class)) {
                            Scope scopeAnnotation = (Scope) clazz.getAnnotation(Scope.class);
                            beanDefinition.setScope(scopeAnnotation.value());
                        } else {
                            beanDefinition.setScope("singleton");
                        }

                        // 缓存beanName -> beanDefinition
                        beanDefinitionMap.put(beanName, beanDefinition);
                    }
                } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 递归遍历文件目录
     * @param file
     */
    private void listFile(File file) {
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isFile()) {
                this.classFiles.add(f);
            } else {
                listFile(f);
            }
        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {

        Class<?> clazz = beanDefinition.getType();
        Object instance = null;
        try {
            // 使用无参构造方法实例化Bean
            instance = clazz.getConstructor().newInstance();

            // Autowired依赖注入（使用getFields获取不到没有被public修饰的变量）
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    // 设置权限，允许在反射时访问实例私有变量
                    field.setAccessible(true);
                    // 直接将实例化的bean赋值给变量
                    field.set(instance, getBean(field.getName()));
                }
                // 处理@Value注解的变量
                if (field.isAnnotationPresent(Value.class)) {
                    field.setAccessible(true);
                    Value valueAnnotation = field.getAnnotation(Value.class);
                    if (Float.class.isAssignableFrom(field.getType())) {
                        field.set(instance, Float.valueOf(valueAnnotation.value()));
                    } else if (Integer.class.isAssignableFrom(field.getType())) {
                        field.set(instance, Integer.valueOf(valueAnnotation.value()));
                    } else if (String.class.isAssignableFrom(field.getType())) {
                        field.set(instance, valueAnnotation.value());
                    }
                }
            }

            // 是否实现相关回调接口（这边是BeanNameAware，用于设置bean名称）
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            // BeanPostProcessor相关功能
            // 初始化之前
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            // 初始化
            if (instance instanceof InitializeBean) {
                ((InitializeBean) instance).afterPropertiesSet();
            }

            // 初始化后
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return instance;
    }

    public Object getBean(String beanName) {

        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition != null) {
            // bean的作用域是否为单例
            if (beanDefinition.getScope().equals("singleton")) {
                Object singletonBean = singletonObjects.get(beanName);
                if (singletonBean == null) {
                    singletonBean = this.createBean(beanName, beanDefinition);
                    singletonObjects.put(beanName, singletonBean);
                }
                return singletonBean;
            } else {
                // 原型模式
                Object prototypeBean = createBean(beanName, beanDefinition);
                return prototypeBean;
            }
        } else {
            throw new NullPointerException();
        }
    }
}
