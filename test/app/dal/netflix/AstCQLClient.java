package app.dal.netflix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.ColumnListMutation;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.model.CqlResult;
import com.netflix.astyanax.model.Row;
import com.netflix.astyanax.serializers.IntegerSerializer;
import com.netflix.astyanax.serializers.StringSerializer;

import dal.CassandraAstyanaxConnection;
import dal.ConnectKeyspaceConfig;

/**
 * Example code for demonstrating how to access Cassandra using Astyanax and CQL3.
 * 
 * @author elandau
 * @author Marko Asplund
 */
public class AstCQLClient {
	private static final Logger logger = LoggerFactory.getLogger(AstCQLClient.class);

	private AstyanaxContext<Keyspace> context;
	private Keyspace keyspace;
	private ColumnFamily<Integer, String> EMP_CF;
	private static final String EMP_CF_NAME = "employees1";
	private static final String INSERT_STATEMENT = String.format(
			"INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?);", EMP_CF_NAME, ModelConstants.COL_NAME_EMPID,
			ModelConstants.COL_NAME_DEPTID, ModelConstants.COL_NAME_FIRST_NAME, ModelConstants.COL_NAME_LAST_NAME);
	private static final String CREATE_STATEMENT = String.format(
			"CREATE TABLE %s (%s int, %s int, %s varchar, %s varchar, PRIMARY KEY (%s, %s))", EMP_CF_NAME,
			ModelConstants.COL_NAME_EMPID, ModelConstants.COL_NAME_DEPTID, ModelConstants.COL_NAME_FIRST_NAME,
			ModelConstants.COL_NAME_LAST_NAME, ModelConstants.COL_NAME_EMPID, ModelConstants.COL_NAME_DEPTID);

	public static void main(final String[] args) {
		logger.debug("main");
		final AstCQLClient c = new AstCQLClient();
		c.init();
		// c.createCF();
		c.insert(222, 333, "Eric", "Cartman");
		c.read(222);
	}

	public void createCF() {
		logger.debug("CQL: " + CREATE_STATEMENT);
		try {
			@SuppressWarnings("unused")
			final OperationResult<CqlResult<Integer, String>> result = keyspace.prepareQuery(EMP_CF)
					.withCql(CREATE_STATEMENT).execute();
		} catch (final ConnectionException e) {
			logger.error("failed to create CF", e);
			throw new RuntimeException("failed to create CF", e);
		}
	}

	public void init() {
		logger.debug("init()");

		final ConnectKeyspaceConfig parameterObject = new ConnectKeyspaceConfig();
		final String keyspaceName = "currencies";
		parameterObject.setKeyspace(keyspaceName);
		context = CassandraAstyanaxConnection.connectClusterContext(parameterObject);

		context.start();
		keyspace = context.getEntity();

		EMP_CF = ColumnFamily.newColumnFamily(EMP_CF_NAME, IntegerSerializer.get(), StringSerializer.get());
	}

	public void insert(final int empId, final int deptId, final String firstName, final String lastName) {
		try {
			@SuppressWarnings("unused")
			final OperationResult<CqlResult<Integer, String>> result = keyspace.prepareQuery(EMP_CF)
					.withCql(INSERT_STATEMENT).asPreparedStatement().withIntegerValue(empId).withIntegerValue(deptId)
					.withStringValue(firstName).withStringValue(lastName).execute();
		} catch (final ConnectionException e) {
			logger.error("failed to write data to C*", e);
			throw new RuntimeException("failed to write data to C*", e);
		}
		logger.debug("insert ok");
	}

	public void insertDynamicProperties(final int id, final String[]... entries) {
		final MutationBatch m = keyspace.prepareMutationBatch();

		final ColumnListMutation<String> clm = m.withRow(EMP_CF, id);
		for (final String[] kv : entries) {
			clm.putColumn(kv[0], kv[1], null);
		}

		try {
			@SuppressWarnings("unused")
			final OperationResult<Void> result = m.execute();
		} catch (final ConnectionException e) {
			logger.error("failed to write data to C*", e);
			throw new RuntimeException("failed to write data to C*", e);
		}
		logger.debug("insert ok");
	}

	public void read(final int empId) {
		logger.debug("read()");
		try {
			final OperationResult<CqlResult<Integer, String>> result = keyspace
					.prepareQuery(EMP_CF)
					.withCql(
							String.format("SELECT * FROM %s WHERE %s=%d;", EMP_CF_NAME, ModelConstants.COL_NAME_EMPID,
									empId)).execute();
			for (final Row<Integer, String> row : result.getResult().getRows()) {
				logger.debug("row: " + row.getKey() + "," + row); // why is rowKey null?

				final ColumnList<String> cols = row.getColumns();
				logger.debug("emp");
				logger.debug("- emp id: " + cols.getIntegerValue(ModelConstants.COL_NAME_EMPID, null));
				logger.debug("- dept: " + cols.getIntegerValue(ModelConstants.COL_NAME_DEPTID, null));
				logger.debug("- firstName: " + cols.getStringValue(ModelConstants.COL_NAME_FIRST_NAME, null));
				logger.debug("- lastName: " + cols.getStringValue(ModelConstants.COL_NAME_LAST_NAME, null));
			}
		} catch (final ConnectionException e) {
			logger.error("failed to read from C*", e);
			throw new RuntimeException("failed to read from C*", e);
		}
	}

}
