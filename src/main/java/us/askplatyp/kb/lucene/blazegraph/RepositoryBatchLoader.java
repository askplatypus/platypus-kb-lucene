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

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class RepositoryBatchLoader implements AutoCloseable {
    private RepositoryConnection connection;
    private int maxBatchSize;
    private int currentBatchSize = 0;

    public RepositoryBatchLoader(RepositoryConnection connection, int maxBatchSize) throws RepositoryException {
        this.connection = connection;
        this.maxBatchSize = maxBatchSize;
        connection.begin();
    }

    public ValueFactory getValueFactory() {
        return connection.getValueFactory();
    }

    public void add(Resource subject, URI predicate, Value object) throws RepositoryException {
        connection.add(subject, predicate, object);
        currentBatchSize++;
        commitIfNeeded();
    }

    public void remove(Resource subject, URI predicate, Value object) throws RepositoryException {
        connection.remove(subject, predicate, object);
        currentBatchSize++;
        commitIfNeeded();
    }

    private void commitIfNeeded() throws RepositoryException {
        if (currentBatchSize >= maxBatchSize) {
            try {
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.begin();
            }
        }
    }

    @Override
    public void close() throws RepositoryException {
        //Last commit
        try {
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.close();
        }
    }
}
