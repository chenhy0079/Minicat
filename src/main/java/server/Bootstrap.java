package server;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Minicat的主类
 */
public class Bootstrap {

    /**
     * 定义socket监听的端口号
     */
    private int port = 8080;

    private String appBase = "E:\\lagou\\wabapps";

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private Service service;


    /**
     * Minicat启动需要初始化展开的一些操作
     */
    public void start() throws Exception {

        // 加载解析相关的配置，server.xml
        createServer();

        // 定义一个线程池
        int corePoolSize = 10;
        int maximumPoolSize = 50;
        long keepAliveTime = 100L;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(50);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();


        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                threadFactory,
                handler
        );





        /*
            完成Minicat 1.0版本
            需求：浏览器请求http://localhost:8080,返回一个固定的字符串到页面"Hello Minicat!"
         */
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("=====>>>Minicat start on port：" + port);

        while (true) {

            Socket socket = serverSocket.accept();
            RequestProcessor requestProcessor = new RequestProcessor(socket, service);
            //requestProcessor.start();
            threadPoolExecutor.execute(requestProcessor);
        }


    }

    private void scanProject(String appBase, Host host) throws FileNotFoundException {

        List<String> projectNames = new ArrayList<>();
        File appBaseFile = new File(appBase);
        System.out.println(appBaseFile.exists());
        File[] files = appBaseFile.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                projectNames.add(file.getName());
            }
        }

        for (String projectName : projectNames) {
            loadServlet(appBase + "\\" + projectName + "\\" + "web.xml", host);
        }
    }

    /**
     * 加载解析web.xml，初始化Servlet
     */
    private void loadServlet(String file, Host host) throws FileNotFoundException {
        InputStream resourceAsStream = new FileInputStream(file);
        SAXReader saxReader = new SAXReader();

        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();

            List<Element> selectNodes = rootElement.selectNodes("//servlet");
            for (int i = 0; i < selectNodes.size(); i++) {
                Element element = selectNodes.get(i);
                // <servlet-name>lagou</servlet-name>
                Element servletnameElement = (Element) element.selectSingleNode("servlet-name");
                String servletName = servletnameElement.getStringValue();
                // <servlet-class>server.LagouServlet</servlet-class>
                Element servletclassElement = (Element) element.selectSingleNode("servlet-class");
                String servletClass = servletclassElement.getStringValue();

                File classFile = new File(new File(file).getParent());

                //2.获取URL对象
                URL url = classFile.toURI().toURL();

                //3.创建URL类加载器
                URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url});

                //4.通过urlClassLoader加载器调用loadClass方法传入类名动态加载class文件并获取class对象:会初始化静态块
                Class<?> classs = urlClassLoader.loadClass(servletClass);

                //5.通过class对象创建实例
                HttpServlet newInstance = (HttpServlet) classs.newInstance();

                // 根据servlet-name的值找到url-pattern
                Element servletMapping = (Element) rootElement.selectSingleNode("/web-app/servlet-mapping[servlet-name='" + servletName + "']");
                // /lagou
                String urlPattern = servletMapping.selectSingleNode("url-pattern").getStringValue();

                Wapper wapper = new Wapper();
                wapper.setUrlPattern(urlPattern);
                wapper.setHttpServlet(newInstance);

                host.getWapperList().add(wapper);

            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    private void createServer() throws DocumentException, FileNotFoundException {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("server.xml");

        SAXReader saxReader = new SAXReader();

        Document document = saxReader.read(resourceAsStream);
        Element rootElement = document.getRootElement();

        service = new Service();

        List<Element> selectNodes = rootElement.selectNodes("//Service");
        for (int i = 0; i < selectNodes.size(); i++) {

            Element element = selectNodes.get(i);

            Element connectorElement = (Element) element.selectSingleNode("//Connector");

            port = Integer.valueOf(connectorElement.attribute("port").getValue());

            Element enginElement = (Element) element.selectSingleNode("//Engine");

            List<Element> hostElements = enginElement.selectNodes("//Host");

            ArrayList<Host> hosts = new ArrayList<>();

            for (Element hostElement : hostElements) {

                Host host = new Host();

                host.setName(hostElement.attribute("name").getValue());

                host.setAppBase(hostElement.attribute("appBase").getValue());

                scanProject(host.getAppBase(), host);

                hosts.add(host);
            }

            service.getMapper().setHosts(hosts);
        }

    }
        /**
         * Minicat 的程序启动入口
         * @param args
         */
        public static void main(String[] args){
            Bootstrap bootstrap = new Bootstrap();
            try {
                // 启动Minicat
                bootstrap.start();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
}
