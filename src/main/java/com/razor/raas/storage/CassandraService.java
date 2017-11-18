package com.razor.raas.storage;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.BuiltStatement;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Select;
import com.google.gson.Gson;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

/**
 * Created by paulhemmings on 10/19/15.
 * https://academy.datastax.com/demos/getting-started-apache-cassandra-and-java-part-i
 */

public class CassandraService {

    private final String host;

    public CassandraService(String hostUrl) {
        this.host = hostUrl;
    }

    /**
     * Insert a document into the store
     * @param keySpace
     * @param tableName
     * @param contentDocument
     * @return
     */

    public void insert(String keySpace, String tableName, ContentDocument contentDocument) {
        this.withSession(host, keySpace, session -> {
            // insert
            BuiltStatement insertStatement = this.buildInsertStatement(keySpace, tableName, contentDocument);
            PreparedStatement preparedStatement = session.prepare(insertStatement);
            for(ContentDocument.ContentRow row : contentDocument) {
                this.withBound(preparedStatement, row.values(), session::execute);
            }
        });
    }

    /**
     * Retrieve Content from the Content Store
     * @param keySpace
     * @param filters
     * @return
     */

    public ContentDocument retrieve(String keySpace, String tableName, Map<String, Object> filters) {
        ContentDocument contentDocument = new ContentDocument();
        this.withSession(host, keySpace, session -> {
            BuiltStatement retrieveStatement = this.buildRetrieveStatement(keySpace, tableName, filters);
            PreparedStatement preparedStatement = session.prepare(retrieveStatement);
            this.withBound(preparedStatement, filters.values(), statement -> {
                List<Row> rows = session.execute(statement).all();
                for (Row row : rows) {
                    String field = row.getString(0);
                    String val = new Gson().toJson(row.getString(1));
                    contentDocument.createRow().add(field, val);
                }
            });
        });
        return contentDocument;
    }

    /**
     * Retrieve a session. Accepts a consumer that will use the session. Once finished, the session closes.
     * @param host
     * @param keySpace
     * @param connected
     */

    protected void withSession(String host, String keySpace, Consumer<Session> connected) {
        Session session = Cluster.builder().addContactPoint(host).build().connect(keySpace);
        connected.accept(session);
        if (!session.isClosed()) session.close();
    }

    /**
     * Binds a prepared statement with the values
     * @param preparedStatement
     * @param values
     * @param useStatement
     */

    protected <V> void withBound(PreparedStatement preparedStatement, Collection<V> values, Consumer<BoundStatement> useStatement) {
        BoundStatement boundStatement = new BoundStatement(preparedStatement).bind(values.toArray());
        useStatement.accept(boundStatement);
    }

    /**
     * Builds an insert statement for a defined content document
     * @param keySpace
     * @param tableName
     * @param contentDocument
     * @return
     */

    protected BuiltStatement buildInsertStatement(String keySpace, String tableName, ContentDocument contentDocument) {
        Insert insertStatement = insertInto(keySpace, tableName);
        contentDocument.get(0).keySet().forEach(key -> {
            insertStatement.value(key, bindMarker());
        });
        return insertStatement;
    }

    /**
     * Builds a retrieve statement based on the filters
     * @param keySpace
     * @param tableName
     * @param filters
     * @return
     */

    protected BuiltStatement buildRetrieveStatement(String keySpace, String tableName, Map<String, Object> filters) {
        Select select = select().from(keySpace, tableName);
        for (String key : filters.keySet()) {
            select.where(eq(key, bindMarker()));
        }
        return select;
    }
}
