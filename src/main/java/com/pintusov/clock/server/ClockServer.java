package com.pintusov.clock.server;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@ServerEndpoint("/clock")
public class ClockServer {

	Thread updateThread;
	boolean running = false;

	@OnOpen
	public void startClock(Session session) {
		final Session mySession = session;
		this.running = true;

		final DateTimeFormatter f = DateTimeFormatter.ofPattern("dd MMMM, yyyy, hh:mm:ss a z");
		this.updateThread = new Thread() {
			public void run() {
				while (running) {
					String dateString = f.format(ZonedDateTime.now(ZoneId.of("Europe/Minsk")));
					try {
						mySession.getBasicRemote().sendText(dateString);
						sleep(1000);
					} catch (IOException | InterruptedException ie) {
						running = false;
					}
				}
			}
		};
		this.updateThread.start();
	}

	@OnMessage
	public String handleMessage(String incomingMessage) {
		if ("stop".equals(incomingMessage)) {
			this.stopClock();
			return "clock stopped";
		} else {
			return "unknown message: " + incomingMessage;
		}
	}

	@OnError
	public void clockError(Throwable t) {
		this.stopClock();
	}

	@OnClose
	public void stopClock() {
		this.running = false;
		this.updateThread = null;
	}

}
