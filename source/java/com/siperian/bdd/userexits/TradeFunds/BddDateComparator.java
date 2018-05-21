package com.siperian.bdd.userexits.TradeFunds;

import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.siperian.bdd.userexits.datamodel.BDDObject;

public class BddDateComparator implements Comparator<BDDObject> {

  

    private String bddDateCol;

    public BddDateComparator(String effectiveStartDateCol) {
        this.bddDateCol = effectiveStartDateCol;
    }

    @Override
    public int compare(BDDObject bddObject, BDDObject bddObjectToCompare) {
        Date bddObjectEffectiveStartDate = null;
        Date bddObjectToCompareEffectiveEndDate = null;

        try {
            bddObjectEffectiveStartDate = AppUtils.convertObjectToDate(bddObject.getValue(bddDateCol));

        } catch (ParseException e1) {
  
        }
        try {
            bddObjectToCompareEffectiveEndDate = AppUtils.convertObjectToDate(bddObjectToCompare.getValue(bddDateCol));
        } catch (ParseException e1) {
  
        }

        long diffTime = bddObjectEffectiveStartDate.getTime() - bddObjectToCompareEffectiveEndDate.getTime();
        long diffDays = TimeUnit.DAYS.convert(diffTime, TimeUnit.MILLISECONDS);

        if (diffDays < 0) {
            return -1;
        } else if (diffDays > 0) {
            return 1;
        }

        return 0;
    }

}
