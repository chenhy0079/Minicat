package server;

import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class RequestProcessor extends Thread {

    private Socket socket;
    private Service service;

    public RequestProcessor(Socket socket, Service service) {
        this.socket = socket;
        this.service = service;
    }

    @Override
    public void run() {
        try{
            InputStream inputStream = socket.getInputStream();

            // 封装Request对象和Response对象
            Request request = new Request(inputStream);
            Response response = new Response(socket.getOutputStream());

            List<Host> hosts = service.getMapper().getHosts();

            HttpServlet httpServlet = null;

            for (Host host : hosts) {
                if(host.getName().equals(request.getHost())){
                    for (Wapper wapper : host.getWapperList()) {
                        if(wapper.getUrlPattern().equals(request.getUrl())){
                            httpServlet = wapper.getHttpServlet();
                        }
                    }
                }
            }




            if(httpServlet == null) {
                response.outputHtml(request.getUrl());
            }else{
                // 动态资源servlet请求
                httpServlet.service(request,response);
            }

            socket.close();

        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}
