package com.cyslab.craftvm.mongo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;

public class MongoUtil {

	/*
	 * Make Sure if you have more than one server addresses put all thise things
	 * with comma seperated values. For Example MongoServer is looks like
	 * "<server:port>(127.0.0.1:8081)".
	 */
	private String mongoServers;

	/*
	 * If not possible all mongo server addresses with comma seperated values
	 * just maitain a list and passed to this constructor.
	 */
	private List<String> mongoServersList;

	/*
	 * It accepts mongo server port number.
	 */
	private int port;

	/*
	 * It accepts mongo server host.
	 */
	private String host;

	/*
	 * Mongo Initialization. The Mongo object instance actually represents a
	 * pool of connections to the database; you will only need one object of
	 * class Mongo even with multiple threads.The Mongo class is designed to be
	 * thread safe and shared among threads. Typically you create only 1
	 * instance for a given database cluster and use it across your application.
	 */
	private Mongo mongo;

	private Properties properties;

	public static MongoUtil createInstance(String argPropertiesfilepath) {
		MongoUtil mongoUtil = new MongoUtil();
		mongoUtil.readPropertiesFile(argPropertiesfilepath);
		mongoUtil.initializeMongo(mongoUtil);
		return mongoUtil;
	}

	private void initializeMongo(MongoUtil mongoUtil) {
		String mongoServers = this.getProperties().getProperty("mongo.servers");
		String host = this.getProperties().getProperty("host");
		String port = this.getProperties().getProperty("port");
		if (mongoServers != null && !mongoServers.isEmpty()) {
			List<String> mongoServerAddressList = getMongoServerAddresses(mongoServers);
			initializeMongo(mongoServerAddressList);
		} else if (host != null && !host.isEmpty() && port != null
				&& !port.isEmpty()) {
			int mongoPort = Integer.parseInt(port);
			initializeMongo(host, mongoPort);
		}
	}

	private void readPropertiesFile(String argPropertiesfilepath) {
		InputStream inputstream = null;
		Properties properties = null;
		try {
			properties = new Properties();
			inputstream = new FileInputStream(new File(argPropertiesfilepath));
			properties.load(inputstream);
		} catch (Exception e) {
			throw new RuntimeException(
					"Exception while reading the properties from a file:", e);
		}
		this.setProperties(properties);
	}

	public MongoUtil() {
		super();
	}

	public MongoUtil(String argMongoServers) {
		super();
		this.mongoServers = argMongoServers;
		List<String> mongoServerAddressList = getMongoServerAddresses(this.mongoServers);
		initializeMongo(mongoServerAddressList);
	}

	public MongoUtil(List<String> argMongoServersList) {
		super();
		this.mongoServersList = argMongoServersList;
		initializeMongo(this.mongoServersList);
	}

	public MongoUtil(int port, String host) {
		super();
		this.port = port;
		this.host = host;
		initializeMongo(this.host, this.port);
	}

	private List<String> getMongoServerAddresses(String argMongoServers) {
		if (argMongoServers != null && !argMongoServers.isEmpty()) {
			String[] mongoServerAddressArray = StringUtils.split(
					argMongoServers, ",");
			return Arrays.asList(mongoServerAddressArray);
		}
		return null;
	}

	private void initializeMongo(List<String> argMongoServerAddressList) {
		if (argMongoServerAddressList != null
				&& argMongoServerAddressList.size() > 0) {
			List<ServerAddress> serverAddressList = new ArrayList<ServerAddress>();
			try {
				for (String mongoServerAddress : argMongoServerAddressList) {
					String[] hostAndPortArray = StringUtils.split(
							mongoServerAddress, ":");
					if (hostAndPortArray == null
							|| hostAndPortArray.length != 2) {
						throw new IllegalArgumentException(
								"mongo server must be <server:port>");
					}
					String host = hostAndPortArray[0];
					int port = Integer.parseInt(hostAndPortArray[1]);
					ServerAddress serverAddress = new ServerAddress(host, port);
					serverAddressList.add(serverAddress);
				}
				this.mongo = new Mongo(serverAddressList);
			} catch (Exception e) {
				throw new RuntimeException(
						"Exception while initializing Mongo: ", e);
			}
		}
	}

	private void initializeMongo(String argHost, int argPort) {
		if (argHost != null && !argHost.isEmpty() && argPort > 0) {
			try {
				this.mongo = new Mongo(argHost, argPort);
			} catch (MongoException e) {
				throw new RuntimeException(
						"Exception while initializing Mongo: ", e);
			} catch (UnknownHostException e) {
				throw new RuntimeException(
						"Exception, because of unknown host: ", e);
			}
		}
	}

	public Properties getProperties() {
		return this.properties;
	}

	public void setProperties(Properties argProperties) {
		this.properties = argProperties;
	}

	public Mongo getMongo() {
		return this.mongo;
	}

	/*
	 * To dispose of an instance, make sure you call mongo.close() to clean up
	 * resources.
	 */
	public void close() {
		if (this.mongo != null) {
			this.mongo.close();
		}
	}
}
