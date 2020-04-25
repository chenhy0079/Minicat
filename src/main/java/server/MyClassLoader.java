package server;

import sun.misc.Launcher;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import static sun.misc.Launcher.*;

public class MyClassLoader extends ClassLoader {

    private String path;

    private final String fileType = ".class";

    private byte[] loadClassData(String name) throws ClassNotFoundException{

        FileInputStream fis = null;

        ByteArrayOutputStream baos = null;

        byte[] data = null;

        try{

            // 读取文件内容

            name = name.replaceAll("\\.","\\\\");

            System.out.println("加载文件名："+name);

            // 将文件读取到数据流中

            fis = new FileInputStream(path + "\\" + name  + fileType);

            baos = new ByteArrayOutputStream();

            int ch = 0;

            while ((ch = fis.read()) != -1){

                baos.write(ch);

            }

            data = baos.toByteArray();

        }catch (Exception e){

            throw new ClassNotFoundException("Class is not found:"+name,e);

        }finally {

            // 关闭数据流

            try {

                fis.close();

                baos.close();

            }catch (Exception e){

                e.printStackTrace();

            }

        }

        return data;

    }

    protected Class findClass(String name) throws ClassNotFoundException{

        byte[] data = loadClassData(name);

        return defineClass(name,data,0,data.length);

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public MyClassLoader(ClassLoader parent, String path) {
        super(parent);
        this.path = path;
    }

    public MyClassLoader(String path) {
        this.path = path;
    }
}
