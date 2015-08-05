package attribution.selenium.capp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright (c) 2013-2015 Abakus, Inc. All rights reserved.
 * User: Alexander Dudarenko
 * Date: 8/3/2015
 */
class TestSteps implements Iterable<TestSteps.Step> {
    
    public static class Step {

        private String name_;
        private String[] args_;
        
        public Step (String name, String[] args) {
            name_ = name;
            args_ = args;
        }

        public String getName() {
            return name_;
        }

        public String[] getArgs() {
            return args_;
        }
        
    }

    private List<Step> steps_;

    public TestSteps () {
        steps_ = new ArrayList<>();
    }
    
    public void addStep(Step step) {
        steps_.add(step);
    }

    public Step getStep(int i) {
        return steps_.get(i);
    }

    public int size() {
        return steps_.size();
    }
    
    @Override
    public Iterator<Step> iterator() {
        return steps_.iterator();
    }

}

