package org.learn.config.jsp;


import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.*;
import org.apache.tomcat.util.buf.UriUtil;
import org.learn.utils.BaseEnum;
import org.learn.utils.BaseEnumUtil;
import org.learn.utils.ClassResearchUtil;
import org.springframework.util.ResourceUtils;

import javax.servlet.ServletContext;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * @date: 2020/03/24
 * @description: 参考
 * https://juejin.im/post/5ad21eb5f265da23945feb62
 * https://github.com/hengyunabc/spring-boot-fat-jar-jsp-sample
 * https://stackoverflow.com/questions/56537151/why-does-spring-boot-not-support-jsp-while-it-can-render-the-page-if-we-add-prop
 */
@Slf4j
public class StaticResourceConfigurer implements LifecycleListener {

    private final Context context;

    StaticResourceConfigurer(Context context) {
        this.context = context;
    }

    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        if (event.getType().equals(Lifecycle.CONFIGURE_START_EVENT)) {
            try {
                //jar:file:/a.jar!/BOOT-INF/classes!/
                URL url = ResourceUtils.getURL(ResourceUtils.CLASSPATH_URL_PREFIX);
                String path = "/";
                BaseLocation baseLocation = new BaseLocation(url);
                if (baseLocation.getArchivePath() != null) {//当有archivePath时肯定是jar包运行
                    //url= jar:file:/a.jar
                    //此时Tomcat再拆分出base = /a.jar archivePath= /
                    url = new URL(url.getPath().replace("!/" + baseLocation.getArchivePath(), ""));
                    //path=/BOOT-INF/classes
                    log.info("URL >> {}", url);
                    path = "/" + baseLocation.getArchivePath().replace("!/", "");
                }
                log.info("URL >> {}", path);
                context.getResources().createWebResourceSet(
                        WebResourceRoot.ResourceSetType.RESOURCE_JAR, "/", url, path);
                ServletContext servletContext = context.getServletContext();
                initAttributeOfCxtPath(servletContext, "scm-product");
                initAttributeOfEnums(servletContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Unit tests need to access this class
    static class BaseLocation {

        private final String basePath;
        private final String archivePath;

        BaseLocation(URL url) {
            File f = null;

            if ("jar".equals(url.getProtocol()) || "war".equals(url.getProtocol())) {
                String jarUrl = url.toString();
                int endOfFileUrl = -1;
                if ("jar".equals(url.getProtocol())) {
                    endOfFileUrl = jarUrl.indexOf("!/");
                } else {
                    endOfFileUrl = jarUrl.indexOf(UriUtil.getWarSeparator());
                }
                String fileUrl = jarUrl.substring(4, endOfFileUrl);
                try {
                    f = new File(new URL(fileUrl).toURI());
                } catch (MalformedURLException | URISyntaxException e) {
                    throw new IllegalArgumentException(e);
                }
                int startOfArchivePath = endOfFileUrl + 2;
                if (jarUrl.length() > startOfArchivePath) {
                    archivePath = jarUrl.substring(startOfArchivePath);
                } else {
                    archivePath = null;
                }
            } else if ("file".equals(url.getProtocol())) {
                try {
                    f = new File(url.toURI());
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(e);
                }
                archivePath = null;
            } else {
                throw new IllegalArgumentException("standardRoot.unsupportedProtocol: " + url.getProtocol());
            }

            basePath = f.getAbsolutePath();
        }


        String getBasePath() {
            return basePath;
        }


        String getArchivePath() {
            return archivePath;
        }
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private void initAttributeOfEnums(ServletContext context) {
        List<Class<? extends Enum>> list = (List<Class<? extends Enum>>) ClassResearchUtil.getClasssFromPackage("com.calm.b.common.enums", BaseEnum.class);
        for (Class<? extends Enum> clazz : list) {
            //存放到application域中，供el表达式使用
            String simpleName = clazz.getSimpleName();
            Map<String, Enum> consts = new LinkedHashMap<String, Enum>();
            for (Enum e : clazz.getEnumConstants()) {
                consts.put(e.name(), e);
            }
            context.setAttribute(simpleName, consts);
            //将枚举拼成jsonArray字符串，存于application域中
            context.setAttribute(clazz.getSimpleName() + "Array", BaseEnumUtil.enumsToJsonArray(clazz));
            context.setAttribute(clazz.getSimpleName() + "Json", BaseEnumUtil.enumsToJsonObject(clazz));
        }
    }

    private void initAttributeOfCxtPath(ServletContext context, String contextName) {
        //将ctxpath放入application域中，供el表达式使用
        context.setAttribute("ctxPath", "/" + contextName);
    }


}
