package net.sf.l2j.loginserver;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.LogManager;
import org.slf4j.Logger;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.mmocore.SelectorConfig;
import net.sf.l2j.commons.mmocore.SelectorThread;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.loginserver.network.LoginClient;
import net.sf.l2j.loginserver.network.LoginPacketHandler;

public class LoginServer {

	private static final Logger _log = LoggerFactory.getLogger(LoginServer.class.getName());

	public static final int PROTOCOL_REV = 0x0102;

	private static LoginServer _loginServer;

	private GameServerListener _gameServerListener;
	private SelectorThread<LoginClient> _selectorThread;

	public static void main(String[] args) throws Exception {
		_loginServer = new LoginServer();
	}

	public LoginServer() throws Exception {
		// Create log folder
		new File("./log").mkdir();
		new File("./log/console").mkdir();
		new File("./log/error").mkdir();

		// Create input stream for log file -- or store file data into memory
		try (InputStream is = new FileInputStream(new File("config/logging.properties"))) {
			LogManager.getLogManager().readConfiguration(is);
		}

		StringUtil.printSection("aCis");

		// Initialize config
		Config.loadLoginServer();

		// Factories
		L2DatabaseFactory.getInstance();

		StringUtil.printSection("LoginController");
		LoginController.load();
		GameServerTable.getInstance();

		StringUtil.printSection("Ban List");
		loadBanFile();

		StringUtil.printSection("IP, Ports & Socket infos");
		InetAddress bindAddress = null;
		if (!Config.LOGIN_BIND_ADDRESS.equals("*")) {
			try {
				bindAddress = InetAddress.getByName(Config.LOGIN_BIND_ADDRESS);
			} catch (UnknownHostException uhe) {
				_log.error("WARNING: The LoginServer bind address is invalid, using all available IPs. Reason: " + uhe.getMessage());
				uhe.printStackTrace();
			}
		}

		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;

		final LoginPacketHandler lph = new LoginPacketHandler();
		final SelectorHelper sh = new SelectorHelper();
		try {
			_selectorThread = new SelectorThread<>(sc, sh, lph, sh, sh);
		} catch (IOException ioe) {
			_log.error("FATAL: Failed to open selector. Reason: " + ioe.getMessage());
			ioe.printStackTrace();

			System.exit(1);
		}

		try {
			_gameServerListener = new GameServerListener();
			_gameServerListener.start();
			_log.info("Listening for gameservers on " + Config.GAME_SERVER_LOGIN_HOST + ":" + Config.GAME_SERVER_LOGIN_PORT);
		} catch (IOException ioe) {
			_log.error("FATAL: Failed to start the gameserver listener. Reason: " + ioe.getMessage());
			ioe.printStackTrace();

			System.exit(1);
		}

		try {
			_selectorThread.openServerSocket(bindAddress, Config.PORT_LOGIN);
		} catch (IOException ioe) {
			_log.error("FATAL: Failed to open server socket. Reason: " + ioe.getMessage());
			ioe.printStackTrace();

			System.exit(1);
		}
		_selectorThread.start();
		_log.info("Loginserver ready on " + (bindAddress == null ? "*" : bindAddress.getHostAddress()) + ":" + Config.PORT_LOGIN);

		StringUtil.printSection("Waiting for gameserver answer");
	}

	public static LoginServer getInstance() {
		return _loginServer;
	}

	public GameServerListener getGameServerListener() {
		return _gameServerListener;
	}

	private static void loadBanFile() {
		File banFile = new File("config/banned_ips.properties");
		if (banFile.exists() && banFile.isFile()) {
			try (LineNumberReader reader = new LineNumberReader(new FileReader(banFile))) {
				String line;
				String[] parts;

				while ((line = reader.readLine()) != null) {
					line = line.trim();
					// check if this line isnt a comment line
					if (line.length() > 0 && line.charAt(0) != '#') {
						// split comments if any
						parts = line.split("#");

						// discard comments in the line, if any
						line = parts[0];
						parts = line.split(" ");

						String address = parts[0];
						long duration = 0;

						if (parts.length > 1) {
							try {
								duration = Long.parseLong(parts[1]);
							} catch (NumberFormatException e) {
								_log.warn("Skipped: Incorrect ban duration (" + parts[1] + "). Line: " + reader.getLineNumber());
								continue;
							}
						}

						try {
							LoginController.getInstance().addBanForAddress(address, duration);
						} catch (UnknownHostException e) {
							_log.warn("Skipped: Invalid address (" + parts[0] + "). Line: " + reader.getLineNumber());
						}
					}
				}
			} catch (IOException e) {
				_log.warn("Error while reading banned_ips.properties. Details: " + e.getMessage());
				e.printStackTrace();
			}
			_log.info("Loaded " + LoginController.getInstance().getBannedIps().size() + " banned IP(s).");
		} else {
			_log.warn("banned_ips.properties is missing. Ban listing is skipped.");
		}
	}

	public void shutdown(boolean restart) {
		Runtime.getRuntime().exit(restart ? 2 : 0);
	}
}
