package attribution.selenium.tests;
import static org.testng.Assert.*;
import org.testng.annotations.*;

/**
 * Copyright (c) 2013-2014 Abakus, Inc. All rights reserved.
 * User: Alexander Dudarenko
 * Date: 28/6/2015
 */

public class ReportsTest {

    @Test( groups = { "all" } )
    public void reportTest() {
        assertEquals(10, 10, "test 10");
    }
    @Test( groups = { "all" } )
    public void failTest() {
        assertEquals(40, 20, "test 20");
    }

}
