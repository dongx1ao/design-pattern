package com.gupao.designpattern.factorydesign;

import com.gupao.designpattern.factorydesign.annotation.ServletMatch;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileFilter;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ServletFactory {

    private static final String CAL_PRICE_PACKAGE = "com.gupao.designpattern.factorydesign";
    private ClassLoader classLoader = getClass().getClassLoader();
    private List<Class<? extends HttpServlet>> servletList;
    private static final String URL_SEPARATOR = "/";
    private static final String SERVLET_PREFIX = "servlet/";
    private ServletFactory(){
        init();
    }

    //负责解析请求的URI，我们约定请求的格式必须是/contextPath/servlet/servletName
    //不要怀疑约定的好处，因为LZ一直坚信一句话，约定优于配置
    private String parseRequestURI(HttpServletRequest httpServletRequest){
        String validURI = httpServletRequest.getRequestURI().replaceFirst(httpServletRequest.getContextPath() + URL_SEPARATOR, "");
        String servletName = "";
        if (validURI.startsWith(SERVLET_PREFIX)) {
            servletName = validURI.split(URL_SEPARATOR)[1];
        }

        return servletName;
    }

    public HttpServlet createHttpServlet(HttpServletRequest request){
        String servletName = parseRequestURI(request);
        //在策略列表查找策略
        return getHttpServlet(servletName);
    }

    private HttpServlet getHttpServlet(String servletName) {
        for (Class<? extends HttpServlet> clazz : servletList) {
            ServletMatch servletMatch = handleAnnotation(clazz);//获取该策略的注解
            //判断金额是否在注解的区间
            if (servletMatch.value().equals(servletName)) {
                try {
                    //是的话我们返回一个当前策略的实例
                    return clazz.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("策略获得失败");
                }
            }
        }
        throw new RuntimeException("策略获得失败");
    }

    public HttpServlet createHttpServlet(String servletName){
        //在策略列表查找策略
        return getHttpServlet(servletName);
    }

    //处理注解，我们传入一个策略类，返回它的注解
    private ServletMatch handleAnnotation(Class<? extends HttpServlet> clazz){
        Annotation[] annotations = clazz.getDeclaredAnnotations();
        if (annotations == null || annotations.length == 0) {
            return null;
        }
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i] instanceof ServletMatch) {
                return (ServletMatch) annotations[i];
            }
        }
        return null;
    }


    //在工厂初始化时要初始化策略列表
    private void init(){
        servletList = new ArrayList<Class<? extends HttpServlet>>();
        File[] resources = getResources();//获取到包下所有的class文件
        Class<HttpServlet> httpServletClazz = null;
        try {
            httpServletClazz = (Class<HttpServlet>) classLoader.loadClass(HttpServlet.class.getName());//使用相同的加载器加载策略接口
        } catch (ClassNotFoundException e1) {
            throw new RuntimeException("未找到策略接口");
        }
        for (int i = 0; i < resources.length; i++) {
            try {
                //载入包下的类
                Class<?> clazz = classLoader.loadClass(CAL_PRICE_PACKAGE + "."+resources[i].getName().replace(".class", ""));
                //判断是否是CalPrice的实现类并且不是CalPrice它本身，满足的话加入到策略列表
                if (HttpServlet.class.isAssignableFrom(clazz) && clazz != httpServletClazz) {
                    servletList.add((Class<? extends HttpServlet>) clazz);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    //获取扫描的包下面所有的class文件
    private File[] getResources(){
        try {
            File file = new File(classLoader.getResource(CAL_PRICE_PACKAGE.replace(".", "/")).toURI());
            return file.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    if (pathname.getName().endsWith(".class")) {//我们只扫描class文件
                        return true;
                    }
                    return false;
                }
            });
        } catch (URISyntaxException e) {
            throw new RuntimeException("未找到策略资源");
        }
    }

    public static ServletFactory getInstance(){
        return ServletFactoryInstance.instance;
    }

    private static class ServletFactoryInstance{

        private static ServletFactory instance = new ServletFactory();
    }
}
