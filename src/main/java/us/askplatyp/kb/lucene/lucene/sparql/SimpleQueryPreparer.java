/*
 * Copyright (c) 2017 Platypus Knowledge Base developers.
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

package us.askplatyp.kb.lucene.lucene.sparql;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.query.algebra.QueryRoot;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.UpdateExpr;
import org.eclipse.rdf4j.query.algebra.evaluation.AbstractQueryPreparer;
import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategy;
import org.eclipse.rdf4j.query.algebra.evaluation.TripleSource;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.*;
import org.eclipse.rdf4j.query.impl.EmptyBindingSet;
import org.eclipse.rdf4j.repository.sparql.federation.SPARQLServiceResolver;

/**
 * @author Thomas Pellissier Tanon
 */
public class SimpleQueryPreparer extends AbstractQueryPreparer {

    private EvaluationStatistics evaluationStatistics = new EvaluationStatistics();

    public SimpleQueryPreparer(TripleSource tripleSource) {
        super(tripleSource);
    }

    @Override
    protected CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(
            TupleExpr tupleExpr, Dataset dataset, BindingSet bindings, boolean includeInferred, int maxExecutionTime
    ) throws QueryEvaluationException {
        tupleExpr = tupleExpr.clone(); //TODO: is it required?
        if (!(tupleExpr instanceof QueryRoot)) {
            tupleExpr = new QueryRoot(tupleExpr);
        }

        EvaluationStrategy strategy = new ExtendedEvaluationStrategy(
                getTripleSource(), dataset, new SPARQLServiceResolver(), 0L, evaluationStatistics
        );

        new BindingAssigner().optimize(tupleExpr, dataset, bindings);
        new ConstantOptimizer(strategy).optimize(tupleExpr, dataset, bindings);
        new CompareOptimizer().optimize(tupleExpr, dataset, bindings);
        new ConjunctiveConstraintSplitter().optimize(tupleExpr, dataset, bindings);
        new DisjunctiveConstraintOptimizer().optimize(tupleExpr, dataset, bindings);
        new SameTermFilterOptimizer().optimize(tupleExpr, dataset, bindings);
        new QueryModelNormalizer().optimize(tupleExpr, dataset, bindings);
        new QueryJoinOptimizer(evaluationStatistics).optimize(tupleExpr, dataset, bindings);
        new IterativeEvaluationOptimizer().optimize(tupleExpr, dataset, bindings);
        new FilterOptimizer().optimize(tupleExpr, dataset, bindings);
        new OrderLimitOptimizer().optimize(tupleExpr, dataset, bindings);

        return strategy.evaluate(tupleExpr, EmptyBindingSet.getInstance());
    }

    @Override
    protected void execute(
            UpdateExpr updateExpr, Dataset dataset, BindingSet bindings, boolean includeInferred, int maxExecutionTime
    ) throws UpdateExecutionException {
        throw new UpdateExecutionException("This repository is read only");
    }
}
