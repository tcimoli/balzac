/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.utils;

import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import it.unica.tcs.bitcoinTM.AbsoluteTime;
import it.unica.tcs.bitcoinTM.RelativeTime;
import it.unica.tcs.bitcoinTM.Script;
import it.unica.tcs.bitcoinTM.SignatureType;
import it.unica.tcs.bitcoinTM.StringLiteral;
import it.unica.tcs.bitcoinTM.Time;
import it.unica.tcs.bitcoinTM.Tlock;
import it.unica.tcs.bitcoinTM.TransactionBody;
import it.unica.tcs.bitcoinTM.TransactionDeclaration;
import it.unica.tcs.bitcoinTM.UserTransactionDeclaration;
import it.unica.tcs.bitcoinTM.Versig;
import it.unica.tcs.xsemantics.BitcoinTMTypeSystem;

@Singleton
public class ASTUtils extends JavaASTUtils {
	
	@Inject private BitcoinTMTypeSystem typeSystem;
//	public boolean allAbsoluteAreBlock(Tlock tlock) {
//    	return tlock.getTimes().stream()
//    			.filter(x -> x instanceof AbsoluteTime)
//    			.map(x -> (AbsoluteTime) x)
//    			.allMatch(ASTUtils::isAbsoluteBlock);
//    }
//	
//	public boolean allRelativeAreBlock(Tlock tlock) {
//    	return tlock.getTimes().stream()
//    			.filter(x -> x instanceof RelativeTime)
//    			.map(x -> (RelativeTime) x)
//    			.allMatch(ASTUtils::isRelativeBlock);
//    }
//	
//	public boolean allAbsoluteAreDate(Tlock tlock) {
//    	return tlock.getTimes().stream()
//    			.filter(x -> x instanceof AbsoluteTime)
//    			.map(x -> (AbsoluteTime) x)
//    			.allMatch(ASTUtils::isAbsoluteDate);
//    }
//	
//	public boolean allRelativeAreDate(Tlock tlock) {
//    	return tlock.getTimes().stream()
//    			.filter(x -> x instanceof RelativeTime)
//    			.map(x -> (RelativeTime) x)
//    			.allMatch(ASTUtils::isRelativeDate);
//    }
	
	public boolean isCoinbase(UserTransactionDeclaration tx) {
		return isCoinbase(tx.getBody());
	}
	
	public boolean isCoinbase(TransactionBody tx) {
		return tx.getInputs().size()==1 && tx.getInputs().get(0).isPlaceholder();
	}

	public boolean isP2PKH(Script script) {
        boolean onlyOneSignatureParam = 
        		script.getParams().size() == 1 && script.getParams().get(0).getParamType() instanceof SignatureType;
        boolean onlyOnePubkey = (typeSystem.interpretAndSimplify(script.getExp()) instanceof Versig) 
        		&& ((Versig) typeSystem.interpretAndSimplify(script.getExp())).getPubkeys().size() == 1;

        return onlyOneSignatureParam && onlyOnePubkey;
    }

	public boolean isOpReturn(Script script) {
        boolean noParam = script.getParams().size() == 0;
        boolean onlyString = typeSystem.interpretAndSimplify(script.getExp()) instanceof StringLiteral;
        return noParam && onlyString;
    }

	public boolean isP2SH(Script script) {
        return !isP2PKH(script) && !isOpReturn(script);
    }

	
	
	public boolean containsAbsolute(Tlock tlock) {
    	return tlock.getTimes().stream().filter(x -> x instanceof AbsoluteTime).count()>0;
    }
    
    public boolean containsRelative(Tlock tlock, TransactionDeclaration tx) {
    	return tlock.getTimes().stream()
    			.filter(x -> x instanceof RelativeTime && ((RelativeTime) x).getTx()==tx)
    			.count()>0;
    }
	
    public int getAbsolute(Tlock tlock) {
    	return tlock.getTimes().stream()
    			.filter(x -> x instanceof AbsoluteTime)
    			.collect(Collectors.toList()).get(0).getValue();
    }
    
    public int getRelative(Tlock tlock, TransactionDeclaration tx) {
    	return tlock.getTimes().stream()
    			.filter(x -> x instanceof RelativeTime && ((RelativeTime) x).getTx()==tx)
    			.collect(Collectors.toList()).get(0).getValue();
    }
    
    
    
    
    
	public boolean isBlock(Time time) {
    	if (isRelative(time)) return isRelativeBlock(time);
    	if (isAbsolute(time)) return isAbsoluteBlock(time);
    	throw new IllegalArgumentException();
    }
	
	public boolean isDate(Time time) {
    	if (isRelative(time)) return isRelativeDate(time);
    	if (isAbsolute(time)) return isAbsoluteDate(time);
    	throw new IllegalArgumentException();
    }
    
    public boolean isAbsolute(Time time) {
    	return time instanceof AbsoluteTime;
    }
    
    public boolean isRelative(Time time) {
    	return time instanceof RelativeTime;
    }
    
    public boolean isAbsoluteBlock(Time time) {
    	return isAbsolute(time) && ((AbsoluteTime) time).isBlock();
    }
    
    public boolean isAbsoluteDate(Time time) {
    	return isAbsolute(time) && ((AbsoluteTime)time).isDate();
    }
    
    public boolean isRelativeBlock(Time time) {
    	// true if the 22th bit is UNSET
    	int mask = 0b0000_0000__0100_0000__0000_0000__0000_0000;
    	return isRelative(time) && (((RelativeTime) time).getValue() & mask) == 0;
    }
    
    public boolean isRelativeDate(Time time) {
    	return isRelative(time) && !isRelativeBlock(time);   
	}
    
    public int setRelativeDate(int i) {
    	// true if the 22th bit is UNSET
    	int mask = 0b0000_0000__0100_0000__0000_0000__0000_0000;
    	return i | mask;
    }
    
    public int castUnsignedShort(int i) {
		int mask = 0x0000FFFF;
		return i & mask;
	}
    
    public int safeCastUnsignedShort(int i) {
		int value = castUnsignedShort(i);
		
		if (value!=i)
			throw new NumberFormatException("The number does not fit in 16 bits");
		
		return value;
	}
}
