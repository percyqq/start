package org.learn.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 *
 */
public class ClassResearchUtil {

    private static final Logger logger = LoggerFactory.getLogger(ClassResearchUtil.class);

    /**
     * 获得包下面的所有的class
     *
     * @param pack package完整名称
     * @return List包含所有class的实例
     */
    public static List<?> getClasssFromPackage(String pack, Class<?> extendsClass) {
        List<Class<?>> clazzs = new ArrayList<>();

        // 是否循环搜索子包
        boolean recursive = true;

        // 包名字
        String packageName = pack;
        // 包名对应的路径名称
        String packageDirName = packageName.replace('.', '/');

        Enumeration<URL> dirs;

        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();

                String protocol = url.getProtocol();

                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    findClassInPackageByFile(packageName, filePath, recursive, clazzs, extendsClass);
                }
//				else if ("jar".equals(protocol)) {
//					System.out.println("jar类型的扫描");
//				}
            }

        } catch (Exception e) {
//			e.printStackTrace();
            logger.error("====>>>>error", e);
        }

        return clazzs;
    }

    /**
     * 在package对应的路径下找到所有的class
     *
     * @param packageName  package名称
     * @param filePath     package对应的路径
     * @param recursive    是否查找子package
     * @param clazzs       找到class以后存放的集合
     * @param extendsClass
     */
    public static void findClassInPackageByFile(String packageName, String filePath, final boolean recursive, List<Class<?>> clazzs, final Class<?> extendsClass) {
        File dir = new File(filePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 在给定的目录下找到所有的文件，并且进行条件过滤
        File[] dirFiles = dir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                boolean acceptDir = recursive && file.isDirectory();// 接受dir目录
                boolean acceptClass = file.getName().endsWith("class");// 接受class文件
                return acceptDir || acceptClass;
            }
        });

        for (File file : dirFiles) {
            if (file.isDirectory()) {
                findClassInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, clazzs, extendsClass);
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> curClass = Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + className);
                    if (extendsClass.isAssignableFrom(curClass)) {
                        clazzs.add(curClass);
                    }
                } catch (Exception e) {
//					e.printStackTrace();
                    logger.error("====>>>>error", e);
                }
            }
        }
    }

}
