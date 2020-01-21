package org.springframework;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.PreDestroy;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.stereotype.Component;
import org.springframework.beans.factory.stereotype.Service;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class BeanFactory {

    private Map<String, Object> singletons = new HashMap();

    public Object getBean(String beanName){
        return singletons.get(beanName);
    }

    private List<BeanPostProcessor> postProcessors = new ArrayList<>();

    public void addPostProcessor(BeanPostProcessor postProcessor){
        postProcessors.add(postProcessor);
    }

    public void instantiate() throws URISyntaxException, ClassNotFoundException, IllegalAccessException, InstantiationException {

        ClassLoader classLoader = BeanFactory.class.getClassLoader();

        URL[] urls = ((URLClassLoader) classLoader).getURLs();

        int flag = 0;

        for (URL url : urls){
            if(flag == 0) {
                File fileDir = new File(url.toURI());
                String classLocationStart = "";
                classFinder(fileDir, classLocationStart);
                flag++;
            }
        }
        System.out.println(singletons);
    }

    private void classFinder(File fileDir, String classLocationStart) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        for(File classFile : Objects.requireNonNull(fileDir.listFiles())){
            String fileName = classFile.getName();
            System.out.println(fileName);
            if(classFile.isDirectory()){
                String classLocation = classLocationStart + getClassLocation(classFile);
                classFinder(classFile, classLocation);
            }
            else if(fileName.endsWith(".class")){

                String classLocation = classLocationStart + getClassLocation(classFile).substring(0 , fileName.lastIndexOf("."));

                Class classObject = Class.forName(classLocation);

                if(classObject.isAnnotationPresent(Component.class) || classObject.isAnnotationPresent(Service.class)){
                    System.out.println("Component: " + classObject);
                    Object instance = classObject.newInstance();
                    String beanName  = fileName.substring(0,1).toLowerCase() + fileName.substring(1, fileName.lastIndexOf("."));
                    singletons.put(beanName,instance);
                }
            }
        }
    }

    private String getClassLocation(File fileDir) {
        return fileDir
                .toString()
                .substring(fileDir
                        .toString()
                        .lastIndexOf("\\") + 1)
                .replace("/", ".") + ".";
    }


    public void populateProperties() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for(Object object : singletons.values()){
            autowiredScan(object);
        }
    }

    public void autowiredScan(Object object) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for(Field field : object.getClass().getDeclaredFields()){
            if(field.isAnnotationPresent(Autowired.class)){
                for(Object dependency : singletons.values()){
                    if(dependency.getClass().equals(field.getType())){
                        String setterName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
                        System.out.println("Setter name = " + setterName);
                        Method setter = object.getClass().getMethod(setterName, dependency.getClass());
                        setter.invoke(object, dependency);
                    }
                }
            }
        }
    }

    public void injectBeanNames(){
        for (String name : singletons.keySet()){
            Object bean = singletons.get(name);
            if(bean instanceof BeanNameAware){
                ((BeanNameAware) bean).setBeanName(name.substring(0,1).toUpperCase() + name.substring(1));
            }
        }
    }

    public void initializeBeans(){
        for (String name : singletons.keySet()) {
            Object bean = singletons.get(name);

            for (BeanPostProcessor postProcessor : postProcessors) {
                postProcessor.postProcessBeforeInitialization(bean, name);
            }

            if(bean instanceof InitializingBean){
                ((InitializingBean) bean).afterPropertiesSet();
            }

            for (BeanPostProcessor postProcessor : postProcessors) {
                postProcessor.postProcessAfterInitialization(bean, name);
            }
        }
    }

    public void close() {
        for (Object bean : singletons.values()) {
            for (Method method : bean.getClass().getMethods()) {
                if (method.isAnnotationPresent(PreDestroy.class)) {
                    try {
                        method.invoke(bean);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (bean instanceof DisposableBean) {
                ((DisposableBean) bean).destroy();
            }
        }
    }

    public Map<String, Object> getSingletons() {
        return singletons;
    }
}
