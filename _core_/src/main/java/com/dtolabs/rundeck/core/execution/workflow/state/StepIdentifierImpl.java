/*
 * Copyright 2016 SimplifyOps, Inc. (http://simplifyops.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtolabs.rundeck.core.execution.workflow.state;

import java.util.List;

/**
 * $INTERFACE is ... User: greg Date: 10/15/13 Time: 3:54 PM
 */
public class StepIdentifierImpl implements StepIdentifier {
    private List<StepContextId> context;

    public StepIdentifierImpl(List<StepContextId> context) {
        this.context = context;
    }

    public List<StepContextId> getContext() {
        return context;
    }

    @Override
    public String toString() {
        return StateUtils.stepIdentifierToString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StepIdentifierImpl that = (StepIdentifierImpl) o;

        if (!context.equals(that.context)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return context.hashCode();
    }

    @Override
    public int compareTo(StepIdentifier o) {
        int size = context.size();
        int thatsize = o.getContext().size();
        for(int i=0;i<Math.min(size, thatsize);i++) {
            int c = context.get(i).compareTo(o.getContext().get(i));
            if(c!=0){
                return c;
            }
        }
        return size < thatsize ? -1 : size > thatsize ? 1 : 0;
    }
}
