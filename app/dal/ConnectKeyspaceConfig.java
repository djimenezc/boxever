/**
 * 
 */
package dal;

/**
 * Bean that store the minimum information to connect Astyanax with Cassandra A defult configuration is define in the 0
 * arguments constructor to connect with an Cassandra instance running in local
 * 
 * @author david
 */
public class ConnectKeyspaceConfig {

	private static final int DEFAULT_MAX_CONNECTION_PER_HOST = 4;
	public static final String DEFAULT_CONNECTION_POOL = "MyConnectionPool";
	public static final String DEFAULT_SEED = "127.0.0.1:9160";
	public static final String DEFAULT_KEYSPACE = "system";
	public static final int DEFAULT_PORT = 9160;

	private String keyspace;
	private String connectionPoolName;
	private int port;
	private String seed;
	private int maxConnectionPerHost;

	/**
	 * 
	 */
	public ConnectKeyspaceConfig() {
		port = DEFAULT_PORT;
		keyspace = DEFAULT_KEYSPACE;
		seed = DEFAULT_SEED;
		connectionPoolName = DEFAULT_CONNECTION_POOL;
		maxConnectionPerHost = DEFAULT_MAX_CONNECTION_PER_HOST;
	}

	public ConnectKeyspaceConfig(final String keySpace) {
		this();
		keyspace = keySpace;
	}

	/**
	 * @param maxConnectionPerHost
	 * 
	 */
	public ConnectKeyspaceConfig(final String keyspace, final String connectionPoolName, final int port,
			final String seed, final int maxConnectionPerHost) {
		this.keyspace = keyspace;
		this.connectionPoolName = connectionPoolName;
		this.port = port;
		this.seed = seed;
		this.maxConnectionPerHost = maxConnectionPerHost;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ConnectKeyspaceConfig other = (ConnectKeyspaceConfig) obj;
		if (connectionPoolName == null) {
			if (other.connectionPoolName != null) {
				return false;
			}
		}
		else if (!connectionPoolName.equals(other.connectionPoolName)) {
			return false;
		}
		if (keyspace == null) {
			if (other.keyspace != null) {
				return false;
			}
		}
		else if (!keyspace.equals(other.keyspace)) {
			return false;
		}
		if (port != other.port) {
			return false;
		}
		if (seed == null) {
			if (other.seed != null) {
				return false;
			}
		}
		else if (!seed.equals(other.seed)) {
			return false;
		}
		return true;
	}

	/**
	 * Getter for the attribute connectionPoolName
	 * 
	 * @return the connectionPoolName
	 */
	public String getConnectionPoolName() {
		return connectionPoolName;
	}

	/**
	 * Getter for the attribute keyspace
	 * 
	 * @return the keyspace
	 */
	public String getKeyspace() {
		return keyspace;
	}

	public int getMaxConnectionPerHost() {
		return maxConnectionPerHost;
	}

	/**
	 * Getter for the attribute port
	 * 
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Getter for the attribute seed
	 * 
	 * @return the seed
	 */
	public String getSeed() {
		return seed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (connectionPoolName == null ? 0 : connectionPoolName.hashCode());
		result = prime * result + (keyspace == null ? 0 : keyspace.hashCode());
		result = prime * result + port;
		result = prime * result + (seed == null ? 0 : seed.hashCode());
		return result;
	}

	/**
	 * @param connectionPoolName
	 *            the connectionPoolName to set
	 */
	public void setConnectionPoolName(final String connectionPoolName) {
		this.connectionPoolName = connectionPoolName;
	}

	/**
	 * @param keyspace
	 *            the keyspace to set
	 */
	public void setKeyspace(final String keyspace) {
		this.keyspace = keyspace;
	}

	public void setMaxConnectionPerHost(final int maxConnectionPerHost) {
		this.maxConnectionPerHost = maxConnectionPerHost;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(final int port) {
		this.port = port;
	}

	/**
	 * @param seed
	 *            the seed to set
	 */
	public void setSeed(final String seed) {
		this.seed = seed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ConnectKeyspaceConfig [keyspace=" + keyspace + ", connectionPoolName=" + connectionPoolName + ", port="
				+ port + ", seed=" + seed + "]";
	}
}