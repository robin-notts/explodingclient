/**
 * Copyright 2010 The University of Nottingham
 * 
 * This file is part of GenericAndroidClient.
 *
 *  GenericAndroidClient is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  GenericAndroidClient is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with GenericAndroidClient.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package uk.ac.horizon.ug.exploding.client;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.json.JSONStringer;

import uk.ac.horizon.ug.exploding.client.logging.LoggingUtils;
import uk.ac.horizon.ug.exploding.client.model.Player;
import uk.ac.horizon.ug.exploding.client.model.Position;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/** Background thread to handle communication with the server.
 * A full service is not required as it is all within a single application.
 * 
 * 
 * @author cmg
 *
 */
public class BackgroundThread implements Runnable {
	public static final String TAG = "ExplodingPlacesBackgroundThread";
	private static final long THREAD_SLEEP_MS = 1000;
	// in preferences.xml
	public static final int CLIENT_VERSION = 1;
	private static final String CLIENT_TYPE = "AndroidDevclient";
	private static final String LOGIN_PATH = "login";
	private static final String MESSAGES_PATH = "messages";
	// 10 seconds for testing?!
	private static final int POLL_INTERVAL_MS = 10000;
	private static Handler handler;
	public static final String LOGTYPE_CLIENT_STATE = "ClientState";
	public static final String LOGTYPE_BACKGROUND_THREAD = "BackgroundThread";
	/** cons - private */
	private BackgroundThread() {
		super();
	}
	public static synchronized void setHandler(Handler h) {
		handler = h;
	}
	static synchronized Handler getHandler() {
		return handler;
	}
	/** singleton */
	private static Thread singleton;
	private long lastPollTime;
	/** log from thread */
	private void log(String message) {
		log(message, null, null);
	}
	private void log(String message, String extraKey, String extraValue) {
		try {
			JSONStringer js = new JSONStringer();
			js.object();
			js.key("thread");
			js.value(Thread.currentThread().getName());
			js.key("message");
			js.value(message);
			if (extraKey!=null) {
				js.key(extraKey);
				js.value(extraValue);
			}
			js.endObject();
			LoggingUtils.log(LOGTYPE_BACKGROUND_THREAD, js.toString());
		}
		catch (Exception e) {
			Log.e(TAG,"Logging "+message, e);
		}
	}
	/** run method */
	@Override
	public void run() {
		log("run()");
		mainloop:
		while (Thread.currentThread()==singleton) {
			try {
				boolean doLogin = false, doGetState = false, doPoll = false, doSendQueuedMessages = false;
				ClientState clientStateEvent = null;
				synchronized (BackgroundThread.class) {
					// recheck in sync block
					if (Thread.currentThread()!=singleton)
						break;
					// Synchronized!
					//Log.d(TAG, "Background action on state "+currentClientState);
					switch(currentClientState.getClientStatus()) {
					case CONFIGURING:
						// no op
						break;
					case CANCELLED_BY_USER:
					case ERROR_DOING_LOGIN:
					case ERROR_GETTING_STATE:
					case ERROR_IN_SERVER_URL:
						Log.i(TAG, "Background thread give up on state "+currentClientState.getClientStatus());
						break mainloop;
					case NEW:
						// TODO log in
						currentClientState.setClientStatus(ClientStatus.LOGGING_IN);
						clientStateEvent = currentClientState.clone();
						doLogin = true;
						break;
					case GETTING_STATE:
						doGetState = true;
						break;
					case POLLING:
					case IDLE:
					case ERROR_AFTER_STATE: {
						doSendQueuedMessages = true;
						int pollInterval = POLL_INTERVAL_MS;
						SharedPreferences preferences = getSharedPreferences();
						if (preferences!=null) {
 							try {
								pollInterval = Integer.parseInt(preferences.getString("pollInterval", ""+pollInterval));
							}
 							catch (NumberFormatException e) {
 								Log.e(TAG, "Getting pollInterval", e);
 							}
						}
						if (System.currentTimeMillis()-lastPollTime > pollInterval) {
							doPoll = true;
						}
					}
					}
					// End Synchronized!
				}
				// unsync
				if (clientStateEvent!=null)
					fireClientStateChanged(clientStateEvent);
				if (doLogin) 
					doLogin();
				else if (doGetState)
					doGetState();
				else if (doPoll) {
					lastPollTime = System.currentTimeMillis();
					doPoll();
				}
				else if (doSendQueuedMessages) {
					client.waitOnQueuedMessages((int)THREAD_SLEEP_MS);
					doSendQueuedMessages();
				}
				else {
					try {
						synchronized(Thread.currentThread()) {
							Thread.currentThread().wait(THREAD_SLEEP_MS);
						}
					}
					catch (InterruptedException ie) {
						Log.d(TAG,"BackgroundThread interrtuped");
					}
				}
			}
			catch (Exception e) {
				Log.e(TAG, "Exception in background thread "+Thread.currentThread(), e);
				// TODO ERROR?
			}
		}
		log("done");
		Log.i(TAG, "Background thread "+Thread.currentThread()+" exiting (interrupted="+Thread.interrupted()+")");
	}
	/** HTTP client */
	private HttpClient httpClient;
	/** get HTTP Client */
	private synchronized HttpClient getHttpClient() {
		if (httpClient!=null)
			return httpClient;
		httpClient = new DefaultHttpClient();
		return httpClient;
	}
	private String clientId;
	/** conversation */
	private String conversationId;
	//private static String server
	/** attempt login - called from background thread, unsync. */
	private static Context getContext() {
		// TODO Auto-generated method stub
		if (contextRef==null)
		{
			Log.e(TAG,"doLogin: contextRef==null");
			setClientStatus(ClientStatus.ERROR_IN_SERVER_URL);
			return null;
		}
		Context context = contextRef.get();
		if (context==null) {
			Log.e(TAG,"doLogin: context==null");
			setClientStatus(ClientStatus.ERROR_IN_SERVER_URL);
			return null;			
		}
		return context;
	}
	private SharedPreferences getSharedPreferences() {
		Context context = getContext();
		if (context==null)
			return null;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences;
	}
	private String getServerUrl() {
		SharedPreferences preferences = getSharedPreferences();
		if (preferences==null)
			return null;
		String serverUrl = preferences.getString("serverUrl", null);
		if (serverUrl==null || serverUrl.length()==0) {
			Log.e(TAG,"getServerUrl: serverUrl==null");
			setClientStatus(ClientStatus.ERROR_IN_SERVER_URL, "The Server URL is not set\n(See Preferences)");
			return null;
		}
		return serverUrl;
	}
	/** attempt login - called from background thread, unsync. */
	private void doLogin() {
		Context context = getContext();
		if (context==null)
			return;
		// check location providers
		boolean providersOk = LocationUtils.locationProviderEnabled(context);
		if (!providersOk) {
			String error = LocationUtils.getLocationProviderError(context);
			Log.e(TAG, "Location provider error: "+error);
			setClientStatus(ClientStatus.ERROR_DOING_LOGIN, error);
			return;			
		}
        // get device unique ID(s)
		clientId = ExplodingPreferences.getDeviceId(context);
		String serverUrl = getServerUrl();
		if (serverUrl==null)
			return;
        conversationId = GUIDFactory.newGUID(clientId);
        SharedPreferences preferences = getSharedPreferences();
        if (preferences==null)
        	return;
        
        HttpClient httpClient = getHttpClient();
		HttpPost request = null;
		try {
			serverUrl = serverUrl+LOGIN_PATH;
			request = new HttpPost(new URI(serverUrl));
		} catch (Exception e) {
			Log.e(TAG, "parsing serverUrl "+serverUrl, e);
			setClientStatus(ClientStatus.ERROR_IN_SERVER_URL, "There is a problem with the Server URL\n("+e.getMessage()+")");
			return;
		}
		try {
			LoginMessage login = new LoginMessage();
			login.setClientId(clientId);
			login.setPlayerName(ExplodingPreferences.getPlayerName(context));
			login.setConversationId(conversationId);
			login.setClientVersion(CLIENT_VERSION);
			login.setClientType(CLIENT_TYPE);
			// TODO XPP3 driver?
			XStream xs = new XStream(/*new DomDriver()*/);
			xs.alias("login", LoginMessage.class);
			xs.alias("reply", LoginReplyMessage.class);
			String xmlText = xs.toXML(login);
			// name?
			Log.d(TAG,"Login: "+xmlText);
			log("login", "request", xmlText);
			//request.setHeader("Content-Type", )
			request.setEntity(new StringEntity(xmlText));
			HttpResponse response = httpClient.execute(request);
			StatusLine statusLine = response.getStatusLine();
			Log.d(TAG, "Http status on login: "+statusLine);
			if (statusLine.getStatusCode()!=200) {
				Log.e(TAG, "Error - Http status on login: "+statusLine);
				setClientStatus(ClientStatus.ERROR_DOING_LOGIN, "Error logging in!\n("+statusLine.getReasonPhrase()+")");				
				if (response.getEntity()!=null)
					response.getEntity().consumeContent();
				return;
			}
			LoginReplyMessage reply = (LoginReplyMessage )xs.fromXML(response.getEntity().getContent());
			response.getEntity().consumeContent();
			Log.d(TAG,"Reply: "+reply);
			log("loginReply", "reply", reply.toString());
			synchronized (BackgroundThread.class) {
				checkCurrentThread();

				currentClientState.setGameStatus(GameStatus.valueOf(reply.getGameStatus()));
				currentClientState.setLoginStatus(LoginReplyMessage.Status.valueOf(reply.getStatus()));
				currentClientState.setLoginMessage(reply.getMessage());
				//fireClientStateChanged(currentClientState.clone());

				if (currentClientState.getLoginStatus()==LoginReplyMessage.Status.OK && 
						(currentClientState.getGameStatus()==GameStatus.ACTIVE ||
								currentClientState.getGameStatus()==GameStatus.NOT_STARTED ||
								currentClientState.getGameStatus()==GameStatus.ENDING)) {
					currentClientState.setClientStatus(ClientStatus.GETTING_STATE);
				} else if ((currentClientState.getLoginStatus()==LoginReplyMessage.Status.OK && 
						(currentClientState.getGameStatus()==GameStatus.ENDED)) ||
						currentClientState.getLoginStatus()==LoginReplyMessage.Status.GAME_NOT_FOUND) {
					// game is over - shouldn't ever actually be returned to login
					currentClientState.setClientStatus(ClientStatus.STOPPED);
				}
				else
					currentClientState.setClientStatus(ClientStatus.ERROR_DOING_LOGIN);

				fireClientStateChanged(currentClientState.clone());
				
			}
		} catch (Exception e) {
			Log.e(TAG, "Attempting post to serverUrl "+serverUrl, e);
			setClientStatus(ClientStatus.ERROR_DOING_LOGIN, "Error logging in!\n("+e.getMessage()+")");
			return;
		}
	}
	private Client client;
	private void doGetState() {
		String serverUrl = getServerUrl();
		if (serverUrl==null)
			return;
		try {
			client = new Client(httpClient, serverUrl+MESSAGES_PATH+"?conversationID="+conversationId, clientId);
			currentClientState.setCache(client);
		}
		catch (Exception e) {
			Log.e(TAG, "Creating message client", e);
			setClientStatus(ClientStatus.ERROR_IN_SERVER_URL, "There is a problem with the Server messages URL\n("+e.getMessage()+")");
			return;			
		}
		try {
			updatePlayer();
			client.sendQueuedMessages();
			client.poll();
			lastPollTime = System.currentTimeMillis();
			// success = good
			setClientStatus(ClientStatus.POLLING, "Ready to play");
		}
		catch (Exception e) {
			Log.e(TAG, "Doing first poll", e);
			setClientStatus(ClientStatus.ERROR_GETTING_STATE, "Could not join the game\n("+e.getMessage()+")");
			return;						
		}
	}
	private void doPoll() {
		try {
			setClientStatus(ClientStatus.POLLING, "Trying to get updates");	
			updatePlayer();
			client.sendQueuedMessages();
			client.poll();
			// success = good
			setClientStatus(ClientStatus.IDLE, "Ready to play");			
		}
		catch (Exception e) {
			Log.e(TAG, "Doing (later) poll", e);
			setClientStatus(ClientStatus.ERROR_AFTER_STATE, "Could not get updates\n("+e.getMessage()+")");
			return;						
		}
	}
	private void updatePlayer() throws IOException {
		// APPLICATION-SPECIFIC
		Context context = getContext();
		if (context==null)
			return;
		Player player = new Player();
		Location loc = LocationUtils.getCurrentLocation(context);
		if (loc!=null) {
			Position pos = new Position();
			pos.setLatitude(loc.getLatitude());
			pos.setLongitude(loc.getLongitude());
			if (loc.hasAltitude())
				pos.setElevation(loc.getAltitude());	
			else
				pos.setElevation(0.0);
			player.setPosition(pos);
		}
		log("updatePlayer()");
		// relying on this being handled as a special case - no old value, no ID!
		client.queueMessage(client.updateFactMessage(null, player), null);
	}
	private void doSendQueuedMessages() {
		// TODO Auto-generated method stub
		try {
			if (client.isQueuedMessage())
			{
				setClientStatus(ClientStatus.POLLING, "Trying to send queued updates");	
				client.sendQueuedMessages();
				// success = good
				setClientStatus(ClientStatus.IDLE, "Ready to play");			
			}
		}
		catch (Exception e) {
			Log.e(TAG, "Sending queued messages", e);
			setClientStatus(ClientStatus.ERROR_AFTER_STATE, "Could not send updates\n("+e.getMessage()+")");
			return;						
		}
		
	}
	/** listener info */
	private static class ListenerInfo {
		WeakReference<ClientStateListener> listener;
		int flags;
		Set<String> types;
	}
	/** listeners */
	private static LinkedList<ListenerInfo> listeners = new LinkedList<ListenerInfo>();
	/** add listener */
	public static void addClientStateListener(ClientStateListener listener, Context context) {
		addClientStateListener(listener , context, ClientState.Part.ALL.flag(), null);
	}
	/** add listener */
	public static void addClientStateListener(ClientStateListener listener, Context context, int flags) {
		addClientStateListener(listener, context, flags, null);
	}
	/** add listener */
	public static void addClientStateListener(ClientStateListener listener, Context context, int flags, Set<String> types) {
		checkThread(context);
		ListenerInfo li = new ListenerInfo();
		li.listener = new WeakReference<ClientStateListener>(listener);
		li.flags = flags;
		li.types = types;
		listeners.add(li);
	}
	/** add listener */
	public static void removeClientStateListener(ClientStateListener listener) {
		for (int i=0; i<listeners.size(); i++) {
			ClientStateListener l = listeners.get(i).listener.get();
			if (l==null || l==listener) {
				listeners.remove(i);
				i--;
			}
		}
	}
	/** check we are current background thread */
	private static synchronized void checkCurrentThread() {
		if (singleton!=Thread.currentThread()) {
			Log.e(TAG, "setClientStatus called by thread non-current thread");
			throw new RuntimeException("checkCurrentThread called by thread non-current thread");
		} else if (currentClientState!=null && currentClientState.getClientStatus()==ClientStatus.CANCELLED_BY_USER) {
			Log.e(TAG, "setClientStatus called by thread when Cancelled by user");
			throw new RuntimeException("checkCurrentThread called by cancelled thread");
		}
	}
	/** set client status and fire */
	private static synchronized void setClientStatus(ClientStatus clientStatus) {
		setClientStatus(clientStatus, null);
	}
	/** set client status and fire */
	private static synchronized void setClientStatus(ClientStatus clientStatus, String message) {
		checkCurrentThread();
		if (currentClientState!=null && (currentClientState.getClientStatus()!=clientStatus || message!=currentClientState.getLoginMessage())) {
			currentClientState.setLoginMessage(message);
			currentClientState.setClientStatus(clientStatus);
			fireClientStateChanged(currentClientState.clone());
		}
	}
	/** set location and zone and fire; Note called by Location thread via ZoneService, not
	 * current client/background thread. */
	static synchronized void setLocation(Location location, String zoneID, int zoneOrgID) {
		if (currentClientState!=null) {
			currentClientState.setLastLocation(location);
			// either could be null!
			if (zoneID!=currentClientState.getZoneID() && (zoneID==null || currentClientState.getZoneID()==null || !zoneID.equals(currentClientState.getZoneID())))
				currentClientState.setZoneID(zoneID);
			if (zoneOrgID!=currentClientState.getZoneOrgID())
				currentClientState.setZoneOrgID(zoneOrgID);
			ClientState clone = currentClientState.clone();
			Log.d(TAG,"setLocation("+zoneID+") - current="+currentClientState.getZoneID()+"/"+currentClientState.isZoneChanged()+", clone="+clone.getZoneID()+"/"+clone.isZoneChanged());
			fireClientStateChanged(clone);
		}
	}
	/** set game status and fire */
	public static synchronized void setGameStatus(GameStatus gameStatus) {
		if (singleton!=Thread.currentThread()) {
			Log.e(TAG, "setGameStatus called by thread non-current thread");
			throw new RuntimeException("setGameStatus called by thread non-current thread");
		}
		if (currentClientState!=null && currentClientState.getGameStatus()!=gameStatus) {
			currentClientState.setGameStatus(gameStatus);
			fireClientStateChanged(currentClientState.clone());
		}
	}
	/** fire event listeners */
	private static void fireClientStateChanged(final ClientState clientState) {
		if (handler!=null)
			handler.post(new Runnable() {
				public void run() {
					fireClientStateChangedInHandler(clientState);
				}
			});
		else
			fireClientStateChangedInHandler(clientState);
	}
	/** fire event listeners */
	private static void fireClientStateChangedInHandler(final ClientState clientState) {
		try {
			JSONStringer js = new JSONStringer();
			js.object();
			js.key("clientState");
			js.value(clientState.toString());
			js.endObject();
			LoggingUtils.log(LOGTYPE_CLIENT_STATE, clientState.toString());
		}
		catch (Exception e) {
			Log.e(TAG,"Logging clientState changed="+clientState, e);
		}
		switch(clientState.getClientStatus()) {
		case CANCELLED_BY_USER:
		case ERROR_DOING_LOGIN:
		case ERROR_IN_SERVER_URL:
		case ERROR_GETTING_STATE:
		case STOPPED:
			LocationUtils.updateRequired(getContext(), false);
			AudioUtils.autoPause();
			break;
		default:
			LocationUtils.updateRequired(getContext(), true);	
			AudioUtils.autoResume();
		}
		for (ListenerInfo li : listeners) {
			ClientStateListener listener = li.listener.get();
			if (listener!=null) {
				boolean stateMatch = false;
				if (li.types!=null && clientState.getChangedTypes().size()>0) {
					for (String type : li.types)
						if (clientState.getChangedTypes().contains(type)) {
							stateMatch = true;
							break;
						}
					if (!stateMatch) {
						//Log.d(TAG,"Skip listener "+li.listener.get()+" on types ("+li.types+" vs "+clientState.getChangedTypes());
					}
				}
				if (stateMatch ||
						(clientState.isLocationChanged() && ((li.flags & ClientState.Part.LOCATION.flag())!=0)) ||
						(clientState.isZoneChanged() && ((li.flags & ClientState.Part.ZONE.flag())!=0)) ||
						(clientState.isStatusChanged() && ((li.flags & ClientState.Part.STATUS.flag())!=0)))
				{
					try {
						// TODO GUI thread?
						listener.clientStateChanged(clientState);
					}
					catch (Exception e) {
						//Log.e(TAG, "Error calling listener "+listener, e);
					}
				}
				//else if (li.flags!=0)
				//Log.d(TAG,"Skip listener "+li.listener.get()+" on flags: "+li.flags+" vs "+clientState.isStatusChanged()+","+clientState.isLocationChanged()+","+clientState.isZoneChanged());
			}
		}
	}
	/** client state */
	private static ClientState currentClientState;
	/** context */
	private static WeakReference<Context> contextRef;
	/** check */
	private static synchronized void checkThread(Context context) {
		if (currentClientState==null)
			currentClientState = new ClientState(ClientStatus.CONFIGURING, GameStatus.UNKNOWN);
//		if (singleton==null || !singleton.isAlive()) {
//			Log.i(TAG, (singleton!=null ? "(Re)": "")+"starting background thread");
//			singleton = new Thread(new BackgroundThread());
//			singleton.start();			
//		}
		if (contextRef==null || contextRef.get()==null) {
			contextRef = new WeakReference<Context>(context);
//			PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(new SharedPreferenceChangeListener());
		}
		// TODO
	}
	/** get state (copy) */
	public static synchronized ClientState getClientState(Context context) {
		checkThread(context);
		return currentClientState.clone();
	}
	/** restart client */
	public static synchronized void restart(Context context) {
		LoggingUtils.log(LOGTYPE_BACKGROUND_THREAD, "{\"message\"=\"restart()\"}");
		Log.i(TAG, "Restart client - explicit request");
		currentClientState = new ClientState(ClientStatus.NEW, GameStatus.UNKNOWN);
		if (singleton!=null && singleton.isAlive()) 
			singleton.interrupt();
		singleton = new Thread(new BackgroundThread());
		singleton.start();			
	}
	/** retry client */
	public static synchronized void retry(Context context) {
		LoggingUtils.log(LOGTYPE_BACKGROUND_THREAD, "{\"message\"=\"retry()\"}");
		if (currentClientState==null)
			currentClientState = new ClientState(ClientStatus.NEW, GameStatus.UNKNOWN);
		Log.i(TAG, "Retry client - explicit request (state "+currentClientState.getClientStatus());
		synchronized (BackgroundThread.class) {
			switch (currentClientState.getClientStatus()) {
			case ERROR_DOING_LOGIN:
			case ERROR_GETTING_STATE:
			case ERROR_IN_SERVER_URL:
			case CANCELLED_BY_USER:
			case ERROR_AFTER_STATE:
				Log.i(TAG, "Retry from "+currentClientState.getClientStatus()+" to NEW");
				restart(context);
				break;
			default:
				// no-op
			}			
		}
	}
	public static synchronized void cancel(Context context) {
		LoggingUtils.log(LOGTYPE_BACKGROUND_THREAD, "{\"message\"=\"cancel()\"}");
		if (currentClientState==null)
			currentClientState = new ClientState(ClientStatus.NEW, GameStatus.UNKNOWN);
		Log.i(TAG, "Cancel client - explicit request (state "+currentClientState.getClientStatus());
		synchronized (BackgroundThread.class) {
			switch (currentClientState.getClientStatus()) {
			case LOGGING_IN:
			case GETTING_STATE:
				Log.i(TAG, "Cacnel from "+currentClientState.getClientStatus()+" to CANCELLED_BY_USER");
				if (singleton!=null && singleton.isAlive()) {
					singleton.interrupt();
					singleton = null;
				}
				currentClientState.setLoginMessage("Cancelled by user");
				currentClientState.setClientStatus(ClientStatus.CANCELLED_BY_USER);
				fireClientStateChanged(currentClientState.clone());
				break;
			default:
				// no op
			}
		}
	}
	public static synchronized void shutdown(Context context) {
		LoggingUtils.log(LOGTYPE_BACKGROUND_THREAD, "{\"message\"=\"shutdown()\"}");
		checkThread(context);
		Log.i(TAG, "Shutdown client - explicit request (state "+currentClientState.getClientStatus());
		Log.i(TAG, "Cancel from "+currentClientState.getClientStatus()+" to CANCELLED_BY_USER");
		if (singleton!=null && singleton.isAlive()) {
			singleton.interrupt();
			singleton = null;
		}
		currentClientState.setLoginMessage("Stopped by user");
		currentClientState.setClientStatus(ClientStatus.CANCELLED_BY_USER);
		fireClientStateChanged(currentClientState.clone());
	}
	/** we have received update(s) from the server */
	public static synchronized void cachedStateChanged(Client cache, Set<String> changedTypes) {
		if (currentClientState==null) {
			Log.e(TAG, "cachedStateChanged called with null currentClientState");
			return;
		}
		if (currentClientState.getCache()!=cache) {
			Log.w(TAG, "cachedStateChanged called by non-current cache");
			return;
		}
		Log.d(TAG,"cachedStateChanged: "+changedTypes);
		currentClientState.getChangedTypes().addAll(changedTypes);
		fireClientStateChanged(currentClientState.clone());
	}
	public static void addClientStateListener(
			ClientStateListener listener,
			Context context, String name) {
		Set<String> types = new HashSet<String>();
		types.add(name);
		addClientStateListener(listener, context, types);
	}
	private static void addClientStateListener(ClientStateListener listener,
			Context context, Set<String> types) {
		addClientStateListener(listener, context, 0, types);
	}
}
