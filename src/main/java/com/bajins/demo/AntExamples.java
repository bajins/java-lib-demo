package com.bajins.demo;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.compilers.CompilerAdapter;
import org.apache.tools.ant.taskdefs.compilers.CompilerAdapterFactory;
import org.apache.tools.ant.types.Path;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.eclipse.jdt.internal.compiler.batch.Main;

import java.io.*;

/**
 * https://github.com/apache/ant
 */
public class AntExamples {

    /**
     * 创建ZIP文件
     *
     * @param sourcePath 文件或文件夹路径
     * @param zipPath    生成的zip文件保存路径（包括文件名）
     * @param isDrop     是否删除原文件:true删除、false不删除
     */
    public static void createZip(String sourcePath, String zipPath, Boolean isDrop) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(zipPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zos.setEncoding("gbk");//此处修改字节码方式。
            //createXmlFile(sourcePath,"293.xml");
            writeZip(new File(sourcePath), "", zos, isDrop);
        }
    }


    /**
     * 清空文件和文件目录
     *
     * @param f
     * @throws Exception
     */
    public static void clean(File f) throws Exception {
        String cs[] = f.list();
        if (cs == null || cs.length <= 0) {
            System.out.println("delFile:[ " + f + " ]");
            boolean isDelete = f.delete();
            if (!isDelete) {
                System.out.println("delFile:[ " + f.getName() + "文件删除失败！" + " ]");
                throw new Exception(f.getName() + "文件删除失败！");
            }
        } else {
            for (int i = 0; i < cs.length; i++) {
                String cn = cs[i];
                String cp = f.getPath() + File.separator + cn;
                File f2 = new File(cp);
                if (f2.exists() && f2.isFile()) {
                    System.out.println("delFile:[ " + f2 + " ]");
                    boolean isDelete = f2.delete();
                    if (!isDelete) {
                        System.out.println("delFile:[ " + f2.getName() + "文件删除失败！" + " ]");
                        throw new Exception(f2.getName() + "文件删除失败！");
                    }
                } else if (f2.exists() && f2.isDirectory()) {
                    clean(f2);
                }
            }
            System.out.println("delFile:[ " + f + " ]");
            boolean isDelete = f.delete();
            if (!isDelete) {
                System.out.println("delFile:[ " + f.getName() + "文件删除失败！" + " ]");
                throw new Exception(f.getName() + "文件删除失败！");
            }
        }
    }

    /**
     * @param file
     * @param parentPath
     * @param zos
     * @param isDrop
     * @throws Exception
     */
    private static void writeZip(File file, String parentPath, ZipOutputStream zos, Boolean isDrop) throws Exception {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) { // 处理文件夹
            parentPath += file.getName() + File.separator;
            File[] files = file.listFiles();
            if (files.length != 0) {
                for (File f : files) {
                    writeZip(f, parentPath, zos, isDrop);
                }
            } else { // 空目录则创建当前目录
                zos.putNextEntry(new ZipEntry(parentPath));
            }
        } else {
            try (FileInputStream fis = new FileInputStream(file);) {
                ZipEntry ze = new ZipEntry(parentPath + file.getName());
                zos.putNextEntry(ze);
                byte[] content = new byte[1024];
                int len;
                while ((len = fis.read(content)) != -1) {
                    zos.write(content, 0, len);
                    zos.flush();
                }
            } finally {
                if (isDrop) {
                    clean(file);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        /**
         * Eclipse JDT中的增量式编译器(ECJ)
         */
        //new ClasspathDirectory().initialize();
        /*JavaBuilder javaBuilder = new JavaBuilder();
        IBuildConfiguration buildConfig = javaBuilder.getBuildConfig();*/
        //CompilationProgress
        Main main = new Main(new PrintWriter(System.out), new PrintWriter(System.err), true, null, null);
        boolean compile = main.compile(new String[]{"", ""});


        /**
         * Apache Ant
         */
        Project project = new Project(); // 对应src/main/resources/build.xml
        /*Hashtable<String, Target> targets = project.getTargets();
        Target target = targets.get("");
        Task[] tasks = target.getTasks();*/
        Task task = project.createTask("test");
        Path resources = new Path(project, "d:\\");
        //resources.add();
        /**
         * 创建必要的编译器适配器
         *
         * jikes = jikes编译器
         * classic, javac1.1, javac1.2 = JDK 1.1 / 1.2中的标准编译器
         * modern, javac1.3, javac1.4, javac1.5 = JDK 1.3+的编译器
         * jvc, microsoft = 来自Microsoft SDK for Java / Visual J ++的命令行编译器
         * kjc = kopi编译器
         * gcj = 来自gcc的gcj编译器
         * sj, symantec = Symantec Java编译器
         * a fully qualified classname = 编译器适配器的名称
         */
        CompilerAdapter modern = CompilerAdapterFactory.getCompiler("modern", task, resources);
        boolean execute = modern.execute();

    }
}
