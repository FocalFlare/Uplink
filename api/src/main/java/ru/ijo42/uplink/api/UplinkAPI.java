package ru.ijo42.uplink.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import ru.ijo42.uplink.api.config.Config;
import ru.ijo42.uplink.api.util.DisplayDataManager;
import ru.ijo42.uplink.api.util.MiscUtil;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class UplinkAPI {
	public static ForgeAPI forgeImpl;
	private static IPCClient RPC;

	public static void init(ForgeAPI forgeImpl, PresenceListener presenceListener) {
		UplinkAPI.forgeImpl = forgeImpl;
		setupPresenceManager(forgeImpl.getConfigDir().resolve("Uplink.json"), presenceListener);
	}

	private static void setupPresenceManager(Path configPath, PresenceListener presenceListener) {
		if (Files.notExists(configPath)) {
			try {
				Files.copy(getResource("Uplink.json"), configPath);
			} catch (Exception e) {
				System.err.println("[Uplink] Could not copy default config to " + configPath);
				System.err.println(e.toString());
				return;
			}
		}

		Gson gson = new GsonBuilder().create();

		Config config;

		try {
			config = MiscUtil.verifyConfig(
					gson.fromJson(Files.newBufferedReader(configPath), Config.class)
			);
		} catch (Exception e) {
			System.err.println("[Uplink] Could not load config");
			System.err.println(e.toString());
			return;
		}

		DisplayDataManager dataManager = new DisplayDataManager(config, forgeImpl.getConfigDir().resolve("Uplink\\"));

		PresenceManager manager = new PresenceManager(dataManager, config);

		RPC = new IPCClient(Long.parseLong(manager.getConfig().clientId));

		Thread callbackHandler = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				RPC.getStatus();
				try {
					//noinspection BusyWait
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					RPC.close();
				}
			}
			RPC.close();
		}, "RPC-Callback-Handler");
		callbackHandler.start();

		Runtime.getRuntime().addShutdownHook(new Thread(callbackHandler::interrupt));

		try {
			RPC.setListener(new IPCListener() {
				@Override
				public void onReady() {
					RPC.sendRichPresence(manager.initLoading());
					presenceListener.init(RPC, manager);
					forgeImpl.afterInit(presenceListener);
				}
			});
			RPC.connect();
		} catch (NoDiscordClientException e) {
			System.err.println(e.toString());
			e.printStackTrace();
		}
	}

	public static InputStream getResource(String name) {
		return UplinkAPI.class.getResourceAsStream(name);
	}
}
