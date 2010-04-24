import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public final class DepencyBrowser {
	private static final int port = 1234;
	
	public static void main(String[] args) {
		DemoBase.initLnF();
		DepencyBrowser.listen();
	}

	static void listen () {
		try {
			ServerSocket sock = new ServerSocket(port);
			while (true) {
				Socket cli = sock.accept();
				System.out.println("new connection");
				DemoBase d = new DemoBase(cli);
				d.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
