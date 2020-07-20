package com.maceve;

import org.junit.Assert;
import org.junit.Test;

public class EmailCheckerTests {

    @Test
    public void testGetMXRecords(){
        EmailChecker checker = new EmailChecker();
        String mxDomain = checker.getWeightedMXRecord("gmail.com");
        Assert.assertEquals("gmail-smtp-in.l.google.com.", mxDomain);
    }

    @Test
    public void testCheckEmail() throws CheckerException {
        EmailChecker checker = new EmailChecker();
        checker.setDebugEnabled(true);
        boolean checkOk = checker.checkEmail("dali@pmcaff.com");
        Assert.assertTrue(checkOk);
        checkOk = checker.checkEmail("noone@iikexue.cn");
        Assert.assertFalse(checkOk);
    }
}
