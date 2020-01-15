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
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
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

    public void instantiate(String basePackage) throws IOException, URISyntaxException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        String path = basePackage.replace(".","/");
        System.out.println(path);
        Enumeration<URL> resources = classLoader.getResources(path);
        while (resources.hasMoreElements()){
            URL resource = resources.nextElement();
            File file = new File(resource.toURI());
            for(File classFile : Objects.requireNonNull(file.listFiles(), "The package is empty")){
                String fileName = classFile.getName();
                System.out.println(fileName);
                if(fileName.endsWith(".class")){
                    String className = fileName.substring(0,fileName.lastIndexOf("."));

                    Class classObject = Class.forName(basePackage + "." + className);

                    if(classObject.isAnnotationPresent(Component.class) || classObject.isAnnotationPresent(Service.class)){
                        System.out.println("Component: " + classObject);
                        Object instance = classObject.newInstance();
                        String beanName  = className.substring(0,1).toLowerCase() + className.substring(1);
                        singletons.put(beanName,instance);
                    }
                }
            }
        }
    }
    public void populateProperties() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        System.out.println("populateProperties");
        System.out.println(singletons);
        for(Object object : singletons.values()){
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
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
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
