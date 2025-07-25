package be.vibes.fexpression;

/*-
 * #%L
 * VIBeS: featured expressions
 * %%
 * Copyright (C) 2014 - 2018 University of Namur
 * Copyright 2025 Sophie Fortz
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import be.vibes.fexpression.exception.FExpressionException;
import java.util.List;

/**
 *
 * @author Xavier Devroey - xavier.devroey@unamur.be
 */
public interface FExpressionVisitorWithReturn<E> {

    public E constant(boolean val) throws FExpressionException;

    public E feature(Feature<?> feature) throws FExpressionException;

    public E not(FExpression expr) throws FExpressionException;

    public E and(List<FExpression> operands) throws FExpressionException;

    public E or(List<FExpression> operands) throws FExpressionException;

}
