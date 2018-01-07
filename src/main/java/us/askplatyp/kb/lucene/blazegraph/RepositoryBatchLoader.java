/*
 * Copyright (c) 2018 Platypus Knowledge Base developers.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package us.askplatyp.kb.lucene.blazegraph;

import org.openrdf.model.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import java.util.HashSet;
import java.util.Set;

public class RepositoryBatchLoader implements AutoCloseable {
    private RepositoryConnection connection;
    private ValueFactory valueFactory;
    private int batchSize;
    private Set<Statement> toAdd = new HashSet<>();
    private Set<Statement> toRemove = new HashSet<>();

    public RepositoryBatchLoader(RepositoryConnection connection, int batchSize) {
        this.connection = connection;
        this.valueFactory = connection.getValueFactory();
        this.batchSize = batchSize;
    }

    public ValueFactory getValueFactory() {
        return valueFactory;
    }

    public void add(Resource subject, URI predicate, Value object) throws RepositoryException {
        Statement statement = valueFactory.createStatement(subject, predicate, object);
        toRemove.remove(statement);
        toAdd.add(statement);
        commitIfNeeded();
    }

    public void remove(Resource subject, URI predicate, Value object) throws RepositoryException {
        RepositoryResult<Statement> statements = connection.getStatements(subject, predicate, object, false);
        while (statements.hasNext()) {
            Statement statement = statements.next();
            toAdd.remove(statement);
            toRemove.add(statement);
        }
        statements.close();
    }

    private void commitIfNeeded() throws RepositoryException {
        if (toAdd.size() + toRemove.size() > batchSize) {
            commit();
        }
    }

    private void commit() throws RepositoryException {
        if (toRemove.isEmpty() && toAdd.isEmpty()) {
            return;
        }

        connection.begin();
        try {
            connection.remove(toRemove);
            connection.add(toAdd);
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }

    @Override
    public void close() throws RepositoryException {
        commit();
        connection.close();
    }
}
