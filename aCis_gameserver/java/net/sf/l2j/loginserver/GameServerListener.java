package net.sf.l2j.loginserver;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.Config;

public class GameServerListener extends FloodProtectedListener {

	private static List<GameServerThread> _gameServers = new ArrayList<>();

	public GameServerListener() throws IOException {
		super(Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT);
	}

	@Override
	public void addClient(Socket s) {
		_gameServers.add(new GameServerThread(s));
	}

	public void removeGameServer(GameServerThread gst) {
		_gameServers.remove(gst);
	}
}
